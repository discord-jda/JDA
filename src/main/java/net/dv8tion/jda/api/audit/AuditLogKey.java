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

package net.dv8tion.jda.api.audit;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;

/**
 * Enum of possible/expected keys that can be provided
 * to {@link AuditLogEntry#getChangeByKey(AuditLogKey) AuditLogEntry.getChangeByKey(AuditLogEntry.AuditLogKey}.
 *
 * <p>Each constant in this enum has elaborate documentation on expected values for the
 * returned {@link AuditLogChange AuditLogChange}.
 * <br>There is no guarantee that the resulting type is accurate or that the value selected is not {@code null}!
 *
 * @see <a href="https://discord.com/developers/docs/resources/audit-log#audit-log-change-object-audit-log-change-key" target="_blank">Audit Log Change Key</a>
 */
public enum AuditLogKey
{
    /**
     * This is sometimes visible for {@link ActionType ActionTypes}
     * which create a new entity.
     * <br>Use with designated {@code getXById} method.
     *
     * <p>Expected type: <b>String</b>
     */
    ID("id"),

    /**
     * Entity type (like channel type or webhook type)
     *
     * <p>Expected type: <b>String or int</b>
     */
    TYPE("type"),

    /**
     * The id for an authorized application (webhook/bot/integration)
     *
     * <p>Expected type: <b>String</b>
     */
    APPLICATION_ID("application_id"),

    // GUILD
    /**
     * Change for the {@link net.dv8tion.jda.api.entities.Guild#getName() Guild.getName()} value
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_NAME("name"),

    /**
     * Change of User ID for the owner of a {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_OWNER("owner_id"),

    /**
     * Change of region represented by a key.
     * <br>Use with {@link net.dv8tion.jda.api.Region#fromKey(String) Region.fromKey(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_REGION("region"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild.Timeout AFKTimeout} of a Guild.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild.Timeout#fromKey(int) Timeout.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_AFK_TIMEOUT("afk_timeout"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getAfkChannel() Guild.getAfkChannel()} value represented by a VoiceChannel ID.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getVoiceChannelById(String) Guild.getVoiceChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_AFK_CHANNEL("afk_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getSystemChannel() Guild.getSystemChannel()} value represented by a TextChannel ID.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_SYSTEM_CHANNEL("system_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getRulesChannel() Guild.getRulesChannel()} value represented by a TextChannel ID.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_RULES_CHANNEL("rules_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getCommunityUpdatesChannel() Guild.getCommunityUpdatesChannel()} value represented by a TextChannel ID.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_COMMUNITY_UPDATES_CHANNEL("public_updates_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getExplicitContentLevel() Guild.getExplicitContentLevel()} of a Guild.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel#fromKey(int) Guild.ExplicitContentLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_EXPLICIT_CONTENT_FILTER("explicit_content_filter"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getIconId() Icon ID} of a Guild.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_ICON("icon_hash"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getSplashId() Splash ID} of a Guild.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_SPLASH("splash_hash"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getVerificationLevel() Guild.getVerificationLevel()} value.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel#fromKey(int) Guild.VerificationLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_VERIFICATION_LEVEL("verification_level"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getDefaultNotificationLevel() Guild.getDefaultNotificationLevel()} value.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel#fromKey(int) Guild.NotificationLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_NOTIFICATION_LEVEL("default_message_notifications"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Guild#getRequiredMFALevel() Guild.getRequiredMFALevel()} value
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild.MFALevel#fromKey(int) Guild.MFALevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_MFA_LEVEL("mfa_level"),

    /**
     * Change of the {@link Guild#getVanityCode()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_VANITY_URL_CODE("vanity_url_code"),

    /**
     * Days of inactivity for a prune event.
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_PRUNE_DELETE_DAYS("prune_delete_days"),

    /**
     * Whether the guild widget is disabled or enabled
     *
     * <p>Expected type: <b>Boolean</b>
     */
    GUILD_WIDGET_ENABLED("widget_enabled"),

    /**
     * The target channel for a widget
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_WIDGET_CHANNEL_ID("widget_channel_id"),


    // CHANNEL
    /**
     * Change of the {@link GuildChannel#getName() GuildChannel.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_NAME("name"),

    /**
     * Change of the {@link ICategorizableChannel#getParentCategory()} ICategorizable.getParentCategory()} value.
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getCategoryById(String) Guild.getCategoryById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_PARENT("parent_id"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.TextChannel#getTopic() TextChannel.getTopic()} value.
     * <br>Only for {@link net.dv8tion.jda.api.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_TOPIC("topic"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.TextChannel#getSlowmode() TextChannel.getSlowmode()} value.
     * <br>Only for {@link net.dv8tion.jda.api.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_SLOWMODE("rate_limit_per_user"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.VoiceChannel#getBitrate() VoiceChannel.getBitrate()} value.
     * <br>Only for {@link net.dv8tion.jda.api.entities.ChannelType#VOICE ChannelType.VOICE}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_BITRATE("bitrate"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.VoiceChannel#getUserLimit() VoiceChannel.getUserLimit()} value.
     * <br>Only for {@link net.dv8tion.jda.api.entities.ChannelType#VOICE ChannelType.VOICE}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_USER_LIMIT("user_limit"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.TextChannel#isNSFW() TextChannel.isNSFW()} value.
     * <br>Only for {@link net.dv8tion.jda.api.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>Boolean</b>
     */
    CHANNEL_NSFW("nsfw"),

    /**
     * Change of the {@link net.dv8tion.jda.api.Region Region} value.
     * <br>Only for {@link net.dv8tion.jda.api.entities.ChannelType#VOICE ChannelType.VOICE} and {@link net.dv8tion.jda.api.entities.ChannelType#STAGE ChannelType.STAGE}
     *
     * <p>Expected type: <b>String</b></p>
     */
    CHANNEL_REGION("rtc_region"),

    /**
     * The integer type of this channel.
     * <br>Use with {@link net.dv8tion.jda.api.entities.ChannelType#fromId(int) ChannelType.fromId(int)}.
     *
     * <p>Expected type: <b>int</b>
     */
    CHANNEL_TYPE("type"),

    /**
     * The overrides for this channel.
     *
     * <p>Expected type: <b>List{@literal <Map<String, Object>>}</b>
     */
    CHANNEL_OVERRIDES("permission_overwrites"),

    // THREADS

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.ThreadChannel#getName() ThreadChannel.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    THREAD_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.ThreadChannel#getSlowmode() ThreadChannel.getSlowmode()} value.
     *
     * <p>Expected type: <b>Integer</b>
     */
    THREAD_SLOWMODE("rate_limit_per_user"),

    /**
     * Change of the {@link ThreadChannel#getAutoArchiveDuration() ThreadChannel.getAutoArchiveDuration()} value.
     *
     * <p>Expected type: <b>Integer</b>
     */
    THREAD_AUTO_ARCHIVE_DURATION("auto_archive_duration"),

    /**
     * Change of the {@link ThreadChannel#isArchived() ThreadChannel.isArchived()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    THREAD_ARCHIVED("archived"),

    /**
     * Change of the {@link ThreadChannel#isLocked() ThreadChannel.isLocked()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    THREAD_LOCKED("locked"),

    /**
     * Change of the {@link ThreadChannel#isInvitable() ThreadChannel.isInvitable()} value.
     * This can only be set/modified on a {@link ThreadChannel#isPublic() private thread}.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    THREAD_INVITABLE("invitable"),

    // STAGE_INSTANCE

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.StageInstance#getPrivacyLevel() StageInstance.getPrivacyLevel()} value
     * <br>Use with {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel#fromKey(int) StageInstance.PrivacyLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    PRIVACY_LEVEL("privacy_level"),


    // MEMBER
    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Member#getNickname() Member.getNickname()} value
     *
     * <p>Expected type: <b>String</b>
     */
    MEMBER_NICK("nick"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Member#getVoiceState() GuildVoiceState} of a Member.
     * <br>Indicating that the {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildMuted() Guild.isGuildMuted()} value updated.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    MEMBER_MUTE("mute"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Member#getVoiceState() GuildVoiceState} of a Member.
     * <br>Indicating that the {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildDeafened() Guild.isGuildDeafened()} value updated.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    MEMBER_DEAF("deaf"),

    /**
     * Roles added to {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.api.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    MEMBER_ROLES_ADD("$add"),

    /**
     * Roles removed from {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.api.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    MEMBER_ROLES_REMOVE("$remove"),


    // PERMISSION OVERRIDE
    /**
     * Modified raw denied permission bits
     * <br>Similar to the value returned by {@link net.dv8tion.jda.api.entities.PermissionOverride#getDeniedRaw() PermissionOverride.getDeniedRaw()}
     * <br>Use with {@link net.dv8tion.jda.api.Permission#getPermissions(long) Permission.getPermissions(long)}
     *
     * <p>Expected type: <b>long</b>
     */
    OVERRIDE_DENY("deny"),

    /**
     * Modified raw allowed permission bits
     * <br>Similar to the value returned by {@link net.dv8tion.jda.api.entities.PermissionOverride#getAllowedRaw() PermissionOverride.getAllowedRaw()}
     * <br>Use with {@link net.dv8tion.jda.api.Permission#getPermissions(long) Permission.getPermissions(long)}
     *
     * <p>Expected type: <b>long</b>
     */
    OVERRIDE_ALLOW("allow"),

    /**
     * The string type of this override.
     * <br>{@code "role"} or {@code "member"}.
     *
     * <p>Expected type: <b>String</b>
     */
    OVERRIDE_TYPE("type"),


    // ROLE
    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Role#getName() Role.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    ROLE_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()} value.
     * <br>Use with {@link net.dv8tion.jda.api.Permission#getPermissions(long) Permission.getPermissions(long)}
     *
     * <p>Expected type: <b>Long</b>
     */
    ROLE_PERMISSIONS("permissions"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Role#getColor() Role.getColor()} value.
     * <br>Use with {@link java.awt.Color#Color(int) Color(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    ROLE_COLOR("color"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Role#isHoisted() Role.isHoisted()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    ROLE_HOISTED("hoist"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Role#isMentionable() Role.isMentionable()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    ROLE_MENTIONABLE("mentionable"),


    // EMOTE
    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Emote#getName() Emote.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    EMOTE_NAME("name"),

    /**
     * Roles added to {@link net.dv8tion.jda.api.entities.Emote#getRoles() Emote.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.api.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    EMOTE_ROLES_ADD("$add"),

    /**
     * Roles remove from {@link net.dv8tion.jda.api.entities.Emote#getRoles() Emote.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.api.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    EMOTE_ROLES_REMOVE("$remove"),


    // WEBHOOK
    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Webhook#getName() Webhook.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Webhook#getDefaultUser() Webhook.getDefaultUser()}'s avatar hash of a Webhook.
     * <br>This is used to build the {@link net.dv8tion.jda.api.entities.User#getAvatarUrl() User.getAvatarUrl()}!
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_ICON("avatar_hash"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Webhook#getChannel() Webhook.getChannel()} for
     * the target {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_CHANNEL("channel_id"),


    // INVITE
    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#getCode() Invite.getCode()} for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.api.entities.Invite#resolve(net.dv8tion.jda.api.JDA, String)} Invite.resolve(JDA, String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_CODE("code"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#getMaxAge() Invite.getMaxAge()} for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_MAX_AGE("max_age"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#isTemporary() Invite.isTemporary()} for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     *
     * <p>Expected type: <b>boolean</b>
     */
    INVITE_TEMPORARY("temporary"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#getInviter() Invite.getInviter()} ID for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.api.JDA#getUserById(String) JDA.getUserById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_INVITER("inviter"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#getChannel() Invite.getChannel()} ID for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.api.JDA#getTextChannelById(String) JDA.getTextChannelById(String)}
     * or {@link net.dv8tion.jda.api.JDA#getVoiceChannelById(String) JDA.getVoiceChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_CHANNEL("channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#getUses() Invite.getUses()} for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_USES("uses"),

    /**
     * Change of the {@link net.dv8tion.jda.api.entities.Invite#getMaxUses() Invite.getMaxUses()} for
     * the target {@link net.dv8tion.jda.api.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_MAX_USES("max_uses");


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
