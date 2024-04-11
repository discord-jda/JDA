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
     * Returns the ID of the authorizing user.
     * <br>This is not the ID of the interaction caller,
     * but rather the ID of the user which started the original interaction.
     *
     * <p>This is only available if the app was installed on the user.
     *
     * @return the ID of the authorizing user, or {@code null}
     */
    @Nullable
    UserSnowflake getUserIntegration();

    /**
     * In some conditions, returns the guild ID if the interaction is triggered from a guild,
     * or {@code 0} if the interaction is triggered from a DM with the app's bot user.
     *
     * <p>In a guild and in the app's DMs, this is only available if:
     * <ul>
     *     <li>The command's integration contexts contains {@link IntegrationType#GUILD_INSTALL} and {@link IntegrationType#USER_INSTALL}</li>
     *     <li>The interaction comes from a component</li>
     * </ul>
     *
     * @return the guild ID in which the interaction is triggered in, or {@code 0}, or {@code null}
     */
    @Nullable
    Long getGuildIntegration();
}
