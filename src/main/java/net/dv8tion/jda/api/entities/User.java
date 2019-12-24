/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a Discord User.
 * Contains all publicly available information about a specific Discord User.
 *
 * <h1>Formattable</h1>
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
public interface User extends IMentionable, IFakeable
{
    /**
     * Compiled pattern for a Discord Tag: {@code (.{2,32})#(\d{4})}
     */
    Pattern USER_TAG = Pattern.compile("(.{2,32})#(\\d{4})");

    /** Template for {@link #getAvatarUrl()}. */
    String AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s.%s";
    /** Template for {@link #getDefaultAvatarUrl()} */
    String DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/%s.png";

    /**
     * The username of the {@link net.dv8tion.jda.api.entities.User User}. Length is between 2 and 32 characters (inclusive).
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.User User}'s username.
     */
    @Nonnull
    String getName();

    /**
     * <br>The discriminator of the {@link net.dv8tion.jda.api.entities.User User}. Used to differentiate between users with the same usernames.
     * <br>This only contains the 4 digits after the username and the #.
     * Ex: 6297
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.User User} discriminator.
     */
    @Nonnull
    String getDiscriminator();

    /**
     * The Discord Id for this user's avatar image.
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
     * The Discord Id for this user's default avatar image.
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.User User} default avatar id.
     */
    @Nonnull
    String getDefaultAvatarId();

    /**
     * The URL for the for the user's default avatar image.
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.User User} default avatar url.
     */
    @Nonnull
    default String getDefaultAvatarUrl()
    {
        return String.format(DEFAULT_AVATAR_URL, getDefaultAvatarId());
    }

    /**
     * The URL for the user's avatar image
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
     * The "tag" for this user
     * <p>This is the equivalent of calling {@link java.lang.String#format(String, Object...) String.format}("%#s", user)
     *
     * @return Never-null String containing the tag for this user, for example DV8FromTheWorld#6297
     */
    @Nonnull
    String getAsTag();

    /**
     * Whether or not the currently logged in user and this user have a currently open
     * {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} or not.
     *
     * @return True if the logged in account shares a PrivateChannel with this user.
     */
    boolean hasPrivateChannel();

    /**
     * Opens a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with this User.
     * <br>If a channel has already been opened with this user, it is immediately returned in the RestAction's
     * success consumer without contacting the Discord API.
     *
     * <h2>Examples</h2>
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
     * @throws java.lang.UnsupportedOperationException
     *         If the recipient User is the currently logged in account (represented by {@link net.dv8tion.jda.api.entities.SelfUser SelfUser})
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         <br>Retrieves the PrivateChannel to use to directly message this User.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<PrivateChannel> openPrivateChannel();

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
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this User
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();
}
