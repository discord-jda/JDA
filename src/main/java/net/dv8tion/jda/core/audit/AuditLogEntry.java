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

package net.dv8tion.jda.core.audit;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.entities.impl.WebhookImpl;
import net.dv8tion.jda.core.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Single entry for an {@link net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction
 * AuditLogPaginationAction}.
 * <br>This entry contains all options/changes and details for the action
 * that was logged by the {@link net.dv8tion.jda.core.entities.Guild Guild} audit-logs.
 */
public class AuditLogEntry implements ISnowflake
{

    protected final long id;
    protected final long targetId;
    protected final GuildImpl guild;
    protected final UserImpl user;
    protected final WebhookImpl webhook;
    protected final String reason;

    protected final Map<String, AuditLogChange> changes;
    protected final Map<String, Object> options;
    protected final ActionType type;

    public AuditLogEntry(ActionType type, long id, long targetId, GuildImpl guild, UserImpl user, WebhookImpl webhook, 
                        String reason, Map<String, AuditLogChange> changes, Map<String, Object> options)
    {
        this.type = type;
        this.id = id;
        this.targetId = targetId;
        this.guild = guild;
        this.user = user;
        this.webhook = webhook;
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
     * <br>This references an entity based on the {@link net.dv8tion.jda.core.audit.TargetType TargetType}
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
     * <br>This references an entity based on the {@link net.dv8tion.jda.core.audit.TargetType TargetType}
     * which is specified by {@link #getTargetType()}!
     *
     * @return The target id
     */
    public String getTargetId()
    {
        return Long.toUnsignedString(targetId);
    }
    
    /**
     * The {@link net.dv8tion.jda.core.entities.Webhook Webhook} that the target id of this audit-log entry refers to
     * 
     * @return Possibly-null Webhook instance
     */
    @Nullable
    public Webhook getWebhook()
    {
        return webhook;
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
     * @return Possibly-null User instance
     */
    @Nullable
    public User getUser()
    {
        return user;
    }

    /**
     * The optional reason why this action was executed.
     *
     * @return Possibly-null reason String
     */
    @Nullable
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
     * Key-Value {@link java.util.Map Map} containing all {@link AuditLogChange
     * AuditLogChanges} made in this entry.
     * The keys for the returned map are case-insensitive keys defined in the regarding AuditLogChange value.
     * <br>To iterate only the changes you can use {@link java.util.Map#values() Map.values()}!
     *
     * @return Key-Value Map of changes
     */
    public Map<String, AuditLogChange> getChanges()
    {
        return changes;
    }

    /**
     * Shortcut to <code>{@link #getChanges() getChanges()}.get(key)</code> lookup!
     * <br>This lookup is case-insensitive!
     *
     * @param  key
     *         The {@link net.dv8tion.jda.core.audit.AuditLogKey AuditLogKey} to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
    @Nullable
    public AuditLogChange getChangeByKey(final AuditLogKey key)
    {
        return key == null ? null : getChangeByKey(key.getKey());
    }

    /**
     * Shortcut to <code>{@link #getChanges() getChanges()}.get(key)</code> lookup!
     * <br>This lookup is case-insensitive!
     *
     * @param  key
     *         The key to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
    @Nullable
    public AuditLogChange getChangeByKey(final String key)
    {
        return changes.get(key);
    }

    /**
     * Filters all changes by the specified keys
     *
     * @param  keys
     *         Varargs {@link net.dv8tion.jda.core.audit.AuditLogKey AuditLogKeys} to look for
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null array
     *
     * @return Possibly-empty, never-null immutable list of {@link AuditLogChange AuditLogChanges}
     */
    public List<AuditLogChange> getChangesForKeys(AuditLogKey... keys)
    {
        Checks.notNull(keys, "Keys");
        List<AuditLogChange> changes = new ArrayList<>(keys.length);
        for (AuditLogKey key : keys)
        {
            AuditLogChange change = getChangeByKey(key);
            if (change != null)
                changes.add(change);
        }
        return Collections.unmodifiableList(changes);
    }

    /**
     * Key-Value {@link java.util.Map Map} containing all Options made in this entry. The keys for the returned map are
     * case-insensitive keys defined in the regarding AuditLogChange value.
     * <br>To iterate only the changes you can use {@link java.util.Map#values() Map.values()}!
     *
     * <p>Options may include secondary targets or details that do not qualify as "change".
     * <br>An example of that would be the {@code member} option
     * for {@link net.dv8tion.jda.core.audit.ActionType#CHANNEL_OVERRIDE_UPDATE CHANNEL_OVERRIDE_UPDATE}
     * containing the user_id of a {@link net.dv8tion.jda.core.entities.Member Member}.
     *
     * @return Key-Value Map of changes
     */
    public Map<String, Object> getOptions()
    {
        return options;
    }

    /**
     * Shortcut to <code>{@link #getOptions() getOptions()}.get(name)</code> lookup!
     * <br>This lookup is case-insensitive!
     *
     * @param  <T>
     *         The expected type for this option <br>Will be used for casting
     * @param  name
     *         The field name to look for
     *
     * @throws java.lang.ClassCastException
     *         If the type-cast failed for the generic type.
     *
     * @return Possibly-null value corresponding to the specified key
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getOptionByName(String name)
    {
        return (T) options.get(name);
    }

    /**
     * Shortcut to <code>{@link #getOptions() getOptions()}.get(name)</code> lookup!
     *
     * @param  <T>
     *         The expected type for this option <br>Will be used for casting
     * @param  option
     *         The {@link net.dv8tion.jda.core.audit.AuditLogOption AuditLogOption}
     *
     * @throws java.lang.ClassCastException
     *         If the type-cast failed for the generic type.
     * @throws java.lang.IllegalArgumentException
     *         If provided with {@code null} option.
     *
     * @return Possibly-null value corresponding to the specified option constant
     */
    @Nullable
    public <T> T getOption(AuditLogOption option)
    {
        Checks.notNull(option, "Option");
        return getOptionByName(option.getKey());
    }

    /**
     * Constructs a filtered, immutable list of options corresponding to
     * the provided {@link net.dv8tion.jda.core.audit.AuditLogOption AuditLogOptions}.
     * <br>This will exclude options with {@code null} values!
     *
     * @param  options
     *         The not-null {@link net.dv8tion.jda.core.audit.AuditLogOption AuditLogOptions}
     *         which will be used to gather option values via {@link #getOption(AuditLogOption) getOption(AuditLogOption)}!
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null options
     *
     * @return Unmodifiable list of representative values
     */
    public List<Object> getOptions(AuditLogOption... options)
    {
        Checks.notNull(options, "Options");
        List<Object> items = new ArrayList<>(options.length);
        for (AuditLogOption option : options)
        {
            Object obj = getOption(option);
            if (obj != null)
                items.add(obj);
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * The {@link net.dv8tion.jda.core.audit.ActionType ActionType} defining what auditable
     * Action is referred to by this entry.
     *
     * @return The {@link net.dv8tion.jda.core.audit.ActionType ActionType}
     */
    public ActionType getType()
    {
        return type;
    }

    /**
     * The {@link net.dv8tion.jda.core.audit.TargetType TargetType} defining what kind of
     * entity was targeted by this action.
     * <br>Shortcut for {@code getType().getTargetType()}
     *
     * @return The {@link net.dv8tion.jda.core.audit.TargetType TargetType}
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
