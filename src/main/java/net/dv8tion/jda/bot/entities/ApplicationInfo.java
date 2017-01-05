/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.bot.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.User;

import java.util.Collection;
import java.util.List;

public interface ApplicationInfo extends ISnowflake
{

    /**
     * Returns the description of the bot's application
     * @return
     *      The description of the bot's application
     */
    String getDescription();

    /**
     * Returns the iconId of the bot's application
     * @return
     *      The iconId of the bot's application or null, if no icon is defined
     */
    String getIconId();

    /**
     * Returns the icon-url of the bot's application
     * @return
     *      The icon-url of the bot's application or null, if no icon is defined
     */
    String getIconUrl();

    /**
     * Creates a OAuth invite-link used to invite bot-accounts.
     * This requires a JDA instance of a bot account for which it retrieves the parent application.
     *
     * @param permissions
     *      Possibly empty list of Permissions that should be requested via invite
     * @return
     *      The link used to invite the bot or null on failure
     */
    String getInviteUrl(String guildId, Collection<Permission> permissions);

    /**
     * Creates a OAuth invite-link used to invite bot-accounts.
     * This requires a JDA instance of a bot account for which it retrieves the parent application.
     *
     * @param permissions
     *      Possibly empty list of Permissions that should be requested via invite
     * @return
     *      The link used to invite the bot or null on failure
     */
    String getInviteUrl(String guildId, Permission... permissions);

    JDA getJDA();

    /**
     * Returns the name of the bot's application
     * @return
     *      The name of the bot's application
     */
    String getName();

    /**
     * Returns the owner of the bot's application.
     * @return
     *      The owner of the bot's application
     */
    User getOwner();

    /**
     * Returns a {@link List List<}{@link String}{@link List >} of RPC origins of the bot's application
     * @return
     *      The name of the bot's application
     */
    List<String> getRpcOrigins();
}
