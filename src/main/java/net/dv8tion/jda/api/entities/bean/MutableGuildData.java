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

package net.dv8tion.jda.api.entities.bean;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.bean.light.LightGuildData;
import net.dv8tion.jda.api.entities.bean.rich.RichGuildData;
import net.dv8tion.jda.api.utils.DataProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MutableGuildData extends GuildData
{
    DataProvider<MutableGuildData> LIGHT_PROVIDER = (id, flags) -> LightGuildData.SINGLETON;
    DataProvider<MutableGuildData> RICH_PROVIDER = (id, flags) -> new RichGuildData();

    // Returning old value, this can be useful for update events!
    @Nullable
    String setIconId(@Nullable String id);
    @Nullable
    String setSplashId(@Nullable String id);
    @Nullable
    String setDescription(@Nullable String description);
    @Nullable
    String setBannerId(@Nullable String id);
    @Nonnull
    Guild.BoostTier setBoostTier(@Nonnull Guild.BoostTier tier);
    int setBoostCount(int count);
    int setMaxMembers(int members);
    int setMaxPresences(int presences);
    long setAfkChannelId(long id);
    long setSystemChannelId(long id);
    @Nonnull
    Guild.Timeout setAfkTimeout(@Nonnull Guild.Timeout timeout);
    @Nonnull
    Guild.VerificationLevel setVerificationLevel(@Nonnull Guild.VerificationLevel level);
    @Nonnull
    Guild.NotificationLevel setNotificationLevel(@Nonnull Guild.NotificationLevel level);
    @Nonnull
    Guild.ExplicitContentLevel setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level);
    @Nonnull
    Guild.MFALevel setMFALevel(@Nonnull Guild.MFALevel level);
    @Nonnull
    String setRegion(@Nonnull String region);
}
