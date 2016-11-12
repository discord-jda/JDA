/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;

/**
 * Represents a Discord User.
 * Contains all publicly available information about a specific Discord User.
 */
public interface User
{
    /**
     * The Discord ID of the {@link net.dv8tion.jda.entities.User User}. This is typically 18 characters long.
     *
     * @return
     *      Never-null String containing the {@link net.dv8tion.jda.entities.User User} id.
     */
    String getId();

    /**
     * The username of the {@link net.dv8tion.jda.entities.User User}. Length is between 2 and 32 characters (inclusive).
     *
     * @return
     *      Never-null String containing the {@link net.dv8tion.jda.entities.User User} username.
     */
    String getUsername();

    /**
     * The discriminator of the {@link net.dv8tion.jda.entities.User User}. Used to differentiate between users with the same usernames.<br>
     * This will be important when the friends list is released for human readable searching.<br>
     * Ex: DV8FromTheWorld#9148
     *
     * @return
     *      Never-null String containing the {@link net.dv8tion.jda.entities.User User} discriminator.
     */
    String getDiscriminator();

    /**
     * Returns the String needed to mention this User in a {@link net.dv8tion.jda.entities.Message Message}.
     *
     * @return
     *      The String needed to mention this User
     */
    String getAsMention();

    /**
     * The Discord Id for this user's avatar image.
     * If the user has not set an image, this will return null.
     *
     * @return
     *      Possibly-null String containing the {@link net.dv8tion.jda.entities.User User} avatar id.
     */
    String getAvatarId();

    /**
     * The URL for the for the user's avatar image.
     * If the user has not set an image, this will return null.
     *
     * @return
     *      Possibly-null String containing the {@link net.dv8tion.jda.entities.User User} avatar url.
     */
    String getAvatarUrl();

    /**
     * The Discord Id for this user's default avatar image.
     *
     * @return
     *      Never-null String containing the {@link net.dv8tion.jda.entities.User User} default avatar id.
     */
    String getDefaultAvatarId();

    /**
     * The URL for the for the user's default avatar image.
     *
     * @return
     *      Never-null String containing the {@link net.dv8tion.jda.entities.User User} default avatar url.
     */
    String getDefaultAvatarUrl();

    /**
     * The game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.Game Game} containing the game that the {@link net.dv8tion.jda.entities.User User} is currently playing.
     */
    Game getCurrentGame();

    /**
     * Returns the {@link net.dv8tion.jda.OnlineStatus OnlineStatus} of the User.<br>
     * If the {@link net.dv8tion.jda.OnlineStatus OnlineStatus} is unrecognized, will return {@link net.dv8tion.jda.OnlineStatus#UNKNOWN UNKNOWN}.
     *
     * @return
     *      The current {@link net.dv8tion.jda.OnlineStatus OnlineStatus} of the {@link net.dv8tion.jda.entities.User User}.
     */
    OnlineStatus getOnlineStatus();

    /**
     * Gets the {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} of this
     * {@link net.dv8tion.jda.entities.User User} for use in sending direct messages.
     *
     * @return
     *      Never-null {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} that is associated with this {@link net.dv8tion.jda.entities.User User}.
     */
    PrivateChannel getPrivateChannel();

    /**
     * Returns whether or not the given user is a Bot-Account (special badge in client, some different behaviour)
     *
     * @return
     *      If the User's Account is marked as Bot
     */
    boolean isBot();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this User
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();
}
