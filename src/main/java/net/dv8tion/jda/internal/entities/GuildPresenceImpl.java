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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.GuildPresence;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuildPresenceImpl implements GuildPresence
{
    private final Map<ClientType, OnlineStatus> clientStatus;
    private List<Activity> activities = Collections.emptyList();
    private OnlineStatus status;

    public GuildPresenceImpl(EnumSet<CacheFlag> flags)
    {
        if (flags.contains(CacheFlag.CLIENT_STATUS))
            this.clientStatus = new ConcurrentHashMap<>(5);
        else
            this.clientStatus = null;
        this.status = OnlineStatus.OFFLINE;
    }

    @Nonnull
    @Override
    public List<Activity> getActivities()
    {
        return activities;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus()
    {
        return status;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus(@Nonnull ClientType type)
    {
        Checks.notNull(type, "Type");
        if (clientStatus == null)
            return getOnlineStatus();
        OnlineStatus status = clientStatus.get(type);
        return status == null ? OnlineStatus.OFFLINE : status;
    }

    public GuildPresenceImpl setActivities(List<Activity> activities)
    {
        this.activities = activities == null ? Collections.emptyList() : Collections.unmodifiableList(activities);
        return this;
    }

    public GuildPresenceImpl setOnlineStatus(ClientType type, OnlineStatus status)
    {
        if (this.clientStatus == null || type == ClientType.UNKNOWN || type == null)
            return this;
        if (status == null || status == OnlineStatus.UNKNOWN || status == OnlineStatus.OFFLINE)
            this.clientStatus.remove(type);
        else
            this.clientStatus.put(type, status);
        return this;
    }

    public GuildPresenceImpl setOnlineStatus(OnlineStatus status)
    {
        this.status = status;
        return this;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(status, clientStatus, activities);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof GuildPresenceImpl))
            return false;
        GuildPresenceImpl impl = (GuildPresenceImpl) obj;
        return impl.status == status
            && Objects.equals(impl.activities, activities)
            && Objects.equals(impl.clientStatus, clientStatus);
    }

    @Override
    public String toString()
    {
        return "GuildPresence(" + status + ')';
    }
}
