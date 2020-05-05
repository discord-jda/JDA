/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.utils.config.sharding;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.annotation.Nonnull;

public class ShardingConfig
{
    private int shardsTotal;
    private int intents;
    private MemberCachePolicy memberCachePolicy;
    private final boolean useShutdownNow;

    public ShardingConfig(int shardsTotal, boolean useShutdownNow, int intents, MemberCachePolicy memberCachePolicy)
    {
        this.shardsTotal = shardsTotal;
        this.useShutdownNow = useShutdownNow;
        this.intents = intents;
        this.memberCachePolicy = memberCachePolicy;
    }

    public void setShardsTotal(int shardsTotal)
    {
        this.shardsTotal = shardsTotal;
    }

    public int getShardsTotal()
    {
        return shardsTotal;
    }

    public int getIntents()
    {
        return intents;
    }

    public MemberCachePolicy getMemberCachePolicy()
    {
        return memberCachePolicy;
    }

    public boolean isUseShutdownNow()
    {
        return useShutdownNow;
    }

    @Nonnull
    public static ShardingConfig getDefault()
    {
        return new ShardingConfig(1, false, GatewayIntent.ALL_INTENTS, MemberCachePolicy.ALL);
    }
}
