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

import net.dv8tion.jda.core.ActionType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.TargetType;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;

import java.util.Collections;
import java.util.Map;

public class AuditLogEntry implements ISnowflake
{

    protected final long id;
    protected final long targetId;
    protected final GuildImpl guild;
    protected final UserImpl user;
    protected final String reason;

    protected final Map<String, AuditLogChange> changes;
    protected final Map<String, Object> options;
    protected final ActionType type;

    public AuditLogEntry(ActionType type, long id, long targetId, GuildImpl guild, UserImpl user, String reason,
                         Map<String, AuditLogChange> changes, Map<String, Object> options)
    {
        this.type = type;
        this.id = id;
        this.targetId = targetId;
        this.guild = guild;
        this.user = user;
        this.reason = reason;
        this.changes = changes != null && !changes.isEmpty()
                ? Collections.unmodifiableMap(changes)
                : Collections.emptyMap();
        this.options = options != null && !options.isEmpty()
                ? Collections.unmodifiableMap(options)
                : Collections.emptyMap();
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

    public String getReason()
    {
        return reason;
    }

    public JDA getJDA()
    {
        return guild.getJDA();
    }

    public Map<String, AuditLogChange> getChanges()
    {
        return changes;
    }

    public AuditLogChange getChangeByKey(final String key)
    {
        return changes.get(key);
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





}
