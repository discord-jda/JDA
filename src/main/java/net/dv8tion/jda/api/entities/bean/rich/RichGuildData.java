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

package net.dv8tion.jda.api.entities.bean.rich;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.bean.MutableGuildData;

import java.util.Objects;

public class RichGuildData implements MutableGuildData
{
    private String iconId, splashId, bannerId;
    private String description = "", region = "";
    private int maxMembers, maxPresences, boostCount;
    private Guild.BoostTier boostTier;
    private Guild.NotificationLevel notificationLevel;
    private Guild.ExplicitContentLevel explicitContentLevel;
    private Guild.MFALevel mfaLevel;
    private Guild.VerificationLevel verificationLevel;
    private Guild.Timeout afkTimeout;
    private long systemChannelId, afkChannelId;

    @Override
    public RichGuildData copy()
    {
        RichGuildData data = new RichGuildData();
        data.setIconId(iconId);
        data.setSplashId(splashId);
        data.setBannerId(bannerId);
        data.setDescription(description);
        data.setRegion(region);
        data.setMaxMembers(maxMembers);
        data.setMaxPresences(maxPresences);
        data.setBoostCount(boostCount);
        data.setBoostTier(boostTier);
        data.setNotificationLevel(notificationLevel);
        data.setExplicitContentLevel(explicitContentLevel);
        data.setMFALevel(mfaLevel);
        data.setVerificationLevel(verificationLevel);
        data.setAfkTimeout(afkTimeout);
        data.setSystemChannelId(systemChannelId);
        data.setAfkChannelId(afkChannelId);
        return data;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof RichGuildData))
            return false;

        RichGuildData data = (RichGuildData) obj;
        return Objects.equals(iconId, data.iconId)
            && Objects.equals(splashId, data.splashId)
            && Objects.equals(bannerId, data.bannerId)
            && Objects.equals(description, data.description)
            && Objects.equals(region, data.region)
            && maxMembers == data.maxMembers
            && maxPresences == data.maxPresences
            && boostCount == data.boostCount
            && boostTier == data.boostTier
            && notificationLevel == data.notificationLevel
            && explicitContentLevel == data.explicitContentLevel
            && mfaLevel == data.mfaLevel
            && verificationLevel == data.verificationLevel
            && afkTimeout == data.afkTimeout
            && systemChannelId == data.systemChannelId
            && afkChannelId == data.afkChannelId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(
            iconId, splashId, bannerId,
            description, region,
            maxMembers, maxPresences, boostCount,
            boostTier, notificationLevel, explicitContentLevel,
            mfaLevel, verificationLevel, afkTimeout,
            systemChannelId, afkChannelId
        );
    }

    @Override
    public String setIconId(String id)
    {
        String oldIcon = this.iconId;
        this.iconId = id;
        return oldIcon;
    }

    @Override
    public String setSplashId(String id)
    {
        String oldSplash = this.splashId;
        this.splashId = id;
        return oldSplash;
    }

    @Override
    public String setDescription(String description)
    {
        String oldDescription = this.description;
        this.description = description;
        return oldDescription;
    }

    @Override
    public String setBannerId(String id)
    {
        String oldBanner = this.bannerId;
        this.bannerId = id;
        return oldBanner;
    }

    @Override
    public Guild.BoostTier setBoostTier(Guild.BoostTier tier)
    {
        Guild.BoostTier oldTier = this.boostTier;
        this.boostTier = tier;
        return oldTier;
    }

    @Override
    public int setBoostCount(int count)
    {
        int oldCount = this.boostCount;
        this.boostCount = count;
        return count;
    }

    @Override
    public int setMaxMembers(int members)
    {
        int oldMax = this.maxMembers;
        this.maxMembers = members;
        return oldMax;
    }

    @Override
    public int setMaxPresences(int presences)
    {
        int oldMax = this.maxPresences;
        this.maxPresences = presences;
        return oldMax;
    }

    @Override
    public long setAfkChannelId(long id)
    {
        long oldId = this.afkChannelId;
        this.afkChannelId = id;
        return oldId;
    }

    @Override
    public long setSystemChannelId(long id)
    {
        long oldId = this.systemChannelId;
        this.systemChannelId = id;
        return oldId;
    }

    @Override
    public Guild.Timeout setAfkTimeout(Guild.Timeout timeout)
    {
        Guild.Timeout oldTimeout = this.afkTimeout;
        this.afkTimeout = timeout;
        return oldTimeout;
    }

    @Override
    public Guild.VerificationLevel setVerificationLevel(Guild.VerificationLevel level)
    {
        Guild.VerificationLevel oldLevel = this.verificationLevel;
        this.verificationLevel = level;
        return oldLevel;
    }

    @Override
    public Guild.NotificationLevel setNotificationLevel(Guild.NotificationLevel level)
    {
        Guild.NotificationLevel oldLevel = this.notificationLevel;
        this.notificationLevel = level;
        return oldLevel;
    }

    @Override
    public Guild.ExplicitContentLevel setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        Guild.ExplicitContentLevel oldLevel = this.explicitContentLevel;
        this.explicitContentLevel = level;
        return oldLevel;
    }

    @Override
    public Guild.MFALevel setMFALevel(Guild.MFALevel level)
    {
        Guild.MFALevel oldLevel = this.mfaLevel;
        this.mfaLevel = level;
        return oldLevel;
    }

    @Override
    public String setRegion(String region)
    {
        String oldRegion = this.region;
        this.region = region;
        return oldRegion;
    }

    @Override
    public String getIconId()
    {
        return iconId;
    }

    @Override
    public String getSplashId()
    {
        return splashId;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getBannerId()
    {
        return bannerId;
    }

    @Override
    public Guild.BoostTier getBoostTier()
    {
        return boostTier;
    }

    @Override
    public int getBoostCount()
    {
        return boostCount;
    }

    @Override
    public int getMaxMembers()
    {
        return maxMembers;
    }

    @Override
    public int getMaxPresences()
    {
        return maxPresences;
    }

    @Override
    public long getAfkChannelId()
    {
        return afkChannelId;
    }

    @Override
    public long getSystemChannelId()
    {
        return systemChannelId;
    }

    @Override
    public Guild.Timeout getAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public Guild.VerificationLevel getVerificationLevel()
    {
        return verificationLevel;
    }

    @Override
    public Guild.NotificationLevel getNotificationLevel()
    {
        return notificationLevel;
    }

    @Override
    public Guild.MFALevel getMFALevel()
    {
        return mfaLevel;
    }

    @Override
    public Guild.ExplicitContentLevel getExplicitContentLevel()
    {
        return explicitContentLevel;
    }

    @Override
    public String getRegion()
    {
        return region;
    }
}
