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

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ClientType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class MemberPresenceImpl
{
    private List<Activity> activities = Collections.emptyList();
    private EnumMap<ClientType, OnlineStatus> clientStatus;
    private OnlineStatus status = OnlineStatus.OFFLINE;

    public void setActivities(List<Activity> activities)
    {
        this.activities = activities;
    }

    public void setClientStatus(EnumMap<ClientType, OnlineStatus> clientStatus)
    {
        this.clientStatus = clientStatus;
    }

    public void setOnlineStatus(OnlineStatus status)
    {
        this.status = status;
    }

    public List<Activity> getActivities()
    {
        return activities;
    }

    public EnumMap<ClientType, OnlineStatus> getClientStatus()
    {
        if (clientStatus == null)
            return new EnumMap<>(ClientType.class);
        return clientStatus;
    }

    public OnlineStatus getOnlineStatus()
    {
        return status;
    }

    public void setOnlineStatus(ClientType type, OnlineStatus clientStatus)
    {
        if (this.clientStatus == null)
        {
            if (clientStatus == null || clientStatus == OnlineStatus.OFFLINE)
                return;
            this.clientStatus = new EnumMap<>(ClientType.class);
        }
        if (clientStatus == OnlineStatus.OFFLINE)
            this.clientStatus.remove(type);
        else
            this.clientStatus.put(type, clientStatus);
    }
}
