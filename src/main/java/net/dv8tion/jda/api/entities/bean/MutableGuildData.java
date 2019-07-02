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

import java.util.function.LongFunction;

public interface MutableGuildData extends GuildData
{
    LongFunction<MutableGuildData> LIGHT_PROVIDER = (id) -> new LightGuildData();
    LongFunction<MutableGuildData> RICH_PROVIDER = (id) -> new RichGuildData();

    // Returning old value, this can be useful for update events!
    String setIconId(String id);
    String setSplashId(String id);
    String setDescription(String description);
    String setBannerId(String id);
    Guild.BoostTier setBoostTier(Guild.BoostTier tier);
    int setBoostCount(int count);
    int setMaxMembers(int members);
    int setMaxPresences(int presences);
    long setAfkChannelId(long id);
    long setSystemChannelId(long id);
    Guild.Timeout setAfkTimeout(Guild.Timeout timeout);
    Guild.VerificationLevel setVerificationLevel(Guild.VerificationLevel level);
    Guild.NotificationLevel setNotificationLevel(Guild.NotificationLevel level);
    Guild.ExplicitContentLevel setExplicitContentLevel(Guild.ExplicitContentLevel level);
    Guild.MFALevel setMFALevel(Guild.MFALevel level);
    String setRegion(String region);
}
