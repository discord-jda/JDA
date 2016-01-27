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
import net.dv8tion.jda.hooks.AnnotatedEventManager;
import net.dv8tion.jda.hooks.IEventManager;
import net.dv8tion.jda.managers.AccountManager;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.managers.GuildManager;
import org.apache.http.HttpHost;

import java.util.List;
import java.util.function.Consumer;


/**
 * Represents the core of the Discord API. All functionality is connected through this.
 */
public interface JDA
{
    /**
     * Changes the internal EventManager.
     * The default EventManager is {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener}.
     * There is also an {@link AnnotatedEventManager AnnotatedEventManager} available.
     *
     * @param manager
     *          The new EventManager to use
     */
    void setEventManager(IEventManager manager);

    /**
     * Adds an Object to the event-listeners that will be used to handle events.
     * This uses the {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link AnnotatedEventManager AnnotatedEventManager}, use {@link #setEventManager(IEventManager)}.
     *
     * Note: when using the {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.hooks.EventListener EventListener}!
     *
     * @param listener
     *          The listener
     */
    void addEventListener(Object listener);

    /**
     * Removes the provided Object from the event-listeners and no longer uses it to handle events.
     *
     * @param listener
     *          The listener to be removed.
     */
    void removeEventListener(Object listener);

    /**
     * Creates a new {@link net.dv8tion.jda.entities.Guild Guild}.
     * This will a return the Manager to the existing, but still empty Guild (no members, no channels).
     * To create a Guild asynchronously (wait for generation of #general chat), use {@link #createGuildAsync(String, Consumer)} instead.
     *
     * In the very rare case, that the Discord-server has problems, The created guild can be unavailable until it is actually created.
     * In that case, this will return null.
     *
     * @param name
     *      the name of the new {@link net.dv8tion.jda.entities.Guild Guild}
     * @return
     *      the {@link net.dv8tion.jda.managers.GuildManager GuildManager} for the created Guild, or null, if the created Guild is temporarily unavailable
     */
    GuildManager createGuild(String name);

    /**
     * Creates a new {@link net.dv8tion.jda.entities.Guild Guild}.
     * This function will wait until the Guild was fully created by the Discord-Server (default channels,...),
     * and then call the provided callback-function with the GuildManager-object
     * To create a Guild synchronously, use {@link #createGuild(String)} instead
     *
     * @param name
     *      the name of the new {@link net.dv8tion.jda.entities.Guild Guild}
     * @param callback
     *      the callback-function that gets called once the guild was fully initialized
     */
    void createGuildAsync(String name, Consumer<GuildManager> callback);

    /**
     * The login token that is currently being used for Discord authentication.
     *
     * @return
     *      Never-null, 18 character length string containing the auth token.
     */
    String getAuthToken();

    /**
     * An unmodifiable list of all known {@link net.dv8tion.jda.entities.User Users}.<br>
     * This list will never contain duplicates and represents all {@link net.dv8tion.jda.entities.User Users} that
     * JDA can currently see.
     *
     * @return
     *      List of all known {@link net.dv8tion.jda.entities.User Users}.
     */
    List<User> getUsers();

    /**
     * This returns the {@link net.dv8tion.jda.entities.User User} who has the same id as the one provided.<br>
     * If there is no known user with an id that matches the provided one, this this returns <code>null</code>.
     *
     * @param id
     *          The id of the {@link net.dv8tion.jda.entities.User User}.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.User User} with matching id.
     */
    User getUserById(String id);

    /**
     * This unmodifiable returns all {@link net.dv8tion.jda.entities.User Users} that have the same username as the one provided.<br>
     * If there are no {@link net.dv8tion.jda.entities.User Users} with the provided name, then this returns an empty list.
     *
     * @param name
     *          The name of the requested {@link net.dv8tion.jda.entities.User Users}.
     * @return
     *      Possibly-empty list of {@link net.dv8tion.jda.entities.User Users} that all have the same name as the provided name.
     */
    List<User> getUsersByName(String name);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.entities.Guild Guilds} that this account is connected to.<br>
     * If this account is not connected to any {@link net.dv8tion.jda.entities.Guild Guilds}, this will return
     * an empty list.
     *
     * @return
     *      Possibly-empty list of all the {@link net.dv8tion.jda.entities.Guild Guilds} that this account is connected to.
     */
    List<Guild> getGuilds();

    /**
     * This returns the {@link net.dv8tion.jda.entities.Guild Guild} which has the same id as the one provided.<br>
     * If there is no known guild with an id that matches the provided one, then this returns <code>null</code>.
     *
     * @param id
     *          The id of the {@link net.dv8tion.jda.entities.Guild Guild}.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.Guild Guild} with matching id.
     */
    Guild getGuildById(String id);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.entities.Guild Guilds} that have the same name as the one provided.<br>
     * If there are no {@link net.dv8tion.jda.entities.Guild Guilds} with the provided name, then this returns an empty list.
     *
     * @param name
     *          The name of the requested {@link net.dv8tion.jda.entities.Guild Guilds}.
     * @return
     *      Possibly-empty list of all the {@link net.dv8tion.jda.entities.Guild Guilds} that all have the same name as
     *      the provided name.
     */
    List<Guild> getGuildsByName(String name);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.entities.TextChannel TextChannels} of all {@link net.dv8tion.jda.entities.Guild Guilds}
     * that this account is a member of.
     * <p>
     * <b>Note:</b> just because a {@link net.dv8tion.jda.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @return
     *      Possibly-empty list of all known {@link net.dv8tion.jda.entities.TextChannel TextChannels}.
     */
    List<TextChannel> getTextChannels();

    /**
     * This returns the {@link net.dv8tion.jda.entities.TextChannel TextChannel} which has the same id as the one provided.<br>
     * If there is no known {@link net.dv8tion.jda.entities.TextChannel TextChannel} with an id that matches the provided
     * one, then this returns <code>null</code>.
     * <p>
     * <b>Note:</b> just because a {@link net.dv8tion.jda.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param id
     *          The id of the {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.TextChannel TextChannel} with matching id.
     */
    TextChannel getTextChannelById(String id);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.entities.TextChannel TextChannels} that have the same name as the one provided.<br>
     * If there are no {@link net.dv8tion.jda.entities.TextChannel TextChannels} with the provided name, then this returns an empty list.
     * <p>
     * <b>Note:</b> just because a {@link net.dv8tion.jda.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param name
     *          The name of the requested {@link net.dv8tion.jda.entities.TextChannel TextChannels}.
     * @return
     *      Possibly-empty list of all the {@link net.dv8tion.jda.entities.TextChannel TextChannels} that all have the
     *      same name as the provided name.
     */
    List<TextChannel> getTextChannelsByName(String name);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels} of all {@link net.dv8tion.jda.entities.Guild Guilds}
     * that this account is a member of.
     *
     * @return
     *      Possible-empty list of all known {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * This returns the {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.<br>
     * If there is no known {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns <code>null</code>.
     *
     * @param id
     *          The id of the {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} with matching id.
     */
    VoiceChannel getVoiceChannelById(String id);

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels} that have the same name as the one provided.<br>
     * If there are no {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels} with the provided name, then this returns an empty list.
     *
     * @param name
     *          The name of the requested {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}.
     * @return
     *      Possibly-empty list of all the {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels} that all have the
     *      same name as the provided name.
     */
    List<VoiceChannel> getVoiceChannelByName(String name);

    /**
     * An unmodifiable list of all known {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannels}.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannels}.
     */
    List<PrivateChannel> getPrivateChannels();

    /**
     * This returns the {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.<br>
     * If there is no known {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} with an id that matches the
     * provided one, then this returns <code>null</code>.
     *
     * @param id
     *          The id of the {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel}.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} with matching id.
     */
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

    /**
     * Returns the {@link net.dv8tion.jda.managers.AccountManager AudioManager} for this {@link net.dv8tion.jda.JDA JDA}
     * instance. AudioManager deals with creating, managing and severing audio connections to
     * {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}.
     *
     * @return
     *      The AudioManager for this JDA instance.
     */
    AudioManager getAudioManager();

    /**
     * This value is the total amount of JSON responses that discord has sent.<br>
     * This value resets every time the websocket has to reconnect.
     *
     * @return
     *      Never-negative int containing total response amount.
     */
    int getResponseTotal();

    /**
     * The proxy settings used by all JDA instances.
     *
     * @return
     *      The proxy settings used by all JDA instances. If JDA currently isn't using a proxy, {@link java.net.Proxy#NO_PROXY Proxy.NO_PROXY} is returned.
     */
    HttpHost getGlobalProxy();

    /**
     * Used to enable JDA debug output.
     *
     * @param enableDebug
     *          If true - enables debug output.
     */
    void setDebug(boolean enableDebug);

    /**
     * Used to determine if JDA is currently in debug mode.
     *
     * @return
     *      True if JDA is currently in debug mode.
     */
    boolean isDebug();

    /**
     * Shuts down JDA, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * This will also close the background-thread used for requests (which is required for further api calls of other JDA instances).
     * If this is not desired, use {@link #shutdown(boolean)} instead.
     * To reconnect, just create a new JDA instance.
     */
    void shutdown();

    /**
     * Shuts down JDA, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * Depending on the free-parameter, this will also close the background-thread used for requests.
     * If the background-thread is closed, the system can exit properly, but no further JDA requests are possible (includes other JDA instances).
     * If you want to reconnect, and the request-thread was not freed, just create a new JDA instance.
     *
     * @param free
     *          If true, shuts down JDA's rest system permanently.
     */
    void shutdown(boolean free);
}
