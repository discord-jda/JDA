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

import com.iwebpp.crypto.TweetNaclFast.SecretBox
import com.neovisionaries.ws.client.*
import com.sun.jna.ptr.PointerByReference
import gnu.trove.map.TIntLongMap
import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntLongHashMap
import gnu.trove.map.hash.TIntObjectHashMap
import net.dv8tion.jda.api.audio.*
import net.dv8tion.jda.api.audio.AudioNatives.ensureOpus
import net.dv8tion.jda.api.audio.factory.IAudioSendSystem
import net.dv8tion.jda.api.audio.factory.IPacketProvider
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.ExceptionEvent
import net.dv8tion.jda.api.utils.MiscUtil.locked
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.managers.AudioManagerImpl
import net.dv8tion.jda.internal.utils.IOUtil
import net.dv8tion.jda.internal.utils.JDALogger
import tomp2p.opuswrapper.Opus
import java.net.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import javax.annotation.Nonnull
import kotlin.concurrent.Volatile

class AudioConnection(
    manager: AudioManagerImpl,
    endpoint: String?,
    sessionId: String?,
    token: String?,
    @JvmField var channel: AudioChannel
) {
    @Volatile
    var udpSocket: DatagramSocket? = null
    private val ssrcMap: TIntLongMap = TIntLongHashMap()
    private val opusDecoders: TIntObjectMap<Decoder> = TIntObjectHashMap()
    private val combinedQueue = HashMap<User, Queue<AudioData>>()
    private val threadIdentifier: String
    private val webSocket: AudioWebSocket
    val jDA: JDAImpl?
    val readyLock = ReentrantLock()
    val readyCondvar = readyLock.newCondition()
    private var opusEncoder: PointerByReference? = null
    private var combinedAudioExecutor: ScheduledExecutorService? = null
    private var sendSystem: IAudioSendSystem? = null
    private var receiveThread: Thread? = null
    private var queueTimeout: Long = 0
    private var shutdown = false

    @Volatile
    private var sendHandler: AudioSendHandler? = null

    @Volatile
    private var receiveHandler: AudioReceiveHandler? = null

    @Volatile
    private var couldReceive = false

    @Volatile
    private var speakingMode = SpeakingMode.VOICE.raw

    init {
        jDA = channel.jDA as JDAImpl?
        val api = channel.jDA as JDAImpl?
        threadIdentifier = api!!.identifierString + " AudioConnection Guild: " + channel.guild.getId()
        webSocket = AudioWebSocket(
            this,
            manager.listenerProxy,
            endpoint,
            channel.guild,
            sessionId,
            token,
            manager.isAutoReconnect
        )
    }

    /* Used by AudioManagerImpl */
    fun startConnection() {
        webSocket.startConnection()
    }

    val connectionStatus: ConnectionStatus?
        get() = webSocket.connectionStatus

    fun setAutoReconnect(shouldReconnect: Boolean) {
        webSocket.setAutoReconnect(shouldReconnect)
    }

    @Deprecated("")
    fun setSpeakingDelay(millis: Int) {
    }

    fun setSendingHandler(handler: AudioSendHandler?) {
        sendHandler = handler
        if (webSocket.isReady) setupSendSystem()
    }

    fun setReceivingHandler(handler: AudioReceiveHandler?) {
        receiveHandler = handler
        if (webSocket.isReady) setupReceiveSystem()
    }

    fun setSpeakingMode(mode: EnumSet<SpeakingMode?>?) {
        val raw: Int = SpeakingMode.getRaw(mode)
        if (raw != speakingMode && webSocket.isReady) setSpeaking(raw)
        speakingMode = raw
    }

    fun setQueueTimeout(queueTimeout: Long) {
        this.queueTimeout = queueTimeout
    }

    val guild: Guild
        get() = channel.guild

    fun close(closeStatus: ConnectionStatus?) {
        shutdown()
        webSocket.close(closeStatus)
    }

    @Synchronized
    fun shutdown() {
        shutdown = true
        if (sendSystem != null) {
            sendSystem!!.shutdown()
            sendSystem = null
        }
        if (receiveThread != null) {
            receiveThread!!.interrupt()
            receiveThread = null
        }
        if (combinedAudioExecutor != null) {
            combinedAudioExecutor!!.shutdownNow()
            combinedAudioExecutor = null
        }
        if (opusEncoder != null) {
            Opus.INSTANCE.opus_encoder_destroy(opusEncoder)
            opusEncoder = null
        }
        opusDecoders.valueCollection().forEach(Consumer { obj: Decoder -> obj.close() })
        opusDecoders.clear()
        locked(readyLock) { readyCondvar.signalAll() }
    }

    fun getWebSocket(): WebSocket? {
        return webSocket.socket
    }

    /* Used by AudioWebSocket */
    fun prepareReady() {
        val readyThread = Thread {
            jDA!!.setContext()
            val ready = locked<Boolean>(readyLock) {
                val timeout: Long = guild.getAudioManager().getConnectTimeout()
                while (!webSocket.isReady) {
                    try {
                        val activated = readyCondvar.await(timeout, TimeUnit.MILLISECONDS)
                        if (!activated) {
                            webSocket.close(ConnectionStatus.ERROR_CONNECTION_TIMEOUT)
                            shutdown = true
                        }
                        if (shutdown) return@locked false
                    } catch (e: InterruptedException) {
                        LOG.error("AudioConnection ready thread got interrupted while sleeping", e)
                        return@locked false
                    }
                }
                true
            }
            if (ready) {
                setupSendSystem()
                setupReceiveSystem()
            }
        }
        readyThread.setUncaughtExceptionHandler { thread: Thread?, throwable: Throwable? ->
            LOG.error("Uncaught exception in Audio ready-thread", throwable)
            val api = jDA
            api!!.handleEvent(ExceptionEvent(api, throwable!!, true))
        }
        readyThread.setDaemon(true)
        readyThread.setName("$threadIdentifier Ready Thread")
        readyThread.start()
    }

    fun removeUserSSRC(userId: Long) {
        val ssrcRef = AtomicInteger(0)
        val modified = ssrcMap.retainEntries { ssrc: Int, id: Long ->
            val isEntry = id == userId
            if (isEntry) ssrcRef.set(ssrc)
            !isEntry
        }
        if (!modified) return
        val decoder = opusDecoders.remove(ssrcRef.get())
        decoder?.close()
    }

    fun updateUserSSRC(ssrc: Int, userId: Long) {
        if (ssrcMap.containsKey(ssrc)) {
            val previousId = ssrcMap[ssrc]
            if (previousId != userId) {
                //Different User already existed with this ssrc. What should we do? Just replace? Probably should nuke the old opusDecoder.
                //Log for now and see if any user report the error.
                LOG.error(
                    "Yeah.. So.. JDA received a UserSSRC update for an ssrc that already had a User set. Inform devs.\nChannelId: {} SSRC: {} oldId: {} newId: {}",
                    channel.id, ssrc, previousId, userId
                )
            }
        } else {
            ssrcMap.put(ssrc, userId)

            //Only create a decoder if we are actively handling received audio.
            if (receiveThread != null && ensureOpus()) opusDecoders.put(ssrc, Decoder(ssrc))
        }
    }

    /* Internals */
    @Synchronized
    private fun setupSendSystem() {
        if (udpSocket != null && !udpSocket!!.isClosed && sendHandler != null && sendSystem == null) {
            setSpeaking(speakingMode)
            val factory = jDA!!.audioSendFactory
            sendSystem = factory.createSendSystem(PacketProvider(SecretBox(webSocket.secretKey)))
            sendSystem!!.setContextMap(jDA.contextMap)
            sendSystem!!.start()
        } else if (sendHandler == null && sendSystem != null) {
            sendSystem!!.shutdown()
            sendSystem = null
            if (opusEncoder != null) {
                Opus.INSTANCE.opus_encoder_destroy(opusEncoder)
                opusEncoder = null
            }
        }
    }

    @Synchronized
    private fun setupReceiveSystem() {
        if (udpSocket != null && !udpSocket!!.isClosed && receiveHandler != null && receiveThread == null) {
            setupReceiveThread()
        } else if (receiveHandler == null && receiveThread != null) {
            receiveThread!!.interrupt()
            receiveThread = null
            if (combinedAudioExecutor != null) {
                combinedAudioExecutor!!.shutdownNow()
                combinedAudioExecutor = null
            }
            opusDecoders.valueCollection().forEach(Consumer { obj: Decoder -> obj.close() })
            opusDecoders.clear()
        } else if (receiveHandler != null && !receiveHandler!!.canReceiveCombined() && combinedAudioExecutor != null) {
            combinedAudioExecutor!!.shutdownNow()
            combinedAudioExecutor = null
        }
    }

    @Synchronized
    private fun setupReceiveThread() {
        if (receiveThread == null) {
            receiveThread = Thread {
                jDA!!.setContext()
                try {
                    udpSocket!!.soTimeout = 1000
                } catch (e: SocketException) {
                    LOG.error("Couldn't set SO_TIMEOUT for UDP socket", e)
                }
                while (!udpSocket!!.isClosed && !Thread.currentThread().isInterrupted) {
                    val receivedPacket = DatagramPacket(ByteArray(1920), 1920)
                    try {
                        udpSocket!!.receive(receivedPacket)
                        val shouldDecode =
                            receiveHandler != null && (receiveHandler!!.canReceiveUser() || receiveHandler!!.canReceiveCombined())
                        val canReceive =
                            receiveHandler != null && (receiveHandler!!.canReceiveUser() || receiveHandler!!.canReceiveCombined() || receiveHandler!!.canReceiveEncoded())
                        if (canReceive && webSocket.secretKey != null) {
                            couldReceive = true
                            val decryptedPacket: AudioPacket = AudioPacket.Companion.decryptAudioPacket(
                                webSocket.encryption,
                                receivedPacket,
                                webSocket.secretKey
                            )
                                ?: continue
                            val ssrc = decryptedPacket.ssrc
                            val userId = ssrcMap[ssrc]
                            var decoder = opusDecoders[ssrc]
                            if (userId == ssrcMap.noEntryValue) {
                                val audio = decryptedPacket.encodedAudio

                                //If the bytes are silence, then this was caused by a User joining the voice channel,
                                // and as such, we haven't yet received information to pair the SSRC with the UserId.
                                if (audio != silenceBytes) LOG.debug("Received audio data with an unknown SSRC id. Ignoring")
                                continue
                            }
                            if (decoder == null) {
                                if (ensureOpus()) {
                                    opusDecoders.put(ssrc, Decoder(ssrc).also { decoder = it })
                                } else if (!receiveHandler!!.canReceiveEncoded()) {
                                    LOG.error("Unable to decode audio due to missing opus binaries!")
                                    break
                                }
                            }
                            val opusPacket = OpusPacket(decryptedPacket, userId, decoder)
                            if (receiveHandler!!.canReceiveEncoded()) receiveHandler!!.handleEncodedAudio(opusPacket)
                            if (!shouldDecode || !opusPacket.canDecode()) continue
                            val user = jDA.getUserById(userId)
                            if (user == null) {
                                LOG.warn("Received audio data with a known SSRC, but the userId associate with the SSRC is unknown to JDA!")
                                continue
                            }
                            val decodedAudio = opusPacket.decode()
                                ?: //decoder error logged in method
                                continue
                            //If decodedAudio is null, then the Opus decode failed, so throw away the packet.
                            if (receiveHandler!!.canReceiveUser()) {
                                receiveHandler!!.handleUserAudio(UserAudio(user, decodedAudio))
                            }
                            if (receiveHandler!!.canReceiveCombined() && receiveHandler!!.includeUserInCombinedAudio(
                                    user
                                )
                            ) {
                                var queue = combinedQueue[user]
                                if (queue == null) {
                                    queue = ConcurrentLinkedQueue()
                                    combinedQueue[user] = queue
                                }
                                queue.add(AudioData(decodedAudio))
                            }
                        } else {
                            couldReceive = false
                        }
                    } catch (e: SocketTimeoutException) {
                        //Ignore. We set a low timeout so that we wont block forever so we can properly shutdown the loop.
                    } catch (e: SocketException) {
                        //The socket was closed while we were listening for the next packet.
                        //This is expected. Ignore the exception. The thread will exit during the next while
                        // iteration because the udpSocket.isClosed() will return true.
                    } catch (e: Exception) {
                        LOG.error("There was some random exception while waiting for udp packets", e)
                    }
                }
            }
            receiveThread!!.setUncaughtExceptionHandler { thread: Thread?, throwable: Throwable? ->
                LOG.error("There was some uncaught exception in the audio receive thread", throwable)
                val api = jDA
                api!!.handleEvent(ExceptionEvent(api, throwable!!, true))
            }
            receiveThread!!.setDaemon(true)
            receiveThread!!.setName("$threadIdentifier Receiving Thread")
            receiveThread!!.start()
        }
        if (receiveHandler!!.canReceiveCombined()) {
            setupCombinedExecutor()
        }
    }

    @Synchronized
    private fun setupCombinedExecutor() {
        if (combinedAudioExecutor == null) {
            combinedAudioExecutor = Executors.newSingleThreadScheduledExecutor { task: Runnable? ->
                val t = Thread(task, "$threadIdentifier Combined Thread")
                t.setDaemon(true)
                t.setUncaughtExceptionHandler { thread: Thread?, throwable: Throwable? ->
                    LOG.error(
                        "I have no idea how, but there was an uncaught exception in the combinedAudioExecutor",
                        throwable
                    )
                    val api = jDA
                    api!!.handleEvent(ExceptionEvent(api, throwable!!, true))
                }
                t
            }
            combinedAudioExecutor.scheduleAtFixedRate(Runnable {
                jDA!!.setContext()
                try {
                    val users: MutableList<User> = LinkedList()
                    val audioParts: MutableList<ShortArray> = LinkedList()
                    if (receiveHandler != null && receiveHandler!!.canReceiveCombined()) {
                        val currentTime = System.currentTimeMillis()
                        for ((user, queue) in combinedQueue) {
                            if (queue.isEmpty()) continue
                            var audioData = queue.poll()
                            //Make sure the audio packet is younger than 100ms
                            while (audioData != null && currentTime - audioData.time > queueTimeout) {
                                audioData = queue.poll()
                            }

                            //If none of the audio packets were younger than 100ms, then there is nothing to add.
                            if (audioData == null) {
                                continue
                            }
                            users.add(user)
                            audioParts.add(audioData.data)
                        }
                        if (!audioParts.isEmpty()) {
                            val audioLength =
                                audioParts.stream().mapToInt { it: ShortArray -> it.size }.max().getAsInt()
                            val mix = ShortArray(1920) //960 PCM samples for each channel
                            var sample: Int
                            for (i in 0 until audioLength) {
                                sample = 0
                                val iterator = audioParts.iterator()
                                while (iterator.hasNext()) {
                                    val audio = iterator.next()
                                    if (i < audio.size) sample += audio[i].toInt() else iterator.remove()
                                }
                                if (sample > Short.MAX_VALUE) mix[i] =
                                    Short.MAX_VALUE else if (sample < Short.MIN_VALUE) mix[i] =
                                    Short.MIN_VALUE else mix[i] = sample.toShort()
                            }
                            receiveHandler!!.handleCombinedAudio(CombinedAudio(users, mix))
                        } else {
                            //No audio to mix, provide 20 MS of silence. (960 PCM samples for each channel)
                            receiveHandler!!.handleCombinedAudio(CombinedAudio(emptyList(), ShortArray(1920)))
                        }
                    }
                } catch (e: Exception) {
                    LOG.error("There was some unexpected exception in the combinedAudioExecutor!", e)
                }
            }, 0, 20, TimeUnit.MILLISECONDS)
        }
    }

    private fun encodeToOpus(rawAudio: ByteBuffer): ByteBuffer? {
        val nonEncodedBuffer = ShortBuffer.allocate(rawAudio.remaining() / 2)
        val encoded = ByteBuffer.allocate(4096)
        var i = rawAudio.position()
        while (i < rawAudio.limit()) {
            val firstByte =
                0x000000FF and rawAudio[i].toInt() //Promotes to int and handles the fact that it was unsigned.
            val secondByte = 0x000000FF and rawAudio[i + 1].toInt()

            //Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
            val toShort = (firstByte shl 8 or secondByte).toShort()
            nonEncodedBuffer.put(toShort)
            i += 2
        }
        (nonEncodedBuffer as Buffer).flip()
        val result = Opus.INSTANCE.opus_encode(
            opusEncoder,
            nonEncodedBuffer,
            OpusPacket.OPUS_FRAME_SIZE,
            encoded,
            encoded.capacity()
        )
        if (result <= 0) {
            LOG.error("Received error code from opus_encode(...): {}", result)
            return null
        }
        (encoded as Buffer).position(0).limit(result)
        return encoded
    }

    private fun setSpeaking(raw: Int) {
        val obj = empty()
            .put("speaking", raw)
            .put("ssrc", webSocket.ssrc)
            .put("delay", 0)
        webSocket.send(VoiceCode.USER_SPEAKING_UPDATE, obj)
    }

    @Suppress("deprecation")
    /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */ protected fun finalize() {
        shutdown()
    }

    private inner class PacketProvider(private val boxer: SecretBox) : IPacketProvider {
        private val nonceBuffer = ByteArray(SecretBox.nonceLength)
        private var seq = 0 //Sequence of audio packets. Used to determine the order of the packets.
            .toChar()
        private var timestamp = 0 //Used to sync up our packets within the same timeframe of other people talking.
        private var nonce: Long = 0
        private var buffer = ByteBuffer.allocate(512)
        private var encryptionBuffer = ByteBuffer.allocate(512)

        @get:Nonnull
        override val identifier: String
            get() = threadIdentifier

        @get:Nonnull
        override val connectedChannel: AudioChannel
            get() = channel

        @Nonnull
        override fun getUdpSocket(): DatagramSocket {
            return udpSocket!!
        }

        @get:Nonnull
        override val socketAddress: InetSocketAddress?
            get() = webSocket.address

        override fun getNextPacket(unused: Boolean): DatagramPacket? {
            val buffer = getNextPacketRaw(unused)
            return buffer?.let { getDatagramPacket(it) }
        }

        override fun getNextPacketRaw(unused: Boolean): ByteBuffer? {
            var nextPacket: ByteBuffer? = null
            try {
                if (sendHandler != null && sendHandler!!.canProvide()) {
                    var rawAudio = sendHandler!!.provide20MsAudio()
                    if (rawAudio != null && !rawAudio.hasArray()) {
                        // we can't use the boxer without an array so encryption would not work
                        LOG.error("AudioSendHandler provided ByteBuffer without a backing array! This is unsupported.")
                    }
                    if (rawAudio != null && rawAudio.hasRemaining() && rawAudio.hasArray()) {
                        if (!sendHandler!!.isOpus) {
                            rawAudio = encodeAudio(rawAudio)
                            if (rawAudio == null) return null
                        }
                        nextPacket = getPacketData(rawAudio)
                        if (seq.code + 1 > Character.MAX_VALUE.code) seq = 0.toChar() else seq++
                    }
                }
            } catch (e: Exception) {
                LOG.error("There was an error while getting next audio packet", e)
            }
            if (nextPacket != null) timestamp += OpusPacket.OPUS_FRAME_SIZE
            return nextPacket
        }

        private fun encodeAudio(rawAudio: ByteBuffer): ByteBuffer? {
            if (opusEncoder == null) {
                if (!ensureOpus()) {
                    if (!printedError) LOG.error("Unable to process PCM audio without opus binaries!")
                    printedError = true
                    return null
                }
                val error = IntBuffer.allocate(1)
                opusEncoder = Opus.INSTANCE.opus_encoder_create(
                    OpusPacket.OPUS_SAMPLE_RATE,
                    OpusPacket.OPUS_CHANNEL_COUNT,
                    Opus.OPUS_APPLICATION_AUDIO,
                    error
                )
                if (error.get() != Opus.OPUS_OK && opusEncoder == null) {
                    LOG.error("Received error status from opus_encoder_create(...): {}", error.get())
                    return null
                }
            }
            return encodeToOpus(rawAudio)
        }

        private fun getDatagramPacket(b: ByteBuffer): DatagramPacket {
            val data = b.array()
            val offset = b.arrayOffset() + b.position()
            val length = b.remaining()
            return DatagramPacket(data, offset, length, webSocket.address)
        }

        private fun getPacketData(rawAudio: ByteBuffer): ByteBuffer {
            ensureEncryptionBuffer(rawAudio)
            val packet = AudioPacket(encryptionBuffer, seq, timestamp, webSocket.ssrc, rawAudio)
            val nlen: Int
            when (webSocket.encryption) {
                AudioEncryption.XSALSA20_POLY1305 -> nlen = 0
                AudioEncryption.XSALSA20_POLY1305_LITE -> {
                    if (nonce >= MAX_UINT_32) loadNextNonce(
                        0.also { nonce = it.toLong() }.toLong()
                    ) else loadNextNonce(++nonce)
                    nlen = 4
                }

                AudioEncryption.XSALSA20_POLY1305_SUFFIX -> {
                    ThreadLocalRandom.current().nextBytes(nonceBuffer)
                    nlen = SecretBox.nonceLength
                }

                else -> throw IllegalStateException("Encryption mode [" + webSocket.encryption + "] is not supported!")
            }
            return packet.asEncryptedPacket(boxer, buffer, nonceBuffer, nlen).also { buffer = it }
        }

        private fun ensureEncryptionBuffer(data: ByteBuffer) {
            (encryptionBuffer as Buffer).clear()
            val currentCapacity = encryptionBuffer.remaining()
            val requiredCapacity: Int = AudioPacket.Companion.RTP_HEADER_BYTE_LENGTH + data.remaining()
            if (currentCapacity < requiredCapacity) encryptionBuffer = ByteBuffer.allocate(requiredCapacity)
        }

        private fun loadNextNonce(nonce: Long) {
            IOUtil.setIntBigEndian(nonceBuffer, 0, nonce.toInt())
        }

        override fun onConnectionError(@Nonnull status: ConnectionStatus?) {
            LOG.warn("IAudioSendSystem reported a connection error of: {}", status)
            LOG.warn("Shutting down AudioConnection.")
            webSocket.close(status)
        }

        override fun onConnectionLost() {
            LOG.warn("Closing AudioConnection due to inability to send audio packets.")
            LOG.warn(
                """
    Cannot send audio packet because JDA cannot navigate the route to Discord.
    Are you sure you have internet connection? It is likely that you've lost connection.
    """.trimIndent()
            )
            webSocket.close(ConnectionStatus.ERROR_LOST_CONNECTION)
        }
    }

    private class AudioData(val data: ShortArray) {
        val time: Long

        init {
            time = System.currentTimeMillis()
        }
    }

    companion object {
        val LOG = JDALogger.getLog(AudioConnection::class.java)
        const val MAX_UINT_32 = 4294967295L
        private val silenceBytes = ByteBuffer.wrap(byteArrayOf(0xF8.toByte(), 0xFF.toByte(), 0xFE.toByte()))
        private var printedError = false
    }
}
