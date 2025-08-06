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

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a Discord {@link ActivityInstance Activity instance}.
 * <br>This is different from {@link Activity}, which are for your bot's presence,
 * while this class is for Discord's activities.
 */
public interface ActivityInstance
{
    /**
     * The instance ID of the launched activity.
     * <br>Users joining the activity will receive the same instance ID,
     * when all users leave, the instance will be closed and never used again.
     *
     * @return The instance ID of the launched activity
     */
    @Nonnull
    String getInstanceId();

    /**
     * The unique identifier for the activity's launch.
     *
     * @return The unique identifier for the activity's launch
     */
    @Nonnull
    default String getLaunchId()
    {
        return Long.toUnsignedString(getLaunchIdLong());
    }

    /**
     * The unique identifier for the activity's launch.
     *
     * @return The unique identifier for the activity's launch
     */
    long getLaunchIdLong();

    /**
     * The location in which this activity is running in.
     *
     * @return The location in which this activity is running in
     */
    @Nonnull
    Location getLocation();

    /**
     * The snowflakes of the users currently connected to the instance.
     *
     * @return The snowflakes of the users currently connected to the instance
     */
    @Nonnull
    List<UserSnowflake> getUsers();

    /**
     * The location an {@link ActivityInstance} is running in.
     */
    interface Location
    {
        /**
         * The unique identifier for the location.
         *
         * @return The unique identifier for the location
         */
        @Nonnull
        String getId();

        /**
         * The kind of location this activity runs in.
         *
         * @return The kind of location this activity runs in.
         */
        @Nonnull
        Kind getKind();

        /**
         * The ID of the channel this activity runs in.
         * <br>The returned ID may refer to a channel the bot does not have access to,
         * such as with {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity detached entities}.
         *
         * @return The ID of the channel this activity runs in
         */
        @Nonnull
        default String getChannelId()
        {
            return Long.toUnsignedString(getChannelIdLong());
        }

        /**
         * The ID of the channel this activity runs in.
         * <br>The returned ID may refer to a channel the bot does not have access to,
         * such as with {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity detached entities}.
         *
         * @return The ID of the channel this activity runs in
         */
        long getChannelIdLong();

        /**
         * The ID of the guild this activity runs in, or {@code null} if this activity is outside a guild.
         * <br>The returned ID may refer to a guild the bot does not have access to,
         * such as with {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity detached entities}.
         *
         * @return The ID of the guild this activity runs in, or {@code null}
         */
        @Nullable
        default String getGuildId()
        {
            final Long id = getGuildIdLong();
            if (id == null) return null;
            return Long.toUnsignedString(id);
        }

        /**
         * The ID of the guild this activity runs in, or {@code null} if this activity is outside a guild.
         * <br>The returned ID may refer to a guild the bot does not have access to,
         * such as with {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity detached entities}.
         *
         * @return The ID of the guild this activity runs in, or {@code null}
         */
        @Nullable
        Long getGuildIdLong();

        /**
         * Represents the kind of location this activity runs in.
         */
        enum Kind
        {
            UNKNOWN(""),
            GUILD_CHANNEL("gc"),
            PRIVATE_CHANNEL("pc");

            private final String key;

            Kind(String key)
            {
                this.key = key;
            }

            /**
             * The raw key used by the API to identify this kind
             *
             * @return The raw key
             */
            @Nonnull
            public String getKey()
            {
                return key;
            }

            /**
             * Gets the {@link Kind} related to the provided key.
             * <br>If an unknown key is provided, this returns {@link #UNKNOWN}
             *
             * @param  key
             *         The Discord key referencing a {@link Kind}.
             *
             * @return The {@link Kind} that has the key provided, or {@link #UNKNOWN} for unknown key.
             */
            @Nonnull
            public static Kind fromKey(@Nonnull String key)
            {
                Checks.notNull(key, "Key");
                for (Kind kind : values())
                {
                    if (kind.key.equals(key))
                        return kind;
                }

                return UNKNOWN;
            }
        }
    }
}
