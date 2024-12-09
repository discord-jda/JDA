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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nullable;

/**
 * Includes details about the authorizing user or guild for the installation(s) relevant to the interaction.
 * For apps installed to a user, it can be used to tell the difference between the authorizing user
 * and the user that triggered an interaction (like a message component).
 *
 * @see <a href="https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-authorizing-integration-owners-object" target="_blank">Discord Docs about Authorizing Integration Owners Object</a>
 */
public interface IntegrationOwners
{

    /**
     * Whether this interaction was first authorized by a command with the {@link IntegrationType#USER_INSTALL USER_INSTALL} integration type.
     *
     * <p>You can retrieve the authorizing user with {@link #getAuthorizingUserIdLong()}.
     *
     * @return {@code true} if this interaction started from a user-installable command.
     */
    default boolean isUserIntegration()
    {
        return getAuthorizingUserIdLong() != 0;
    }

    /**
     * When the interaction has the {@link IntegrationType#USER_INSTALL USER_INSTALL} integration type,
     * returns the {@link UserSnowflake} which first authorized this interaction,
     * or {@code 0} otherwise.
     *
     * @return the {@link UserSnowflake} which triggered the interaction,
     *         or {@code 0} for non-user-installable commands
     */
    long getAuthorizingUserIdLong();

    /**
     * When the interaction has the {@link IntegrationType#USER_INSTALL USER_INSTALL} integration type,
     * returns the {@link UserSnowflake} which first authorized this interaction,
     * or {@code null} otherwise.
     *
     * @return the {@link UserSnowflake} which triggered the interaction,
     *         or {@code null} for non-user-installable commands
     */
    @Nullable
    default String getAuthorizingUserId()
    {
        if (getAuthorizingUserIdLong() == 0) return null;
        return Long.toUnsignedString(getAuthorizingUserIdLong());
    }

    /**
     * Whether this interaction was first authorized by a command with the {@link IntegrationType#GUILD_INSTALL} integration type.
     * <br>This includes guild commands and bot DMs commands.
     *
     * <p>You can retrieve the authorizing guild with {@link Interaction#getGuild()}.
     *
     * @return {@code true} if this interaction started from a guild-installable command.
     */
    default boolean isGuildIntegration()
    {
        return getAuthorizingGuildIdLong() != null;
    }

    /**
     * When the interaction has the {@link IntegrationType#GUILD_INSTALL GUILD_INSTALL} integration type,
     * returns the {@link Guild} ID which first authorized this interaction,
     * or {@code 0} if the interaction is used in the app's bot DMs,
     * returns {@code null} otherwise.
     *
     * @return the guild ID in which the interaction is triggered in, or {@code 0} for Bot DMs,
     *         or {@code null} for non-guild-installable commands
     */
    @Nullable
    Long getAuthorizingGuildIdLong();

    /**
     * When the interaction has the {@link IntegrationType#GUILD_INSTALL GUILD_INSTALL} integration type,
     * returns the {@link Guild} ID which first authorized this interaction,
     * or {@code 0} if the interaction is used in the app's bot DMs,
     * returns {@code null} otherwise.
     *
     * @return the guild ID in which the interaction is triggered in, or {@code 0} for Bot DMs,
     *         or {@code null} for non-guild-installable commands
     */
    @Nullable
    default String getAuthorizingGuildId()
    {
        if (getAuthorizingGuildIdLong() == null) return null;
        return Long.toUnsignedString(getAuthorizingGuildIdLong());
    }
}
