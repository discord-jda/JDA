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

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.data.GuildData;
import net.dv8tion.jda.api.entities.data.MutableGuildData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LightGuildData implements MutableGuildData
{
    public static final LightGuildData SINGLETON = new LightGuildData();

    @Nonnull
    @Override
    public GuildData copy()
    {
        return this;
    }

    public void setIconId(String id) {}
    public void setSplashId(String id) {}
    public void setDescription(String description) {}
    public void setBannerId(String id) {}
    public void setBoostTier(@Nonnull Guild.BoostTier tier) {}
    public void setBoostCount(int count) {}
    public void setMaxMembers(int members) {}
    public void setMaxPresences(int presences) {}
    public void setAfkChannelId(long id) {}
    public void setSystemChannelId(long id) {}
    public void setAfkTimeout(@Nonnull Guild.Timeout timeout) {}
    public void setVerificationLevel(@Nonnull Guild.VerificationLevel level) {}
    public void setNotificationLevel(@Nonnull Guild.NotificationLevel level) {}
    public void setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level) {}
    public void setMFALevel(@Nonnull Guild.MFALevel level) {}
    public void setRegion(@Nonnull String region) {}
    public void setVanityCode(@Nullable String code) {}

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

    @Nullable
    @Override
    public String getVanityCode()
    {
        return null;
    }
}
