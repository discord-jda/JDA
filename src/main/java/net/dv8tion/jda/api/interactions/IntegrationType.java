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

import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Represents how an app was installed, or where a command can be used.
 *
 * @see <a target="_blank" href="https://discord.com/developers/docs/interactions/application-commands#installation-context">Discord docs</a>
 */
public enum IntegrationType
{
    UNKNOWN("-1"),
    /**
     * Allows commands to be added to a guild by a {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER server manager},
     * all users who are in the guild can access the commands and the app's DMs,
     * assuming the app was invited with the {@code bot} scope.
     */
    GUILD_INSTALL("0"),
    /**
     * Allows commands to be added to a user after the app has been connected to their account.
     * <br>Users can use the commands according to the {@link InteractionContextType InteractionContextTypes} set on each command,
     * and can also access the app's DMs, assuming the app has a bot.
     *
     * <p><b>Requirements</b>
     * <br>This requires the bot to be user-installable,
     * see on <a target="_blank" href="https://discord.com/developers/applications">Your dashboard</a>,
     * in the {@code Installation} section, and select the {@code User Install} authorization method.
     */
    USER_INSTALL("1");

    /**
     * Contains all integration types.
     */
    public static final Set<IntegrationType> ALL = Helpers.unmodifiableEnumSet(GUILD_INSTALL, USER_INSTALL);

    private final String key;

    IntegrationType(String key)
    {
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
