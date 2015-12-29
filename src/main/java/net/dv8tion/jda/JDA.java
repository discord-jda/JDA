/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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
package net.dv8tion.jda;

import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.hooks.EventListener;
import org.apache.http.HttpHost;

import javax.security.auth.login.LoginException;
import java.util.List;


/**
 * Represents the core of the Discord API. All functionality is connected through this.
 */
public abstract class JDA
{
    /**
     * Attempts to login to Discord.
     * Upon successful auth with Discord, a token is generated and stored in token.json.
     *
     * @param email
     *          The email of the account attempting to log in.
     * @param password
     *          The password of the account attempting to log in.
     * @throws IllegalArgumentException
     *          Thrown if this email or password provided are empty or null.
     * @throws LoginException
     *          Thrown if the email-password combination fails the auth check with the Discord servers.
     */
    protected abstract void login(String email, String password) throws IllegalArgumentException, LoginException;

    public abstract String getAuthToken();

    public abstract void addEventListener(EventListener listener);

    public abstract void removeEventListener(EventListener listener);

    public abstract List<User> getUsers();

    public abstract User getUserById(String id);

    public abstract List<Guild> getGuilds();

    public abstract Guild getGuildById(String id);

    public abstract List<TextChannel> getTextChannels();

    public abstract TextChannel getTextChannelById(String id);

    public abstract List<VoiceChannel> getVoiceChannels();

    public abstract VoiceChannel getVoiceChannelById(String id);

    public abstract PrivateChannel getPrivateChannelById(String id);

    /**
     * Returns the currently logged in account represented by {@link net.dv8tion.jda.entities.SelfInfo SelfInfo}.<br>
     * Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     *   use the AccountManager.
     *
     * @return
     *      The currently logged in account.
     */
    public abstract SelfInfo getSelfInfo();

    /**
     * Returns the {@link net.dv8tion.jda.entities.AccountManager AccountManager} for the currently logged in account.<br>
     * Account settings <b>can only</b> be modified using this object. 
     *
     * @return
     *      The AccountManager for the currently logged in account.
     */
    public abstract AccountManager getAccountManager();

    public abstract int getResponseTotal();

    /**
     * The proxy settings used by all JDA instances.
     *
     * @return
     *      The proxy settings used by all JDA instances. If JDA currently isn't using a proxy, {@link java.net.Proxy#NO_PROXY Proxy.NO_PROXY} is returned.
     */
    public abstract HttpHost getGlobalProxy();
}
