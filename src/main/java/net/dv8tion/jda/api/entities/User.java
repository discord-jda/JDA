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
package net.dv8tion.jda.api.entities;


import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.UserSnowflakeImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a Discord User.
 * Contains all publicly available information about a specific Discord User.
 *
 * <p><b>Formattable</b><br>
 * This interface extends {@link java.util.Formattable Formattable} and can be used with a {@link java.util.Formatter Formatter}
 * such as used by {@link String#format(String, Object...) String.format(String, Object...)}
 * or {@link java.io.PrintStream#printf(String, Object...) PrintStream.printf(String, Object...)}.
 *
 * <p>This will use {@link #getAsMention()} rather than {@link Object#toString()}!
 * <br>Supported Features:
 * <ul>
 *     <li><b>Alternative</b>
 *     <br>   - Uses the <u>Discord Tag</u> (Username#Discriminator) instead
 *              (Example: {@code %#s} - results in <code>{@link User#getName()}#{@link User#getDiscriminator()}
 *              {@literal ->} Minn#6688</code>)</li>
 *
 *     <li><b>Width/Left-Justification</b>
 *     <br>   - Ensures the size of a format
 *              (Example: {@code %20s} - uses at minimum 20 chars;
 *              {@code %-10s} - uses left-justified padding)</li>
 *
 *     <li><b>Precision</b>
 *     <br>   - Cuts the content to the specified size
 *              (Example: {@code %.20s})</li>
 * </ul>
 *
 * <p>More information on formatting syntax can be found in the {@link java.util.Formatter format syntax documentation}!
 *
 * @see User#openPrivateChannel()
 *
 * @see JDA#getUserCache()
 * @see JDA#getUserById(long)
 * @see JDA#getUserByTag(String)
 * @see JDA#getUserByTag(String, String)
 * @see JDA#getUsersByName(String, boolean)
 * @see JDA#getUsers()
 *
 * @see JDA#retrieveUserById(String)
 */
public interface User extends UserSnowflake
{
    /**
     * Compiled pattern for a Discord Tag: {@code (.{2,32})#(\d{4})}
     */
    Pattern USER_TAG = Pattern.compile("(.{2,32})#(\\d{4})");

    /** Template for {@link #getAvatarUrl()}. */
    String AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s.%s";
    /** Template for {@link #getDefaultAvatarUrl()} */
    String DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/%s.png";
    /** Template for {@link Profile#getBannerUrl()} */
    String BANNER_URL = "https://cdn.discordapp.com/banners/%s/%s.%s";

    /** Used to keep consistency between color values used in the API */
    int DEFAULT_ACCENT_COLOR_RAW = 0x1FFFFFFF; // java.awt.Color fills the MSB with FF, we just use 1F to provide better consistency

    /**
     * Creates a User instance which only wraps an ID.
     *
     * @param  id
     *         The user id
     *
     * @return A {@link UserSnowflake} instance
     *
     * @see    JDA#retrieveUserById(long)
     * @see    UserSnowflake#fromId(long)
     */
    @Nonnull
    static UserSnowflake fromId(long id)
    {
        return new UserSnowflakeImpl(id);
    }

    /**
     * Creates a User instance which only wraps an ID.
     *
     * @param  id
     *         The user id
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not a valid snowflake
     *
     * @return A {@link UserSnowflake} instance
     *
     * @see    JDA#retrieveUserById(String)
     * @see    UserSnowflake#fromId(String)
     */
    @Nonnull
    static UserSnowflake fromId(@Nonnull String id)
    {
        return fromId(MiscUtil.parseSnowflake(id));
    }

    /**
     * The username of the {@link net.dv8tion.jda.api.entities.User User}. Length is between 2 and 32 characters (inclusive).
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.User User}'s username.
     */
    @Nonnull
    String getName();

    /**
     * The global display name of the user.
     * <br>This name is not unique and allows more characters.
     *
     * <p>This name is usually displayed in the UI.
     *
     * @return The global display name or null if unset.
     */
    @Nullable
    String getGlobalName();

    /**
     * The name visible in the UI.
     * <br>If the {@link #getGlobalName() global name} is {@code null}, this returns the {@link #getName() username} instead.
     *
     * @return The effective display name
     */
    @Nonnull
    default String getEffectiveName()
    {
        String globalName = getGlobalName();
        return globalName != null ? globalName : getName();
    }

    /**
     * <br>The discriminator of the {@link net.dv8tion.jda.api.entities.User User}. Used to differentiate between users with the same usernames.
     * <br>This only contains the 4 digits after the username and the #.
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.User User} discriminator.
     *
     * @deprecated This will become obsolete in the future.
     *             Discriminators are being phased out and replaced by globally unique usernames.
     *             For more information, see <a href="https://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    String getDiscriminator();

    /**
     * The Discord ID for this user's avatar image.
     * If the user has not set an image, this will return null.
     *
     * @return Possibly-null String containing the {@link net.dv8tion.jda.api.entities.User User} avatar id.
     */
    @Nullable
    String getAvatarId();

    /**
     * The URL for the user's avatar image.
     * If the user has not set an image, this will return null.
     *
     * @return Possibly-null String containing the {@link net.dv8tion.jda.api.entities.User User} avatar url.
     */
    @Nullable
    default String getAvatarUrl()
    {
        String avatarId = getAvatarId();
        return avatarId == null ? null : String.format(AVATAR_URL, getId(), avatarId, avatarId.startsWith("a_") ? "gif" : "png");
    }

    /**
     * Returns an {@link ImageProxy} for this user's avatar.
     *
     * @return Possibly-null {@link ImageProxy} of this user's avatar
     *
     * @see    #getAvatarUrl()
     */
    @Nullable
    default ImageProxy getAvatar()
    {
        final String avatarUrl = getAvatarUrl();
        return avatarUrl == null ? null : new ImageProxy(avatarUrl);
    }

    /**
     * The URL for the user's avatar image.
     * If they do not have an avatar set, this will return the URL of their
     * default avatar
     *
     * @return  Never-null String containing the {@link net.dv8tion.jda.api.entities.User User} effective avatar url.
     */
    @Nonnull
    default String getEffectiveAvatarUrl()
    {
        String avatarUrl = getAvatarUrl();
        return avatarUrl == null ? getDefaultAvatarUrl() : avatarUrl;
    }

    /**
     * Returns an {@link ImageProxy} for this user's effective avatar image.
     *
     * @return Never-null {@link ImageProxy} of this user's effective avatar image
     *
     * @see    #getEffectiveAvatarUrl()
     */
    @Nonnull
    default ImageProxy getEffectiveAvatar()
    {
        final ImageProxy avatar = getAvatar();
        return avatar == null ? getDefaultAvatar() : avatar;
    }

    /**
     * Loads the user's {@link User.Profile} data.
     * Returns a completed RestAction if this User has been retrieved using {@link JDA#retrieveUserById(long)}.
     * You can use {@link CacheRestAction#useCache(boolean) useCache(false)} to force the request for a new profile with up-to-date information.
     *
     * @return {@link CacheRestAction} - Type: {@link User.Profile}
     */
    @Nonnull
    @CheckReturnValue
    CacheRestAction<Profile> retrieveProfile();

    /**
     * The "tag" for this user
     * <p>This is the equivalent of calling {@link java.lang.String#format(String, Object...) String.format}("%#s", user)
     *
     * @return Never-null String containing the tag for this user, for example DV8FromTheWorld#6297
     *
     * @deprecated This will become obsolete in the future.
     *             Discriminators are being phased out and replaced by globally unique usernames.
     *             For more information, see <a href="https://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    @ReplaceWith("getName()")
    String getAsTag();

    /**
     * Whether or not the currently logged in user and this user have a currently open
     * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} or not.
     *
     * @return True if the logged in account shares a PrivateChannel with this user.
     */
    boolean hasPrivateChannel();

    /**
     * Opens a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel} with this User.
     * <br>If a channel has already been opened with this user, it is immediately returned in the RestAction's
     * success consumer without contacting the Discord API.
     * You can use {@link CacheRestAction#useCache(boolean) useCache(false)} to force the request for a new channel object,
     * which is rarely useful since the channel id never changes.
     *
     * <p><b>Examples</b><br>
     * <pre>{@code
     * // Send message without response handling
     * public void sendMessage(User user, String content) {
     *     user.openPrivateChannel()
     *         .flatMap(channel -> channel.sendMessage(content))
     *         .queue();
     * }
     *
     * // Send message and delete 30 seconds later
     * public RestAction<Void> sendSecretMessage(User user, String content) {
     *     return user.openPrivateChannel() // RestAction<PrivateChannel>
     *                .flatMap(channel -> channel.sendMessage(content)) // RestAction<Message>
     *                .delay(30, TimeUnit.SECONDS) // RestAction<Message> with delayed response
     *                .flatMap(Message::delete); // RestAction<Void> (executed 30 seconds after sending)
     * }
     * }</pre>
     *
     * @throws UnsupportedOperationException
     *         If the recipient User is the currently logged in account (represented by {@link net.dv8tion.jda.api.entities.SelfUser SelfUser})
     *         or if the user was created with {@link #fromId(long)}
     *
     * @return {@link CacheRestAction} - Type: {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel}
     *         <br>Retrieves the PrivateChannel to use to directly message this User.
     *
     * @see    JDA#openPrivateChannelById(long)
     */
    @Nonnull
    @CheckReturnValue
    CacheRestAction<PrivateChannel> openPrivateChannel();

    /**
     * Finds and collects all {@link net.dv8tion.jda.api.entities.Guild Guild} instances that contain this {@link net.dv8tion.jda.api.entities.User User} within the current {@link net.dv8tion.jda.api.JDA JDA} instance.<br>
     * <p>This method is a shortcut for {@link net.dv8tion.jda.api.JDA#getMutualGuilds(User...) JDA.getMutualGuilds(User)}.</p>
     *
     * @return Immutable list of all {@link net.dv8tion.jda.api.entities.Guild Guilds} that this user is a member of.
     */
    @Nonnull
    List<Guild> getMutualGuilds();

    /**
     * Returns whether or not the given user is a Bot-Account (special badge in client, some different behaviour)
     *
     * @return If the User's Account is marked as Bot
     */
    boolean isBot();

    /**
     * Returns whether or not the given user is a System account, which includes the urgent message account
     * and the community updates bot.
     *
     * @return Whether the User's account is marked as System
     */
    boolean isSystem();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this User
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of this user.
     *
     * @return EnumSet containing the flags of the user.
     */
    @Nonnull
    EnumSet<UserFlag> getFlags();

    /**
     * Returns the bitmask representation of the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of this user.
     *
     * @return bitmask representation of the user's flags.
     */
    int getFlagsRaw();

    /**
     * Represents the information contained in a {@link User User}'s profile.
     *
     * @since 4.3.0
     */
    class Profile
    {
        private final long userId;
        private final String bannerId;
        private final int accentColor;

        public Profile(long userId, String bannerId, int accentColor)
        {
            this.userId = userId;
            this.bannerId = bannerId;
            this.accentColor = accentColor;
        }

        /**
         * The Discord Id for this user's banner image.
         * If the user has not set a banner, this will return null.
         *
         * @return Possibly-null String containing the {@link User User} banner id.
         */
        @Nullable
        public String getBannerId()
        {
            return bannerId;
        }

        /**
         * The URL for the user's banner image.
         * If the user has not set a banner, this will return null.
         *
         * @return Possibly-null String containing the {@link User User} banner url.
         *
         * @see User#BANNER_URL
         */
        @Nullable
        public String getBannerUrl()
        {
            return bannerId == null ? null : String.format(BANNER_URL, Long.toUnsignedString(userId), bannerId, bannerId.startsWith("a_") ? "gif" : "png");
        }

        /**
         * Returns an {@link ImageProxy} for this user's banner.
         *
         * @return Possibly-null {@link ImageProxy} of this user's banner
         *
         * @see    #getBannerUrl()
         */
        @Nullable
        public ImageProxy getBanner()
        {
            final String bannerUrl = getBannerUrl();
            return bannerUrl == null ? null : new ImageProxy(bannerUrl);
        }

        /**
         * The user's accent color.
         * If the user has not set an accent color, this will return null.
         * The automatically calculated color is not returned.
         * The accent color is not shown in the client if the user has set a banner.
         *
         * @return Possibly-null {@link java.awt.Color} containing the {@link User User} accent color.
         */
        @Nullable
        public Color getAccentColor()
        {
            return accentColor == DEFAULT_ACCENT_COLOR_RAW ? null : new Color(accentColor);
        }

        /**
         * The raw RGB value of this user's accent color.
         * <br>Defaults to {@link #DEFAULT_ACCENT_COLOR_RAW} if this user's banner color is not available.
         *
         * @return The raw RGB color value or {@link User#DEFAULT_ACCENT_COLOR_RAW}
         */
        public int getAccentColorRaw()
        {
            return accentColor;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("userId", userId)
                    .addMetadata("bannerId", bannerId)
                    .addMetadata("accentColor", accentColor)
                    .toString();
        }
    }

    /**
     * Represents the bit offsets used by Discord for public flags
     */
    enum UserFlag
    {
        STAFF(                 0, "Discord Employee"),
        PARTNER(               1, "Partnered Server Owner"),
        HYPESQUAD(             2, "HypeSquad Events"),
        BUG_HUNTER_LEVEL_1(    3, "Bug Hunter Level 1"),

        // HypeSquad
        HYPESQUAD_BRAVERY(     6, "HypeSquad Bravery"),
        HYPESQUAD_BRILLIANCE(  7, "HypeSquad Brilliance"),
        HYPESQUAD_BALANCE(     8, "HypeSquad Balance"),

        EARLY_SUPPORTER(       9, "Early Supporter"),
        /**
         * User is a {@link ApplicationTeam team}
         */
        TEAM_USER(            10, "Team User"),
        BUG_HUNTER_LEVEL_2(   14, "Bug Hunter Level 2"),
        VERIFIED_BOT(         16, "Verified Bot"),
        VERIFIED_DEVELOPER(   17, "Early Verified Bot Developer"),
        CERTIFIED_MODERATOR(  18, "Discord Certified Moderator"),
        /**
         * Bot uses only HTTP interactions and is shown in the online member list
         */
        BOT_HTTP_INTERACTIONS(19, "HTTP Interactions Bot"),
        /**
         * User is an <a href="https://support-dev.discord.com/hc/articles/10113997751447">Active Developer</a>
         */
        ACTIVE_DEVELOPER(     22, "Active Developer"),

        UNKNOWN(-1, "Unknown");

        /**
         * Empty array of UserFlag enum, useful for optimized use in {@link java.util.Collection#toArray(Object[])}.
         */
        public static final UserFlag[] EMPTY_FLAGS = new UserFlag[0];

        private final int offset;
        private final int raw;
        private final String name;

        UserFlag(int offset, @Nonnull String name)
        {
            this.offset = offset;
            this.raw = 1 << offset;
            this.name = name;
        }

        /**
         * The readable name as used in the Discord Client.
         *
         * @return The readable name of this UserFlag.
         */
        @Nonnull
        public String getName()
        {
            return this.name;
        }

        /**
         * The binary offset of the flag.
         *
         * @return The offset that represents this UserFlag.
         */
        public int getOffset()
        {
            return offset;
        }

        /**
         * The value of this flag when viewed as raw value.
         * <br>This is equivalent to: <code>1 {@literal <<} {@link #getOffset()}</code>
         *
         * @return The raw value of this specific flag.
         */
        public int getRawValue()
        {
            return raw;
        }

        /**
         * Gets the first UserFlag relating to the provided offset.
         * <br>If there is no UserFlag that matches the provided offset,
         * {@link #UNKNOWN} is returned.
         *
         * @param  offset
         *         The offset to match a UserFlag to.
         *
         * @return UserFlag relating to the provided offset.
         */
        @Nonnull
        public static UserFlag getFromOffset(int offset)
        {
            for (UserFlag flag : values())
            {
                if (flag.offset == offset)
                    return flag;
            }
            return UNKNOWN;
        }

        /**
         * A set of all UserFlags that are specified by this raw int representation of
         * flags.
         *
         * @param  flags
         *         The raw {@code int} representation if flags.
         *
         * @return Possibly-empty EnumSet of UserFlags.
         */
        @Nonnull
        public static EnumSet<UserFlag> getFlags(int flags)
        {
            final EnumSet<UserFlag> foundFlags = EnumSet.noneOf(UserFlag.class);

            if (flags == 0)
                return foundFlags; //empty

            for (UserFlag flag : values())
            {
                if (flag != UNKNOWN && (flags & flag.raw) == flag.raw)
                    foundFlags.add(flag);
            }

            return foundFlags;
        }

        /**
         * This is effectively the opposite of {@link #getFlags(int)}, this takes 1 or more UserFlags
         * and returns the bitmask representation of the flags.
         *
         * @param  flags
         *         The array of flags of which to form into the raw int representation.
         *
         * @throws java.lang.IllegalArgumentException
         *         When the provided UserFlags are null.
         *
         * @return bitmask representing the provided flags.
         */
        public static int getRaw(@Nonnull UserFlag... flags){
            Checks.noneNull(flags, "UserFlags");

            int raw = 0;
            for (UserFlag flag : flags)
            {
                if (flag != null && flag != UNKNOWN)
                    raw |= flag.raw;
            }

            return raw;
        }

        /**
         * This is effectively the opposite of {@link #getFlags(int)}. This takes a collection of UserFlags
         * and returns the bitmask representation of the flags.
         * <br>Example: {@code getRaw(EnumSet.of(UserFlag.STAFF, UserFlag.HYPESQUAD))}
         *
         * @param  flags
         *         The flags to convert
         *
         * @throws java.lang.IllegalArgumentException
         *         When the provided UserFLags are null.
         *
         * @return bitmask representing the provided flags.
         *
         * @see java.util.EnumSet EnumSet
         */
        public static int getRaw(@Nonnull Collection<UserFlag> flags)
        {
            Checks.notNull(flags, "Flag Collection");

            return getRaw(flags.toArray(EMPTY_FLAGS));
        }
    }
}
