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

import net.dv8tion.jda.api.entities.bean.MutableGuildData;

import java.util.function.LongFunction;

public class DataProviderConfig
{
    private static final DataProviderConfig DEFAULT = new DataProviderConfig();

    private LongFunction<? extends MutableGuildData> guildProvider = MutableGuildData.RICH_PROVIDER;

    public void setGuildProvider(LongFunction<? extends MutableGuildData> provider)
    {
        this.guildProvider = provider;
    }

    public MutableGuildData provideGuildData(long guildId)
    {
        return guildProvider.apply(guildId);
    }

    public static DataProviderConfig getDefault()
    {
        return DEFAULT;
    }
}
