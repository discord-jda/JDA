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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core;

import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import org.apache.http.HttpHost;
import org.apache.http.util.Args;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to create a new {@link net.dv8tion.jda.core.JDA} instance. This is useful for making sure all of
 * your {@link net.dv8tion.jda.core.hooks.EventListener EventListeners} as registered
 * before {@link net.dv8tion.jda.core.JDA} attempts to log in.
 * <p>
 * A single JDABuilder can be reused multiple times. Each call to
 * {@link net.dv8tion.jda.core.JDABuilder#buildAsync() buildAsync()} or
 * {@link net.dv8tion.jda.core.JDABuilder#buildBlocking() buildBlocking()}
 * creates a new {@link net.dv8tion.jda.core.JDA} instance using the same information.
 * This means that you can have listeners easily registered to multiple {@link net.dv8tion.jda.core.JDA} instances.
 */
public class JDABuilder
{
    protected static boolean jdaCreated = false;
    protected static HttpHost proxy = null;
    protected final List<Object> listeners;
    protected AccountType accountType;
    protected String token = null;
    protected boolean enableVoice = true;
    protected boolean enableShutdownHook = true;
    protected boolean enableBulkDeleteSplitting = true;
    protected boolean autoReconnect = true;
    protected boolean idle = false;
    protected IEventManager eventManager = null;
    protected IAudioSendFactory audioSendFactory = null;
    protected JDA.ShardInfo shardInfo = null;
    protected Game game = null;
    protected OnlineStatus status = OnlineStatus.ONLINE;

    /**
     * Creates a completely empty JDABuilder.<br>
     * If you use this, you need to set the  token using
     * {@link net.dv8tion.jda.core.JDABuilder#setToken(String) setBotToken(String)}
     * before calling {@link net.dv8tion.jda.core.JDABuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.core.JDABuilder#buildBlocking() buildBlocking()}
     *
     * @param accountType
     *          The {@link net.dv8tion.jda.core.AccountType AccountType}.
     */
    public JDABuilder(AccountType accountType)
    {
        if (accountType == null)
            throw new NullPointerException("Provided AccountType was null!");
        this.accountType = accountType;
        listeners = new LinkedList<>();
    }

    /**
     * Sets the botToken that will be used by the {@link net.dv8tion.jda.core.JDA} instance to log in when
     * {@link net.dv8tion.jda.core.JDABuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.core.JDABuilder#buildBlocking() buildBlocking()}
     * is called.
     *
     * @param token
     *          The token of the bot-account that you would like to login with.
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Sets the proxy that will be used by <b>ALL</b> JDA instances.<br>
     * Once this is set <b>IT CANNOT BE CHANGED.</b><br>
     * After a JDA instance as been created, this method can never be called again, even if you are creating a new JDA object.<br>
     * <b>Note:</b> currently this only supports HTTP proxies.
     *
     * @param proxy
     *          The proxy to use.
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     * @throws UnsupportedOperationException
     *          If this method is called after proxy settings have already been set or after at least 1 JDA object has been created.
     */
    public JDABuilder setProxy(HttpHost proxy)
    {
        if (jdaCreated)
            throw new UnsupportedOperationException("You cannot change the proxy after a JDA object has been created. Proxy settings are global among all instances!");
        this.proxy = proxy;
        return this;
    }

    /**
     * Enables/Disables Voice functionality.<br>
     * This is useful, if your current system doesn't support Voice and you do not need it.
     * <p>
     * Default: <b>true (enabled)</b>
     *
     * @param enabled
     *          True - enables voice support.
     * @return
     *          Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setAudioEnabled(boolean enabled)
    {
        this.enableVoice = enabled;
        return this;
    }

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}
     * <p>
     * Default: <b>true (enabled)</b>
     *
     * @param enabled
     *          True - The MESSAGE_DELTE_BULK will be split into multiple individual MessageDeleteEvents.
     * @return
     *       Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setBulkDeleteSplittingEnabled(boolean enabled)
    {
        this.enableBulkDeleteSplitting = enabled;
        return this;
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up JDA.<br>
     * When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly severe connections.
     * <p>
     * Default: <b>true (enabled)</b>
     *
     * @param enable
     *          True (default) - use shutdown hook to clean up JDA if the Java program is closed.
     * @return
     *      Return the {@link net.dv8tion.jda.core.JDABuilder JDABuilder } instance. Useful for chaining.
     */
    public JDABuilder setEnableShutdownHook(boolean enable)
    {
        this.enableShutdownHook = enable;
        return this;
    }

    /**
     * Sets whether or not JDA should try to reconnect, if a connection-error occured.
     * This will use and incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * Default is true.
     *
     * @param autoReconnect
     *      If true - enables autoReconnect
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setAutoReconnect(boolean autoReconnect)
    {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Changes the internally used EventManager.
     * There are 2 provided Implementations:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.hooks.InterfacedEventManager} which uses the Interface {@link net.dv8tion.jda.core.hooks.EventListener}
     *     (tip: use the {@link net.dv8tion.jda.core.hooks.ListenerAdapter}). This is the default EventManager.</li>
     *     <li>{@link net.dv8tion.jda.core.hooks.AnnotatedEventManager} which uses the Annotation {@link net.dv8tion.jda.core.hooks.SubscribeEvent} to mark the methods that listen for events.</li>
     * </ul>
     * You can also create your own EventManager (See {@link net.dv8tion.jda.core.hooks.IEventManager}).
     *
     * @param manager
     *      The new {@link net.dv8tion.jda.core.hooks.IEventManager} to use
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setEventManager(IEventManager manager)
    {
        this.eventManager = manager;
        return this;
    }

    public JDABuilder setAudioSendFactory(IAudioSendFactory factory)
    {
        this.audioSendFactory = factory;
        return this;
    }

    /**
     * Sets whether or not we should mark our session as afk<p>
     * This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * @param idle
     *      boolean value that will be provided with our IDENTIFY package to mark our session as afk or not. (default false)
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     * @see net.dv8tion.jda.core.managers.Presence#setIdle(boolean)
     */
    public JDABuilder setIdle(boolean idle)
    {
        this.idle = idle;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for our session.<p>
     * This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * @param game
     *      An instance of {@link net.dv8tion.jda.core.entities.Game Game} (null allowed)
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     * @see net.dv8tion.jda.core.managers.Presence#setGame(Game)
     */
    public JDABuilder setGame(Game game)
    {
        this.game = game;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} our connection will display.<p>
     * This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.<p>
     * <b>This will not take affect for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT} if the status specified in the user_settings
     * is not "online" as it is overriding our identify status.</b>
     *
     * @param status
     *      Not-null OnlineStatus (default online)
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     * @throws IllegalArgumentException
     *      if the provided OnlineStatus is null or {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     * @see net.dv8tion.jda.core.managers.Presence#setStatus(OnlineStatus)
     */
    public JDABuilder setStatus(OnlineStatus status)
    {
        if (status == null || status == OnlineStatus.UNKNOWN)
            throw new IllegalArgumentException("OnlineStatus cannot be null or unknown!");
        this.status = status;
        return this;
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link net.dv8tion.jda.core.JDA} object.
     * This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManager(net.dv8tion.jda.core.hooks.IEventManager) setEventManager(new AnnotatedEventManager())}.
     *
     * Note: when using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param listeners
     *          The listeners to add to the list.
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder addListener(Object... listeners)
    {
        Collections.addAll(this.listeners, listeners);
        return this;
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param listeners
     *          The listeners to remove from the list.
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder removeListener(Object... listeners)
    {
        this.listeners.removeAll(Arrays.asList(listeners));
        return this;
    }

    /**
     * This will enable sharding mode for JDA.
     * In sharding mode, guilds are split up and assigned one of multiple shards (clients).
     * The shardId that receives all stuff related to given bot is calculated as follows: shardId = (guildId &gt;&gt; 22)%numShards .
     * PMs are only sent to shard 0.
     *
     * Please note, that a shard will not even know about guilds not assigned to.
     *
     * @param shardId
     *      The id of this shard (starting at 0).
     * @param numShards
     *      The number of overall shards.
     * @return
     *      Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder useSharding(int shardId, int numShards)
    {
        if (shardId < 0 || numShards < 2 || shardId >= numShards)
        {
            throw new RuntimeException("This configuration of shardId and numShards is not allowed! 0 <= shardId < numShards with numShards > 1");
        }
        shardInfo = new JDA.ShardInfo(shardId, numShards);
        return this;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.core.JDA} instance and uses the provided token to start the login process.<br>
     * The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.core.JDA} has not
     * finished loading, thus many {@link net.dv8tion.jda.core.JDA} methods have the chance to return incorrect information.
     * <p>
     * If you wish to be sure that the {@link net.dv8tion.jda.core.JDA} information is correct, please use
     * {@link net.dv8tion.jda.core.JDABuilder#buildBlocking() buildBlocking()} or register a
     * {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent} {@link net.dv8tion.jda.core.hooks.EventListener EventListener}.
     *
     * @return
     *      A {@link net.dv8tion.jda.core.JDA} instance that has started the login process. It is unknown as to whether or not loading has finished when this returns.
     * @throws LoginException
     *          If the provided token is invalid.
     * @throws IllegalArgumentException
     *          If the provided token is empty or null.
     * @throws RateLimitedException
     *          If we are being Rate limited.
     */
    public JDA buildAsync() throws LoginException, IllegalArgumentException, RateLimitedException
    {
        jdaCreated = true;

        JDAImpl jda = new JDAImpl(accountType, proxy, autoReconnect, enableVoice, enableShutdownHook,
                enableBulkDeleteSplitting);

        if (eventManager != null)
            jda.setEventManager(eventManager);

        if (audioSendFactory != null)
            jda.setAudioSendFactory(audioSendFactory);

        listeners.forEach(jda::addEventListener);
        jda.setStatus(JDA.Status.INITIALIZED);  //This is already set by JDA internally, but this is to make sure the listeners catch it.
//        jda.login(token, sharding);
        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        ((PresenceImpl) jda.getPresence())
                .setCacheGame(game)
                .setCacheIdle(idle)
                .setCacheStatus(status);
        jda.login(token, shardInfo);
        return jda;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.core.JDA} instance and uses the provided token to start the login process.<br>
     * This method will block until JDA has logged in and finished loading all resources. This is an alternative
     * to using {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent}.
     *
     * @return
     *      A {@link net.dv8tion.jda.core.JDA} Object that is <b>guaranteed</b> to be logged in and finished loading.
     * @throws LoginException
     *          If the provided token is invalid.
     * @throws IllegalArgumentException
     *          If the provided token is empty or null.
     * @throws InterruptedException
     *          If an interrupt request is received while waiting for {@link net.dv8tion.jda.core.JDA} to finish logging in.
     *          This would most likely be caused by a JVM shutdown request.
     * @throws RateLimitedException
     *          If we are being Rate limited.
     */
    public JDA buildBlocking() throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException
    {
        JDA jda = buildAsync();
        while(jda.getStatus() != Status.CONNECTED)
        {
            Thread.sleep(50);
        }
        return jda;
    }
}
