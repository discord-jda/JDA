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

import net.dv8tion.jda.client.managers.ApplicationManager;
import net.dv8tion.jda.client.managers.ApplicationManagerUpdatable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.Collection;
import java.util.List;

/**
 * Represents a Application
 *
 * @see
 */
public interface Application extends ISnowflake
{

    /**
     * Creates a new Bot for this Application.
     * This is only possible, if no bot-account is already assigned.
     * The created Bot-account will have its name set to the name of the Application
     *
     * @return
     *      The created Bot
     */
    RestAction<Bot> createBot();

    /**
     * Deletes this Application <b>and its assigned Bot (if present)</b>.
     */
    RestAction<Void> delete();

    boolean doesBotRequireCodeGrant();

    /**
     * Returns the Bot assigned to this Application
     * @return
     *      The Bot assigned to this application, or null if no bot is assigned
     */
    Application.Bot getBot();

    /**
     * Returns the description of this Application
     * @return
     *      The description of this Application
     */
    String getDescription();

    // TODO find out what this is used for
    int getFlags();

    /**
     * Returns the iconId of this Application
     * @return
     *      The iconId of this Application or null, if no icon is defined
     */
    String getIconId();

    /**
     * Returns the icon-url of this Application
     * @return
     *      The icon-url of this Application or null, if no icon is defined
     */
    String getIconUrl();

    JDA getJDA();

    ApplicationManager getManager();

    ApplicationManagerUpdatable getManagerUpdatable();

    /**
     * Returns the name of this Application
     * @return
     *      The name of this Application
     */
    String getName();

    List<String> getRedirectUris();

    int getRpcApplicationState();

    /**
     * Returns a {@link java.util.List List<}{@link String String}{@link java.util.List >} of RPC origins of the Application
     * @return
     *      The name of the bot's Application
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

    boolean isBotPublic();

    RestAction<Application> resetSecret();

    /**
     * Represents a Bot assigned to an Application
     * To change its Username, login to JDA and use the {@link net.dv8tion.jda.managers.AccountManager AccountManager}.
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
         * Returns the avatarId of this Bot
         * @return
         *      The avatarId of this Bot or null, if no avatar is set
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

        RestAction<Application.Bot> resetToken();
    }
}