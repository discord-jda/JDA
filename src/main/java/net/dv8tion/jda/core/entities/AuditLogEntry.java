/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import org.apache.http.util.Args;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditLogEntry implements ISnowflake
{

    protected final long id;
    protected final long targetId;
    protected final GuildImpl guild;
    protected final UserImpl user;

    protected final List<AuditLogChange<?>> changes;
    protected final Map<String, Object> options;
    protected final ActionType type;

    public AuditLogEntry(ActionType type, long id, long targetId, GuildImpl guild, UserImpl user,
                         List<AuditLogChange<?>> changes, Map<String, Object> options)
    {
        this.type = type;
        this.id = id;
        this.targetId = targetId;
        this.guild = guild;
        this.user = user;
        this.changes = changes != null ? Collections.unmodifiableList(changes) : Collections.emptyList();
        this.options = options != null ? Collections.unmodifiableMap(options) : Collections.emptyMap();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    public long getTargetIdLong()
    {
        return targetId;
    }

    public String getTargetId()
    {
        return Long.toUnsignedString(targetId);
    }

    public Guild getGuild()
    {
        return guild;
    }

    public User getUser()
    {
        return user;
    }

    public JDA getJDA()
    {
        return guild.getJDA();
    }

    public List<AuditLogChange<?>> getChanges()
    {
        return changes;
    }

    public List<AuditLogChange<?>> getChangesByKey(final String key)
    {
        Args.notNull(key, "Key");
        return Collections.unmodifiableList(changes.stream()
            .filter(change -> change.getKey().equals(key))
            .collect(Collectors.toList()));
    }

    public Map<String, Object> getOptions()
    {
        return options;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOptionByName(String name)
    {
        return (T) options.get(name);
    }

    public ActionType getType()
    {
        return type;
    }

    public TargetType getTargetType()
    {
        return type.getTargetType();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AuditLogEntry))
            return false;
        AuditLogEntry other = (AuditLogEntry) obj;
        return other.id == id && other.targetId == targetId;
    }

    @Override
    public String toString()
    {
        return "ALE:" + type + "(ID:" + id + " / TID:" + targetId + " / " + guild + ')';
    }

    // todo move?
    public enum ActionType
    {
        // TODO check if valid list
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

        public int getKey()
        {
            return key;
        }

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

    public enum TargetType
    {
        GUILD,
        CHANNEL,
        ROLE,
        MEMBER,
        INVITE,
        WEBHOOK,
        EMOTE,
        UNKNOWN
    }

}
