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

package net.dv8tion.jda.api.entities.data;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.data.light.LightGuildData;
import net.dv8tion.jda.api.entities.data.provider.SnowflakeDataProvider;
import net.dv8tion.jda.api.entities.data.rich.RichGuildData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MutableGuildData extends GuildData
{
    SnowflakeDataProvider<MutableGuildData> LIGHT_PROVIDER = (id, flags) -> LightGuildData.SINGLETON;
    SnowflakeDataProvider<MutableGuildData> RICH_PROVIDER = (id, flags) -> new RichGuildData();

    void setIconId(@Nullable String id);
    void setSplashId(@Nullable String id);
    void setDescription(@Nullable String description);
    void setBannerId(@Nullable String id);
    void setBoostTier(@Nonnull Guild.BoostTier tier);
    void setBoostCount(int count);
    void setMaxMembers(int members);
    void setMaxPresences(int presences);
    void setAfkChannelId(long id);
    void setSystemChannelId(long id);
    void setAfkTimeout(@Nonnull Guild.Timeout timeout);
    void setVerificationLevel(@Nonnull Guild.VerificationLevel level);
    void setNotificationLevel(@Nonnull Guild.NotificationLevel level);
    void setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level);
    void setMFALevel(@Nonnull Guild.MFALevel level);
    void setRegion(@Nonnull String region);
    void setVanityCode(@Nullable String code);
}
