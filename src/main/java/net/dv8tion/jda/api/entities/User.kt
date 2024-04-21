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

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User.UserFlag
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.entities.UserSnowflakeImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import java.awt.Color
import java.util.*
import java.util.regex.Pattern
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Discord User.
 * Contains all publicly available information about a specific Discord User.
 *
 *
 * **Formattable**<br></br>
 * This interface extends [Formattable][java.util.Formattable] and can be used with a [Formatter][java.util.Formatter]
 * such as used by [String.format(String, Object...)][String.format]
 * or [PrintStream.printf(String, Object...)][java.io.PrintStream.printf].
 *
 *
 * This will use [.getAsMention] rather than [Object.toString]!
 * <br></br>Supported Features:
 *
 *  * **Alternative**
 * <br></br>   - Uses the <u>Discord Tag</u> (Username#Discriminator) instead
 * (Example: `%#s` - results in `[User.getName]#[User.getDiscriminator]
 * -> Minn#6688`)
 *
 *  * **Width/Left-Justification**
 * <br></br>   - Ensures the size of a format
 * (Example: `%20s` - uses at minimum 20 chars;
 * `%-10s` - uses left-justified padding)
 *
 *  * **Precision**
 * <br></br>   - Cuts the content to the specified size
 * (Example: `%.20s`)
 *
 *
 *
 * More information on formatting syntax can be found in the [format syntax documentation][java.util.Formatter]!
 *
 * @see User.openPrivateChannel
 * @see JDA.getUserCache
 * @see JDA.getUserById
 * @see JDA.getUserByTag
 * @see JDA.getUserByTag
 * @see JDA.getUsersByName
 * @see JDA.getUsers
 * @see JDA.retrieveUserById
 */
interface User : UserSnowflake {
    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * The global display name of the user.
     * <br></br>This name is not unique and allows more characters.
     *
     *
     * This name is usually displayed in the UI.
     *
     * @return The global display name or null if unset.
     */
    @JvmField
    val globalName: String?

    @get:Nonnull
    val effectiveName: String?
        /**
         * The name visible in the UI.
         * <br></br>If the [global name][.getGlobalName] is `null`, this returns the [username][.getName] instead.
         *
         * @return The effective display name
         */
        get() {
            val globalName = globalName
            return globalName ?: name
        }
    /**
     * <br></br>The discriminator of the [User][net.dv8tion.jda.api.entities.User]. Used to differentiate between users with the same usernames.
     * <br></br>This only contains the 4 digits after the username and the #.
     *
     * @return Never-null String containing the [User][net.dv8tion.jda.api.entities.User] discriminator.
     *
     */
    @Nonnull @ForRemoval @Deprecated(
        """This will become obsolete in the future.
                  Discriminators are being phased out and replaced by globally unique usernames.
                  For more information, see <a href=""" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") open fun /*@@lshgjz@@*/getDiscriminator(): /*@@bslmjn@@*/kotlin.String?
                /**
                 * The Discord ID for this user's avatar image.
                 * If the user has not set an image, this will return null.
                 *
                 * @return Possibly-null String containing the [User][net.dv8tion.jda.api.entities.User] avatar id.
                 */
                open fun getAvatarId(): String?
                /**
                 * The URL for the user's avatar image.
                 * If the user has not set an image, this will return null.
                 *
                 * @return Possibly-null String containing the [User][net.dv8tion.jda.api.entities.User] avatar url.
                 */
                open fun getAvatarUrl(): String? {
            val avatarId: String = getAvatarId()
            return if (avatarId == null) null else String.format(
                AVATAR_URL,
                id,
                avatarId,
                if (avatarId.startsWith("a_")) "gif" else "png"
            )
        }
                /**
                 * Returns an [ImageProxy] for this user's avatar.
                 *
                 * @return Possibly-null [ImageProxy] of this user's avatar
                 *
                 * @see .getAvatarUrl
                 */
                open fun getAvatar(): ImageProxy? {
            val avatarUrl: String = getAvatarUrl()
            return if (avatarUrl == null) null else ImageProxy(avatarUrl)
        }
        /**
         * The URL for the user's avatar image.
         * If they do not have an avatar set, this will return the URL of their
         * default avatar
         *
         * @return  Never-null String containing the [User][net.dv8tion.jda.api.entities.User] effective avatar url.
         */
        @Nonnull
        fun getEffectiveAvatarUrl(): String? {
            val avatarUrl: String = getAvatarUrl()
            return avatarUrl ?: defaultAvatarUrl
        }
        /**
         * Returns an [ImageProxy] for this user's effective avatar image.
         *
         * @return Never-null [ImageProxy] of this user's effective avatar image
         *
         * @see .getEffectiveAvatarUrl
         */
        @Nonnull
        fun getEffectiveAvatar(): ImageProxy? {
            val avatar: ImageProxy = getAvatar()
            return avatar ?: defaultAvatar
        }
        /**
         * Loads the user's [User.Profile] data.
         * Returns a completed RestAction if this User has been retrieved using [JDA.retrieveUserById].
         * You can use [useCache(false)][CacheRestAction.useCache] to force the request for a new profile with up-to-date information.
         *
         * @return [CacheRestAction] - Type: [User.Profile]
         */
        @Nonnull
        @CheckReturnValue
        fun retrieveProfile(): CacheRestAction<User.Profile?>?
        /**
         * The "tag" for this user
         *
         * This is the equivalent of calling [String.format][java.lang.String.format]("%#s", user)
         *
         * @return Never-null String containing the tag for this user, for example DV8FromTheWorld#6297
         *
         */
        @Nonnull @ForRemoval @ReplaceWith("getName()") @Deprecated(
            """This will become obsolete in the future.
                  Discriminators are being phased out and replaced by globally unique usernames.
                  For more information, see <a href=""" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") open fun /*@@kgzvom@@*/getAsTag(): /*@@bslmjn@@*/kotlin.String?
                    /**
                     * Whether or not the currently logged in user and this user have a currently open
                     * [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] or not.
                     *
                     * @return True if the logged in account shares a PrivateChannel with this user.
                     */
                    open fun hasPrivateChannel(): Boolean
            /**
             * Opens a [net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] with this User.
             * <br></br>If a channel has already been opened with this user, it is immediately returned in the RestAction's
             * success consumer without contacting the Discord API.
             * You can use [useCache(false)][CacheRestAction.useCache] to force the request for a new channel object,
             * which is rarely useful since the channel id never changes.
             *
             *
             * **Examples**<br></br>
             * <pre>`// Send message without response handling
             * public void sendMessage(User user, String content) {
             * user.openPrivateChannel()
             * .flatMap(channel -> channel.sendMessage(content))
             * .queue();
             * }
             *
             * // Send message and delete 30 seconds later
             * public RestAction<Void> sendSecretMessage(User user, String content) {
             * return user.openPrivateChannel() // RestAction<PrivateChannel>
             * .flatMap(channel -> channel.sendMessage(content)) // RestAction<Message>
             * .delay(30, TimeUnit.SECONDS) // RestAction<Message> with delayed response
             * .flatMap(Message::delete); // RestAction<Void> (executed 30 seconds after sending)
             * }
            `</pre> *
             *
             * @throws UnsupportedOperationException
             * If the recipient User is the currently logged in account (represented by [SelfUser][net.dv8tion.jda.api.entities.SelfUser])
             * or if the user was created with [.fromId]
             *
             * @return [CacheRestAction] - Type: [net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel]
             * <br></br>Retrieves the PrivateChannel to use to directly message this User.
             *
             * @see JDA.openPrivateChannelById
             */
            @Nonnull
            @CheckReturnValue
            fun openPrivateChannel(): CacheRestAction<PrivateChannel?>?
            /**
             * Finds and collects all [Guild][net.dv8tion.jda.api.entities.Guild] instances that contain this [User][net.dv8tion.jda.api.entities.User] within the current [JDA][net.dv8tion.jda.api.JDA] instance.<br></br>
             *
             * This method is a shortcut for [JDA.getMutualGuilds(User)][net.dv8tion.jda.api.JDA.getMutualGuilds].
             *
             * @return Immutable list of all [Guilds][net.dv8tion.jda.api.entities.Guild] that this user is a member of.
             */
            @Nonnull
            fun getMutualGuilds(): List<Guild?>?
                    /**
                     * Returns whether or not the given user is a Bot-Account (special badge in client, some different behaviour)
                     *
                     * @return If the User's Account is marked as Bot
                     */
                    open fun isBot(): Boolean
                    /**
                     * Returns whether or not the given user is a System account, which includes the urgent message account
                     * and the community updates bot.
                     *
                     * @return Whether the User's account is marked as System
                     */
                    open fun isSystem(): Boolean
            /**
             * Returns the [JDA][net.dv8tion.jda.api.JDA] instance of this User
             *
             * @return the corresponding JDA instance
             */
            @Nonnull
            fun getJDA(): JDA?
            /**
             * Returns the [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of this user.
             *
             * @return EnumSet containing the flags of the user.
             */
            @Nonnull
            fun getFlags(): EnumSet<UserFlag?>?
                    /**
                     * Returns the bitmask representation of the [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of this user.
                     *
                     * @return bitmask representation of the user's flags.
                     */
                    open fun getFlagsRaw(): Int
            /**
             * Represents the information contained in a [User]'s profile.
             *
             * @since 4.3.0
             */
            class Profile(userId:kotlin. Long, bannerId:kotlin. String?, accentColor:kotlin.Int)
    {
        var userId: Long
        var bannerId: String
        var accentColor: Int
        init {
            userId = userId
            bannerId = bannerId
            accentColor = accentColor
        }
        /**
         * The Discord Id for this user's banner image.
         * If the user has not set a banner, this will return null.
         *
         * @return Possibly-null String containing the [User] banner id.
         */
        fun getBannerId(): String? {
            return bannerId
        }

        /**
         * The URL for the user's banner image.
         * If the user has not set a banner, this will return null.
         *
         * @return Possibly-null String containing the [User] banner url.
         *
         * @see User.BANNER_URL
         */
        fun getBannerUrl(): String? {
            return if (bannerId == null) null else String.format(
                BANNER_URL,
                java.lang.Long.toUnsignedString(userId),
                bannerId,
                if (bannerId.startsWith("a_")) "gif" else "png"
            )
        }

        /**
         * Returns an [ImageProxy] for this user's banner.
         *
         * @return Possibly-null [ImageProxy] of this user's banner
         *
         * @see .getBannerUrl
         */
        fun getBanner(): ImageProxy? {
            val bannerUrl = getBannerUrl()
            return bannerUrl?.let { ImageProxy(it) }
        }

        /**
         * The user's accent color.
         * If the user has not set an accent color, this will return null.
         * The automatically calculated color is not returned.
         * The accent color is not shown in the client if the user has set a banner.
         *
         * @return Possibly-null [java.awt.Color] containing the [User] accent color.
         */
        fun getAccentColor(): Color? {
            return if (accentColor == DEFAULT_ACCENT_COLOR_RAW) null else Color(accentColor)
        }

        /**
         * The raw RGB value of this user's accent color.
         * <br></br>Defaults to [.DEFAULT_ACCENT_COLOR_RAW] if this user's banner color is not available.
         *
         * @return The raw RGB color value or [User.DEFAULT_ACCENT_COLOR_RAW]
         */
        fun getAccentColorRaw(): Int {
            return accentColor
        }

        fun toString(): String {
            return EntityString(this)
                .addMetadata("userId", userId)
                .addMetadata("bannerId", bannerId)
                .addMetadata("accentColor", accentColor)
                .toString()
        }
    }

    /**
     * Represents the bit offsets used by Discord for public flags
     */
    enum class UserFlag(
        /**
         * The binary offset of the flag.
         *
         * @return The offset that represents this UserFlag.
         */
        val offset: Int,
        /**
         * The readable name as used in the Discord Client.
         *
         * @return The readable name of this UserFlag.
         */
        @get:Nonnull
        @param:Nonnull override val name: String
    ) {
        STAFF(0, "Discord Employee"),
        PARTNER(1, "Partnered Server Owner"),
        HYPESQUAD(2, "HypeSquad Events"),
        BUG_HUNTER_LEVEL_1(3, "Bug Hunter Level 1"),

        // HypeSquad
        HYPESQUAD_BRAVERY(6, "HypeSquad Bravery"),
        HYPESQUAD_BRILLIANCE(7, "HypeSquad Brilliance"),
        HYPESQUAD_BALANCE(8, "HypeSquad Balance"),
        EARLY_SUPPORTER(9, "Early Supporter"),

        /**
         * User is a [team][ApplicationTeam]
         */
        TEAM_USER(10, "Team User"),
        BUG_HUNTER_LEVEL_2(14, "Bug Hunter Level 2"),
        VERIFIED_BOT(16, "Verified Bot"),
        VERIFIED_DEVELOPER(17, "Early Verified Bot Developer"),
        CERTIFIED_MODERATOR(18, "Discord Certified Moderator"),

        /**
         * Bot uses only HTTP interactions and is shown in the online member list
         */
        BOT_HTTP_INTERACTIONS(19, "HTTP Interactions Bot"),

        /**
         * User is an [Active Developer](https://support-dev.discord.com/hc/articles/10113997751447)
         */
        ACTIVE_DEVELOPER(22, "Active Developer"),
        UNKNOWN(-1, "Unknown");

        /**
         * The value of this flag when viewed as raw value.
         * <br></br>This is equivalent to: `1 << [.getOffset]`
         *
         * @return The raw value of this specific flag.
         */
        val rawValue: Int

        init {
            rawValue = 1 shl offset
        }

        companion object {
            /**
             * Empty array of UserFlag enum, useful for optimized use in [java.util.Collection.toArray].
             */
            val EMPTY_FLAGS = arrayOfNulls<UserFlag>(0)

            /**
             * Gets the first UserFlag relating to the provided offset.
             * <br></br>If there is no UserFlag that matches the provided offset,
             * [.UNKNOWN] is returned.
             *
             * @param  offset
             * The offset to match a UserFlag to.
             *
             * @return UserFlag relating to the provided offset.
             */
            @Nonnull
            fun getFromOffset(offset: Int): UserFlag {
                for (flag in entries) {
                    if (flag.offset == offset) return flag
                }
                return UNKNOWN
            }

            /**
             * A set of all UserFlags that are specified by this raw int representation of
             * flags.
             *
             * @param  flags
             * The raw `int` representation if flags.
             *
             * @return Possibly-empty EnumSet of UserFlags.
             */
            @JvmStatic
            @Nonnull
            fun getFlags(flags: Int): EnumSet<UserFlag> {
                val foundFlags = EnumSet.noneOf(UserFlag::class.java)
                if (flags == 0) return foundFlags //empty
                for (flag in entries) {
                    if (flag != UNKNOWN && flags and flag.rawValue == flag.rawValue) foundFlags.add(flag)
                }
                return foundFlags
            }

            /**
             * This is effectively the opposite of [.getFlags], this takes 1 or more UserFlags
             * and returns the bitmask representation of the flags.
             *
             * @param  flags
             * The array of flags of which to form into the raw int representation.
             *
             * @throws java.lang.IllegalArgumentException
             * When the provided UserFlags are null.
             *
             * @return bitmask representing the provided flags.
             */
            fun getRaw(@Nonnull vararg flags: UserFlag?): Int {
                Checks.noneNull(flags, "UserFlags")
                var raw = 0
                for (flag in flags) {
                    if (flag != null && flag != UNKNOWN) raw = raw or flag.rawValue
                }
                return raw
            }

            /**
             * This is effectively the opposite of [.getFlags]. This takes a collection of UserFlags
             * and returns the bitmask representation of the flags.
             * <br></br>Example: `getRaw(EnumSet.of(UserFlag.STAFF, UserFlag.HYPESQUAD))`
             *
             * @param  flags
             * The flags to convert
             *
             * @throws java.lang.IllegalArgumentException
             * When the provided UserFLags are null.
             *
             * @return bitmask representing the provided flags.
             *
             * @see java.util.EnumSet EnumSet
             */
            fun getRaw(@Nonnull flags: Collection<UserFlag?>): Int {
                Checks.notNull(flags, "Flag Collection")
                return getRaw(*flags.toArray<UserFlag>(EMPTY_FLAGS))
            }
        }
    }

    companion object {
        /**
         * Creates a User instance which only wraps an ID.
         *
         * @param  id
         * The user id
         *
         * @return A [UserSnowflake] instance
         *
         * @see JDA.retrieveUserById
         * @see UserSnowflake.fromId
         */
        @JvmStatic
        @Nonnull
        fun fromId(id: Long): UserSnowflake? {
            return UserSnowflakeImpl(id)
        }

        /**
         * Creates a User instance which only wraps an ID.
         *
         * @param  id
         * The user id
         *
         * @throws IllegalArgumentException
         * If the provided ID is not a valid snowflake
         *
         * @return A [UserSnowflake] instance
         *
         * @see JDA.retrieveUserById
         * @see UserSnowflake.fromId
         */
        @Nonnull
        fun fromId(@Nonnull id: String?): UserSnowflake? {
            return fromId(MiscUtil.parseSnowflake(id))
        }

        /**
         * Compiled pattern for a Discord Tag: `(.{2,32})#(\d{4})`
         */
        @JvmField
        val USER_TAG = Pattern.compile("(.{2,32})#(\\d{4})")

        /** Template for [.getAvatarUrl].  */
        const val AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s.%s"

        /** Template for [.getDefaultAvatarUrl]  */
        const val DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/%s.png"

        /** Template for [Profile.getBannerUrl]  */
        const val BANNER_URL = "https://cdn.discordapp.com/banners/%s/%s.%s"

        /** Used to keep consistency between color values used in the API  */
        const val DEFAULT_ACCENT_COLOR_RAW =
            0x1FFFFFFF // java.awt.Color fills the MSB with FF, we just use 1F to provide better consistency
    }
}
