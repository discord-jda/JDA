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

package net.dv8tion.jda.internal.audio;

public final class VoiceCode {
    // PROTOCOL
    public static final int IDENTIFY = 0;
    public static final int SELECT_PROTOCOL = 1;
    public static final int READY = 2;
    public static final int HEARTBEAT = 3;
    public static final int SESSION_DESCRIPTION = 4;
    public static final int USER_SPEAKING_UPDATE = 5;
    public static final int HEARTBEAT_ACK = 6;
    public static final int RESUME = 7;
    public static final int HELLO = 8;
    public static final int RESUMED = 9;
    public static final int USER_BULK_CONNECT = 11;
    public static final int USER_CONNECT = 12;
    public static final int USER_DISCONNECT = 13;
    public static final int DAVE_PREPARE_TRANSITION = 21;
    public static final int DAVE_EXECUTE_TRANSITION = 22;
    public static final int DAVE_TRANSITION_READY = 23;
    public static final int DAVE_PREPARE_EPOCH = 24;
    public static final int MLS_EXTERNAL_SENDER = 25;
    public static final int MLS_KEY_PACKAGE = 26;
    public static final int MLS_PROPOSALS = 27;
    public static final int MLS_COMMIT_WELCOME = 28;
    public static final int MLS_ANNOUNCE_COMMIT_TRANSITION = 29;
    public static final int MLS_WELCOME = 30;
    public static final int MLS_INVALID_COMMIT_WELCOME = 31;

    // CLOSE
    public enum Close {
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
        BAD_REQUEST(4020, "We sent a malformed request"),
        RATE_LIMIT_EXCEEDED(4021, "We exceeded the rate limit"),
        DISCONNECTED_ALL_CLIENTS(4022, "All clients were disconnected, likely the channel was deleted"),

        UNKNOWN(0, "Unknown code");

        private final int code;
        private final String meaning;

        Close(int code, String meaning) {
            this.code = code;
            this.meaning = meaning;
        }

        public static Close from(int code) {
            for (Close c : values()) {
                if (c.code == code) {
                    return c;
                }
            }
            return UNKNOWN;
        }

        public int getCode() {
            return code;
        }

        public String getMeaning() {
            return meaning;
        }
    }
}
