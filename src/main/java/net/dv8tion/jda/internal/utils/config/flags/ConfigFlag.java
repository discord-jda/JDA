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

package net.dv8tion.jda.internal.utils.config.flags;

import java.util.EnumSet;

public enum ConfigFlag
{
    RAW_EVENTS,
    USE_RELATIVE_RATELIMIT(true),
    RETRY_TIMEOUT(true),
    BULK_DELETE_SPLIT(true),
    SHUTDOWN_HOOK(true),
    MDC_CONTEXT(true),
    AUTO_RECONNECT(true);

    private final boolean isDefault;

    ConfigFlag()
    {
        this(false);
    }

    ConfigFlag(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    public static EnumSet<ConfigFlag> getDefault()
    {
        EnumSet<ConfigFlag> set = EnumSet.noneOf(ConfigFlag.class);
        for (ConfigFlag flag : values())
        {
            if (flag.isDefault)
                set.add(flag);
        }
        return set;
    }
}
