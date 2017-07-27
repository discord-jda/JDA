/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.audio;

public final class VoiceCode
{
    // PROTOCOL
    public static final int IDENTIFY = 0;
    public static final int SELECT_PROTOCOL = 1;
    public static final int READY = 2;
    public static final int HEARTBEAT = 3;
    public static final int SESSION_DESCRIPTION = 4;
    public static final int USER_SPEAKING_UPDATE = 5;
    public static final int HEARTBEAT_ACK = 6;
    public static final int RESUME = 7;
    public static final int HEARTBEAT_INTERVAL = 8;
    public static final int RESUMED = 9;
    public static final int USER_DISCONNECT = 13;

    // CLOSE
    public enum CloseCode {
        AUTHENTICATION_FAILED(4004, "The token sent in the identify payload is incorrect"),
        INVALID_SESSION(4006, "The session with which we attempted to resume is invalid"),
        SERVER_NOT_FOUND(4011, "The server we attempted to connect to was not found"),
        SERVER_CRASH(4015, "The server we were connected to has crashed"),
        HEARTBEAT_TIMEOUT(4800, "We did not heartbeat in time"),
        UNRESUMABLE(4801, "Discord was unable to resume your connection"),

        UNKNOWN(0, "Unknown code");

        private final int code;
        private final String meaning;

        CloseCode(final int code, final String meaning)
        {
            this.code = code;
            this.meaning = meaning;
        }

        public static CloseCode from(int code)
        {
            for (CloseCode c : values())
            {
                if (c.code == code)
                    return c;
            }
            return UNKNOWN;
        }

        public int getCode()
        {
            return code;
        }

        public String getMeaning()
        {
            return meaning;
        }
    }
}
