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

import javax.annotation.Nonnull;

public class LightGuildData implements MutableGuildData
{
    public static final LightGuildData SINGLETON = new LightGuildData();

    @Override
    public GuildData copy()
    {
        return this;
    }

    // EQUALS = IDENTITY (see Object)

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

    @Nonnull
    @Override
    public Guild.BoostTier setBoostTier(@Nonnull Guild.BoostTier tier)
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

    @Nonnull
    @Override
    public Guild.Timeout setAfkTimeout(@Nonnull Guild.Timeout timeout)
    {
        return timeout;
    }

    @Nonnull
    @Override
    public Guild.VerificationLevel setVerificationLevel(@Nonnull Guild.VerificationLevel level)
    {
        return level;
    }

    @Nonnull
    @Override
    public Guild.NotificationLevel setNotificationLevel(@Nonnull Guild.NotificationLevel level)
    {
        return level;
    }

    @Nonnull
    @Override
    public Guild.ExplicitContentLevel setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level)
    {
        return level;
    }

    @Nonnull
    @Override
    public Guild.MFALevel setMFALevel(@Nonnull Guild.MFALevel level)
    {
        return level;
    }

    @Nonnull
    @Override
    public String setRegion(@Nonnull String region)
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
        return null;
    }

    @Override
    public String getBannerId()
    {
        return null;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public Guild.Timeout getAfkTimeout()
    {
        return Guild.Timeout.SECONDS_3600;
    }

    @Nonnull
    @Override
    public Guild.VerificationLevel getVerificationLevel()
    {
        return Guild.VerificationLevel.UNKNOWN;
    }

    @Nonnull
    @Override
    public Guild.NotificationLevel getNotificationLevel()
    {
        return Guild.NotificationLevel.UNKNOWN;
    }

    @Nonnull
    @Override
    public Guild.MFALevel getMFALevel()
    {
        return Guild.MFALevel.UNKNOWN;
    }

    @Nonnull
    @Override
    public Guild.ExplicitContentLevel getExplicitContentLevel()
    {
        return Guild.ExplicitContentLevel.UNKNOWN;
    }

    @Nonnull
    @Override
    public String getRegion()
    {
        return Region.UNKNOWN.getKey();
    }
}
