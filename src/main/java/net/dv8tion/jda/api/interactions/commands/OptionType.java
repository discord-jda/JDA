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

package net.dv8tion.jda.api.interactions.commands;

import javax.annotation.Nonnull;

public enum OptionType
{
    UNKNOWN(-1), SUB_COMMAND(1), SUB_COMMAND_GROUP(2), STRING(3, true), INTEGER(4, true), BOOLEAN(5), USER(6), CHANNEL(7), ROLE(8);
    private final int raw;
    private final boolean supportsChoices;

    OptionType(int raw)
    {
        this(raw, false);
    }

    OptionType(int raw, boolean supportsChoices)
    {
        this.raw = raw;
        this.supportsChoices = supportsChoices;
    }

    public int getKey()
    {
        return raw;
    }

    public boolean canSupportChoices()
    {
        return supportsChoices;
    }

    @Nonnull
    public static OptionType fromKey(int key)
    {
        for (OptionType type : values())
        {
            if (type.raw == key)
                return type;
        }
        return UNKNOWN;
    }
}
