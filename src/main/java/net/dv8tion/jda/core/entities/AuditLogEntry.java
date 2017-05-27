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
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Single entry for an {@link net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction
 * AuditLogPaginationAction}.
 * <br>This entry contains all options/changes and details for the action
 * that was logged by the {@link net.dv8tion.jda.core.entities.Guild Guild} audit-logs.
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
     * Key-Value {@link java.util.Map Map} containing all {@link net.dv8tion.jda.core.entities.AuditLogChange
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
     *         The {@link net.dv8tion.jda.core.entities.AuditLogEntry.AuditLogKey AuditLogKey} to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
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
    public AuditLogChange getChangeByKey(final String key)
    {
        return changes.get(key);
    }

    /**
     * Filters all changes by the specified keys
     *
     * @param  keys
     *         Varargs {@link net.dv8tion.jda.core.entities.AuditLogEntry.AuditLogKey AuditLogKeys} to look for
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null array
     *
     * @return Possibly-empty, never-null immutable list of {@link net.dv8tion.jda.core.entities.AuditLogChange AuditLogChanges}
     */
    public List<AuditLogChange> getChangesForKeys(AuditLogKey... keys)
    {
        Args.notNull(keys, "Keys");
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
     * for {@link net.dv8tion.jda.core.ActionType#CHANNEL_OVERRIDE_UPDATE CHANNEL_OVERRIDE_UPDATE}
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

    /**
     * Enum of possible/expected keys that can be provided
     * to {@link AuditLogEntry#getChangeByKey(AuditLogEntry.AuditLogKey) AuditLogEntry.getChangeByKey(AuditLogEntry.AuditLogKey}.
     *
     * <p>Each constant in this enum has elaborate documentation on expected values for the
     * returned {@link net.dv8tion.jda.core.entities.AuditLogChange AuditLogChange}.
     * <br>There is no guarantee that the resulting type is accurate or that the value selected is not {@code null}!
     */
    public enum AuditLogKey
    {
        // GUILD
        /**
         * Change for the {@link Guild#getName() Guild.getName()} value
         *
         * <p>Expected type: <b>String</b>
         */
        GUILD_NAME("name"),

        /**
         * Change of User ID for the owner of a {@link net.dv8tion.jda.core.entities.Guild Guild}
         *
         * <p>Expected type: <b>String</b>
         */
        GUILD_OWNER("owner_id"),

        /**
         * Change of region represented by a key.
         * <br>Use with {@link net.dv8tion.jda.core.Region#fromKey(String) Region.fromKey(String)}
         *
         * <p>Expected type: <b>String</b>
         */
        GUILD_REGION("region"),

        /**
         * Change of the {@link net.dv8tion.jda.core.entities.Guild.Timeout AFKTimeout} of a Guild.
         * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.Timeout#fromKey(int) Timeout.fromKey(int)}
         *
         * <p>Expected type: <b>Integer</b>
         */
        GUILD_AFK_TIMEOUT("afk_timeout"),

        /**
         * Change of the {@link Guild#getAfkChannel() Guild.getAfkChannel()} value represented by a VoiceChannel ID.
         * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getVoiceChannelById(String)
         * Guild.getVoiceChannelById(String)}
         *
         * <p>Expected type: <b>String</b>
         */
        GUILD_AFK_CHANNEL("afk_channel_id"),

        /**
         * Change of the {@link Guild#getExplicitContentLevel() Guild.getExplicitContentLevel()} of a Guild.
         * <br>Use with {@link Guild.ExplicitContentLevel#fromKey(int) Guild.ExplicitContentLevel.fromKey(int)}
         *
         * <p>Expected type: <b>Integer</b>
         */
        GUILD_EXPLICIT_CONTENT_FILTER("explicit_content_filter"),

        /**
         * Change of the {@link Guild#getIconId() Icon ID} of a Guild.
         *
         * <p>Expected type: <b>String</b>
         */
        GUILD_ICON("icon"),

        /**
         * Change of the {@link Guild#getSplashId() Splash ID} of a Guild.
         *
         * <p>Expected type: <b>String</b>
         */
        GUILD_SPLASH("splash"),

        /**
         * Change of the {@link Guild#getVerificationLevel() Guild.getVerificationLevel()} value.
         * <br>Use with {@link Guild.VerificationLevel#fromKey(int) Guild.VerificationLevel.fromKey(int)}
         *
         * <p>Expected type: <b>Integer</b>
         */
        GUILD_VERIFICATION_LEVEL("verification_level"),

        /**
         * Change of the {@link Guild#getDefaultNotificationLevel() Guild.getDefaultNotificationLevel()} value.
         * <br>Use with {@link Guild.NotificationLevel#fromKey(int) Guild.NotificationLevel.fromKey(int)}
         *
         * <p>Expected type: <b>Integer</b>
         */
        GUILD_NOTIFICATION_LEVEL("default_message_notifications"),

        /**
         * Change of the {@link Guild#getRequiredMFALevel() Guild.getRequiredMFALevel()} value
         * <br>Use with {@link Guild.MFALevel#fromKey(int) Guild.MFALevel.fromKey(int)}
         *
         * <p>Expected type: <b>Integer</b>
         */
        GUILD_MFA_LEVEL("mfa_level"),


        // CHANNEL
        /**
         * Change of the {@link Channel#getName() Channel.getName()} value.
         *
         * <p>Expected type: <b>String</b>
         */
        CHANNEL_NAME("name"),

        /**
         * Change of the {@link TextChannel#getTopic() TextChannel.getTopic()} value.
         * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#TEXT ChannelType.TEXT}
         *
         * <p>Expected type: <b>String</b>
         */
        CHANNEL_TOPIC("topic"),

        /**
         * Change of the {@link VoiceChannel#getBitrate() VoiceChannel.getBitrate()} value.
         * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE ChannelType.VOICE}
         *
         * <p>Expected type: <b>Integer</b>
         */
        CHANNEL_BITRATE("bitrate"),

        /**
         * Change of the {@link VoiceChannel#getUserLimit() VoiceChannel.getUserLimit()} value.
         * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE ChannelType.VOICE}
         *
         * <p>Expected type: <b>Integer</b>
         */
        CHANNEL_USER_LIMIT("user_limit"),


        // MEMBER
        /**
         * Change of the {@link Member#getNickname() Member.getNickname()} value
         *
         * <p>Expected type: <b>String</b>
         */
        MEMBER_NICK("nick"),

        /**
         * Change of the {@link Member#getVoiceState() GuildVoiceState} of a Member.
         * <br>Indicating that the {@link GuildVoiceState#isGuildMuted() Guild.isGuildMuted()} value updated.
         *
         * <p>Expected type: <b>Boolean</b>
         */
        MEMBER_MUTE("mute"),

        /**
         * Change of the {@link Member#getVoiceState() GuildVoiceState} of a Member.
         * <br>Indicating that the {@link GuildVoiceState#isGuildDeafened() Guild.isGuildDeafened()} value updated.
         *
         * <p>Expected type: <b>Boolean</b>
         */
        MEMBER_DEAF("deaf"),

        /**
         * Roles added to {@link Member#getRoles() Member.getRoles()} with this action
         * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
         * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
         *
         * <p>Expected type: <b>List{@literal <String>}</b>
         */
        MEMBER_ROLES_ADD("$add"),

        /**
         * Roles removed from {@link Member#getRoles() Member.getRoles()} with this action
         * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
         * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
         *
         * <p>Expected type: <b>List{@literal <String>}</b>
         */
        MEMBER_ROLES_REMOVE("$remove"),


        // PERMISSION OVERRIDE
        /**
         * Modified raw denied permission bits
         * <br>Similar to the value returned by {@link PermissionOverride#getDeniedRaw() PermissionOverride.getDeniedRaw()}
         * <br>Use with {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)}
         *
         * <p>Expected type: <b>Long</b>
         */
        OVERRIDE_DENY("deny"),

        /**
         * Modified raw allowed permission bits
         * <br>Similar to the value returned by {@link PermissionOverride#getAllowedRaw() PermissionOverride.getAllowedRaw()}
         * <br>Use with {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)}
         *
         * <p>Expected type: <b>Long</b>
         */
        OVERRIDE_ALLOW("allow"),


        // ROLE
        /**
         * Change of the {@link Role#getName() Role.getName()} value.
         *
         * <p>Expected type: <b>String</b>
         */
        ROLE_NAME("name"),

        /**
         * Change of the {@link Role#getPermissionsRaw() Role.getPermissionsRaw()} value.
         * <br>Use with {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)}
         *
         * <p>Expected type: <b>Long</b>
         */
        ROLE_PERMISSIONS("permissions"),

        /**
         * Change of the {@link Role#getColor() Role.getColor()} value.
         * <br>Use with {@link java.awt.Color#Color(int) Color(int)}
         *
         * <p>Expected type: <b>Integer</b>
         */
        ROLE_COLOR("color"),

        /**
         * Change of the {@link Role#isHoisted() Role.isHoisted()} value.
         *
         * <p>Expected type: <b>Boolean</b>
         */
        ROLE_HOISTED("hoisted"),

        /**
         * Change of the {@link Role#isMentionable() Role.isMentionable()} value.
         *
         * <p>Expected type: <b>Boolean</b>
         */
        ROLE_MENTIONABLE("mentionable"),


        // EMOTE
        /**
         * Change of the {@link Emote#getName() Emote.getName()} value.
         *
         * <p>Expected type: <b>String</b>
         */
        EMOTE_NAME("name"),

        /**
         * Roles added to {@link Emote#getRoles() Emote.getRoles()} with this action
         * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
         * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
         *
         * <p>Expected type: <b>List{@literal <String>}</b>
         */
        EMOTE_ROLES_ADD("$add"),

        /**
         * Roles remove from {@link Emote#getRoles() Emote.getRoles()} with this action
         * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
         * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
         *
         * <p>Expected type: <b>List{@literal <String>}</b>
         */
        EMOTE_ROLES_REMOVE("$remove"),


        // WEBHOOK
        /**
         * Change of the {@link Webhook#getName() Webhook.getName()} value.
         *
         * <p>Expected type: <b>String</b>
         */
        WEBHOOK_NAME("name"),

        /**
         * Change of the {@link Webhook#getDefaultUser() Webhook.getDefaultUser()}'s avatar hash of a Webhook.
         * <br>This is used to build the {@link User#getAvatarUrl() User.getAvatarUrl()}!
         *
         * <p>Expected type: <b>String</b>
         */
        WEBHOOK_ICON("avatar_hash"),

        /**
         * Change of the {@link Webhook#getChannel() Webhook.getChannel()} for
         * the target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
         * <br>Use with {@link Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
         *
         * <p>Expected type: <b>String</b>
         */
        WEBHOOK_CHANNEL("channel_id");

        private final String key;

        AuditLogKey(String key)
        {
            this.key = key;
        }

        public String getKey()
        {
            return key;
        }

        @Override
        public String toString()
        {
            return name() + '(' + key + ')';
        }
    }
}
