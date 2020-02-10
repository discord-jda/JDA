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

package net.dv8tion.jda.api.entities.data.light;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.data.MemberData;
import net.dv8tion.jda.api.entities.data.MutableMemberData;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class LightMemberData implements MutableMemberData
{
    public static final LightMemberData SINGLETON = new LightMemberData();

    @Nonnull
    @Override
    public MemberData copy()
    {
        return this;
    }

    public void setNickname(String nickname) {}
    public void setTimeJoined(long time) {}
    public void setTimeBoosted(long time) {}
    public void setActivities(@Nonnull List<Activity> activities) {}
    public void setOnlineStatus(@Nonnull OnlineStatus status) {}
    public void setOnlineStatus(@Nonnull ClientType type, @Nonnull OnlineStatus status) {}

    @Override
    public String getNickname()
    {
        return null;
    }

    @Override
    public long getTimeJoined()
    {
        return 0;
    }

    @Override
    public long getTimeBoosted()
    {
        return 0;
    }

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
        return OnlineStatus.UNKNOWN;
    }

    @Nonnull
    @Override
    public OnlineStatus getOnlineStatus(ClientType type)
    {
        return OnlineStatus.UNKNOWN;
    }
}
