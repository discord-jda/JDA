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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MemberPresence;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public class MemberPresenceImpl implements MemberPresence
{
    public static final MemberPresence EMPTY = new MemberPresenceImpl(OnlineStatus.OFFLINE, new EnumMap<>(ClientType.class), Collections.emptyList());
    private final OnlineStatus status;
    private final EnumMap<ClientType, OnlineStatus> clientStatus;
    private final List<Activity> activities;

    public MemberPresenceImpl(OnlineStatus status, EnumMap<ClientType, OnlineStatus> clientStatus, List<Activity> activities)
    {
        this.status = status;
        this.clientStatus = clientStatus;
        this.activities = activities;
    }

    public MemberPresenceImpl(Member member)
    {
        this.status = member.getOnlineStatus();
        this.clientStatus = new EnumMap<>(ClientType.class);
        for (ClientType type : ClientType.values())
        {
            OnlineStatus onlineStatus = member.getOnlineStatus(type);
            if (onlineStatus != OnlineStatus.OFFLINE && onlineStatus != OnlineStatus.UNKNOWN)
                this.clientStatus.put(type, onlineStatus);
        }
        this.activities = member.getActivities();
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
        OnlineStatus status = clientStatus.get(type);
        return status == null ? OnlineStatus.OFFLINE : status;
    }

    @Nonnull
    @Override
    public EnumSet<ClientType> getActiveClients()
    {
        return EnumSet.copyOf(clientStatus.keySet());
    }

    @Override
    public String toString()
    {
        return "MemberPresence["
            + "status=" + status + ","
            + "clientStatus=" + clientStatus + ","
            + "activities=" + activities + "]";
    }
}
