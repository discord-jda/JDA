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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.data.MutableGuildData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class RichGuildData implements MutableGuildData
{
    private String iconId, splashId, bannerId;
    private String description, vanityCode, region = "";
    private int maxMembers, maxPresences, boostCount;
    private Guild.BoostTier boostTier;
    private Guild.NotificationLevel notificationLevel;
    private Guild.ExplicitContentLevel explicitContentLevel;
    private Guild.MFALevel mfaLevel;
    private Guild.VerificationLevel verificationLevel;
    private Guild.Timeout afkTimeout;
    private long systemChannelId, afkChannelId;

    @Nonnull
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
        data.setVanityCode(vanityCode);
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
            && Objects.equals(vanityCode, data.vanityCode)
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
            description, region, vanityCode,
            maxMembers, maxPresences, boostCount,
            boostTier, notificationLevel, explicitContentLevel,
            mfaLevel, verificationLevel, afkTimeout,
            systemChannelId, afkChannelId
        );
    }

    @Override
    public void setIconId(String id)
    {
        this.iconId = id;
    }

    @Override
    public void setSplashId(String id)
    {
        this.splashId = id;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public void setBannerId(String id)
    {
        this.bannerId = id;
    }

    @Override
    public void setBoostTier(@Nonnull Guild.BoostTier tier)
    {
        this.boostTier = tier;
    }

    @Override
    public void setBoostCount(int count)
    {
        this.boostCount = count;
    }

    @Override
    public void setMaxMembers(int members)
    {
        this.maxMembers = members;
    }

    @Override
    public void setMaxPresences(int presences)
    {
        this.maxPresences = presences;
    }

    @Override
    public void setAfkChannelId(long id)
    {
        this.afkChannelId = id;
    }

    @Override
    public void setSystemChannelId(long id)
    {
        this.systemChannelId = id;
    }

    @Override
    public void setAfkTimeout(@Nonnull Guild.Timeout timeout)
    {
        this.afkTimeout = timeout;
    }

    @Override
    public void setVerificationLevel(@Nonnull Guild.VerificationLevel level)
    {
        this.verificationLevel = level;
    }

    @Override
    public void setNotificationLevel(@Nonnull Guild.NotificationLevel level)
    {
        this.notificationLevel = level;
    }

    @Override
    public void setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level)
    {
        this.explicitContentLevel = level;
    }

    @Override
    public void setMFALevel(@Nonnull Guild.MFALevel level)
    {
        this.mfaLevel = level;
    }

    @Override
    public void setRegion(@Nonnull String region)
    {
        this.region = region;
    }

    @Override
    public void setVanityCode(@Nullable String code)
    {
        this.vanityCode = code;
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

    @Nonnull
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

    @Nonnull
    @Override
    public Guild.Timeout getAfkTimeout()
    {
        return afkTimeout;
    }

    @Nonnull
    @Override
    public Guild.VerificationLevel getVerificationLevel()
    {
        return verificationLevel;
    }

    @Nonnull
    @Override
    public Guild.NotificationLevel getNotificationLevel()
    {
        return notificationLevel;
    }

    @Nonnull
    @Override
    public Guild.MFALevel getMFALevel()
    {
        return mfaLevel;
    }

    @Nonnull
    @Override
    public Guild.ExplicitContentLevel getExplicitContentLevel()
    {
        return explicitContentLevel;
    }

    @Nonnull
    @Override
    public String getRegion()
    {
        return region;
    }

    @Nullable
    @Override
    public String getVanityCode()
    {
        return vanityCode;
    }
}
