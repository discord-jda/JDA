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

import net.dv8tion.jda.api.OnlineStatus;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface GuildPresence
{
    /**
     * The default presence when caching is disabled.
     *
     * @see net.dv8tion.jda.api.utils.cache.CacheFlag#GUILD_PRESENCE CacheFlag.GUILD_PRESENCE
     */
    GuildPresence EMPTY = new Empty();

    /**
     * The activities of the user.
     * <br>If the user does not currently have any activity, this returns an empty list.
     *
     * @return Immutable list of {@link Activity Activities} for the user
     */
    @Nonnull
    List<Activity> getActivities();

    /**
     * Returns the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} of the User.
     * <br>If the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} is unrecognized, will return {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}.
     *
     * @return The current {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} of the {@link net.dv8tion.jda.api.entities.User User}.
     */
    @Nonnull
    OnlineStatus getOnlineStatus();

    /**
     * The platform dependent {@link net.dv8tion.jda.api.OnlineStatus} of this member.
     * <br>Since a user can be connected from multiple different devices such as web and mobile,
     * discord specifies a status for each {@link net.dv8tion.jda.api.entities.ClientType}.
     *
     * <p>If a user is not online on the specified type,
     * {@link net.dv8tion.jda.api.OnlineStatus#OFFLINE OFFLINE} is returned.
     *
     * @param  type
     *         The type of client
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided type is null
     *
     * @return The status for that specific client or OFFLINE
     */
    @Nonnull
    OnlineStatus getOnlineStatus(@Nonnull ClientType type);

    class Empty implements GuildPresence
    {
        private Empty() {}

        @Nonnull
        @Override
        public List<Activity> getActivities()
        {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public OnlineStatus getOnlineStatus()
        {
            return OnlineStatus.OFFLINE;
        }

        @Nonnull
        @Override
        public OnlineStatus getOnlineStatus(@Nonnull ClientType type)
        {
            return OnlineStatus.OFFLINE;
        }

        @Override
        public String toString()
        {
            return "GuildPresence(Empty)";
        }
    }
}
