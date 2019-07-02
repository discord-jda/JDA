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

package net.dv8tion.jda.api.entities.bean.light;

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.bean.GuildData;
import net.dv8tion.jda.api.entities.bean.MutableGuildData;

public class LightGuildData implements MutableGuildData
{
    @Override
    public GuildData copy()
    {
        return new LightGuildData();
    }

    @Override
    public String setIconId(String id)
    {
        return id;
    }

    @Override
    public String setSplashId(String id)
    {
        return id;
    }

    @Override
    public String setDescription(String description)
    {
        return description;
    }

    @Override
    public String setBannerId(String id)
    {
        return id;
    }

    @Override
    public Guild.BoostTier setBoostTier(Guild.BoostTier tier)
    {
        return tier;
    }

    @Override
    public int setBoostCount(int count)
    {
        return count;
    }

    @Override
    public int setMaxMembers(int members)
    {
        return members;
    }

    @Override
    public int setMaxPresences(int presences)
    {
        return presences;
    }

    @Override
    public long setAfkChannelId(long id)
    {
        return id;
    }

    @Override
    public long setSystemChannelId(long id)
    {
        return id;
    }

    @Override
    public Guild.Timeout setAfkTimeout(Guild.Timeout timeout)
    {
        return timeout;
    }

    @Override
    public Guild.VerificationLevel setVerificationLevel(Guild.VerificationLevel level)
    {
        return level;
    }

    @Override
    public Guild.NotificationLevel setNotificationLevel(Guild.NotificationLevel level)
    {
        return level;
    }

    @Override
    public Guild.ExplicitContentLevel setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        return level;
    }

    @Override
    public Guild.MFALevel setMFALevel(Guild.MFALevel level)
    {
        return level;
    }

    @Override
    public String setRegion(String region)
    {
        return region;
    }

    @Override
    public String getIconId()
    {
        return null;
    }

    @Override
    public String getSplashId()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return "";
    }

    @Override
    public String getBannerId()
    {
        return null;
    }

    @Override
    public Guild.BoostTier getBoostTier()
    {
        return Guild.BoostTier.NONE;
    }

    @Override
    public int getBoostCount()
    {
        return 0;
    }

    @Override
    public int getMaxMembers()
    {
        return 0;
    }

    @Override
    public int getMaxPresences()
    {
        return 0;
    }

    @Override
    public long getAfkChannelId()
    {
        return 0;
    }

    @Override
    public long getSystemChannelId()
    {
        return 0;
    }

    @Override
    public Guild.Timeout getAfkTimeout()
    {
        return Guild.Timeout.SECONDS_3600;
    }

    @Override
    public Guild.VerificationLevel getVerificationLevel()
    {
        return Guild.VerificationLevel.UNKNOWN;
    }

    @Override
    public Guild.NotificationLevel getNotificationLevel()
    {
        return Guild.NotificationLevel.UNKNOWN;
    }

    @Override
    public Guild.MFALevel getMFALevel()
    {
        return Guild.MFALevel.UNKNOWN;
    }

    @Override
    public Guild.ExplicitContentLevel getExplicitContentLevel()
    {
        return Guild.ExplicitContentLevel.UNKNOWN;
    }

    @Override
    public String getRegion()
    {
        return Region.UNKNOWN.getKey();
    }
}
