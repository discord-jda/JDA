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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.UserSnowflakeImpl;

import javax.annotation.Nonnull;

/**
 * Represents an abstract user reference by only the user ID.
 *
 * <p>This is used for methods which only need a user ID to function, you cannot use this for getting names or similar.
 * To get information about a user by their ID you can use {@link JDA#retrieveUserById(long)} or {@link JDA#getUserById(long)} instead.
 */
public interface UserSnowflake extends IMentionable // Make this a value type whenever that's finally released!
{
    /**
     * Creates a User instance which only wraps an ID.
     *
     * @param  id
     *         The user id
     *
     * @return A user snowflake instance
     *
     * @see    JDA#retrieveUserById(long)
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
     * @return A user snowflake instance
     *
     * @see    JDA#retrieveUserById(String)
     */
    @Nonnull
    static UserSnowflake fromId(@Nonnull String id)
    {
        return fromId(MiscUtil.parseSnowflake(id));
    }

    /**
     * The Discord ID for this user's default avatar image.
     *
     * @return Never-null String containing the user's default avatar id.
     */
    @Nonnull
    String getDefaultAvatarId();

    /**
     * The URL for the user's default avatar image.
     *
     * @return Never-null String containing the user's default avatar url.
     */
    @Nonnull
    default String getDefaultAvatarUrl()
    {
        return String.format(User.DEFAULT_AVATAR_URL, getDefaultAvatarId());
    }

    /**
     * Returns an {@link ImageProxy} for this user's default avatar.
     *
     * @return Never-null {@link ImageProxy} of this user's default avatar
     *
     * @see    #getDefaultAvatarUrl()
     */
    @Nonnull
    default ImageProxy getDefaultAvatar()
    {
        return new ImageProxy(getDefaultAvatarUrl());
    }
}
