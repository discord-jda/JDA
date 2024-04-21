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
package net.dv8tion.jda.api.audio.hooks

/**
 * Represents the connection status of an audio connection.
 */
enum class ConnectionStatus @JvmOverloads constructor(private val shouldReconnect: Boolean = true) {
    /** Indicates that there is no open connection or that the connection was closed by choice, not by error. */
    NOT_CONNECTED(false),

    /** Indicates that JDA was shutdown and no further connections should be established  */
    SHUTTING_DOWN(false),

    /** JDA is waiting on Discord to send a valid endpoint which to connect the audio websocket to. */
    CONNECTING_AWAITING_ENDPOINT,

    /** JDA has received a valid endpoint and is attempting to setup and connect the audio websocket  */
    CONNECTING_AWAITING_WEBSOCKET_CONNECT,

    /** JDA has connected the audio websocket to Discord and has sent the authentication information, awaiting reply. */
    CONNECTING_AWAITING_AUTHENTICATION,

    /**
     * JDA successfully authenticated the audio websocket and it now attempting UDP discovery. UDP discovery involves
     * opening a UDP socket and sending a packet to a provided Discord remote resource which responds with the
     * external ip and port which the packet was sent from.
     */
    CONNECTING_ATTEMPTING_UDP_DISCOVERY,

    /**
     * After determining our external ip and port, JDA forwards this information to Discord so that it can send
     * audio packets for us to properly receive. At this point, JDA is waiting for final websocket READY.
     */
    CONNECTING_AWAITING_READY,

    /** The audio connection has been successfully setup and is ready for use.  */
    CONNECTED,

    /**
     * Indicates that the logged in account lost the [Permission.VOICE_CONNECT][net.dv8tion.jda.api.Permission.VOICE_CONNECT]
     * and cannot connect to the channel.
     */
    DISCONNECTED_LOST_PERMISSION(false),

    /**
     * Indicates that the channel which the audio connection was connected to was deleted, thus the connection was severed.
     */
    DISCONNECTED_CHANNEL_DELETED(false),

    /**
     * Indicates that the logged in account was removed from the [Guild][net.dv8tion.jda.api.entities.Guild]
     * that this audio connection was connected to, thus the connection was severed.
     */
    DISCONNECTED_REMOVED_FROM_GUILD(false),

    /** Indicates that we were kicked from a channel by a moderator  */
    DISCONNECTED_KICKED_FROM_CHANNEL(false),

    /**
     * Indicates that the logged in account was removed from the [Guild][net.dv8tion.jda.api.entities.Guild]
     * while reconnecting to the gateway
     */
    DISCONNECTED_REMOVED_DURING_RECONNECT(false),

    /**
     * Indicates that our token was not valid.
     */
    DISCONNECTED_AUTHENTICATION_FAILURE,

    /**
     * Indicates that the audio connection was closed due to the [Region][net.dv8tion.jda.api.Region] of the
     * audio connection being changed. JDA will automatically attempt to reconnect the audio connection regardless
     * of the value of the [AudioManager.isAutoReconnect()][net.dv8tion.jda.api.managers.AudioManager.isAutoReconnect].
     */
    AUDIO_REGION_CHANGE,
    //All will attempt to reconnect unless autoReconnect is disabled
    /**
     * Indicates that the connection was lost, either via UDP socket problems or the audio Websocket disconnecting.
     * <br></br>This is typically caused by a brief loss of internet which results in connection loss.
     * <br></br>JDA automatically attempts to resume the session when this error occurs.
     */
    ERROR_LOST_CONNECTION,

    /**
     * Indicates that the audio WebSocket was unable to resume an active session.
     * <br></br>JDA automatically attempts to reconnect when this error occurs.
     */
    ERROR_CANNOT_RESUME,

    /**
     * Indicates that the audio Websocket was unable to connect to discord. This could be due to an internet
     * problem causing a connection problem or an error on Discord's side (possibly due to load)
     * <br></br>JDA automatically attempts to reconnect when this error occurs.
     */
    ERROR_WEBSOCKET_UNABLE_TO_CONNECT,

    /**
     * Indicates that the audio WebSocket was unable to complete a handshake with discord, because
     * discord did not provide any supported encryption modes.
     * <br></br>JDA automatically attempts to reconnect when this error occurs.
     */
    ERROR_UNSUPPORTED_ENCRYPTION_MODES,

    /**
     * Indicates that the UDP setup failed. This is caused when JDA cannot properly communicate with Discord to
     * discover the system's external IP and port which audio data will be sent from. Typically caused by an internet
     * problem or an overly aggressive NAT port table.
     * <br></br>JDA automatically attempts to reconnect when this error occurs.
     */
    ERROR_UDP_UNABLE_TO_CONNECT,

    /**
     * Occurs when it takes longer than
     * [AudioManager.getConnectTimeout()][net.dv8tion.jda.api.managers.AudioManager.getConnectTimeout] to establish
     * the Websocket connection and setup the UDP connection.
     * <br></br>JDA automatically attempts to reconnect when this error occurs.
     */
    ERROR_CONNECTION_TIMEOUT;

    fun shouldReconnect(): Boolean {
        return shouldReconnect
    }
}
