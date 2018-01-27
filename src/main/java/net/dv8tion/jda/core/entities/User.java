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
package net.dv8tion.jda.core.entities;


import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;
import javax.annotation.CheckReturnValue;

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
 */
public interface User extends ISnowflake, IMentionable, IFakeable
{

    /**
     * The username of the {@link net.dv8tion.jda.core.entities.User User}. Length is between 2 and 32 characters (inclusive).
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.core.entities.User User}'s username.
     */
    String getName();

    /**
     * <br>The discriminator of the {@link net.dv8tion.jda.core.entities.User User}. Used to differentiate between users with the same usernames.
     * <br>This only contains the 4 digits after the username and the #.
     * Ex: 6297
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.core.entities.User User} discriminator.
     */
    String getDiscriminator();

    /**
     * The Discord Id for this user's avatar image.
     * If the user has not set an image, this will return null.
     *
     * @return Possibly-null String containing the {@link net.dv8tion.jda.core.entities.User User} avatar id.
     */
    String getAvatarId();

    /**
     * The URL for the user's avatar image.
     * If the user has not set an image, this will return null.
     *
     * @return Possibly-null String containing the {@link net.dv8tion.jda.core.entities.User User} avatar url.
     */
    String getAvatarUrl();

    /**
     * The Discord Id for this user's default avatar image.
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.core.entities.User User} default avatar id.
     */
    String getDefaultAvatarId();

    /**
     * The URL for the for the user's default avatar image.
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.core.entities.User User} default avatar url.
     */
    String getDefaultAvatarUrl();

    /**
     * The URL for the user's avatar image
     * If they do not have an avatar set, this will return the URL of their
     * default avatar
     *
     * @return  Never-null String containing the {@link net.dv8tion.jda.core.entities.User User} effective avatar url.
     */
    String getEffectiveAvatarUrl();

    /**
     * Whether or not the currently logged in user and this user have a currently open
     * {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} or not.
     *
     * @return True if the logged in account shares a PrivateChannel with this user.
     */
    boolean hasPrivateChannel();

    /**
     * Opens a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with this User.
     * <br>If a channel has already been opened with this user, it is immediately returned in the RestAction's
     * success consumer without contacting the Discord API.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If the recipient User has you blocked</li>
     * </ul>
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the recipient User is the currently logged in account (represented by {@link net.dv8tion.jda.core.entities.SelfUser SelfUser})
     * @throws java.lang.IllegalStateException
     *         If this User is {@link #isFake() fake}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         <br>The PrivateChannel to use to directly message this User.
     */
    @CheckReturnValue
    RestAction<PrivateChannel> openPrivateChannel();

    /**
     * Finds and collects all {@link net.dv8tion.jda.core.entities.Guild Guild} instances that contain this {@link net.dv8tion.jda.core.entities.User User} within the current {@link net.dv8tion.jda.core.JDA JDA} instance.<br>
     * <p>This method is a shortcut for {@link net.dv8tion.jda.core.JDA#getMutualGuilds(User...) JDA.getMutualGuilds(User)}.</p>
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guilds} that this user is a member of.
     */
    List<Guild> getMutualGuilds();

    /**
     * Returns whether or not the given user is a Bot-Account (special badge in client, some different behaviour)
     *
     * @return If the User's Account is marked as Bot
     */
    boolean isBot();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this User
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();
}
