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
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.entities.UserSnowflakeImpl
import javax.annotation.Nonnull

/**
 * Represents an abstract user reference by only the user ID.
 *
 *
 * This is used for methods which only need a user ID to function, you cannot use this for getting names or similar.
 * To get information about a user by their ID you can use [JDA.retrieveUserById] or [JDA.getUserById] instead.
 */
interface UserSnowflake : IMentionable // Make this a value type whenever that's finally released!
{
    @JvmField
    @get:Nonnull
    val defaultAvatarId: String?

    @get:Nonnull
    val defaultAvatarUrl: String?
        /**
         * The URL for the user's default avatar image.
         *
         * @return Never-null String containing the user's default avatar url.
         */
        get() = String.format(User.Companion.DEFAULT_AVATAR_URL, defaultAvatarId)

    @get:Nonnull
    val defaultAvatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this user's default avatar.
         *
         * @return Never-null [ImageProxy] of this user's default avatar
         *
         * @see .getDefaultAvatarUrl
         */
        get() = ImageProxy(defaultAvatarUrl!!)

    companion object {
        /**
         * Creates a User instance which only wraps an ID.
         *
         * @param  id
         * The user id
         *
         * @return A user snowflake instance
         *
         * @see JDA.retrieveUserById
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
         * @return A user snowflake instance
         *
         * @see JDA.retrieveUserById
         */
        @Nonnull
        fun fromId(@Nonnull id: String?): UserSnowflake? {
            return fromId(MiscUtil.parseSnowflake(id))
        }
    }
}
