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

import java.util.Collection;
import java.util.List;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.User;

/**
 * Represents a Discord Application from it's bot's point of view.
 * 
 * @since  JDA 3.0
 * @author Aljoscha Grebe
 * 
 * @see {@link net.dv8tion.jda.bot.JDABot#getApplicationInfo() JDABot#getApplicationInfo()}
 */
public interface ApplicationInfo extends ISnowflake
{

    /**
     * Returns the description of the bot's application.
     * 
     * @return The description of the bot's application or an empty {@link String} if no description is defined.
     */
    String getDescription();

    /**
     * Returns the icon id of the bot's application.
     * 
     * @return The icon id of the bot's application or null if no icon is defined.
     */
    String getIconId();

    /**
     * Returns the icon-url of the bot's application.
     * 
     * @return The icon-url of the bot's application or null if no icon is defined.
     */
    String getIconUrl();

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *  
     * @param  permissions
     *         Possibly empty {@link java.util.List List} of {@link net.dv8tion.jda.core.Permission Permissions}
     *         that should be requested via invite.
     * 
     * @return The link used to invite the bot.
     */
    String getInviteUrl(Collection<Permission> permissions);

    /**
     * Creates a OAuth invite-link used to invite the bot.
     * 
     * @param  permissions
     *         Possibly empty array of {@link net.dv8tion.jda.core.Permission Permissions}
     *         that should be requested via invite.
     * 
     * @return The link used to invite the bot.
     */
    String getInviteUrl(Permission... permissions);

    /**
     * Creates a OAuth invite-link used to invite the bot.
     * 
     * @param  guildId
     *         The id of the pre-selected guild.
     * @param  permissions
     *         Possibly empty {@link java.util.List List} of {@link net.dv8tion.jda.core.Permission Permissions} 
     *         that should be requested via invite.
     * 
     * @return The link used to invite the bot.
     */
    String getInviteUrl(String guildId, Collection<Permission> permissions);

    /**
     * Creates a OAuth invite-link used to invite the bot. 
     * 
     * @param  guildId 
     *         The id of the pre-selected guild.
     * @param  permissions 
     *         Possibly empty array of {@link net.dv8tion.jda.core.Permission Permissions} 
     *         that should be requested via invite.
     * 
     * @return The link used to invite the bot.
     */
    String getInviteUrl(String guildId, Permission... permissions);

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this ApplicationInfo
     * (the one logged into this application's bot account).
     * 
     * @return The JDA instance of this ApplicationInfo.
     */
    JDA getJDA();

    /**
     * Returns the name of the bot's application.
     * 
     * @return The name of the bot's application.
     */
    String getName();

    /**
     * Returns the owner of the bot's application.
     * 
     * @return The owner of the bot's application.
     */
    User getOwner();

    /**
     * Returns a {@link java.util.List List} of {@link String Strings} containing the RPC origins of the bot's application.
     * 
     * @return The RPC origins of the bot's application, possibly empty.
     */
    List<String> getRpcOrigins();
}
