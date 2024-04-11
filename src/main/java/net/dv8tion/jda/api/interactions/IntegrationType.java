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

package net.dv8tion.jda.api.interactions;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents how an app was installed, or where a command can be used.
 */
public enum IntegrationType
{
    UNKNOWN("-1"),
    /**
     * Indicates commands can be installed on the guild the application was invited in
     */
    GUILD_INSTALL("0"),
    /**
     * Indicates commands can be installed on the user inviting the application
     */
    USER_INSTALL("1");

    /**
     * Contains all integration types
     */
    public static final Set<IntegrationType> ALL = Collections.unmodifiableSet(EnumSet.of(GUILD_INSTALL, USER_INSTALL));

    private final String key;

    IntegrationType(String key) {
        this.key = key;
    }

    /**
     * The raw value of this integration type.
     *
     * @return The raw value
     */
    @Nonnull
    public String getType()
    {
        return key;
    }

    /**
     * Gets the integration type corresponding to the key,
     * returns {@link #UNKNOWN} if no entry matches.
     *
     * @param  key
     *         The key to match against
     *
     * @return The integration type corresponding to the key
     */
    @Nonnull
    public static IntegrationType fromKey(@Nonnull String key)
    {
        for (IntegrationType value : values())
        {
            if (value.key.equals(key))
                return value;
        }
        return UNKNOWN;
    }
}
