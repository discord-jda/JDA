/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.config.flags.ConfigFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetaConfig
{
    private static final MetaConfig defaultConfig = new MetaConfig(2048, null, EnumSet.allOf(CacheFlag.class), ConfigFlag.getDefault());
    private final ConcurrentMap<String, String> mdcContextMap;
    private final EnumSet<CacheFlag> cacheFlags;
    private final boolean enableMDC;
    private final boolean useShutdownHook;
    private final int maxBufferSize;

    public MetaConfig(
            int maxBufferSize,
            @Nullable ConcurrentMap<String, String> mdcContextMap,
            @Nullable EnumSet<CacheFlag> cacheFlags, EnumSet<ConfigFlag> flags)
    {
        this.maxBufferSize = maxBufferSize;
        this.cacheFlags = cacheFlags == null ? EnumSet.allOf(CacheFlag.class) : cacheFlags;
        this.enableMDC = flags.contains(ConfigFlag.MDC_CONTEXT);
        if (enableMDC)
            this.mdcContextMap = mdcContextMap == null ? new ConcurrentHashMap<>() : mdcContextMap;
        else
            this.mdcContextMap = null;
        this.useShutdownHook = flags.contains(ConfigFlag.SHUTDOWN_HOOK);
    }

    @Nullable
    public ConcurrentMap<String, String> getMdcContextMap()
    {
        return mdcContextMap;
    }

    @Nonnull
    public EnumSet<CacheFlag> getCacheFlags()
    {
        return cacheFlags;
    }

    public boolean isEnableMDC()
    {
        return enableMDC;
    }

    public boolean isUseShutdownHook()
    {
        return useShutdownHook;
    }

    public int getMaxBufferSize()
    {
        return maxBufferSize;
    }

    @Nonnull
    public static MetaConfig getDefault()
    {
        return defaultConfig;
    }
}
