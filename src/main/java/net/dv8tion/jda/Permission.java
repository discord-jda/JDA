/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda;

public enum Permission
{
    CREATE_INSTANT_INVITE(0),
    KICK_MEMBERS(1),
    BAN_MEMBERS(2),
    MANAGE_ROLES(3),
    MANAGE_PERMISSIONS(3),
    MANAGE_CHANNEL(4),
    MANAGE_SERVER(5),

    MESSAGE_READ(10),
    MESSAGE_WRITE(11),
    MESSAGE_TTS(12),
    MESSAGE_MANAGE(13),
    MESSAGE_EMBED_LINKS(14),
    MESSAGE_ATTACH_FILES(15),
    MESSAGE_HISTORY(16),
    MESSAGE_MENTION_EVERYONE(17),

    VOICE_CONNECT(20),
    VOICE_SPEAK(21),
    VOICE_MUTE_OTHERS(22),
    VOICE_DEAF_OTHERS(23),
    VOICE_MOVE_OTHERS(24),
    VOICE_USE_VAD(25),

    UNKNOWN(-1);

    private final int offset;

    Permission(int offset)
    {
        this.offset = offset;
    }

    public int getOffset()
    {
        return offset;
    }
}
