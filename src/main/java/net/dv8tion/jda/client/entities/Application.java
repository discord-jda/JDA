/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.client.managers.ApplicationManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.List;

/** 
 * Represents a Discord Application from its owning client point of view
 * 
 * @since  3.0
 * @author Aljoscha Grebe
 * 
 * @see    <a href="https://discordapp.com/developers/applications/me">Discord Documentation - My Apps</a>
 * @see    net.dv8tion.jda.client.JDAClient#getApplications() JDAClient.getApplications()
 * @see    net.dv8tion.jda.client.JDAClient#getApplicationById(String) JDAClient.getApplicationById(String)
 */
public interface Application extends ISnowflake
{

    /**
     * Creates a new Bot for this Application.
     * <b>This cannot be undone!</b>
     * A new Bot will only be created if no bot-account is already assigned, otherwise the existing one is returned.
     * A newly created Bot-account will have its name set to the name of the Application.
     *
     * <p><b>Warning!</b> This endpoint has a really long ratelimit (multiple hours)!  
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_APPLICATION UNKNOWN_APPLICATION}
     *      <br>If the Application has been deleted</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link Application.Bot}
     *         <br>The created bot account of this application. 
     */
    @CheckReturnValue
    RestAction<Application.Bot> createBot();

    /**
     * Deletes this Application and its assigned Bot (if present).
     * <b>This cannot be undone!</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_APPLICATION UNKNOWN_APPLICATION}
     *      <br>If the Application has already been deleted</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link Void}
     *         <br>The RestAction to delete this Application.
     */
    @CheckReturnValue
    RestAction<Void> delete();

    /**
     * Whether the bot requires code grant to invite or not.
     * 
     * <p>This means that additional OAuth2 steps are required to authorize the application to make a bot join a guild 
     * like {@code &response_type=code} together with a valid {@code &redirect_uri}. 
     * <br>For more information look at the <a href="https://discordapp.com/developers/docs/topics/oauth2">Discord OAuth2 documentation</a>.  
     * 
     * @return Whether the bot requires code grant
     */
    boolean doesBotRequireCodeGrant();

    /**
     * The Bot assigned to this Application
     * 
     * @return The {@link Application.Bot} assigned to this application, or {@code null} if no bot is assigned
     */
    Application.Bot getBot();

    /**
     * The description of the application.
     * 
     * @return The description of the application or an empty {@link String} if no description is defined
     */
    String getDescription();

    /**
     * The flags for this application. These are used for whitelisted apps.
     * 
     * @return The application flags
     */
    int getFlags();

    /**
     * The icon id of the application.
     * <br>The application icon is <b>not</b> necessarily the same as the bot's avatar!
     * 
     * @return The iconId of the application or {@code null} if no icon is defined
     */
    String getIconId();

    /**
     * The icon-url of the application.
     * <br>The application icon is <b>not</b> necessarily the same as the bot's avatar!
     * 
     * @return The icon-url of the application or {@code null} if no icon is defined
     */
    String getIconUrl();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Application
     * (the one owning this application).
     * 
     * @return The JDA instance of this Application
     */
    JDA getJDA();

    /**
     * Returns the {@link net.dv8tion.jda.client.managers.ApplicationManager ApplicationManager} for this Application.
     * <br>In the ApplicationManager, you can modify things like the name and icon of this Application.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction.queue()}.
     *
     * @return The ApplicationManager of this Channel
     */
    ApplicationManager getManager();

    /**
     * The name of this application.
     * <br>The application name is <b>not</b> necessarily the same as the bot's name!
     * 
     * @return The name of this application
     */
    String getName();

    /**
     * The redirect uris of this application.
     * 
     * @return A {@link java.util.List List} of current redirect uris of the application
     */
    List<String> getRedirectUris();

    /**
     * The rpc application state of this application.
     * 
     * @return The rpc application state of current redirect uris of the application
     */
    int getRpcApplicationState();

    /**
     * Returns the Application secret (Used for oAuth)
     * 
     * @return The Application secret
     */
    String getSecret();

    /**
     * Returns whether or not this Application has a bot-account assigned
     * 
     * @return True, if this Application has a bot-account assigned, false otherwise
     */
    boolean hasBot();

    /**
     * Whether the bot is public or not. 
     * Public bots can be added by anyone. When false only the owner can invite the bot to servers.
     * 
     * @return Whether the bot is public
     */
    boolean isBotPublic();

    /**
     * Generates a new client secret for this Application. This invalidates the old one.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_APPLICATION UNKNOWN_APPLICATION}
     *      <br>If the Application has been deleted</li>
     * </ul>
     * 
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link Application}
     *         <br>This application with the updated secret.
     */
    @CheckReturnValue
    RestAction<Application> resetSecret();

    /**
     * Represents a Bot assigned to an Application
     * To change its Username, login to JDA and use the {@link net.dv8tion.jda.core.managers.AccountManager AccountManager}.
     */
    interface Bot extends ISnowflake
    {

        /**
         * The Application for this Bot
         * 
         * @return The application for this Bot
         */
        Application getApplication();

        /**
         * The avatar id of this Bot
         * 
         * @return The avatar id of this Bot or {@code null}, if no avatar is set
         */
        String getAvatarId();

        /**
         * The avatar-url of this Bot
         * 
         * @return The avatar-url of this Bot or {@code null}, if no avatar is set
         */
        String getAvatarUrl();

        /**
         * The discriminator of this Bot
         * 
         * @return The discriminator of this Bot
         */
        String getDiscriminator();

        /**
         * Creates a OAuth invite-link used to invite the bot.
         * 
         * <p>The link is provided in the following format:
         * <br>{@code https://discordapp.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS}
         * <br>Unnecessary query parameters are stripped.
         *  
         * @param  permissions
         *         Possibly empty {@link java.util.List List} of {@link net.dv8tion.jda.core.Permission Permissions}
         *         that should be requested via invite.
         * 
         * @return The link used to invite the bot
         */
        String getInviteUrl(Collection<Permission> permissions);

        /**
         * Creates a OAuth invite-link used to invite the bot.
         * 
         * <p>The link is provided in the following format:
         * <br>{@code https://discordapp.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS}
         * <br>Unnecessary query parameters are stripped.
         * 
         * @param  permissions
         *         Possibly empty array of {@link net.dv8tion.jda.core.Permission Permissions}
         *         that should be requested via invite.
         * 
         * @return The link used to invite the bot
         */
        String getInviteUrl(Permission... permissions);

        /**
         * Creates a OAuth invite-link used to invite the bot.
         * 
         * <p>The link is provided in the following format:
         * <br>{@code https://discordapp.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID}
         * <br>Unnecessary query parameters are stripped.
         * 
         * @param  guildId
         *         The id of the pre-selected guild.
         * @param  permissions
         *         Possibly empty {@link java.util.List List} of {@link net.dv8tion.jda.core.Permission Permissions} 
         *         that should be requested via invite.
         * 
         * @return The link used to invite the bot
         */
        String getInviteUrl(String guildId, Collection<Permission> permissions);

        /**
         * Creates a OAuth invite-link used to invite the bot.
         * 
         * <p>The link is provided in the following format:
         * <br>{@code https://discordapp.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID}
         * <br>Unnecessary query parameters are stripped. 
         * 
         * @param  guildId 
         *         The id of the pre-selected guild.
         * @param  permissions 
         *         Possibly empty array of {@link net.dv8tion.jda.core.Permission Permissions} 
         *         that should be requested via invite.
         * 
         * @return The link used to invite the bot
         */
        String getInviteUrl(String guildId, Permission... permissions);

        /**
         * The name of this Bot
         * 
         * @return The name of this Bot
         */
        String getName();

        /**
         * The token used to login to JDA with this Bot
         * <br>This can be used in {@link net.dv8tion.jda.core.JDABuilder#setToken(String) JDABuilder.setToken(String)}
         * 
         * @return The authentication token of this Bot
         */
        String getToken();

        /**
         * Generates a new token for this bot.
         * <br><b>This invalidates the old one!</b>
         *
         * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
         * update include the following:
         * <ul>
         *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#ONLY_BOTS_ALLOWED ONLY_BOTS_ALLOWED}
         *      <br>If the Bot doesn't exist</li>
         * </ul>
         *
         * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link Application.Bot}
         *         <br>This bot with the updated token.
         */
        @CheckReturnValue
        RestAction<Application.Bot> resetToken();
    }
}
