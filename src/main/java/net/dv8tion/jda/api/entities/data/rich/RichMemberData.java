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

package net.dv8tion.jda.api.entities.data.rich;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.data.MemberData;
import net.dv8tion.jda.api.entities.data.MutableMemberData;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public class RichMemberData implements MutableMemberData
{
    private final EnumMap<ClientType, OnlineStatus> clientStatus;

    private String nickname;
    private long timeJoined, timeBoosted;
    private OnlineStatus onlineStatus = OnlineStatus.UNKNOWN;
    private List<Activity> activities;

    public RichMemberData(EnumSet<CacheFlag> flags)
    {
        this.activities = flags.contains(CacheFlag.ACTIVITY) ? Collections.emptyList() : null;
        this.clientStatus = flags.contains(CacheFlag.CLIENT_STATUS) ? new EnumMap<>(ClientType.class) : null;
    }

    @Nonnull
    @Override
    public MemberData copy()
    {
        EnumSet<CacheFlag> flags = EnumSet.noneOf(CacheFlag.class);
        if (activities != null)
            flags.add(CacheFlag.ACTIVITY);
        if (clientStatus != null)
            flags.add(CacheFlag.CLIENT_STATUS);
        RichMemberData data = new RichMemberData(flags);
        data.setNickname(nickname);
        data.setTimeJoined(timeJoined);
        data.setTimeBoosted(timeBoosted);
        data.setActivities(activities);
        data.setOnlineStatus(onlineStatus);
        if (clientStatus != null)
            clientStatus.forEach(data::setOnlineStatus);
        return data;
    }

    @Override
    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    @Override
    public void setTimeJoined(long time)
    {
        this.timeJoined = time;
    }

    @Override
    public void setTimeBoosted(long time)
    {
        this.timeBoosted = time;
    }

    @Override
    public void setActivities(@Nonnull List<Activity> activities)
    {
        if (this.activities == null)
            return;
        this.activities = activities;
    }

    @Override
    public void setOnlineStatus(@Nonnull OnlineStatus status)
    {
        this.onlineStatus = status;
    }

    @Override
    public void setOnlineStatus(@Nonnull ClientType type, @Nonnull OnlineStatus status)
    {
        if (this.clientStatus == null)
            return;
        this.clientStatus.put(type, status);
    }

    @Nullable
    @Override
    public String getNickname()
    {
        return this.nickname;
    }

    @Override
    public long getTimeJoined()
    {
        return this.timeJoined;
    }

    @Override
    public long getTimeBoosted()
    {
        return this.timeBoosted;
    }

    @Nonnull
    @Override
    public List<Activity> getActivities()
    {
        return this.activities == null ? Collections.emptyList() : this.activities;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus()
    {
        return onlineStatus;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus(ClientType type)
    {
        if (clientStatus == null)
            return OnlineStatus.UNKNOWN;
        OnlineStatus status = clientStatus.get(type);
        return status == null ? OnlineStatus.OFFLINE : status;
    }
}
