/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import java.util.List;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Represents a Discord Application from the point of view of a client having authorized it.
 * <br>This is an applications that does not belong to you, but rather one that you have authorized to your account. 
 * 
 * @since  3.0
 * @author Aljoscha Grebe
 * 
 * @see <a href="https://discordapp.com/developers/applications/authorized">Discord Documentation - Authorized Apps</a>
 * @see {@link net.dv8tion.jda.client.JDAClient#getAuthorizedApplications() JDAClient#getAuthorizedApplications()}
 * @see {@link net.dv8tion.jda.client.JDAClient#getAuthorizedApplicationById(String) JDAClient#getAuthorizedApplicationById(String)}
 */
public interface AuthorizedApplication extends ISnowflake
{

    /**
     * Removes the authorization from this application.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_TOKEN UNKNOWN_TOKEN}
     *     <br>The Application isn't authorized by this user account anymore.</li>
     * </ul>
     * 
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link Void}
     *         <br>The RestAction to delete this authorisation.
     */
    RestAction<Void> delete();

    /**
     * The authorization id for this application.
     * <br>This id is only used to {@link #delete() remove the authorization}.
     * 
     * @return The authorization id. 
     */
    String getAuthId();

    /**
     * Returns the description of the application.
     * 
     * @return The description of the application or an empty {@link String} if no description is defined.
     */
    String getDescription();

    /**
     * Returns the icon id of this Application.
     * 
     * @return The icon id of this Application or null, if no icon is defined.
     */
    String getIconId();

    /**
     * Returns the icon-url of this Application.
     * 
     * @return The icon-url of this Application or null, if no icon is defined.
     */
    String getIconUrl();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this AuthorizedApplication
     * (the one owning this authorisation).
     * 
     * @return The JDA instance of this AuthorizedApplication.
     */
    JDA getJDA();

    /**
     * Returns the name of this Application.
     * 
     * @return The name of this Application.
     */
    String getName();

    /**
     * Returns a {@link java.util.List List}{@literal <}{@link String String}{@literal >} of authorized scopes of the Application.
     * <br>For a complete list of valid scopes go to the
     * <a href="https://discordapp.com/developers/docs/topics/oauth2#scopes">Official Discord Documentation</a>. 
     * 
     * @return The authorisation scopes.
     * 
     * @see <a href="https://discordapp.com/developers/docs/topics/oauth2#scopes" >List of Discord OAuth2 scopes</a>
     */
    List<String> getScopes();
}
