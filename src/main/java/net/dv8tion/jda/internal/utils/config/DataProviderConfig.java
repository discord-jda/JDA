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

package net.dv8tion.jda.internal.utils.config;

import net.dv8tion.jda.api.entities.data.MutableGuildData;
import net.dv8tion.jda.api.entities.data.MutableMemberData;
import net.dv8tion.jda.api.utils.DataMode;
import net.dv8tion.jda.api.utils.DataProvider;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public class DataProviderConfig
{
    private static final DataProviderConfig DEFAULT = new DataProviderConfig();

    private DataMode mode = DataMode.RICH;
    private DataProvider<? extends MutableGuildData> guildProvider = MutableGuildData.RICH_PROVIDER;
    private DataProvider<? extends MutableMemberData> memberProvider = MutableMemberData.RICH_PROVIDER;

    public void setGuildProvider(DataProvider<? extends MutableGuildData> provider)
    {
        if (provider == null)
        {
            if (mode == DataMode.LIGHT)
                this.guildProvider = MutableGuildData.LIGHT_PROVIDER;
            else
                this.guildProvider = MutableGuildData.RICH_PROVIDER;
        }
        else
        {
            this.guildProvider = provider;
        }
    }

    public MutableGuildData provideGuildData(long guildId, EnumSet<CacheFlag> flags)
    {
        return guildProvider.provide(guildId, flags);
    }

    public void setMemberProvider(DataProvider<? extends MutableMemberData> provider)
    {
        if (provider == null)
        {
            if (mode == DataMode.LIGHT)
                this.memberProvider = MutableMemberData.LIGHT_PROVIDER;
            else
                this.memberProvider = MutableMemberData.RICH_PROVIDER;
        }
        else
        {
            this.memberProvider = provider;
        }
    }

    public MutableMemberData provideMemberData(long userId, EnumSet<CacheFlag> flag)
    {
        return memberProvider.provide(userId, flag);
    }

    public void setMode(DataMode mode)
    {
        this.mode = mode;
        switch (mode)
        {
        case RICH:
            guildProvider = MutableGuildData.RICH_PROVIDER;
            memberProvider = MutableMemberData.RICH_PROVIDER;
            break;
        case LIGHT:
            guildProvider = MutableGuildData.LIGHT_PROVIDER;
            memberProvider = MutableMemberData.LIGHT_PROVIDER;
        }
    }

    public static DataProviderConfig getDefault()
    {
        return DEFAULT;
    }
}
