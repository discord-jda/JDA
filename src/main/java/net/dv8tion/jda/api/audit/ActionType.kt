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

/**
 * ActionTypes for [AuditLogEntry][net.dv8tion.jda.api.audit.AuditLogEntry] instances
 * <br></br>Found via [AuditLogEntry.getType()][net.dv8tion.jda.api.audit.AuditLogEntry.getType]
 */
enum class ActionType(
    /**
     * The raw key used to identify types within the api.
     *
     * @return Raw key for this ActionType
     */
    @JvmField val key: Int,
    /**
     * The expected [TargetType][net.dv8tion.jda.api.audit.TargetType]
     * for this ActionType
     *
     * @return [TargetType][net.dv8tion.jda.api.audit.TargetType]
     */
    val targetType: TargetType
) {
    /**
     * An Administrator updated [Guild][net.dv8tion.jda.api.entities.Guild] information.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [GUILD_AFK_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_AFK_CHANNEL]
     *  * [GUILD_AFK_TIMEOUT][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_AFK_TIMEOUT]
     *  * [GUILD_EXPLICIT_CONTENT_FILTER][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_EXPLICIT_CONTENT_FILTER]
     *  * [GUILD_ICON][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_ICON]
     *  * [GUILD_MFA_LEVEL][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_MFA_LEVEL]
     *  * [GUILD_NAME][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_NAME]
     *  * [GUILD_NOTIFICATION_LEVEL][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_NOTIFICATION_LEVEL]
     *  * [GUILD_OWNER][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_OWNER]
     *  * [GUILD_REGION][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_REGION]
     *  * [GUILD_SPLASH][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_SPLASH]
     *  * [GUILD_SYSTEM_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_SYSTEM_CHANNEL]
     *  * [GUILD_RULES_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_RULES_CHANNEL]
     *  * [GUILD_COMMUNITY_UPDATES_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.GUILD_COMMUNITY_UPDATES_CHANNEL]
     *
     */
    GUILD_UPDATE(1, TargetType.GUILD),

    /**
     * An Administrator created a [GuildChannel]
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_BITRATE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_BITRATE] (VoiceChannel only)
     *  * [CHANNEL_USER_LIMIT][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_USER_LIMIT] (VoiceChannel only)
     *  * [CHANNEL_TOPIC][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TOPIC] (TextChannel only)
     *  * [CHANNEL_SLOWMODE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_SLOWMODE] (TextChannel only)
     *  * [CHANNEL_NSFW][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_NSFW] (TextChannel only)
     *  * [CHANNEL_OVERRIDES][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_OVERRIDES]
     *  * [CHANNEL_NAME][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_NAME]
     *  * [CHANNEL_TYPE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TYPE]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    CHANNEL_CREATE(10, TargetType.CHANNEL),

    /**
     * An Administrator updated [GuildChannel] information.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_BITRATE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_BITRATE] (VoiceChannel only)
     *  * [CHANNEL_USER_LIMIT][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_USER_LIMIT] (VoiceChannel only)
     *  * [CHANNEL_TOPIC][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TOPIC] (TextChannel only)
     *  * [CHANNEL_SLOWMODE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_SLOWMODE] (TextChannel only)
     *  * [CHANNEL_NSFW][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_NSFW] (TextChannel only)
     *  * [CHANNEL_NAME][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_NAME]
     *  * [CHANNEL_TYPE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TYPE]
     *
     */
    CHANNEL_UPDATE(11, TargetType.CHANNEL),

    /**
     * An Administrator deleted a [GuildChannel].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_BITRATE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_BITRATE] (VoiceChannel only)
     *  * [CHANNEL_USER_LIMIT][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_USER_LIMIT] (VoiceChannel only)
     *  * [CHANNEL_TOPIC][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TOPIC] (TextChannel only)
     *  * [CHANNEL_SLOWMODE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_SLOWMODE] (TextChannel only)
     *  * [CHANNEL_NSFW][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_NSFW] (TextChannel only)
     *  * [CHANNEL_OVERRIDES][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_OVERRIDES]
     *  * [CHANNEL_NAME][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_NAME]
     *  * [CHANNEL_TYPE][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TYPE]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    CHANNEL_DELETE(12, TargetType.CHANNEL),

    /**
     * An Administrator created a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [OVERRIDE_ALLOW][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_ALLOW]
     *  * [OVERRIDE_DENY][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_DENY]
     *  * [OVERRIDE_TYPE][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_TYPE]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [ROLE][net.dv8tion.jda.api.audit.AuditLogOption.ROLE]
     *  * [USER][net.dv8tion.jda.api.audit.AuditLogOption.USER]
     *
     */
    CHANNEL_OVERRIDE_CREATE(13, TargetType.CHANNEL),

    /**
     * An Administrator updated [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] information.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [OVERRIDE_ALLOW][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_ALLOW]
     *  * [OVERRIDE_DENY][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_DENY]
     *
     */
    CHANNEL_OVERRIDE_UPDATE(14, TargetType.CHANNEL),

    /**
     * An Administrator deleted a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [OVERRIDE_ALLOW][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_ALLOW]
     *  * [OVERRIDE_DENY][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_DENY]
     *  * [OVERRIDE_TYPE][net.dv8tion.jda.api.audit.AuditLogKey.OVERRIDE_TYPE]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [ROLE][net.dv8tion.jda.api.audit.AuditLogOption.ROLE]
     *  * [USER][net.dv8tion.jda.api.audit.AuditLogOption.USER]
     *
     */
    CHANNEL_OVERRIDE_DELETE(15, TargetType.CHANNEL),

    /**
     * An Administrator has kicked a member.
     */
    KICK(20, TargetType.MEMBER),

    /**
     * An Administrator has pruned members for inactivity.
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [DELETE_MEMBER_DAYS][net.dv8tion.jda.api.audit.AuditLogOption.DELETE_MEMBER_DAYS]
     *  * [MEMBERS_REMOVED][net.dv8tion.jda.api.audit.AuditLogOption.MEMBERS_REMOVED]
     *
     */
    PRUNE(21, TargetType.MEMBER),

    /**
     * An Administrator has banned a user.
     */
    BAN(22, TargetType.MEMBER),

    /**
     * An Administrator has unbanned a user.
     */
    UNBAN(23, TargetType.MEMBER),

    /**
     * A [Member][net.dv8tion.jda.api.entities.Member] was either updated by an administrator or
     * the member updated itself.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [MEMBER_NICK][net.dv8tion.jda.api.audit.AuditLogKey.MEMBER_NICK]
     *
     */
    MEMBER_UPDATE(24, TargetType.MEMBER),

    /**
     * An Administrator updated the roles of a member.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [MEMBER_ROLES_ADD][net.dv8tion.jda.api.audit.AuditLogKey.MEMBER_ROLES_ADD]
     *  * [MEMBER_ROLES_REMOVE][net.dv8tion.jda.api.audit.AuditLogKey.MEMBER_ROLES_REMOVE]
     *
     */
    MEMBER_ROLE_UPDATE(25, TargetType.MEMBER),

    /**
     * One or more members were moved from one voice channel to another by an Administrator
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [AuditLogOption.COUNT] The amount of users moved
     *  * [AuditLogOption.CHANNEL] The target channel
     *
     */
    MEMBER_VOICE_MOVE(26, TargetType.MEMBER),

    /**
     * One or more members were disconnected from a voice channel by an Administrator
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [AuditLogOption.COUNT] The amount of users who were disconnected
     *
     */
    MEMBER_VOICE_KICK(27, TargetType.MEMBER),

    /**
     * An Administrator has added a bot to the server.
     */
    BOT_ADD(28, TargetType.MEMBER),

    /**
     * An Administrator has created a [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [ROLE_COLOR][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_COLOR]
     *  * [ROLE_HOISTED][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_HOISTED]
     *  * [ROLE_MENTIONABLE][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_MENTIONABLE]
     *  * [ROLE_NAME][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_NAME]
     *  * [ROLE_PERMISSIONS][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_PERMISSIONS]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    ROLE_CREATE(30, TargetType.ROLE),

    /**
     * An Administrator has updated a [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [ROLE_COLOR][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_COLOR]
     *  * [ROLE_HOISTED][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_HOISTED]
     *  * [ROLE_MENTIONABLE][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_MENTIONABLE]
     *  * [ROLE_NAME][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_NAME]
     *  * [ROLE_PERMISSIONS][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_PERMISSIONS]
     *
     */
    ROLE_UPDATE(31, TargetType.ROLE),

    /**
     * An Administrator has deleted a [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [ROLE_COLOR][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_COLOR]
     *  * [ROLE_HOISTED][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_HOISTED]
     *  * [ROLE_MENTIONABLE][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_MENTIONABLE]
     *  * [ROLE_NAME][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_NAME]
     *  * [ROLE_PERMISSIONS][net.dv8tion.jda.api.audit.AuditLogKey.ROLE_PERMISSIONS]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    ROLE_DELETE(32, TargetType.ROLE),

    /**
     * Someone has created an [Invite][net.dv8tion.jda.api.entities.Invite].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [INVITE_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_CHANNEL]
     *  * [INVITE_CODE][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_CODE]
     *  * [INVITE_INVITER][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_INVITER]
     *  * [INVITE_MAX_AGE][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_MAX_AGE]
     *  * [INVITE_MAX_USES][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_MAX_USES]
     *  * [INVITE_USES][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_USES]
     *
     */
    INVITE_CREATE(40, TargetType.INVITE),

    /**
     * An [Invite][net.dv8tion.jda.api.entities.Invite] has been updated.
     */
    INVITE_UPDATE(41, TargetType.INVITE),

    /**
     * An Administrator has deleted an [Invite][net.dv8tion.jda.api.entities.Invite].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [INVITE_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_CHANNEL]
     *  * [INVITE_CODE][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_CODE]
     *  * [INVITE_INVITER][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_INVITER]
     *  * [INVITE_MAX_AGE][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_MAX_AGE]
     *  * [INVITE_MAX_USES][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_MAX_USES]
     *  * [INVITE_USES][net.dv8tion.jda.api.audit.AuditLogKey.INVITE_USES]
     *
     */
    INVITE_DELETE(42, TargetType.INVITE),

    /**
     * An Administrator has created a [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [WEBHOOK_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_CHANNEL]
     *  * [WEBHOOK_ICON][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_ICON]
     *  * [WEBHOOK_NAME][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_NAME]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    WEBHOOK_CREATE(50, TargetType.WEBHOOK),

    /**
     * An Administrator has updated a [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [WEBHOOK_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_CHANNEL]
     *  * [WEBHOOK_ICON][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_ICON]
     *  * [WEBHOOK_NAME][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_NAME]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    WEBHOOK_UPDATE(51, TargetType.WEBHOOK),

    /**
     * An Administrator has deleted a [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [WEBHOOK_CHANNEL][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_CHANNEL]
     *  * [WEBHOOK_ICON][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_ICON]
     *  * [WEBHOOK_NAME][net.dv8tion.jda.api.audit.AuditLogKey.WEBHOOK_NAME]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    WEBHOOK_REMOVE(52, TargetType.WEBHOOK),

    /**
     * An Administrator created an [Custom Emoji][RichCustomEmoji].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [EMOJI_NAME][net.dv8tion.jda.api.audit.AuditLogKey.EMOJI_NAME]
     *
     */
    EMOJI_CREATE(60, TargetType.EMOJI),

    /**
     * An Administrator updated an [Custom Emoji][RichCustomEmoji].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [EMOJI_NAME][net.dv8tion.jda.api.audit.AuditLogKey.EMOJI_NAME]
     *  * [EMOJI_ROLES_ADD][net.dv8tion.jda.api.audit.AuditLogKey.EMOJI_ROLES_ADD]
     *  * [EMOJI_ROLES_REMOVE][net.dv8tion.jda.api.audit.AuditLogKey.EMOJI_ROLES_REMOVE]
     *
     */
    EMOJI_UPDATE(61, TargetType.EMOJI),

    /**
     * An Administrator deleted an [Custom Emoji][RichCustomEmoji].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [EMOJI_NAME][net.dv8tion.jda.api.audit.AuditLogKey.EMOJI_NAME]
     *
     */
    EMOJI_DELETE(62, TargetType.EMOJI),

    /**
     * A message was created.
     */
    MESSAGE_CREATE(70, TargetType.UNKNOWN),

    /**
     * A message was updated.
     */
    MESSAGE_UPDATE(71, TargetType.UNKNOWN),

    /**
     * An Administrator has deleted one or more [Messages][net.dv8tion.jda.api.entities.Message].
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [COUNT][net.dv8tion.jda.api.audit.AuditLogOption.COUNT]
     *
     */
    MESSAGE_DELETE(72, TargetType.MEMBER),

    /**
     * An Administrator has performed a bulk delete of messages in a channel
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [AuditLogOption.COUNT]
     *
     */
    MESSAGE_BULK_DELETE(73, TargetType.CHANNEL),

    /**
     * An Administrator has pinned a message in the channel
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [AuditLogOption.CHANNEL]
     *  * [AuditLogOption.MESSAGE]
     *
     */
    MESSAGE_PIN(74, TargetType.CHANNEL),

    /**
     * An Administrator has unpinned a message in the channel
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [AuditLogOption.CHANNEL]
     *  * [AuditLogOption.MESSAGE]
     *
     */
    MESSAGE_UNPIN(75, TargetType.CHANNEL),

    /**
     * An Administrator has added an integration to the guild
     */
    INTEGRATION_CREATE(80, TargetType.INTEGRATION),

    /**
     * An Administrator has updated an integration of the guild
     */
    INTEGRATION_UPDATE(81, TargetType.INTEGRATION),

    /**
     * An Administrator has removed an integration from the guild
     */
    INTEGRATION_DELETE(82, TargetType.INTEGRATION),

    /**
     * A [StageInstance][net.dv8tion.jda.api.entities.StageInstance] was started by a [Stage Moderator][net.dv8tion.jda.api.entities.channel.concrete.StageChannel.isModerator].
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [CHANNEL][net.dv8tion.jda.api.audit.AuditLogOption.CHANNEL]
     *
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_TOPIC][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TOPIC]
     *  * [STAGE_INSTANCE_PRIVACY_LEVEL][net.dv8tion.jda.api.audit.AuditLogKey.PRIVACY_LEVEL]
     *
     */
    STAGE_INSTANCE_CREATE(83, TargetType.STAGE_INSTANCE),

    /**
     * A [StageInstance][net.dv8tion.jda.api.entities.StageInstance] was updated by a [Stage Moderator][net.dv8tion.jda.api.entities.channel.concrete.StageChannel.isModerator].
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [CHANNEL][net.dv8tion.jda.api.audit.AuditLogOption.CHANNEL]
     *
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_TOPIC][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TOPIC]
     *  * [STAGE_INSTANCE_PRIVACY_LEVEL][net.dv8tion.jda.api.audit.AuditLogKey.PRIVACY_LEVEL]
     *
     */
    STAGE_INSTANCE_UPDATE(84, TargetType.STAGE_INSTANCE),

    /**
     * A [StageInstance][net.dv8tion.jda.api.entities.StageInstance] was deleted by a [Stage Moderator][net.dv8tion.jda.api.entities.channel.concrete.StageChannel.isModerator].
     *
     *
     * **Possible Options**<br></br>
     *
     *  * [CHANNEL][net.dv8tion.jda.api.audit.AuditLogOption.CHANNEL]
     *
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_TOPIC][net.dv8tion.jda.api.audit.AuditLogKey.CHANNEL_TOPIC]
     *  * [STAGE_INSTANCE_PRIVACY_LEVEL][net.dv8tion.jda.api.audit.AuditLogKey.PRIVACY_LEVEL]
     *
     */
    STAGE_INSTANCE_DELETE(85, TargetType.STAGE_INSTANCE),

    /**
     * A user created a [ScheduledEvent]
     */
    SCHEDULED_EVENT_CREATE(100, TargetType.SCHEDULED_EVENT),

    /**
     * A user updated a [ScheduledEvent]
     */
    SCHEDULED_EVENT_UPDATE(101, TargetType.SCHEDULED_EVENT),

    /**
     * A user deleted/cancelled a [ScheduledEvent]
     */
    SCHEDULED_EVENT_DELETE(102, TargetType.SCHEDULED_EVENT),

    /**
     * An Administrator created a [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [STICKER_NAME][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_NAME]
     *  * [STICKER_FORMAT][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_FORMAT]
     *  * [STICKER_DESCRIPTION][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_DESCRIPTION]
     *  * [STICKER_TAGS][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_TAGS]
     *
     */
    STICKER_CREATE(90, TargetType.STICKER),

    /**
     * An Administrator updated a [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [STICKER_DESCRIPTION][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_DESCRIPTION]
     *  * [STICKER_TAGS][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_TAGS]
     *
     */
    STICKER_UPDATE(91, TargetType.STICKER),

    /**
     * An Administrator deleted a [GuildSticker][net.dv8tion.jda.api.entities.sticker.GuildSticker].
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [STICKER_NAME][net.dv8tion.jda.api.audit.AuditLogKey.STICKER_NAME]
     *
     */
    STICKER_DELETE(92, TargetType.STICKER),

    /**
     * A user created a [ThreadChannel][net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel]
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [THREAD_NAME][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_NAME]
     *  * [THREAD_SLOWMODE][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_SLOWMODE]
     *  * [THREAD_ARCHIVED][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_ARCHIVED]
     *  * [THREAD_AUTO_ARCHIVE_DURATION][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_AUTO_ARCHIVE_DURATION]
     *  * [THREAD_LOCKED][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_LOCKED]
     *  * [THREAD_INVITABLE][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_INVITABLE]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    THREAD_CREATE(110, TargetType.THREAD),

    /**
     * A user updated a [ThreadChannel][net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel]
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [THREAD_NAME][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_NAME]
     *  * [THREAD_SLOWMODE][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_SLOWMODE]
     *  * [THREAD_ARCHIVED][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_ARCHIVED]
     *  * [THREAD_AUTO_ARCHIVE_DURATION][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_AUTO_ARCHIVE_DURATION]
     *  * [THREAD_LOCKED][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_LOCKED]
     *  * [THREAD_INVITABLE][net.dv8tion.jda.api.audit.AuditLogKey.THREAD_INVITABLE]
     *  * [ID][net.dv8tion.jda.api.audit.AuditLogKey.ID]
     *
     */
    THREAD_UPDATE(111, TargetType.THREAD),

    /**
     * A user deleted a [ThreadChannel][net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel]
     */
    THREAD_DELETE(112, TargetType.THREAD),

    /**
     * A moderator updated the privileges for an application
     */
    APPLICATION_COMMAND_PRIVILEGES_UPDATE(121, TargetType.INTEGRATION),

    /**
     * A moderator created a new [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     */
    AUTO_MODERATION_RULE_CREATE(140, TargetType.AUTO_MODERATION_RULE),

    /**
     * A moderator updated an existing [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     */
    AUTO_MODERATION_RULE_UPDATE(141, TargetType.AUTO_MODERATION_RULE),

    /**
     * A moderator deleted an existing [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     */
    AUTO_MODERATION_RULE_DELETE(142, TargetType.AUTO_MODERATION_RULE),

    /**
     * An automod rule blocked a message from being sent
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [AUTO_MODERATION_RULE_NAME][AuditLogKey.AUTO_MODERATION_RULE_NAME]
     *  * [AUTO_MODERATION_RULE_TRIGGER_TYPE][AuditLogKey.AUTO_MODERATION_RULE_TRIGGER_TYPE]
     *  * [CHANNEL_ID][AuditLogKey.CHANNEL_ID]
     *
     */
    AUTO_MODERATION_RULE_BLOCK_MESSAGE(143, TargetType.MEMBER),

    /**
     * An automod rule sent an alert to a channel
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [AUTO_MODERATION_RULE_NAME][AuditLogKey.AUTO_MODERATION_RULE_NAME]
     *  * [AUTO_MODERATION_RULE_TRIGGER_TYPE][AuditLogKey.AUTO_MODERATION_RULE_TRIGGER_TYPE]
     *
     */
    AUTO_MODERATION_FLAG_TO_CHANNEL(144, TargetType.MEMBER),

    /**
     * An automod rule put a user in [timeout][Member.isTimedOut]
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [AUTO_MODERATION_RULE_NAME][AuditLogKey.AUTO_MODERATION_RULE_NAME]
     *  * [AUTO_MODERATION_RULE_TRIGGER_TYPE][AuditLogKey.AUTO_MODERATION_RULE_TRIGGER_TYPE]
     *
     */
    AUTO_MODERATION_MEMBER_TIMEOUT(145, TargetType.MEMBER),

    /**
     * A user updated the [status][IVoiceStatusChannel.getStatus] of a voice channel.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_VOICE_STATUS][AuditLogKey.CHANNEL_VOICE_STATUS]
     *  * [CHANNEL_ID][AuditLogKey.CHANNEL_ID]
     *
     */
    VOICE_CHANNEL_STATUS_UPDATE(192, TargetType.CHANNEL),

    /**
     * A user removed the [status][IVoiceStatusChannel.getStatus] of a voice channel.
     *
     *
     * **Possible Keys**<br></br>
     *
     *  * [CHANNEL_ID][AuditLogKey.CHANNEL_ID]
     *
     */
    VOICE_CHANNEL_STATUS_DELETE(193, TargetType.CHANNEL),
    UNKNOWN(-1, TargetType.UNKNOWN);

    companion object {
        @JvmStatic
        fun from(key: Int): ActionType {
            for (type in entries) {
                if (type.key == key) return type
            }
            return UNKNOWN
        }
    }
}
