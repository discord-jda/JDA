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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ChunkingFilter
{
    ChunkingFilter ALL = (x) -> true;
    ChunkingFilter NONE = (x) -> false;

    boolean filter(long guildId);

    @Nonnull
    static ChunkingFilter include(@Nonnull long... ids)
    {
        Checks.notNull(ids, "ID array");
        return (guild) -> {
            for (long id : ids)
            {
                if (id == guild)
                    return true;
            }
            return false;
        };
    }

    @Nonnull
    static ChunkingFilter exclude(@Nonnull long... ids)
    {
        Checks.notNull(ids, "ID array");
        return (guild) -> {
            for (long id : ids)
            {
                if (id == guild)
                    return false;
            }
            return true;
        };
    }
}
