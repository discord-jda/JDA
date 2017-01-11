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

package net.dv8tion.jda.client.entities;

import java.util.Collection;
import java.util.List;
import net.dv8tion.jda.client.managers.ApplicationManager;
import net.dv8tion.jda.client.managers.ApplicationManagerUpdatable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.requests.RestAction;

/** 
 * Represents a Discord Application from it's owning client point of view
 * 
 * @since  JDA 3.0
 * @author Aljoscha Grebe
 * 
 * @see <a href="https://discordapp.com/developers/applications/me">Discord Documentation - My Apps</a>
 * @see {@link net.dv8tion.jda.client.JDAClient#getApplications() JDAClient#getApplications()}
 * @see {@link net.dv8tion.jda.client.JDAClient#getApplicationById(String) JDAClient#getApplicationById(String)}
 */
public interface Application extends ISnowflake
{

    /**
     * Creates a new Bot for this Application.
     * <b>This cannot be undone!</b>
     * A new Bot will only be created if no bot-account is already assigned, otherwise the existing one is returned.
     * A newly created Bot-account will have its name set to the name of the Application.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link Application.Bot}
     *         <br>The created bot account of this application. 
     */
    RestAction<Application.Bot> createBot();

    /**
     * Deletes this Application and its assigned Bot (if present).
     * <b>This cannot be undone!</b>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link Void}
     *         <br>The RestAction to delete this Application.
     */
    RestAction<Void> delete();

    /**
     * Whether the bot requires code grant to invite or not. 
     * If your application requires multiple scopes then you may need the full OAuth2 flow to ensure a
     * bot doesn't join before your application is granted a token.
     * <br>
     * <br><b>TODO</b>:  Wait for <a href="https://github.com/hammerandchisel/discord-api-docs/issues/204">Discord
     * Documentation Github - Missing fields when viewing a bot's own application #204</a> as this might be moved into the bot object
     * 
     * @return whether the bot requires code grant
     */
    boolean doesBotRequireCodeGrant();

    /**
     * Returns the Bot assigned to this Application
     * 
     * @return The {@link Application.Bot} assigned to this application, or null if no bot is assigned
     */
    Application.Bot getBot();

    /**
     * Returns the description of the application.
     * 
     * @return The description of the application or an empty {@link String} if no description is defined.
     */
    String getDescription();

    /**
     * Returns the flags for this application. It's used for whitelisted apps.
     * 
     * @return The application flags.
     */
    int getFlags();

    /**
     * Returns the icon id of the application.
     * 
     * @return The iconId of the application or null if no icon is defined.
     */
    String getIconId();

    /**
     * Returns the icon-url of the application.
     * 
     * @return The icon-url of the application or null if no icon is defined.
     */
    String getIconUrl();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Application
     * (the one owning this application).
     * 
     * @return The JDA instance of this Application.
     */
    JDA getJDA();

    ApplicationManager getManager();

    ApplicationManagerUpdatable getManagerUpdatable();


    /**
     * Returns the name of the application.
     * 
     * @return The name of the application.
     */
    String getName();

    List<String> getRedirectUris();

    int getRpcApplicationState();

    /**
     * Returns a {@link java.util.List List} of {@link String Strings} containing the RPC origins of the application.
     * 
     * @return The RPC origins of the application, possibly empty.
     */
    List<String> getRpcOrigins();

    /**
     * Returns the Application secret (Used for oAuth)
     * @return
     *      The Application secret
     */
    String getSecret();

    /**
     * Returns whether or not this Application has a bot-account assigned
     * @return
     *      True if this Application has a bot-account assigned, false otherwise
     */
    boolean hasBot();

    /**
     * Whether the bot is public or not. 
     * Public bots can be added by anyone. When false only the owner can invite the bot to servers.
     * <br>
     * <br><b>TODO</b>:  Wait for <a href="https://github.com/hammerandchisel/discord-api-docs/issues/204">Discord
     * Documentation Github - Missing fields when viewing a bot's own application #204</a> as this might be moved into the bot object
     * 
     * @return Whether the bot is public
     */
    boolean isBotPublic();

    /**
     * Generates a new client secret for this Application. This invalidates the old one.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link Application}
     *         <br>This application with the updated secret.
     */
    RestAction<Application> resetSecret();

    /**
     * Represents a Bot assigned to an Application
     * To change its Username, login to JDA and use the {@link net.dv8tion.jda.core.managers.AccountManager AccountManager}.
     */
    interface Bot extends ISnowflake
    {

        /**
         * Returns the Application of this Bot
         * @return
         *      The application of this Bot
         */
        Application getApplication();

        /**
         * Returns the avatar id of this Bot
         * @return
         *      The avatar id of this Bot or null, if no avatar is set
         */
        String getAvatarId();

        /**
         * Returns the avatar-url of this Bot
         * @return
         *      The avatar-url of this Bot or null, if no avatar is set
         */
        String getAvatarUrl();

        /**
         * Returns the discriminator of this Bot
         * @return
         *      The discriminator of this Bot
         */
        String getDiscriminator();

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
         * Returns the name of this Bot
         * @return
         *      The name of this Bot
         */
        String getName();

        /**
         * Returns the token used to login to JDA with this Bot
         * @return
         *      The login-token of this Bot
         */
        String getToken();

        /**
         * Generates a new token for this bot. This invalidates the old one.
         *
         * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
         *         Type: {@link Application.Bot}
         *         <br>This bot with the updated token.
         */
        RestAction<Application.Bot> resetToken();
    }
}
