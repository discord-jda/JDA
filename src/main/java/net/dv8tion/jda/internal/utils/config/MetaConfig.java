/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetaConfig
{
    private final ConcurrentMap<String, String> mdcContextMap;
    private final EnumSet<CacheFlag> cacheFlags;
    private final boolean enableMDC;
    private final boolean useShutdownHook;

    public MetaConfig(
            ConcurrentMap<String, String> mdcContextMap,
            EnumSet<CacheFlag> cacheFlags, boolean enableMDC, boolean useShutdownHook)
    {
        this.cacheFlags = cacheFlags;
        this.enableMDC = enableMDC;
        if (enableMDC)
            this.mdcContextMap = mdcContextMap == null ? new ConcurrentHashMap<>() : null;
        else
            this.mdcContextMap = null;
        this.useShutdownHook = useShutdownHook;
    }

    public ConcurrentMap<String, String> getMdcContextMap()
    {
        return mdcContextMap;
    }

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
}
