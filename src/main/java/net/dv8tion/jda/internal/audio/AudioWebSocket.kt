/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.internal.audio

import com.neovisionaries.ws.client.*
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.audio.SpeakingMode
import net.dv8tion.jda.api.audio.SpeakingMode.Companion.getModes
import net.dv8tion.jda.api.audio.hooks.ConnectionListener
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.UserSnowflake.Companion.fromId
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.ExceptionEvent
import net.dv8tion.jda.api.utils.MiscUtil.locked
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.api.utils.data.DataObject.Companion.fromJson
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.managers.AudioManagerImpl
import net.dv8tion.jda.internal.utils.IOUtil
import net.dv8tion.jda.internal.utils.JDALogger
import org.slf4j.Logger
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.*
import java.util.function.Consumer
import kotlin.concurrent.Volatile
import kotlin.math.max

internal class AudioWebSocket(
    private val audioConnection: AudioConnection,
    private val listener: ConnectionListener,
    endpoint: String?,
    private val guild: Guild,
    private val sessionId: String?,
    private val token: String?,
    private var shouldReconnect: Boolean
) : WebSocketAdapter() {
    @Volatile
    var encryption: AudioEncryption? = null
    var socket: WebSocket? = null
    private val keepAlivePool: ScheduledExecutorService
    private var wssEndpoint: String? = null

    @Volatile
    var connectionStatus: ConnectionStatus? = ConnectionStatus.NOT_CONNECTED
        private set
    var isReady: Boolean = false
        private set
    private var reconnecting: Boolean = false
    var sSRC: Int = 0
        private set
    private var secretKey: ByteArray
    private var keepAliveHandle: Future<*>? = null
    var address: InetSocketAddress? = null
        private set

    @Volatile
    private var shutdown: Boolean = false

    init {
        keepAlivePool = jDA!!.getAudioLifeCyclePool()

        //Add the version query parameter
        val url: String = IOUtil.addQuery(endpoint, "v", JDAInfo.AUDIO_GATEWAY_VERSION)
        //Append the Secure Websocket scheme so that our websocket library knows how to connect
        if (url.startsWith("wss://")) wssEndpoint = url else wssEndpoint = "wss://" + url
        if (sessionId == null || sessionId.isEmpty()) throw IllegalArgumentException("Cannot create a audio websocket connection using a null/empty sessionId!")
        if (token == null || token.isEmpty()) throw IllegalArgumentException("Cannot create a audio websocket connection using a null/empty token!")
    }

    /* Used by AudioConnection */
    protected fun send(message: String?) {
        LOG.trace("<- {}", message)
        socket!!.sendText(message)
    }

    fun send(op: Int, data: Any?) {
        send(
            empty()
                .put("op", op)
                .put("d", data)
                .toString()
        )
    }

    fun startConnection() {
        if (!reconnecting && socket != null) throw IllegalStateException("Somehow, someway, this AudioWebSocket has already attempted to start a connection!")
        try {
            val socketFactory: WebSocketFactory = WebSocketFactory(jDA!!.getWebSocketFactory())
            IOUtil.setServerName(socketFactory, wssEndpoint)
            if (socketFactory.getSocketTimeout() > 0) socketFactory.setSocketTimeout(
                max(1000.0, socketFactory.getSocketTimeout().toDouble()).toInt()
            ) else socketFactory.setSocketTimeout(10000)
            socket = socketFactory.createSocket(wssEndpoint)
            socket.setDirectTextMessage(true)
            socket.addListener(this)
            changeStatus(ConnectionStatus.CONNECTING_AWAITING_WEBSOCKET_CONNECT)
            socket.connectAsynchronously()
        } catch (e: IOException) {
            LOG.warn(
                "Encountered IOException while attempting to connect to {}: {}\nClosing connection and attempting to reconnect.",
                wssEndpoint, e.message
            )
            close(ConnectionStatus.ERROR_WEBSOCKET_UNABLE_TO_CONNECT)
        }
    }

    fun close(closeStatus: ConnectionStatus?) {
        //Makes sure we don't run this method again after the socket.close(1000) call fires onDisconnect
        if (shutdown) return
        locked(Consumer<AudioManagerImpl>({ manager: AudioManagerImpl ->
            if (shutdown) return@locked
            var status: ConnectionStatus? = closeStatus
            isReady = false
            shutdown = true
            stopKeepAlive()
            if (audioConnection.udpSocket != null) audioConnection.udpSocket!!.close()
            if (socket != null) socket!!.sendClose()
            audioConnection.shutdown()
            val disconnectedChannel: AudioChannel? = manager.connectedChannel
            manager.setAudioConnection(null)

            //Verify that it is actually a lost of connection and not due the connected channel being deleted.
            val api: JDAImpl? = jDA
            if (status == ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL && (!api!!.getClient()
                    .isSession() || !api.getClient().isConnected())
            ) {
                LOG.debug("Connection was closed due to session invalidate!")
                status = ConnectionStatus.ERROR_CANNOT_RESUME
            } else if (status == ConnectionStatus.ERROR_LOST_CONNECTION || status == ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL) {
                //Get guild from JDA, don't use [guild] field to make sure that we don't have
                // a problem of an out of date guild stored in [guild] during a possible mWS invalidate.
                val connGuild: Guild? = api.getGuildById(guild.idLong)
                if (connGuild != null) {
                    val channel: AudioChannel? =
                        connGuild.getGuildChannelById(audioConnection.getChannel().idLong) as AudioChannel?
                    if (channel == null) status = ConnectionStatus.DISCONNECTED_CHANNEL_DELETED
                }
            }
            changeStatus(status)

            //decide if we reconnect.
            if ((shouldReconnect
                        && status!!.shouldReconnect() //indicated that the connection was purposely closed. don't reconnect.
                        && (status != ConnectionStatus.AUDIO_REGION_CHANGE))
            ) //Already handled.
            {
                if (disconnectedChannel == null) {
                    LOG.debug("Cannot reconnect due to null audio channel")
                    return@locked
                }
                api!!.directAudioController.reconnect(disconnectedChannel)
            } else if (status == ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD) {
                //Remove audio manager as we are no longer in the guild
                api!!.getAudioManagersView().remove(guild.idLong)
            } else if (status != ConnectionStatus.AUDIO_REGION_CHANGE && status != ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL) {
                api!!.directAudioController.disconnect(guild)
            }
        }))
    }

    protected fun changeStatus(newStatus: ConnectionStatus?) {
        connectionStatus = newStatus
        listener.onStatusChange(newStatus)
    }

    fun setAutoReconnect(shouldReconnect: Boolean) {
        this.shouldReconnect = shouldReconnect
    }

    fun getSecretKey(): ByteArray? {
        return secretKey
    }

    /* TCP Listeners */
    public override fun onThreadStarted(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        jDA!!.setContext()
    }

    public override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
        if (shutdown) {
            //Somehow this AudioWebSocket was shutdown before we finished connecting....
            // thus we just disconnect here since we were asked to shutdown
            socket!!.sendClose(1000)
            return
        }
        if (reconnecting) resume() else identify()
        changeStatus(ConnectionStatus.CONNECTING_AWAITING_AUTHENTICATION)
        audioConnection.prepareReady()
        reconnecting = false
    }

    public override fun onTextMessage(websocket: WebSocket, data: ByteArray) {
        try {
            handleEvent(fromJson(data))
        } catch (ex: Exception) {
            var message: String? = "malformed"
            try {
                message = String(data, StandardCharsets.UTF_8)
            } catch (ignored: Exception) {
            }
            LOG.error("Encountered exception trying to handle an event message: {}", message, ex)
        }
    }

    public override fun onDisconnected(
        websocket: WebSocket,
        serverCloseFrame: WebSocketFrame,
        clientCloseFrame: WebSocketFrame,
        closedByServer: Boolean
    ) {
        if (shutdown) return
        LOG.debug("The Audio connection was closed!\nBy remote? {}", closedByServer)
        if (serverCloseFrame != null) {
            LOG.debug("Reason: {}\nClose code: {}", serverCloseFrame.getCloseReason(), serverCloseFrame.getCloseCode())
            val code: Int = serverCloseFrame.getCloseCode()
            val closeCode: VoiceCode.Close = VoiceCode.Close.Companion.from(code)
            when (closeCode) {
                VoiceCode.Close.SERVER_NOT_FOUND, VoiceCode.Close.SERVER_CRASH, VoiceCode.Close.INVALID_SESSION -> close(
                    ConnectionStatus.ERROR_CANNOT_RESUME
                )

                VoiceCode.Close.AUTHENTICATION_FAILED -> close(ConnectionStatus.DISCONNECTED_AUTHENTICATION_FAILURE)
                VoiceCode.Close.DISCONNECTED -> close(ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL)
                else -> reconnect()
            }
            return
        }
        if (clientCloseFrame != null) {
            LOG.debug(
                "ClientReason: {}\nClientCode: {}",
                clientCloseFrame.getCloseReason(),
                clientCloseFrame.getCloseCode()
            )
            if (clientCloseFrame.getCloseCode() != 1000) {
                // unexpected close -> error -> attempt resume
                reconnect()
                return
            }
        }
        close(ConnectionStatus.NOT_CONNECTED)
    }

    public override fun onUnexpectedError(websocket: WebSocket, cause: WebSocketException) {
        handleCallbackError(websocket, cause)
    }

    public override fun handleCallbackError(websocket: WebSocket, cause: Throwable) {
        LOG.error("There was some audio websocket error", cause)
        val api: JDAImpl? = jDA
        api!!.handleEvent(ExceptionEvent((api), cause, true))
    }

    public override fun onThreadCreated(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        val identifier: String = jDA!!.getIdentifierString()
        val guildId: String? = guild.id
        when (threadType) {
            ThreadType.CONNECT_THREAD -> thread.setName(identifier + " AudioWS-ConnectThread (guildId: " + guildId + ')')
            ThreadType.FINISH_THREAD -> thread.setName(identifier + " AudioWS-FinishThread (guildId: " + guildId + ')')
            ThreadType.WRITING_THREAD -> thread.setName(identifier + " AudioWS-WriteThread (guildId: " + guildId + ')')
            ThreadType.READING_THREAD -> thread.setName(identifier + " AudioWS-ReadThread (guildId: " + guildId + ')')
            else -> thread.setName(identifier + " AudioWS-" + threadType + " (guildId: " + guildId + ')')
        }
    }

    public override fun onConnectError(webSocket: WebSocket, e: WebSocketException) {
        LOG.warn(
            "Failed to establish websocket connection to {}: {} - {}\nClosing connection and attempting to reconnect.",
            wssEndpoint, e.getError(), e.message
        )
        close(ConnectionStatus.ERROR_WEBSOCKET_UNABLE_TO_CONNECT)
    }

    /* Internals */
    private fun handleEvent(contentAll: DataObject) {
        val opCode: Int = contentAll.getInt("op")
        when (opCode) {
            VoiceCode.HELLO -> {
                LOG.trace("-> HELLO {}", contentAll)
                val payload: DataObject = contentAll.getObject("d")
                val interval: Int = payload.getInt("heartbeat_interval")
                stopKeepAlive()
                setupKeepAlive(interval)
            }

            VoiceCode.READY -> {
                LOG.trace("-> READY {}", contentAll)
                val content: DataObject = contentAll.getObject("d")
                sSRC = content.getInt("ssrc")
                val port: Int = content.getInt("port")
                val ip: String = content.getString("ip")
                val modes: DataArray = content.getArray("modes")
                encryption = AudioEncryption.Companion.getPreferredMode(modes)
                if (encryption == null) {
                    close(ConnectionStatus.ERROR_UNSUPPORTED_ENCRYPTION_MODES)
                    LOG.error("None of the provided encryption modes are supported: {}", modes)
                    return
                } else {
                    LOG.debug("Using encryption mode " + encryption.getKey())
                }

                //Find our external IP and Port using Discord
                var externalIpAndPort: InetSocketAddress?
                changeStatus(ConnectionStatus.CONNECTING_ATTEMPTING_UDP_DISCOVERY)
                var tries: Int = 0
                do {
                    externalIpAndPort = handleUdpDiscovery(InetSocketAddress(ip, port), sSRC)
                    tries++
                    if (externalIpAndPort == null && tries > 5) {
                        close(ConnectionStatus.ERROR_UDP_UNABLE_TO_CONNECT)
                        return
                    }
                } while (externalIpAndPort == null)
                val `object`: DataObject = empty()
                    .put("protocol", "udp")
                    .put(
                        "data", empty()
                            .put("address", externalIpAndPort.getHostString())
                            .put("port", externalIpAndPort.getPort())
                            .put("mode", encryption.getKey())
                    ) //Discord requires encryption
                send(VoiceCode.SELECT_PROTOCOL, `object`)
                changeStatus(ConnectionStatus.CONNECTING_AWAITING_READY)
            }

            VoiceCode.RESUMED -> {
                LOG.trace("-> RESUMED {}", contentAll)
                LOG.debug("Successfully resumed session!")
                changeStatus(ConnectionStatus.CONNECTED)
                isReady = true
                locked(audioConnection.readyLock, Runnable({ audioConnection.readyCondvar.signalAll() }))
            }

            VoiceCode.SESSION_DESCRIPTION -> {
                LOG.trace("-> SESSION_DESCRIPTION {}", contentAll)
                send(
                    VoiceCode.USER_SPEAKING_UPDATE,  // required to receive audio?
                    empty()
                        .put("delay", 0)
                        .put("speaking", 0)
                        .put("ssrc", sSRC)
                )
                //secret_key is an array of 32 ints that are less than 256, so they are bytes.
                val keyArray: DataArray = contentAll.getObject("d").getArray("secret_key")
                secretKey = ByteArray(DISCORD_SECRET_KEY_LENGTH)
                var i: Int = 0
                while (i < keyArray.length()) {
                    secretKey.get(i) = keyArray.getInt(i).toByte()
                    i++
                }
                LOG.debug("Audio connection has finished connecting!")
                isReady = true
                locked(audioConnection.readyLock, Runnable({ audioConnection.readyCondvar.signalAll() }))
                changeStatus(ConnectionStatus.CONNECTED)
            }

            VoiceCode.HEARTBEAT -> {
                LOG.trace("-> HEARTBEAT {}", contentAll)
                send(VoiceCode.HEARTBEAT, System.currentTimeMillis())
            }

            VoiceCode.HEARTBEAT_ACK -> {
                LOG.trace("-> HEARTBEAT_ACK {}", contentAll)
                val ping: Long = System.currentTimeMillis() - contentAll.getLong("d")
                listener.onPing(ping)
            }

            VoiceCode.USER_SPEAKING_UPDATE -> {
                LOG.trace("-> USER_SPEAKING_UPDATE {}", contentAll)
                val content: DataObject = contentAll.getObject("d")
                val speaking: EnumSet<SpeakingMode?> = getModes(content.getInt("speaking"))
                val ssrc: Int = content.getInt("ssrc")
                val userId: Long = content.getLong("user_id")
                val user: User? = getUser(userId)
                if (user == null) {
                    //more relevant for audio connection
                    LOG.trace("Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: {}", contentAll)
                    listener.onUserSpeakingModeUpdate(fromId(userId), speaking)
                } else {
                    listener.onUserSpeaking(user, speaking)
                    listener.onUserSpeakingModeUpdate(user as UserSnowflake?, speaking)
                }
                audioConnection.updateUserSSRC(ssrc, userId)
            }

            VoiceCode.USER_DISCONNECT -> {
                LOG.trace("-> USER_DISCONNECT {}", contentAll)
                val payload: DataObject = contentAll.getObject("d")
                val userId: Long = payload.getLong("user_id")
                audioConnection.removeUserSSRC(userId)
            }

            12, 14 -> {
                LOG.trace("-> OP {} {}", opCode, contentAll)
            }

            else -> LOG.debug("Unknown Audio OP code.\n{}", contentAll)
        }
    }

    private fun identify() {
        val connectObj: DataObject = empty()
            .put("server_id", guild.id)
            .put("user_id", jDA!!.getSelfUser().id)
            .put("session_id", sessionId)
            .put("token", token)
        send(VoiceCode.IDENTIFY, connectObj)
    }

    private fun resume() {
        LOG.debug("Sending resume payload...")
        val resumeObj: DataObject = empty()
            .put("server_id", guild.id)
            .put("session_id", sessionId)
            .put("token", token)
        send(VoiceCode.RESUME, resumeObj)
    }

    private val jDA: JDAImpl?
        private get() {
            return audioConnection.getJDA()
        }

    private fun locked(consumer: Consumer<AudioManagerImpl>) {
        val manager: AudioManagerImpl = guild.getAudioManager() as AudioManagerImpl
        locked(manager.CONNECTION_LOCK, Runnable({ consumer.accept(manager) }))
    }

    private fun reconnect() {
        if (shutdown) return
        locked(Consumer<AudioManagerImpl>({ unused: AudioManagerImpl? ->
            if (shutdown) return@locked
            isReady = false
            reconnecting = true
            changeStatus(ConnectionStatus.ERROR_LOST_CONNECTION)
            startConnection()
        }))
    }

    private fun handleUdpDiscovery(address: InetSocketAddress, ssrc: Int): InetSocketAddress? {
        //We will now send a packet to discord to punch a port hole in the NAT wall.
        //This is called UDP hole punching.
        try {
            //First close existing socket from possible previous attempts
            if (audioConnection.udpSocket != null) audioConnection.udpSocket!!.close()
            //Create new UDP socket for communication
            audioConnection.udpSocket = DatagramSocket()

            //Create a byte array of length 74 containing our ssrc.
            val buffer: ByteBuffer = ByteBuffer.allocate(74) //74 taken from documentation
            buffer.putShort(1.toShort()) // 1 = send (receive will be 2)
            buffer.putShort(70.toShort()) // length = 70 bytes (required)
            buffer.putInt(ssrc) // Put the ssrc that we were given into the packet to send back to discord.
            // rest of the bytes are used only in the response (address/port)

            //Construct our packet to be sent loaded with the byte buffer we store the ssrc in.
            val discoveryPacket: DatagramPacket = DatagramPacket(buffer.array(), buffer.array().size, address)
            audioConnection.udpSocket!!.send(discoveryPacket)

            //Discord responds to our packet, returning a packet containing our external ip and the port we connected through.
            val receivedPacket: DatagramPacket =
                DatagramPacket(ByteArray(74), 74) //Give a buffer the same size as the one we sent.
            audioConnection.udpSocket!!.setSoTimeout(1000)
            audioConnection.udpSocket!!.receive(receivedPacket)

            //The byte array returned by discord containing our external ip and the port that we used
            //to connect to discord with.
            val received: ByteArray = receivedPacket.getData()

            //Example string:"   121.83.253.66                                                   ��"
            //You'll notice that there are 4 leading nulls and a large amount of nulls between the the ip and
            // the last 2 bytes. Not sure why these exist.  The last 2 bytes are the port. More info below.

            //Take bytes between SSRC and PORT and put them into a string
            // null bytes at the beginning are skipped and the rest are appended to the end of the string
            var ourIP: String = String(received, 8, received.size - 10)
            // Removes the extra nulls attached to the end of the IP string
            ourIP = ourIP.trim({ it <= ' ' })

            //The port exists as the last 2 bytes in the packet data, and is encoded as an UNSIGNED short.
            //Furthermore, it is stored in Little Endian instead of normal Big Endian.
            //We will first need to convert the byte order from Little Endian to Big Endian (reverse the order)
            //Then we will need to deal with the fact that the bytes represent an unsigned short.
            //Java cannot deal with unsigned types, so we will have to promote the short to a higher type.

            //Get our port which is stored as little endian at the end of the packet
            // We AND it with 0xFFFF to ensure that it isn't sign extended
            val ourPort: Int = IOUtil.getShortBigEndian(received, received.size - 2).toInt() and 0xFFFF
            this.address = address
            return InetSocketAddress(ourIP, ourPort)
        } catch (e: IOException) {
            // We either timed out or the socket could not be created (firewall?)
            return null
        }
    }

    private fun stopKeepAlive() {
        if (keepAliveHandle != null) keepAliveHandle!!.cancel(true)
        keepAliveHandle = null
    }

    private fun setupKeepAlive(keepAliveInterval: Int) {
        if (keepAliveHandle != null) LOG.error("Setting up a KeepAlive runnable while the previous one seems to still be active!!")
        try {
            if (socket != null) {
                val rawSocket: Socket? = socket!!.getSocket()
                if (rawSocket != null) rawSocket.setSoTimeout(keepAliveInterval + 10000)
            }
        } catch (ex: SocketException) {
            LOG.warn("Failed to setup timeout for socket", ex)
        }
        val keepAliveRunnable: Runnable = Runnable({
            jDA!!.setContext()
            if (socket != null && socket!!.isOpen()) //TCP keep-alive
                send(VoiceCode.HEARTBEAT, System.currentTimeMillis())
            if (audioConnection.udpSocket != null && !audioConnection.udpSocket!!.isClosed()) //UDP keep-alive
            {
                try {
                    val keepAlivePacket: DatagramPacket = DatagramPacket(UDP_KEEP_ALIVE, UDP_KEEP_ALIVE.size, address)
                    audioConnection.udpSocket!!.send(keepAlivePacket)
                } catch (e: NoRouteToHostException) {
                    LOG.warn("Closing AudioConnection due to inability to ping audio packets.")
                    LOG.warn(
                        "Cannot send audio packet because JDA navigate the route to Discord.\n" +
                                "Are you sure you have internet connection? It is likely that you've lost connection."
                    )
                    close(ConnectionStatus.ERROR_LOST_CONNECTION)
                } catch (e: IOException) {
                    LOG.error("There was some error sending an audio keepalive packet", e)
                }
            }
        })
        try {
            keepAliveHandle = keepAlivePool.scheduleAtFixedRate(
                keepAliveRunnable,
                0,
                keepAliveInterval.toLong(),
                TimeUnit.MILLISECONDS
            )
        } catch (ignored: RejectedExecutionException) {
        } //ignored because this is probably caused due to a race condition
        // related to the threadpool shutdown.
    }

    private fun getUser(userId: Long): User? {
        return jDA!!.getUserById(userId)
    }

    @Suppress("deprecation")
    /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */ protected fun finalize() {
        if (!shutdown) {
            LOG.error("Finalization hook of AudioWebSocket was triggered without properly shutting down")
            close(ConnectionStatus.NOT_CONNECTED)
        }
    }

    companion object {
        val LOG: Logger = JDALogger.getLog(AudioWebSocket::class.java)
        val DISCORD_SECRET_KEY_LENGTH: Int = 32
        private val UDP_KEEP_ALIVE: ByteArray = byteArrayOf(0xC9.toByte(), 0, 0, 0, 0, 0, 0, 0, 0)
    }
}
