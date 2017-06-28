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

package net.dv8tion.jda.core.audit;

/**
 * ActionTypes for {@link net.dv8tion.jda.core.audit.AuditLogEntry AuditLogEntry} instances
 * <br>Found via {@link net.dv8tion.jda.core.audit.AuditLogEntry#getType() AuditLogEntry.getType()}
 */
public enum ActionType
{
    GUILD_UPDATE(1, TargetType.GUILD),

    CHANNEL_CREATE(10, TargetType.CHANNEL),
    CHANNEL_UPDATE(11, TargetType.CHANNEL),
    CHANNEL_DELETE(12, TargetType.CHANNEL),

    CHANNEL_OVERRIDE_CREATE(13, TargetType.CHANNEL),
    CHANNEL_OVERRIDE_UPDATE(14, TargetType.CHANNEL),
    CHANNEL_OVERRIDE_DELETE(15, TargetType.CHANNEL),

    KICK( 20, TargetType.MEMBER),
    PRUNE(21, TargetType.MEMBER),
    BAN(  22, TargetType.MEMBER),
    UNBAN(23, TargetType.MEMBER),

    MEMBER_UPDATE(     24, TargetType.MEMBER),
    MEMBER_ROLE_UPDATE(25, TargetType.MEMBER),

    ROLE_CREATE(30, TargetType.ROLE),
    ROLE_UPDATE(31, TargetType.ROLE),
    ROLE_DELETE(32, TargetType.ROLE),

    INVITE_CREATE(40, TargetType.INVITE),
    INVITE_UPDATE(41, TargetType.INVITE),
    INVITE_DELETE(42, TargetType.INVITE),

    WEBHOOK_CREATE(50, TargetType.WEBHOOK),
    WEBHOOK_UPDATE(51, TargetType.WEBHOOK),
    WEBHOOK_REMOVE(52, TargetType.WEBHOOK),

    EMOTE_CREATE(60, TargetType.EMOTE),
    EMOTE_UPDATE(61, TargetType.EMOTE),
    EMOTE_DELETE(62, TargetType.EMOTE),

    MESSAGE_CREATE(70, TargetType.UNKNOWN),
    MESSAGE_UPDATE(71, TargetType.UNKNOWN),
    MESSAGE_DELETE(72, TargetType.MEMBER),

    UNKNOWN(-1, TargetType.UNKNOWN);

    private final int key;
    private final TargetType target;

    ActionType(int key, TargetType target)
    {
        this.key = key;
        this.target = target;
    }

    /**
     * The raw key used to identify types within the api.
     *
     * @return Raw key for this ActionType
     */
    public int getKey()
    {
        return key;
    }

    /**
     * The expected {@link net.dv8tion.jda.core.audit.TargetType TargetType}
     * for this ActionType
     *
     * @return {@link net.dv8tion.jda.core.audit.TargetType TargetType}
     */
    public TargetType getTargetType()
    {
        return target;
    }

    public static ActionType from(int key)
    {
        for (ActionType type : values())
        {
            if (type.key == key)
                return type;
        }
        return UNKNOWN;
    }
}
