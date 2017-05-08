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

/**
 * Single entry for an {@link net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction AuditLogPaginationAction}.
 * <br>This entry contains all options/changes and details for the action that was logged by the {@link net.dv8tion.jda.core.entities.Guild Guild}
 * audit-logs.
 *
 * @since  3.2
 * @author Florian Spieß
 */
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

    /**
     * The id for the target entity.
     * <br>This references an entity based on the {@link net.dv8tion.jda.core.TargetType TargetType}
     * which is specified by {@link #getTargetType()}!
     *
     * @return The target id
     */
    public long getTargetIdLong()
    {
        return targetId;
    }

    /**
     * The id for the target entity.
     * <br>This references an entity based on the {@link net.dv8tion.jda.core.TargetType TargetType}
     * which is specified by {@link #getTargetType()}!
     *
     * @return The target id
     */
    public String getTargetId()
    {
        return Long.toUnsignedString(targetId);
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this audit-log entry refers to
     *
     * @return The Guild instance
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} responsible
     * for this action.
     *
     * @return The User instance
     */
    public User getUser()
    {
        return user;
    }

    /**
     * The optional reason why this action was executed.
     *
     * @return Possibly-null reason String
     */
    public String getReason()
    {
        return reason;
    }

    /**
     * The corresponding JDA instance of the referring Guild
     *
     * @return The corresponding JDA instance
     */
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    /**
     * Key-Value {@link java.util.Map Map} containing all {@link net.dv8tion.jda.core.entities.AuditLogChange AuditLogChanges}
     * made in this entry. The keys for the returned map are case-insensitive keys defined in the regarding AuditLogChange value.
     * <br>To iterate only the changes you can use {@link java.util.Map#values() Map.values()}!
     *
     * @return Key-Value Map of changes
     */
    public Map<String, AuditLogChange> getChanges()
    {
        return changes;
    }

    /**
     * Shortcut to {@code getChanges().get(key)} lookup!
     * <br>This lookup is case-insensitive!
     *
     * @param  key
     *         The key to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
    public AuditLogChange getChangeByKey(final String key)
    {
        return changes.get(key);
    }

    /**
     * Key-Value {@link java.util.Map Map} containing all Options
     * made in this entry. The keys for the returned map are case-insensitive keys defined in the regarding AuditLogChange value.
     * <br>To iterate only the changes you can use {@link java.util.Map#values() Map.values()}!
     *
     * @return Key-Value Map of changes
     */
    public Map<String, Object> getOptions()
    {
        return options;
    }

    /**
     * Shortcut to {@code getOptions().get(key)} lookup!
     * <br>This lookup is case-insensitive!
     *
     * @param  key
     *         The key to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
    @SuppressWarnings("unchecked")
    public <T> T getOptionByName(String name)
    {
        return (T) options.get(name);
    }

    /**
     * The {@link net.dv8tion.jda.core.ActionType ActionType} defining what auditable
     * Action is referred to by this entry.
     *
     * @return The {@link net.dv8tion.jda.core.ActionType ActionType}
     */
    public ActionType getType()
    {
        return type;
    }

    /**
     * The {@link net.dv8tion.jda.core.TargetType TargetType} defining what kind of
     * entity was targeted by this action.
     * <br>Shortcut for {@code getType().getTargetType()}
     *
     * @return The {@link net.dv8tion.jda.core.TargetType TargetType}
     */
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
