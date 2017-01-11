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

import java.util.List;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Represents a Discord Application from the point of view of a client havong authorized it.
 * 
 * @since  JDA 3.0
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
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link Void}
     *         <br>The RestAction to delete this authorisation.
     */
    RestAction<Void> delete();

    /**
     * The authorisartion id for this application.
     *
     * @return The authorisartion id. 
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
     * @return
     *      The icon id of this Application or null, if no icon is defined.
     */
    String getIconId();

    /**
     * Returns the icon-url of this Application.
     * @return
     *      The icon-url of this Application or null, if no icon is defined.
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
     * @return
     *      The name of this Application.
     */
    String getName();

    /**
     * Returns a {@link java.util.List List<}{@link String String}{@link java.util.List >} of RPC origins of the Application.
     * @return
     *      The name of the bot's Application.
     */
    List<String> getRpcOrigins();

    /**
     * Returns a {@link java.util.List List}{@literal <}{@link String String}{@literal >} of authorized scopes of the Application.
     * @return
     *      The authorisation scopes.
     */
    List<String> getScopes();
}
