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

object VoiceCode {
    // PROTOCOL
    val IDENTIFY: Int = 0
    val SELECT_PROTOCOL: Int = 1
    val READY: Int = 2
    val HEARTBEAT: Int = 3
    val SESSION_DESCRIPTION: Int = 4
    val USER_SPEAKING_UPDATE: Int = 5
    val HEARTBEAT_ACK: Int = 6
    val RESUME: Int = 7
    val HELLO: Int = 8
    val RESUMED: Int = 9

    //    public static final int USER_CONNECT = 12;
    val USER_DISCONNECT: Int = 13

    // CLOSE
    enum class Close(val code: Int, val meaning: String) {
        HEARTBEAT_TIMEOUT(1000, "We did not heartbeat in time"),
        UNKNOWN_OP_CODE(4001, "Sent an invalid op code"),
        NOT_AUTHENTICATED(4003, "Tried to send payload before authenticating session"),
        AUTHENTICATION_FAILED(4004, "The token sent in the identify payload is incorrect"),
        ALREADY_AUTHENTICATED(4005, "Tried to authenticate when already authenticated"),
        INVALID_SESSION(4006, "The session with which we attempted to resume is invalid"),
        SESSION_TIMEOUT(4009, "Heartbeat timed out"),
        SERVER_NOT_FOUND(4011, "The server we attempted to connect to was not found"),
        UNKNOWN_PROTOCOL(4012, "The selected protocol is not supported"),
        DISCONNECTED(4014, "The connection has been dropped normally"),
        SERVER_CRASH(4015, "The server we were connected to has crashed"),
        UNKNOWN_ENCRYPTION_MODE(4016, "The specified encryption method is not supported"),
        UNKNOWN(0, "Unknown code");

        companion object {
            fun from(code: Int): Close {
                for (c: Close in entries) {
                    if (c.code == code) return c
                }
                return UNKNOWN
            }
        }
    }
}
