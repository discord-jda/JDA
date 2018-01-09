/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core;

/**
 * WebSocket OP Codes for discord
 * <br>Used in {@link net.dv8tion.jda.core.requests.WebSocketClient WebSocketClient} to handle discord payloads
 * and send payloads with central readable OP Codes
 */
public final class WebSocketCode
{
    public static final int DISPATCH = 0;
    public static final int HEARTBEAT = 1;
    public static final int IDENTIFY = 2;
    public static final int PRESENCE = 3;
    public static final int VOICE_STATE = 4;
    public static final int RESUME = 6;
    public static final int RECONNECT = 7;
    public static final int MEMBER_CHUNK_REQUEST = 8;
    public static final int INVALIDATE_SESSION = 9;
    public static final int HELLO = 10;
    public static final int HEARTBEAT_ACK = 11;
    public static final int GUILD_SYNC = 12;
}
