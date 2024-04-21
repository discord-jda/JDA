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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild.VerificationLevel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.internal.entities.InviteImpl
import java.time.OffsetDateTime
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Representation of a Discord Invite.
 * This class is immutable.
 *
 * @since  3.0
 * @author Aljoscha Grebe
 *
 * @see .resolve
 * @see .resolve
 * @see net.dv8tion.jda.api.entities.Guild.retrieveInvites
 * @see net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer.retrieveInvites
 */
interface Invite {
    /**
     * Deletes this invite.
     * <br></br>Requires [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] in the invite's channel.
     * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * if the account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the invite's channel
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?

    /**
     * Tries to retrieve a new expanded [Invite] with more info.
     * <br></br>As bots can't be in groups this is only available for guild invites and will throw an [IllegalStateException][java.lang.IllegalStateException]
     * for other types.
     * <br></br>Requires either [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the invite's guild or
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] in the invite's channel.
     * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * if the account neither has [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the invite's guild nor
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] in the invite's channel
     * @throws java.lang.IllegalStateException
     * If this is a group invite
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Invite]
     * <br></br>The expanded Invite object
     *
     * @see .getType
     * @see .isExpanded
     */
    @Nonnull
    @CheckReturnValue
    fun expand(): RestAction<Invite?>?

    @get:Nonnull
    val type: InviteType?

    @get:Nonnull
    val targetType: TargetType?

    /**
     * An [Invite.Channel] object
     * containing information about this invite's origin channel.
     *
     * @return Information about this invite's origin channel or null in case of a group invite
     *
     * @see Invite.Channel
     */
    val channel: Channel?

    /**
     * An [Invite.Group] object
     * containing information about this invite's origin group.
     *
     * @return Information about this invite's origin group or null in case of a guild invite
     *
     * @see Invite.Group
     */
    val group: Group?

    /**
     * An [Invite.InviteTarget] object
     * containing information about this invite's target or `null`
     * if this invite does not have a target.
     *
     * @return Information about this invite's target or `null`
     *
     * @see Invite.InviteTarget
     */
    val target: InviteTarget?

    @JvmField
    @get:Nonnull
    val code: String

    @get:Nonnull
    val url: String?
        /**
         * The invite URL for this invite in the format of:
         * `"https://discord.gg/" + getCode()`
         *
         * @return Invite URL for this Invite
         */
        get() = "https://discord.gg/" + this.code

    /**
     * An [Invite.Guild] object
     * containing information about this invite's origin guild.
     *
     * @return Information about this invite's origin guild or null in case of a group invite
     *
     * @see Invite.Guild
     */
    val guild: Guild?

    /**
     * The user who created this invite. For not expanded invites this may be null.
     *
     * @return The user who created this invite
     */
    val inviter: User?

    @get:Nonnull
    val jDA: JDA?

    /**
     * The max age of this invite in seconds.
     *
     *
     * This works only for expanded invites and will throw a [IllegalStateException] otherwise!
     *
     * @throws IllegalStateException
     * if this invite is not expanded
     *
     * @return The max age of this invite in seconds
     *
     * @see .expand
     * @see .isExpanded
     */
    val maxAge: Int

    /**
     * The max uses of this invite. If there is no limit thus will return `0`.
     *
     *
     * This works only for expanded invites and will throw a [IllegalStateException] otherwise!
     *
     * @throws IllegalStateException
     * if this invite is not expanded
     *
     * @return The max uses of this invite or `0` if there is no limit
     *
     * @see .expand
     * @see .isExpanded
     */
    val maxUses: Int

    @get:Nonnull
    val timeCreated: OffsetDateTime?

    /**
     * How often this invite has been used.
     *
     *
     * This works only for expanded invites and will throw a [IllegalStateException] otherwise!
     *
     * @throws IllegalStateException
     * if this invite is not expanded
     *
     * @return The uses of this invite
     *
     * @see .expand
     * @see .isExpanded
     */
    val uses: Int

    /**
     * Whether this Invite is expanded or not. Expanded invites contain more information, but they can only be
     * obtained by [Guild#retrieveInvites()][net.dv8tion.jda.api.entities.Guild.retrieveInvites] (requires
     * [Permission.MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER]) or
     * [IInviteContainer#retrieveInvites()][net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer.retrieveInvites] (requires
     * [Permission.MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL]).
     *
     *
     * There is a convenience method [.expand] to get the expanded invite for an unexpanded one.
     *
     * @return Whether this invite is expanded or not
     *
     * @see .expand
     */
    val isExpanded: Boolean

    /**
     * Whether this Invite grants only temporary access or not.
     *
     *
     * This works only for expanded invites and will throw a [IllegalStateException] otherwise!
     *
     * @throws IllegalStateException
     * if this invite is not expanded
     *
     * @return Whether this invite is temporary or not
     *
     * @see .expand
     * @see .isExpanded
     */
    val isTemporary: Boolean

    /**
     * POJO for the channel information provided by an invite.
     *
     * @see .getChannel
     */
    interface Channel : ISnowflake {
        @get:Nonnull
        val name: String?

        @get:Nonnull
        val type: ChannelType?
    }

    /**
     * POJO for the guild information provided by an invite.
     *
     * @see .getGuild
     */
    interface Guild : ISnowflake {
        /**
         * The icon id of this guild.
         *
         * @return The guild's icon id
         *
         * @see .getIconUrl
         */
        val iconId: String?

        /**
         * The icon url of this guild.
         *
         * @return The guild's icon url
         *
         * @see .getIconId
         */
        val iconUrl: String?
        val icon: ImageProxy?
            /**
             * Returns an [ImageProxy] for this guild's icon
             *
             * @return Possibly-null [ImageProxy] of this guild's icon
             *
             * @see .getIconUrl
             */
            get() {
                val iconUrl = iconUrl
                return iconUrl?.let { ImageProxy(it) }
            }

        @get:Nonnull
        val name: String?

        /**
         * The splash image id of this guild.
         *
         * @return The guild's splash image id or `null` if the guild has no splash image
         *
         * @see .getSplashUrl
         */
        val splashId: String?

        /**
         * Returns the splash image url of this guild.
         *
         * @return The guild's splash image url or `null` if the guild has no splash image
         *
         * @see .getSplashId
         */
        val splashUrl: String?
        val splash: ImageProxy?
            /**
             * Returns an [ImageProxy] for this invite guild's splash image.
             *
             * @return Possibly-null [ImageProxy] of this invite guild's splash image
             *
             * @see .getSplashUrl
             */
            get() {
                val splashUrl = splashUrl
                return splashUrl?.let { ImageProxy(it) }
            }

        @get:Nonnull
        val verificationLevel: net.dv8tion.jda.api.entities.VerificationLevel?

        /**
         * Returns the approximate count of online members in the guild. If the online member count was not included in the
         * invite, this will return -1. Counts will usually only be returned when resolving the invite via the
         * [Invite.resolve()][.resolve] method with the
         * withCounts boolean set to `true`
         *
         * @return the approximate count of online members in the guild, or -1 if not present in the invite
         */
        val onlineCount: Int

        /**
         * Returns the approximate count of total members in the guild. If the total member count was not included in the
         * invite, this will return -1. Counts will usually only be returned when resolving the invite via the
         * [Invite.resolve()][.resolve] method with the
         * withCounts boolean set to `true`
         *
         * @return the approximate count of total members in the guild, or -1 if not present in the invite
         */
        val memberCount: Int

        @get:Nonnull
        val features: Set<String?>?

        /**
         * The welcome screen of the [Guild][Invite.Guild].
         * <br></br>This will be `null` if the Guild has no welcome screen,
         * or if the invite came from a [GuildInviteCreateEvent][net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent].
         *
         * @return The welcome screen of this Guild or `null`
         */
        val welcomeScreen: GuildWelcomeScreen?
    }

    /**
     * POJO for the group information provided by an invite.
     *
     * @see .getChannel
     */
    interface Group : ISnowflake {
        /**
         * The icon id of this group or `null` if the group has no icon.
         *
         * @return The group's icon id
         *
         * @see .getIconUrl
         */
        val iconId: String?

        /**
         * The icon url of this group or `null` if the group has no icon.
         *
         * @return The group's icon url
         *
         * @see .getIconId
         */
        val iconUrl: String?
        val icon: ImageProxy?
            /**
             * Returns an [ImageProxy] for this group invite's icon.
             *
             * @return Possibly-null [ImageProxy] of this group invite's icon
             *
             * @see .getIconUrl
             */
            get() {
                val iconUrl = iconUrl
                return iconUrl?.let { ImageProxy(it) }
            }

        /**
         * The name of this group or `null` if the group has no name.
         *
         * @return The group's name
         */
        val name: String?

        /**
         * The names of all users in this group. If the users were not included in the
         * invite, this will return `null`. Users will only be returned when resolving the invite via the
         * [Invite.resolve()][.resolve] method with the
         * `withCounts` boolean set to `true`.
         *
         * @return The names of the group's users or null if not preset in the invite
         */
        val users: List<String?>?
    }

    /**
     * POJO for the target of this invite.
     *
     * @see .getTarget
     */
    interface InviteTarget {
        @JvmField
        @get:Nonnull
        val type: TargetType?

        @get:Nonnull
        val id: String?

        /**
         * The Snowflake id of the target entity of this invite.
         *
         * @throws IllegalStateException
         * If there is no target entity, [TargetType][.getType] is [TargetType.UNKNOWN]
         *
         * @return The id of the target entity
         */
        val idLong: Long

        /**
         * The target [User] of this invite or `null` if the [TargeType][.getType] is not [TargetType.STREAM]
         *
         * @return The target user of this invite
         *
         * @see net.dv8tion.jda.api.entities.User
         */
        val user: User?

        /**
         * The target [EmbeddedApplication] of this invite or `null` if the [TargeType][.getType] is not [TargetType.EMBEDDED_APPLICATION]
         *
         * @return The target application of this invite
         *
         * @see Invite.EmbeddedApplication
         */
        val application: EmbeddedApplication?
    }

    /**
     * POJO for the target application information provided by an invite.
     *
     * @see InviteTarget.getApplication
     */
    interface EmbeddedApplication : ISnowflake {
        @get:Nonnull
        val name: String?

        @get:Nonnull
        val description: String?

        /**
         * The summary of this application or `null` if this application has no summary.
         *
         * @return The summary of this application.
         */
        val summary: String?

        /**
         * The icon id of this application or `null` if the application has no icon.
         *
         * @return The application's icon id
         *
         * @see .getIconUrl
         */
        val iconId: String?

        /**
         * The icon url of this application or `null` if the application has no icon.
         *
         * @return The application's icon url
         *
         * @see .getIconId
         */
        val iconUrl: String?
        val icon: ImageProxy?
            /**
             * Returns an [ImageProxy] for this application invite's icon.
             *
             * @return Possibly-null [ImageProxy] of this application invite's icon
             *
             * @see .getIconUrl
             */
            get() {
                val iconUrl = iconUrl
                return iconUrl?.let { ImageProxy(it) }
            }

        /**
         * The max participant count of this application or `-1` if no max participant count is set
         *
         * @return `-1` if this application does not have a max participant count
         */
        val maxParticipants: Int
    }

    /**
     * Enum representing the type of an invite.
     *
     * @see .getType
     */
    enum class InviteType {
        GUILD,
        GROUP,
        UNKNOWN
    }

    /**
     * A TargetType indicates additional action to be taken by the client on accepting the invite,
     * typically connecting external services or launching external applications depending on the specific TargetType.
     *
     *
     * Some actions might not be available or show up on certain devices.
     *
     * @see InviteTarget.getType
     */
    enum class TargetType(
        /**
         * The Discord id key used to represent the target type.
         *
         * @return The id key used by discord for this channel type.
         */
        @JvmField val id: Int
    ) {
        /**
         * The invite does not have a target type, [Invite.getTarget] will return `null`.
         */
        NONE(0),

        /**
         * The invite points to a user's stream in a voice channel.
         * The user to whose stream the invite goes can be get with [InviteTarget.getUser] and is not `null`.
         *
         * @see InviteTarget.getUser
         */
        STREAM(1),

        /**
         * The invite points to an application in a voice channel.
         * The application to which the invite goes can be get with [InviteTarget.getApplication] and is not `null`.
         *
         * @see InviteTarget.getApplication
         */
        EMBEDDED_APPLICATION(2),

        /**
         * The invite points to a role subscription listing in a guild.
         * <br></br>These cannot be created by bots.
         */
        ROLE_SUBSCRIPTIONS_PURCHASE(3),

        /**
         * Unknown Discord invite target type. Should never happen and would only possibly happen if Discord implemented a new
         * target type and JDA had yet to implement support for it.
         */
        UNKNOWN(-1);

        companion object {
            /**
             * Static accessor for retrieving a target type based on its Discord id key.
             *
             * @param  id
             * The id key of the requested target type.
             *
             * @return The TargetType that is referred to by the provided key. If the id key is unknown, [.UNKNOWN] is returned.
             */
            @JvmStatic
            @Nonnull
            fun fromId(id: Int): TargetType {
                for (type in entries) {
                    if (type.id == id) return type
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * Retrieves a new [Invite] instance for the given invite code.
         * <br></br>**You cannot resolve invites if you were banned from the origin Guild!**
         *
         *
         * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
         *
         *  * [Unknown Invite][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INVITE]
         * <br></br>The Invite did not exist (possibly deleted) or the account is banned in the guild.
         *
         *
         * @param  api
         * The JDA instance
         * @param  code
         * A valid invite code
         *
         * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Invite]
         * <br></br>The Invite object
         */
        @Nonnull
        fun resolve(@Nonnull api: JDA?, @Nonnull code: String?): RestAction<Invite?>? {
            return resolve(api, code, false)
        }

        /**
         * Retrieves a new [Invite] instance for the given invite code.
         * <br></br>**You cannot resolve invites if you were banned from the origin Guild!**
         *
         *
         * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
         *
         *  * [Unknown Invite][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INVITE]
         * <br></br>The Invite did not exist (possibly deleted) or the account is banned in the guild.
         *
         *
         * @param  api
         * The JDA instance
         * @param  code
         * A valid invite code
         * @param  withCounts
         * Whether or not to include online and member counts for guild invites or users for group invites
         *
         * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Invite]
         * <br></br>The Invite object
         */
        @Nonnull
        fun resolve(@Nonnull api: JDA?, @Nonnull code: String?, withCounts: Boolean): RestAction<Invite?>? {
            return InviteImpl.resolve(api, code, withCounts)
        }
    }
}
