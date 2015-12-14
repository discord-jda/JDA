/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.OnlineStatus;

public interface User
{
    /**
     * The Discord ID of the User. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The username of the user. Length is between 2 and 32 (inclusive).
     * @return
     */
    String getUsername();

    /**
     * The descriminator of the User. Used to differentiate between users with the same usernames.
     * This will be important when the friends list is released for human readable searching.
     * Ex: DV8FromTheWorld#9148
     * @return
     */
    String getDiscriminator();

    /**
     * The Discord Id for this user's avatar image.
     * If the user has not set an image, this will return null.
     * @return
     */
    String getAvatarId();

    /**
     * The URL for the for the user's avatar image.
     * If the user has not set an image, this will return null.
     * @return
     */
    String getAvatarUrl();

    /**
     * The Discord Id for the game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     * @return
     */
    int getCurrentGameId();

    /**
     * The name of the game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     * @return
     */
    String getCurrentGameName();

    /**
     * Returns the {@link OnlineStatus} of the User.
     * @return
     */
    OnlineStatus getOnlineStatus();

    /**
     * Gets the {@link PrivateChannel} of this {@link User} for use in sending direct messages.
     * @return
     */
    PrivateChannel getPrivateChannel();
}
