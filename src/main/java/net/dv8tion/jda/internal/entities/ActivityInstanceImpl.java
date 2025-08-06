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
package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.ActivityInstance;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ActivityInstanceImpl implements ActivityInstance
{
    private final String instanceId;
    private final long launchId;
    private final Location location;
    private final List<UserSnowflake> users;

    public ActivityInstanceImpl(String instanceId, long launchId, Location location, List<UserSnowflake> users)
    {
        this.instanceId = instanceId;
        this.launchId = launchId;
        this.location = location;
        this.users = users;
    }

    @Nonnull
    @Override
    public String getInstanceId()
    {
        return instanceId;
    }

    @Override
    public long getLaunchIdLong()
    {
        return launchId;
    }

    @Nonnull
    @Override
    public Location getLocation()
    {
        return location;
    }

    @Nonnull
    @Override
    public List<UserSnowflake> getUsers()
    {
        return users;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof ActivityInstanceImpl))
            return false;

        ActivityInstanceImpl other = (ActivityInstanceImpl) obj;
        return Objects.equals(instanceId, other.instanceId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(instanceId);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("instanceId", instanceId)
                .toString();
    }

    public static class LocationImpl implements Location
    {
        private final String id;
        private final Kind kind;
        private final long channelId;
        private final Long guildId;

        public LocationImpl(String id, Kind kind, long channelId, Long guildId)
        {
            this.id = id;
            this.kind = kind;
            this.channelId = channelId;
            this.guildId = guildId;
        }

        @Nonnull
        @Override
        public String getId()
        {
            return id;
        }

        @Nonnull
        @Override
        public Kind getKind()
        {
            return kind;
        }

        @Override
        public long getChannelIdLong()
        {
            return channelId;
        }

        @Nullable
        @Override
        public Long getGuildIdLong()
        {
            return guildId;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof LocationImpl))
                return false;

            LocationImpl other = (LocationImpl) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(id);
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("id", id)
                    .toString();
        }
    }
}
