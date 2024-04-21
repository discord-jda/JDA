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
package net.dv8tion.jda.api.audit

import net.dv8tion.jda.annotations.DeprecatedSince
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.internal.utils.EntityString

/**
 * Enum of possible/expected keys that can be provided
 * to [AuditLogEntry.getChangeByKey(AuditLogEntry.AuditLogKey][AuditLogEntry.getChangeByKey].
 *
 *
 * Each constant in this enum has elaborate documentation on expected values for the
 * returned [AuditLogChange].
 * <br></br>There is no guarantee that the resulting type is accurate or that the value selected is not `null`!
 *
 * @see [Audit Log Change Key](https://discord.com/developers/docs/resources/audit-log.audit-log-change-object-audit-log-change-key)
 */
enum class AuditLogKey(val key: String) {
    /**
     * This is sometimes visible for [ActionTypes][ActionType]
     * which create a new entity.
     * <br></br>Use with designated `getXById` method.
     *
     *
     * Expected type: **String**
     */
    ID("id"),

    /**
     * Entity type (like channel type or webhook type)
     *
     *
     * Expected type: **String or int**
     */
    TYPE("type"),

    /**
     * The id for an authorized application (webhook/bot/integration)
     *
     *
     * Expected type: **String**
     */
    APPLICATION_ID("application_id"),
    // GUILD
    /**
     * Change for the [Guild.getName()][net.dv8tion.jda.api.entities.Guild.getName] value
     *
     *
     * Expected type: **String**
     */
    GUILD_NAME("name"),

    /**
     * Change of User ID for the owner of a [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     *
     * Expected type: **String**
     */
    GUILD_OWNER("owner_id"),

    /**
     * Change of region represented by a key.
     * <br></br>Use with [Region.fromKey(String)][net.dv8tion.jda.api.Region.fromKey]
     *
     *
     * Expected type: **String**
     */
    GUILD_REGION("region"),

    /**
     * Change of the [AFKTimeout][net.dv8tion.jda.api.entities.Guild.Timeout] of a Guild.
     * <br></br>Use with [Timeout.fromKey(int)][net.dv8tion.jda.api.entities.Guild.Timeout.fromKey]
     *
     *
     * Expected type: **Integer**
     */
    GUILD_AFK_TIMEOUT("afk_timeout"),

    /**
     * Change of the [Guild.getAfkChannel()][net.dv8tion.jda.api.entities.Guild.getAfkChannel] value represented by a VoiceChannel ID.
     * <br></br>Use with [Guild.getVoiceChannelById(String)][net.dv8tion.jda.api.entities.Guild.getVoiceChannelById]
     *
     *
     * Expected type: **String**
     */
    GUILD_AFK_CHANNEL("afk_channel_id"),

    /**
     * Change of the [Guild.getSystemChannel()][net.dv8tion.jda.api.entities.Guild.getSystemChannel] value represented by a TextChannel ID.
     * <br></br>Use with [Guild.getTextChannelById(String)][net.dv8tion.jda.api.entities.Guild.getTextChannelById]
     *
     *
     * Expected type: **String**
     */
    GUILD_SYSTEM_CHANNEL("system_channel_id"),

    /**
     * Change of the [Guild.getRulesChannel()][net.dv8tion.jda.api.entities.Guild.getRulesChannel] value represented by a TextChannel ID.
     * <br></br>Use with [Guild.getTextChannelById(String)][net.dv8tion.jda.api.entities.Guild.getTextChannelById]
     *
     *
     * Expected type: **String**
     */
    GUILD_RULES_CHANNEL("rules_channel_id"),

    /**
     * Change of the [Guild.getCommunityUpdatesChannel()][net.dv8tion.jda.api.entities.Guild.getCommunityUpdatesChannel] value represented by a TextChannel ID.
     * <br></br>Use with [Guild.getTextChannelById(String)][net.dv8tion.jda.api.entities.Guild.getTextChannelById]
     *
     *
     * Expected type: **String**
     */
    GUILD_COMMUNITY_UPDATES_CHANNEL("public_updates_channel_id"),

    /**
     * Change of the [Guild.getExplicitContentLevel()][net.dv8tion.jda.api.entities.Guild.getExplicitContentLevel] of a Guild.
     * <br></br>Use with [Guild.ExplicitContentLevel.fromKey(int)][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel.fromKey]
     *
     *
     * Expected type: **Integer**
     */
    GUILD_EXPLICIT_CONTENT_FILTER("explicit_content_filter"),

    /**
     * Change of the [Icon ID][net.dv8tion.jda.api.entities.Guild.getIconId] of a Guild.
     *
     *
     * Expected type: **String**
     */
    GUILD_ICON("icon_hash"),

    /**
     * Change of the [Splash ID][net.dv8tion.jda.api.entities.Guild.getSplashId] of a Guild.
     *
     *
     * Expected type: **String**
     */
    GUILD_SPLASH("splash_hash"),

    /**
     * Change of the [Guild.getVerificationLevel()][net.dv8tion.jda.api.entities.Guild.getVerificationLevel] value.
     * <br></br>Use with [Guild.VerificationLevel.fromKey(int)][net.dv8tion.jda.api.entities.Guild.VerificationLevel.fromKey]
     *
     *
     * Expected type: **Integer**
     */
    GUILD_VERIFICATION_LEVEL("verification_level"),

    /**
     * Change of the [Guild.getDefaultNotificationLevel()][net.dv8tion.jda.api.entities.Guild.getDefaultNotificationLevel] value.
     * <br></br>Use with [Guild.NotificationLevel.fromKey(int)][net.dv8tion.jda.api.entities.Guild.NotificationLevel.fromKey]
     *
     *
     * Expected type: **Integer**
     */
    GUILD_NOTIFICATION_LEVEL("default_message_notifications"),

    /**
     * Change of the [Guild.getRequiredMFALevel()][net.dv8tion.jda.api.entities.Guild.getRequiredMFALevel] value
     * <br></br>Use with [Guild.MFALevel.fromKey(int)][net.dv8tion.jda.api.entities.Guild.MFALevel.fromKey]
     *
     *
     * Expected type: **Integer**
     */
    GUILD_MFA_LEVEL("mfa_level"),

    /**
     * Change of the [Guild.getVanityCode] value.
     *
     *
     * Expected type: **String**
     */
    GUILD_VANITY_URL_CODE("vanity_url_code"),

    /**
     * Days of inactivity for a prune event.
     *
     *
     * Expected type: **Integer**
     */
    GUILD_PRUNE_DELETE_DAYS("prune_delete_days"),

    /**
     * Whether the guild widget is disabled or enabled
     *
     *
     * Expected type: **Boolean**
     */
    GUILD_WIDGET_ENABLED("widget_enabled"),

    /**
     * The target channel for a widget
     *
     *
     * Expected type: **String**
     */
    GUILD_WIDGET_CHANNEL_ID("widget_channel_id"),
    // CHANNEL
    /**
     * Change of the [GuildChannel.getName()][GuildChannel.getName] value.
     *
     *
     * Expected type: **String**
     */
    CHANNEL_NAME("name"),

    /**
     * Change of the [flags][Channel.getFlags] value.
     *
     *
     * Expected type: **Integer**
     */
    CHANNEL_FLAGS("flags"),

    /**
     * Change of the [ICategorizableChannel.getParentCategory] ICategorizable.getParentCategory()} value.
     * <br></br>Use with [Guild.getCategoryById(String)][net.dv8tion.jda.api.entities.Guild.getCategoryById]
     *
     *
     * Expected type: **String**
     */
    CHANNEL_PARENT("parent_id"),

    /**
     * Change of the [TextChannel.getTopic()][TextChannel.getTopic] value.
     * <br></br>Only for [ChannelType.TEXT]
     *
     *
     * Expected type: **String**
     */
    CHANNEL_TOPIC("topic"),

    /**
     * Change of the [VoiceChannel.getStatus()][VoiceChannel.getStatus] value.
     * <br></br>Only for [ChannelType.VOICE]
     *
     *
     * Expected type: **String**
     */
    CHANNEL_VOICE_STATUS("status"),

    /**
     * Change of the [ISlowmodeChannel.getSlowmode] value.
     *
     *
     * Expected type: **Integer**
     */
    CHANNEL_SLOWMODE("rate_limit_per_user"),

    /**
     * Change of the [IThreadContainer.getDefaultThreadSlowmode] value.
     *
     *
     * Expected type: **Integer**
     */
    CHANNEL_DEFAULT_THREAD_SLOWMODE("default_thread_rate_limit_per_user"),

    /**
     * Change of the [ForumChannel.getDefaultReaction] value.
     *
     *
     * Expected type: **Map** containing `emoji_id` and `emoji_name`
     */
    CHANNEL_DEFAULT_REACTION_EMOJI("default_reaction_emoji"),

    /**
     * Change of the [VoiceChannel.getBitrate()][VoiceChannel.getBitrate] value.
     * <br></br>Only for [ChannelType.VOICE]
     *
     *
     * Expected type: **Integer**
     */
    CHANNEL_BITRATE("bitrate"),

    /**
     * Change of the [VoiceChannel.getUserLimit()][VoiceChannel.getUserLimit] value.
     * <br></br>Only for [ChannelType.VOICE]
     *
     *
     * Expected type: **Integer**
     */
    CHANNEL_USER_LIMIT("user_limit"),

    /**
     * Change of the [IAgeRestrictedChannel.isNSFW()][net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel.isNSFW] value.
     *
     *
     * Expected type: **Boolean**
     */
    CHANNEL_NSFW("nsfw"),

    /**
     * Change of the [Region][net.dv8tion.jda.api.Region] value.
     * <br></br>Only for [ChannelType.VOICE] and [ChannelType.STAGE]
     *
     *
     * Expected type: **String**
     */
    CHANNEL_REGION("rtc_region"),

    /**
     * The integer type of this channel.
     * <br></br>Use with [ChannelType.fromId(int)][ChannelType.fromId].
     *
     *
     * Expected type: **int**
     */
    CHANNEL_TYPE("type"),

    /**
     * The overrides for this channel.
     *
     *
     * Expected type: **List&lt;Map&lt;String, Object&gt;&gt;**
     */
    CHANNEL_OVERRIDES("permission_overwrites"),

    /**
     * The available tags of this [ForumChannel][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
     *
     *
     * Expected type: **List&lt;Map&lt;String, Object&gt;&gt;**
     */
    CHANNEL_AVAILABLE_TAGS("available_tags"),

    /**
     * The relevant channel for the audit log entry.
     *
     *
     * Expected type: **String**
     */
    CHANNEL_ID("channel_id"),

    /**
     * The [ForumChannel.getDefaultSortOrder] value.
     * <br></br>Only for [ChannelType.FORUM] and [ChannelType.MEDIA].
     *
     *
     * Expected type: **Integer**
     */
    CHANNEL_DEFAULT_SORT_ORDER("default_sort_order"),

    /**
     * The [ForumChannel.getDefaultLayout] value.
     * <br></br>Only for [ChannelType.FORUM].
     *
     *
     * Expected type: **Integer**
     */
    DEFAULT_FORUM_LAYOUT("default_forum_layout"),
    // THREADS
    /**
     * Change of the [ThreadChannel.getName()][ThreadChannel.getName] value.
     *
     *
     * Expected type: **String**
     */
    THREAD_NAME("name"),

    /**
     * Change of the [ISlowmodeChannel.getSlowmode] value.
     *
     *
     * Expected type: **Integer**
     *
     */
    @ForRemoval
    @DeprecatedSince("5.0.0")
    @ReplaceWith("CHANNEL_SLOWMODE")
    @Deprecated("Use {@link #CHANNEL_SLOWMODE} instead")
    THREAD_SLOWMODE("rate_limit_per_user"),

    /**
     * Change of the [ThreadChannel.getAutoArchiveDuration()][ThreadChannel.getAutoArchiveDuration] value.
     *
     *
     * Expected type: **Integer**
     */
    THREAD_AUTO_ARCHIVE_DURATION("auto_archive_duration"),

    /**
     * Change of the [ThreadChannel.isArchived()][ThreadChannel.isArchived] value.
     *
     *
     * Expected type: **Boolean**
     */
    THREAD_ARCHIVED("archived"),

    /**
     * Change of the [ThreadChannel.isLocked()][ThreadChannel.isLocked] value.
     *
     *
     * Expected type: **Boolean**
     */
    THREAD_LOCKED("locked"),

    /**
     * Change of the [ThreadChannel.isInvitable()][ThreadChannel.isInvitable] value.
     * This can only be set/modified on a [private thread][ThreadChannel.isPublic].
     *
     *
     * Expected type: **Boolean**
     */
    THREAD_INVITABLE("invitable"),

    /**
     * The applied tags of this [ThreadChannel], given that it is a forum post.
     *
     *
     * Expected type: **List&lt;String&gt;**
     */
    THREAD_APPLIED_TAGS("applied_tags"),
    // STAGE_INSTANCE
    /**
     * Change of the [StageInstance.getPrivacyLevel()][net.dv8tion.jda.api.entities.StageInstance.getPrivacyLevel] value
     * <br></br>Use with [StageInstance.PrivacyLevel.fromKey(int)][net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel.fromKey]
     *
     *
     * Expected type: **Integer**
     */
    PRIVACY_LEVEL("privacy_level"),
    // MEMBER
    /**
     * Change of the [Member.getNickname()][net.dv8tion.jda.api.entities.Member.getNickname] value
     *
     *
     * Expected type: **String**
     */
    MEMBER_NICK("nick"),

    /**
     * Change of the [GuildVoiceState][net.dv8tion.jda.api.entities.Member.getVoiceState] of a Member.
     * <br></br>Indicating that the [Guild.isGuildMuted()][net.dv8tion.jda.api.entities.GuildVoiceState.isGuildMuted] value updated.
     *
     *
     * Expected type: **Boolean**
     */
    MEMBER_MUTE("mute"),

    /**
     * Change of the [GuildVoiceState][net.dv8tion.jda.api.entities.Member.getVoiceState] of a Member.
     * <br></br>Indicating that the [Guild.isGuildDeafened()][net.dv8tion.jda.api.entities.GuildVoiceState.isGuildDeafened] value updated.
     *
     *
     * Expected type: **Boolean**
     */
    MEMBER_DEAF("deaf"),

    /**
     * Roles added to [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles] with this action
     * <br></br>Containing a list of [Role][net.dv8tion.jda.api.entities.Role] IDs
     * <br></br>Use with [Guild.getRoleById(String)][net.dv8tion.jda.api.entities.Guild.getRoleById]
     *
     *
     * Expected type: **List&lt;String&gt;**
     */
    MEMBER_ROLES_ADD("\$add"),

    /**
     * Roles removed from [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles] with this action
     * <br></br>Containing a list of [Role][net.dv8tion.jda.api.entities.Role] IDs
     * <br></br>Use with [Guild.getRoleById(String)][net.dv8tion.jda.api.entities.Guild.getRoleById]
     *
     *
     * Expected type: **List&lt;String&gt;**
     */
    MEMBER_ROLES_REMOVE("\$remove"),

    /**
     * Change of the [Time out][net.dv8tion.jda.api.entities.Member.getTimeOutEnd] of a Member.
     * <br></br>Indicating that the [Member.getTimeOutEnd()][net.dv8tion.jda.api.entities.Member.getTimeOutEnd] value updated.
     * <br></br>This is provided as an ISO8601 Date-Time string.
     *
     *
     * Expected type: **String**
     */
    MEMBER_TIME_OUT("communication_disabled_until"),
    // PERMISSION OVERRIDE
    /**
     * Modified raw denied permission bits
     * <br></br>Similar to the value returned by [PermissionOverride.getDeniedRaw()][net.dv8tion.jda.api.entities.PermissionOverride.getDeniedRaw]
     * <br></br>Use with [Permission.getPermissions(long)][net.dv8tion.jda.api.Permission.getPermissions]
     *
     *
     * Expected type: **long**
     */
    OVERRIDE_DENY("deny"),

    /**
     * Modified raw allowed permission bits
     * <br></br>Similar to the value returned by [PermissionOverride.getAllowedRaw()][net.dv8tion.jda.api.entities.PermissionOverride.getAllowedRaw]
     * <br></br>Use with [Permission.getPermissions(long)][net.dv8tion.jda.api.Permission.getPermissions]
     *
     *
     * Expected type: **long**
     */
    OVERRIDE_ALLOW("allow"),

    /**
     * The string type of this override.
     * <br></br>`"role"` or `"member"`.
     *
     *
     * Expected type: **String**
     */
    OVERRIDE_TYPE("type"),
    // ROLE
    /**
     * Change of the [Role.getName()][net.dv8tion.jda.api.entities.Role.getName] value.
     *
     *
     * Expected type: **String**
     */
    ROLE_NAME("name"),

    /**
     * Change of the [Role.getPermissionsRaw()][net.dv8tion.jda.api.entities.Role.getPermissionsRaw] value.
     * <br></br>Use with [Permission.getPermissions(long)][net.dv8tion.jda.api.Permission.getPermissions]
     *
     *
     * Expected type: **Long**
     */
    ROLE_PERMISSIONS("permissions"),

    /**
     * Change of the [Role.getColor()][net.dv8tion.jda.api.entities.Role.getColor] value.
     * <br></br>Use with [Color(int)][java.awt.Color.Color]
     *
     *
     * Expected type: **Integer**
     */
    ROLE_COLOR("color"),

    /**
     * Change of the [Role.isHoisted()][net.dv8tion.jda.api.entities.Role.isHoisted] value.
     *
     *
     * Expected type: **Boolean**
     */
    ROLE_HOISTED("hoist"),

    /**
     * Change of the [Role.isMentionable()][net.dv8tion.jda.api.entities.Role.isMentionable] value.
     *
     *
     * Expected type: **Boolean**
     */
    ROLE_MENTIONABLE("mentionable"),
    // EMOJI
    /**
     * Change of the [Emoji.getName()][RichCustomEmoji.getName] value.
     *
     *
     * Expected type: **String**
     */
    EMOJI_NAME("name"),

    /**
     * Roles added to [RichCustomEmoji.getRoles()][RichCustomEmoji.getRoles] with this action
     * <br></br>Containing a list of [Role][net.dv8tion.jda.api.entities.Role] IDs
     * <br></br>Use with [Guild.getRoleById(String)][net.dv8tion.jda.api.entities.Guild.getRoleById]
     *
     *
     * Expected type: **List&lt;String&gt;**
     */
    EMOJI_ROLES_ADD("\$add"),

    /**
     * Roles remove from [RichCustomEmoji.getRoles()][RichCustomEmoji.getRoles] with this action
     * <br></br>Containing a list of [Role][net.dv8tion.jda.api.entities.Role] IDs
     * <br></br>Use with [Guild.getRoleById(String)][net.dv8tion.jda.api.entities.Guild.getRoleById]
     *
     *
     * Expected type: **List&lt;String&gt;**
     */
    EMOJI_ROLES_REMOVE("\$remove"),
    // STICKER
    /**
     * Change of the [Sticker.getName()][GuildSticker.getName] value.
     *
     *
     * Expected type: **String**
     */
    STICKER_NAME("name"),

    /**
     * Change of the [Sticker.getFormatType()][GuildSticker.getFormatType] value.
     *
     *
     * Expected type: **String**
     */
    STICKER_FORMAT("format_type"),

    /**
     * Change of the [Sticker.getDescription()][GuildSticker.getDescription] value.
     *
     *
     * Expected type: **String**
     */
    STICKER_DESCRIPTION("description"),

    /**
     * Change of the [Sticker.getTags()][GuildSticker.getTags] value.
     *
     *
     * Expected type: **String**
     */
    STICKER_TAGS("tags"),
    // WEBHOOK
    /**
     * Change of the [Webhook.getName()][net.dv8tion.jda.api.entities.Webhook.getName] value.
     *
     *
     * Expected type: **String**
     */
    WEBHOOK_NAME("name"),

    /**
     * Change of the [Webhook.getDefaultUser()][net.dv8tion.jda.api.entities.Webhook.getDefaultUser]'s avatar hash of a Webhook.
     * <br></br>This is used to build the [User.getAvatarUrl()][net.dv8tion.jda.api.entities.User.getAvatarUrl]!
     *
     *
     * Expected type: **String**
     */
    WEBHOOK_ICON("avatar_hash"),

    /**
     * Change of the [Webhook.getChannel()][net.dv8tion.jda.api.entities.Webhook.getChannel] for
     * the target [Webhook][net.dv8tion.jda.api.entities.Webhook]
     * <br></br>Use with [Guild.getTextChannelById(String)][net.dv8tion.jda.api.entities.Guild.getTextChannelById]
     *
     *
     * Expected type: **String**
     */
    WEBHOOK_CHANNEL("channel_id"),
    // INVITE
    /**
     * Change of the [Invite.getCode()][net.dv8tion.jda.api.entities.Invite.getCode] for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     * <br></br>Use with [net.dv8tion.jda.api.entities.Invite.resolve] Invite.resolve(JDA, String)}
     *
     *
     * Expected type: **String**
     */
    INVITE_CODE("code"),

    /**
     * Change of the [Invite.getMaxAge()][net.dv8tion.jda.api.entities.Invite.getMaxAge] for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     *
     *
     * Expected type: **int**
     */
    INVITE_MAX_AGE("max_age"),

    /**
     * Change of the [Invite.isTemporary()][net.dv8tion.jda.api.entities.Invite.isTemporary] for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     *
     *
     * Expected type: **boolean**
     */
    INVITE_TEMPORARY("temporary"),

    /**
     * Change of the [Invite.getInviter()][net.dv8tion.jda.api.entities.Invite.getInviter] ID for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     * <br></br>Use with [JDA.getUserById(String)][net.dv8tion.jda.api.JDA.getUserById]
     *
     *
     * Expected type: **String**
     */
    INVITE_INVITER("inviter"),

    /**
     * Change of the [Invite.getChannel()][net.dv8tion.jda.api.entities.Invite.getChannel] ID for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     * <br></br>Use with [JDA.getTextChannelById(String)][net.dv8tion.jda.api.JDA.getTextChannelById]
     * or [JDA.getVoiceChannelById(String)][net.dv8tion.jda.api.JDA.getVoiceChannelById]
     *
     *
     * Expected type: **String**
     */
    INVITE_CHANNEL("channel_id"),

    /**
     * Change of the [Invite.getUses()][net.dv8tion.jda.api.entities.Invite.getUses] for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     *
     *
     * Expected type: **int**
     */
    INVITE_USES("uses"),

    /**
     * Change of the [Invite.getMaxUses()][net.dv8tion.jda.api.entities.Invite.getMaxUses] for
     * the target [Invite][net.dv8tion.jda.api.entities.Invite]
     *
     *
     * Expected type: **int**
     */
    INVITE_MAX_USES("max_uses"),
    // AUTO MODERATION
    /**
     * Change of the [AutoModRule.getName] for the target [AutoModRule]
     *
     *
     * Expected type: **String**
     */
    AUTO_MODERATION_RULE_NAME("auto_moderation_rule_name"),

    /**
     * The [AutoModRule.getTriggerType] for an [AutoModRule] trigger
     *
     *
     * Use with [AutoModTriggerType.fromKey]
     *
     *
     * Expected type: **int**
     */
    AUTO_MODERATION_RULE_TRIGGER_TYPE("auto_moderation_rule_trigger_type");

    override fun toString(): String {
        return EntityString(this)
            .setType(this)
            .addMetadata("key", key)
            .toString()
    }
}
