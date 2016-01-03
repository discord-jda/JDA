/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.managers.AccountManager;
import net.dv8tion.jda.managers.GuildManager;
import org.apache.http.HttpHost;

import java.util.List;
import java.util.function.Consumer;


/**
 * Represents the core of the Discord API. All functionality is connected through this.
 */
public interface JDA
{
    String getAuthToken();

    void addEventListener(EventListener listener);

    void removeEventListener(EventListener listener);

    List<User> getUsers();

    User getUserById(String id);

    List<User> getUsersByName(String name);

    List<Guild> getGuilds();

    /**
     * Creates a new {@link net.dv8tion.jda.entities.Guild Guild}.
     * The created Guild will have the default {@link net.dv8tion.jda.Region#US_WEST US_WEST Region}.
     * This will a return the Manager to the existing, but still empty Guild (no members, no channels).
     * To create a Guild asynchronously (wait for generation of #general chat), use {@link #createGuildAsync(String, Consumer)} instead
     *
     * @param name
     *      the name of the Guild to create
     * @return
     *      the ChannelManager for the created Guild
     */
    GuildManager createGuild(String name);

    /**
     * Creates a new {@link net.dv8tion.jda.entities.Guild Guild}.
     * The created Guild will have the default {@link net.dv8tion.jda.Region#US_WEST US_WEST Region}.
     * This function will wait until the Guild was fully created by the Discord-Server (default channels,...),
     * and then call the provided callback-function with the Guild-object
     * To create a Guild synchronously, use {@link #createGuild(String)} instead
     *
     * @param name
     *      the name of the Guild to create
     * @param callback
     *      the callback-function that gets called once the guild was fully initialized
     */
    void createGuildAsync(String name, Consumer<Guild> callback);

    Guild getGuildById(String id);

    List<TextChannel> getTextChannels();

    TextChannel getTextChannelById(String id);

    List<VoiceChannel> getVoiceChannels();

    VoiceChannel getVoiceChannelById(String id);

    PrivateChannel getPrivateChannelById(String id);

    /**
     * Returns the currently logged in account represented by {@link net.dv8tion.jda.entities.SelfInfo SelfInfo}.<br>
     * Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     *   use the AccountManager.
     *
     * @return
     *      The currently logged in account.
     */
    SelfInfo getSelfInfo();

    /**
     * Returns the {@link net.dv8tion.jda.managers.AccountManager AccountManager} for the currently logged in account.<br>
     * Account settings <b>can only</b> be modified using this object. 
     *
     * @return
     *      The AccountManager for the currently logged in account.
     */
    AccountManager getAccountManager();

    int getResponseTotal();

    /**
     * The proxy settings used by all JDA instances.
     *
     * @return
     *      The proxy settings used by all JDA instances. If JDA currently isn't using a proxy, {@link java.net.Proxy#NO_PROXY Proxy.NO_PROXY} is returned.
     */
    HttpHost getGlobalProxy();

    void setDebug(boolean enableDebug);

    boolean isDebug();
}
