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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.annotations.DeprecatedSince
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member.MemberFlag
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.awt.Color
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Guild-specific User.
 *
 *
 * Contains all guild-specific information about a User. (Roles, Nickname, VoiceStatus etc.)
 *
 * @since 3.0
 *
 * @see Guild.getMember
 * @see Guild.getMemberCache
 * @see Guild.getMemberById
 * @see Guild.getMemberByTag
 * @see Guild.getMemberByTag
 * @see Guild.getMembersByEffectiveName
 * @see Guild.getMembersByName
 * @see Guild.getMembersByNickname
 * @see Guild.getMembersWithRoles
 * @see Guild.getMembers
 */
interface Member : IMentionable, IPermissionHolder, UserSnowflake {
    @JvmField
    @get:Nonnull
    val user: User

    @get:Nonnull
    abstract override val guild: Guild

    @JvmField
    @get:Nonnull
    val jDA: JDA?

    @get:Nonnull
    val timeJoined: OffsetDateTime?

    /**
     * Whether this member has accurate [.getTimeJoined] information.
     * <br></br>Discord doesn't always provide this information when we load members so we have to fallback
     * to the [Guild] creation time.
     *
     *
     * You can use [guild.retrieveMemberById(member.getId())][Guild.retrieveMemberById]
     * to load the join time.
     *
     * @return True, if [.getTimeJoined] is accurate
     */
    fun hasTimeJoined(): Boolean

    /**
     * The time when this member boosted the guild.
     * <br></br>Null indicates this member is not currently boosting the guild.
     *
     * @return The boosting time, or null if the member is not boosting
     *
     * @since  4.0.0
     */
    @JvmField
    val timeBoosted: OffsetDateTime?

    /**
     * Returns whether a member is boosting the guild or not.
     *
     * @return True, if it is boosting
     */
    @JvmField
    val isBoosting: Boolean

    /**
     * The time this Member will be released from time out.
     * <br></br>If this Member is not in time out, this returns `null`.
     * This may also return dates in the past, in which case the time out has expired.
     *
     * @return The time this Member will be released from time out or `null` if not in time out
     */
    @JvmField
    val timeOutEnd: OffsetDateTime?
    val isTimedOut: Boolean
        /**
         * Whether this Member is in time out.
         * <br></br>While a Member is in time out, all permissions except [VIEW_CHANNEL][Permission.VIEW_CHANNEL] and
         * [MESSAGE_HISTORY][Permission.MESSAGE_HISTORY] are removed from them.
         *
         * @return True, if this Member is in time out
         */
        get() = timeOutEnd != null && timeOutEnd!!.isAfter(OffsetDateTime.now())

    /**
     * The [VoiceState][net.dv8tion.jda.api.entities.GuildVoiceState] of this Member.
     * <br></br>**This will be null when the [net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] is disabled manually**
     *
     *
     * This can be used to get the Member's VoiceChannel using [GuildVoiceState.getChannel].
     *
     *
     * This requires [CacheFlag.VOICE_STATE][net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] to be enabled!
     *
     * @return [GuildVoiceState][net.dv8tion.jda.api.entities.GuildVoiceState]
     */
    @JvmField
    val voiceState: GuildVoiceState?

    @JvmField
    @get:Nonnull
    val activities: List<Activity?>?

    @JvmField
    @get:Nonnull
    val onlineStatus: OnlineStatus?

    /**
     * The platform dependent [net.dv8tion.jda.api.OnlineStatus] of this member.
     * <br></br>Since a user can be connected from multiple different devices such as web and mobile,
     * discord specifies a status for each [net.dv8tion.jda.api.entities.ClientType].
     *
     *
     * If a user is not online on the specified type,
     * [OFFLINE][net.dv8tion.jda.api.OnlineStatus.OFFLINE] is returned.
     *
     *
     * This requires [CacheFlag.CLIENT_STATUS][net.dv8tion.jda.api.utils.cache.CacheFlag.CLIENT_STATUS] to be enabled!
     *
     * @param  type
     * The type of client
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided type is null
     *
     * @return The status for that specific client or OFFLINE
     *
     * @since  4.0.0
     */
    @Nonnull
    fun getOnlineStatus(@Nonnull type: ClientType?): OnlineStatus?

    @get:Nonnull
    val activeClients: EnumSet<ClientType?>?

    /**
     * Returns the current nickname of this Member for the parent Guild.
     *
     *
     * This can be changed using
     * [modifyNickname(Member, String)][net.dv8tion.jda.api.entities.Guild.modifyNickname].
     *
     * @return The nickname or null, if no nickname is set.
     */
    @JvmField
    val nickname: String?

    @JvmField
    @get:Nonnull
    val effectiveName: String?

    /**
     * The Discord Id for this member's per guild avatar image.
     * If the member has not set a per guild avatar, this will return null.
     *
     * @return Possibly-null String containing the [net.dv8tion.jda.api.entities.Member] per guild avatar id.
     */
    @JvmField
    val avatarId: String?
    val avatarUrl: String?
        /**
         * The URL for the member's per guild avatar image.
         * If the member has not set a per guild avatar, this will return null.
         *
         * @return Possibly-null String containing the [net.dv8tion.jda.api.entities.Member] per guild avatar url.
         */
        get() {
            val avatarId = avatarId
            return if (avatarId == null) null else String.format(
                AVATAR_URL,
                guild.id,
                id,
                avatarId,
                if (avatarId.startsWith("a_")) "gif" else "png"
            )
        }
    val avatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this member's avatar.
         *
         * @return Possibly-null [ImageProxy] of this member's avatar
         *
         * @see .getAvatarUrl
         */
        get() {
            val avatarUrl = avatarUrl
            return avatarUrl?.let { ImageProxy(it) }
        }

    @get:Nonnull
    val effectiveAvatarUrl: String?
        /**
         * The URL for the member's effective avatar image.
         * If they do not have a per guild avatar set, this will return the URL of
         * their effective [User] avatar.
         *
         * @return Never-null String containing the [net.dv8tion.jda.api.entities.Member] avatar url.
         */
        get() {
            val avatarUrl = avatarUrl
            return avatarUrl ?: user.getEffectiveAvatarUrl()
        }

    @get:Nonnull
    val effectiveAvatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this member's effective avatar image.
         *
         * @return Never-null [ImageProxy] of this member's effective avatar image
         *
         * @see .getEffectiveAvatarUrl
         */
        get() {
            val avatar = avatar
            return avatar ?: user.getEffectiveAvatar()
        }

    @JvmField
    @get:Nonnull
    val roles: List<Role?>

    /**
     * The [Color][java.awt.Color] of this Member's name in a Guild.
     *
     *
     * This is determined by the color of the highest role assigned to them that does not have the default color.
     * <br></br>If all roles have default color, this returns null.
     *
     * @return The display Color for this Member.
     *
     * @see .getColorRaw
     */
    val color: Color?

    /**
     * The raw RGB value for the color of this member.
     * <br></br>Defaulting to [Role.DEFAULT_COLOR_RAW][net.dv8tion.jda.api.entities.Role.DEFAULT_COLOR_RAW]
     * if this member uses the default color (special property, it changes depending on theme used in the client)
     *
     * @return The raw RGB value or the role default
     */
    val colorRaw: Int

    /**
     * The raw [flags][MemberFlag] bitset for this member.
     *
     * @return The raw flag bitset
     */
    val flagsRaw: Int

    @get:Nonnull
    val flags: EnumSet<MemberFlag?>?
        /**
         * The [flags][MemberFlag] for this member as an [EnumSet].
         * <br></br>Modifying this set will not update the member, it is a copy of existing flags.
         *
         * @return The flags
         */
        get() = MemberFlag.fromRaw(flagsRaw)

    /**
     * Whether this Member can interact with the provided Member
     * (kick/ban/etc.)
     *
     * @param  member
     * The target Member to check
     *
     * @throws NullPointerException
     * if the specified Member is null
     * @throws IllegalArgumentException
     * if the specified Member is not from the same guild
     *
     * @return True, if this Member is able to interact with the specified Member
     */
    fun canInteract(@Nonnull member: Member?): Boolean

    /**
     * Whether this Member can interact with the provided [Role][net.dv8tion.jda.api.entities.Role]
     * (kick/ban/move/modify/delete/etc.)
     *
     *
     * If this returns true this member can assign the role to other members.
     *
     * @param  role
     * The target Role to check
     *
     * @throws NullPointerException
     * if the specified Role is null
     * @throws IllegalArgumentException
     * if the specified Role is not from the same guild
     *
     * @return True, if this member is able to interact with the specified Role
     */
    fun canInteract(@Nonnull role: Role?): Boolean

    /**
     * Whether this Member can interact with the provided [RichCustomEmoji]
     * (use in a message)
     *
     * @param  emoji
     * The target emoji to check
     *
     * @throws NullPointerException
     * if the specified emoji is null
     * @throws IllegalArgumentException
     * if the specified emoji is not from the same guild
     *
     * @return True, if this Member is able to interact with the specified emoji
     */
    fun canInteract(@Nonnull emoji: RichCustomEmoji?): Boolean

    /**
     * Checks whether this member is the owner of its related [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @return True, if this member is the owner of the attached Guild.
     */
    @JvmField
    val isOwner: Boolean

    @JvmField
    @get:Incubating
    val isPending: Boolean

    /**
     * The [default channel][DefaultGuildChannelUnion] for a [Member][net.dv8tion.jda.api.entities.Member].
     * <br></br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time
     * after joining the guild.
     * <br></br>The default channel is the channel with the highest position in which the member has
     * [Permission.VIEW_CHANNEL] permissions. If this requirement doesn't apply for
     * any channel in the guild, this method returns `null`.
     *
     * @return The [channel][DefaultGuildChannelUnion] representing the default channel for this member
     * or null if no such channel exists.
     */
    val defaultChannel: DefaultGuildChannelUnion?

    /**
     * Bans this Member and deletes messages sent by the user based on the amount of delDays.
     * <br></br>If you wish to ban a user without deleting any messages, provide `deletionTimeframe` with a value of 0.
     * To set a ban reason, use [AuditableRestAction.reason].
     *
     *
     * You can unban a user with [Guild.unban(UserSnowflake)][net.dv8tion.jda.api.entities.Guild.unban].
     *
     *
     * **Note:** [net.dv8tion.jda.api.entities.Guild.getMembers] will still contain the
     * [Member][net.dv8tion.jda.api.entities.Member] until Discord sends the
     * [GuildMemberRemoveEvent][net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent].
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be banned due to a permission discrepancy
     *
     *  * [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
     * <br></br>The user no longer exists
     *
     *
     * @param  deletionTimeframe
     * The timeframe for the history of messages that will be deleted. (seconds precision)
     * @param  unit
     * Timeframe unit as a [TimeUnit] (for example `member.ban(7, TimeUnit.DAYS)`).
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.BAN_MEMBERS] permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     * If the logged in account cannot ban the other user due to permission hierarchy position.
     * <br></br>See [Member.canInteract]
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided deletionTimeframe is negative.
     *  * If the provided deletionTimeframe is longer than 7 days.
     *  * If the provided time unit is `null`
     *
     *
     * @return [AuditableRestAction]
     *
     * @see Guild.ban
     * @see AuditableRestAction.reason
     */
    @Nonnull
    @CheckReturnValue
    fun ban(deletionTimeframe: Int, @Nonnull unit: TimeUnit?): AuditableRestAction<Void?>? {
        return guild.ban(this, deletionTimeframe, unit)
    }

    /**
     * Kicks this Member from the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     *
     * **Note:** [net.dv8tion.jda.api.entities.Guild.getMembers] will still contain the [User][net.dv8tion.jda.api.entities.User]
     * until Discord sends the [GuildMemberRemoveEvent][net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent].
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be kicked due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.KICK_MEMBERS] permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     * If the logged in account cannot kick the other member due to permission hierarchy position.
     * <br></br>See [Member.canInteract]
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     * Kicks the provided Member from the current Guild
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    fun kick(): AuditableRestAction<Void?>? {
        return guild.kick(this)
    }

    /**
     * Kicks this Member from the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     *
     * **Note:** [net.dv8tion.jda.api.entities.Guild.getMembers] will still contain the [Member][net.dv8tion.jda.api.entities.Member]
     * until Discord sends the [GuildMemberRemoveEvent][net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent].
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be kicked due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @param  reason
     * The reason for this action or `null` if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.KICK_MEMBERS] permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     * If the logged in account cannot kick the other member due to permission hierarchy position.
     * <br></br>See [Member.canInteract]
     * @throws java.lang.IllegalArgumentException
     * If the provided reason is longer than 512 characters
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     * Kicks the provided Member from the current Guild
     *
     */
    @Nonnull
    @CheckReturnValue
    @ForRemoval
    @ReplaceWith("kick().reason(reason)")
    @DeprecatedSince("5.0.0")
    @Deprecated("         Use {@link #kick()} and {@link AuditableRestAction#reason(String)} instead")
    fun kick(reason: String?): AuditableRestAction<Void?>? {
        return guild.kick(this, reason)
    }

    /**
     * Puts this Member in time out in this [Guild][net.dv8tion.jda.api.entities.Guild] for a specific amount of time.
     * <br></br>While a Member is in time out, all permissions except [VIEW_CHANNEL][Permission.VIEW_CHANNEL] and
     * [MESSAGE_HISTORY][Permission.MESSAGE_HISTORY] are removed from them.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be put into time out due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @param  amount
     * The amount of the provided [unit][TimeUnit] to put this Member in time out for
     * @param  unit
     * The [Unit][TimeUnit] type of `amount`
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.MODERATE_MEMBERS] permission.
     * @throws IllegalArgumentException
     * If any of the following checks are true
     *
     *  * The provided `amount` is lower than or equal to `0`
     *  * The provided `unit` is null
     *  * The provided `amount` with the `unit` results in a date that is more than {@value MAX_TIME_OUT_LENGTH} days in the future
     *
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun timeoutFor(amount: Long, @Nonnull unit: TimeUnit?): AuditableRestAction<Void?>? {
        return guild.timeoutFor(this, amount, unit)
    }

    /**
     * Puts this Member in time out in this [Guild][net.dv8tion.jda.api.entities.Guild] for a specific amount of time.
     * <br></br>While a Member is in time out, all permissions except [VIEW_CHANNEL][Permission.VIEW_CHANNEL] and
     * [MESSAGE_HISTORY][Permission.MESSAGE_HISTORY] are removed from them.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be put into time out due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @param  duration
     * The duration to put this Member in time out for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.MODERATE_MEMBERS] permission.
     * @throws IllegalArgumentException
     * If any of the following checks are true
     *
     *  * The provided `duration` is null
     *  * The provided `duration` is not positive
     *  * The provided `duration` results in a date that is more than {@value MAX_TIME_OUT_LENGTH} days in the future
     *
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun timeoutFor(@Nonnull duration: Duration?): AuditableRestAction<Void?>? {
        return guild.timeoutFor(this, duration)
    }

    /**
     * Puts this Member in time out in this [Guild][net.dv8tion.jda.api.entities.Guild] until the specified date.
     * <br></br>While a Member is in time out, all permissions except [VIEW_CHANNEL][Permission.VIEW_CHANNEL] and
     * [MESSAGE_HISTORY][Permission.MESSAGE_HISTORY] are removed from them.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be put into time out due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @param  temporal
     * The time this Member will be released from time out
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.MODERATE_MEMBERS] permission.
     * @throws IllegalArgumentException
     * If any of the following checks are true
     *
     *  * The provided `temporal` is null
     *  * The provided `temporal` is in the past
     *  * The provided `temporal` is more than {@value MAX_TIME_OUT_LENGTH} days in the future
     *
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun timeoutUntil(@Nonnull temporal: TemporalAccessor?): AuditableRestAction<Void?>? {
        return guild.timeoutUntil(this, temporal)
    }

    /**
     * Removes a time out from this Member in this [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The time out cannot be removed due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.MODERATE_MEMBERS] permission.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeTimeout(): AuditableRestAction<Void?>? {
        return guild.removeTimeout(this)
    }

    /**
     * Sets the Guild Muted state state of this Member based on the provided
     * boolean.
     *
     *
     * **Note:** The Member's [GuildVoiceState.isGuildMuted()][net.dv8tion.jda.api.entities.GuildVoiceState.isGuildMuted] value won't change
     * until JDA receives the [GuildVoiceGuildMuteEvent][net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent] event related to this change.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be muted due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *  * [USER_NOT_CONNECTED][net.dv8tion.jda.api.requests.ErrorResponse.USER_NOT_CONNECTED]
     * <br></br>The specified Member is not connected to a voice channel
     *
     *
     * @param  mute
     * Whether this [Member][net.dv8tion.jda.api.entities.Member] should be muted or unmuted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.VOICE_DEAF_OTHERS] permission.
     * @throws java.lang.IllegalStateException
     * If the member is not currently connected to a voice channel.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    fun mute(mute: Boolean): AuditableRestAction<Void?>? {
        return guild.mute(this, mute)
    }

    /**
     * Sets the Guild Deafened state state of this Member based on the provided boolean.
     *
     *
     * **Note:** The Member's [GuildVoiceState.isGuildDeafened()][net.dv8tion.jda.api.entities.GuildVoiceState.isGuildDeafened] value won't change
     * until JDA receives the [GuildVoiceGuildDeafenEvent][net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent] event related to this change.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The target Member cannot be deafened due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *  * [USER_NOT_CONNECTED][net.dv8tion.jda.api.requests.ErrorResponse.USER_NOT_CONNECTED]
     * <br></br>The specified Member is not connected to a voice channel
     *
     *
     * @param  deafen
     * Whether this [Member][net.dv8tion.jda.api.entities.Member] should be deafened or undeafened.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [Permission.VOICE_DEAF_OTHERS] permission.
     * @throws java.lang.IllegalStateException
     * If the member is not currently connected to a voice channel.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    fun deafen(deafen: Boolean): AuditableRestAction<Void?>? {
        return guild.deafen(this, deafen)
    }

    /**
     * Changes this Member's nickname in this guild.
     * The nickname is visible to all members of this guild.
     *
     *
     * To change the nickname for the currently logged in account
     * only the Permission [NICKNAME_CHANGE][Permission.NICKNAME_CHANGE] is required.
     * <br></br>To change the nickname of **any** [Member][net.dv8tion.jda.api.entities.Member] for this [Guild][net.dv8tion.jda.api.entities.Guild]
     * the Permission [NICKNAME_MANAGE][Permission.NICKNAME_MANAGE] is required.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The nickname of the target Member is not modifiable due to a permission discrepancy
     *
     *  * [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER]
     * <br></br>The specified Member was removed from the Guild before finishing the task
     *
     *
     * @param  nickname
     * The new nickname of the [Member][net.dv8tion.jda.api.entities.Member], provide `null` or an
     * empty String to reset the nickname
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If attempting to set nickname for self and the logged in account has neither [Permission.NICKNAME_CHANGE]
     * or [Permission.NICKNAME_MANAGE]
     *  * If attempting to set nickname for another member and the logged in account does not have [Permission.NICKNAME_MANAGE]
     *
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     * If attempting to set nickname for another member and the logged in account cannot manipulate the other user due to permission hierarchy position.
     * <br></br>See [.canInteract].
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    fun modifyNickname(nickname: String?): AuditableRestAction<Void?>? {
        return guild.modifyNickname(this, nickname)
    }

    /**
     * Updates the flags to the new flag set.
     * <br></br>If any of the flags is not [modifiable][MemberFlag.isModifiable], it is not updated.
     *
     *
     * Any flags not provided by the set will be disabled, all contained flags will be enabled.
     *
     * @param  newFlags
     * The new flags for the member.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the bot does not have [Permission.MODERATE_MEMBERS] in the guild
     * @throws IllegalArgumentException
     * If `null` is provided
     *
     * @return [AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun modifyFlags(@Nonnull newFlags: Collection<MemberFlag?>?): AuditableRestAction<Void?>? {
        Checks.noneNull(newFlags, "Flags")
        if (!guild.getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) throw InsufficientPermissionException(
            guild, Permission.MODERATE_MEMBERS
        )
        var flags = flagsRaw
        val updated = Helpers.copyEnumSet(MemberFlag::class.java, newFlags)
        for (flag in MemberFlag.entries) {
            if (flag.isModifiable) {
                flags = if (updated.contains(flag)) flags or flag.raw else flags and flag.raw.inv()
            }
        }
        val body = DataObject.empty().put("flags", flags)
        val route = Route.Guilds.MODIFY_MEMBER.compile(guild.id, id)
        return AuditableRestActionImpl(jDA, route, body)
    }

    /**
     * Member flags indicating information about the membership state.
     */
    enum class MemberFlag(
        /**
         * The raw value used by Discord for this flag
         *
         * @return The raw value
         */
        val raw: Int,
        /**
         * Whether this flag can be modified by the client
         *
         * @return True, if this flag can be modified
         */
        val isModifiable: Boolean
    ) {
        /**
         * The Member has left and rejoined the guild
         */
        DID_REJOIN(1, false),

        /**
         * The Member has completed the onboarding process
         */
        COMPLETED_ONBOARDING(1 shl 1, false),

        /**
         * The Member bypasses guild verification requirements
         */
        BYPASSES_VERIFICATION(1 shl 2, true),

        /**
         * The Member has started the onboarding process
         */
        STARTED_ONBOARDING(1 shl 3, false);

        companion object {
            /**
             * The [Flags][MemberFlag] represented by the provided raw value.
             * <br></br>If the provided raw value is `0` this will return an empty [EnumSet][java.util.EnumSet].
             *
             * @param  raw
             * The raw value
             *
             * @return EnumSet containing the flags represented by the provided raw value
             */
            @JvmStatic
            @Nonnull
            fun fromRaw(raw: Int): EnumSet<MemberFlag?> {
                val flags = EnumSet.noneOf(MemberFlag::class.java)
                for (flag in entries) {
                    if (raw and flag.raw == flag.raw) flags.add(flag)
                }
                return flags
            }

            /**
             * The raw value of the provided [Flags][MemberFlag].
             * <br></br>If the provided set is empty this will return `0`.
             *
             * @param  flags
             * The flags
             *
             * @return The raw value of the provided flags
             */
            fun toRaw(@Nonnull flags: Collection<MemberFlag?>): Int {
                Checks.noneNull(flags, "Flags")
                var raw = 0
                for (flag in flags) raw = raw or flag!!.raw
                return raw
            }
        }
    }

    companion object {
        /** Template for [.getAvatarUrl].  */
        const val AVATAR_URL = "https://cdn.discordapp.com/guilds/%s/users/%s/avatars/%s.%s"

        /** Maximum number of days a Member can be timed out for  */
        const val MAX_TIME_OUT_LENGTH = 28
    }
}
