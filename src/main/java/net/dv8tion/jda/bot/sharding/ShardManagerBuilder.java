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
package net.dv8tion.jda.bot.sharding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.apache.http.HttpHost;
import org.apache.http.util.Args;

/**
 * Used to create new {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instances.
 * 
 * <p>A single ShardManagerBuilder can be reused multiple times. Each call to {@link #buildAsync()} or {@link #buildBlocking()}
 * creates a new {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance using the same information.
 * 
 * @since  3.1
 * @author Aljoscha Grebe
 */
public class ShardManagerBuilder
{

    protected static boolean jdaCreated = false; // FIXME: this ugly thing will be fixed with experimental/okhttp 
    protected static HttpHost proxy = null;

    private int maxShardId = -1;
    private int minShardId = -1;
    private int shardsTotal = -1;

    protected final List<Object> listeners = new ArrayList<>();

    protected String token = null;
    protected IEventManager eventManager = null;
    protected IAudioSendFactory audioSendFactory = null;
    protected Game game = null;
    protected OnlineStatus status = OnlineStatus.ONLINE;
    protected int websocketTimeout = 0;
    protected int maxReconnectDelay = 900;
    protected int corePoolSize = 2;
    protected boolean enableVoice = true;
    protected boolean enableShutdownHook = true;
    protected boolean enableBulkDeleteSplitting = true;
    protected boolean autoReconnect = true;
    protected boolean idle = false;

    final JDABuilder builder = new JDABuilder(AccountType.BOT);

    /**
     * Creates a completely empty ShardManagerBuilder.
     * <br>You need to set the token using
     * {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#setToken(String) setToken(String)}
     * before calling {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildBlocking() buildBlocking()}.
     */
    public ShardManagerBuilder() {}

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} object.
     * <br>This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManager(net.dv8tion.jda.core.hooks.IEventManager) setEventManager(new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param   listeners
     *          The listener(s) to add to the list.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.bot.sharding.ShardManager#addEventListener(Object...) JDA.addEventListener(Object...)
     */
    public ShardManagerBuilder addEventListener(final Object... listeners)
    {
        Collections.addAll(this.listeners, listeners);
        return this;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.core.JDA} instance and uses the provided token to start the login process.
     * <br>The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.core.JDA} has not
     * finished loading, thus many {@link net.dv8tion.jda.core.JDA} methods have the chance to return incorrect information.
     * <br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     * <p>If you wish to be sure that the {@link net.dv8tion.jda.core.JDA} information is correct, please use
     * {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildBlocking() buildBlocking()} or register an
     * {@link net.dv8tion.jda.core.hooks.EventListener EventListener} to listen for the
     * {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent} .
     *
     * @throws  LoginException
     *          If the provided token is invalid.
     * @throws  IllegalArgumentException
     *          If the provided token is empty or null.
     * @throws  RateLimitedException
     *          If we are being Rate limited.
     *
     * @return A {@link net.dv8tion.jda.core.JDA} instance that has started the login process. It is unknown as
     *         to whether or not loading has finished when this returns.
     */
    public ShardManager buildAsync() throws LoginException, IllegalArgumentException, RateLimitedException
    {
        jdaCreated = true;

        final ShardManager manager = new ShardManager(shardsTotal, minShardId, maxShardId, listeners, token, eventManager, audioSendFactory, game, status, websocketTimeout, maxReconnectDelay, corePoolSize, enableVoice, enableShutdownHook, enableBulkDeleteSplitting, autoReconnect, idle);
        manager.login();

        return manager;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.core.JDA} instance and uses the provided token to start the login process.
     * <br>This method will block until JDA has logged in and finished loading all resources. This is an alternative
     * to using {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent}.
     *
     * @throws  LoginException
     *          If the provided token is invalid.
     * @throws  IllegalArgumentException
     *          If the provided token is empty or null.
     * @throws  InterruptedException
     *          If an interrupt request is received while waiting for {@link net.dv8tion.jda.core.JDA} to finish logging in.
     *          This would most likely be caused by a JVM shutdown request.
     * @throws  RateLimitedException
     *          If we are being Rate limited.
     *
     * @return A {@link net.dv8tion.jda.core.JDA} Object that is <b>guaranteed</b> to be logged in and finished loading.
     */
    public ShardManager buildBlocking()
            throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException
    {
        final ShardsReadyListener listener = new ShardsReadyListener(this.maxShardId - this.minShardId + 1);
        this.addEventListener(listener);
        final ShardManager manager = this.buildAsync();
        synchronized (listener)
        {
            while (!listener.allReady())
                listener.wait();
        }
        return manager;
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     *         The listener(s) to remove from the list.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.JDA#removeEventListener(Object...) JDA.removeEventListener(Object...)
     */
    public ShardManagerBuilder removeEventListener(final Object... listeners)
    {
        for (Object listener : listeners)
            this.listeners.remove(listener);
        return this;
    }

    /**
     * Enables/Disables Voice functionality.
     * <br>This is useful, if your current system doesn't support Voice and you do not need it.
     *
     * <p>Default: <b>true (enabled)</b>
     *
     * @param  enabled
     *         True - enables voice support.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setAudioEnabled(final boolean enabled)
    {
        this.enableVoice = enabled;
        return this;
    }

    /**
     * Changes the factory used to create {@link net.dv8tion.jda.core.audio.factory.IAudioSendSystem IAudioSendSystem}
     * objects which handle the sending loop for audio packets.
     * <br>By default, JDA uses {@link net.dv8tion.jda.core.audio.factory.DefaultSendFactory DefaultSendFactory}.
     *
     * @param  factory
     *         The new {@link net.dv8tion.jda.core.audio.factory.IAudioSendFactory IAudioSendFactory} to be used
     *         when creating new {@link net.dv8tion.jda.core.audio.factory.IAudioSendSystem} objects.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setAudioSendFactory(final IAudioSendFactory factory)
    {
        this.audioSendFactory = factory;
        return this;
    }

    /**
     * Sets whether or not JDA should try to reconnect if a connection-error is encountered.
     * <br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * Default: <b>true (enabled)</b>
     *
     * @param  autoReconnect
     *         If true - enables autoReconnect
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setAutoReconnect(final boolean autoReconnect)
    {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}
     *
     * <p>Default: <b>true (enabled)</b>
     *
     * @param  enabled
     *         True - The MESSAGE_DELETE_BULK will be split into multiple individual MessageDeleteEvents.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setBulkDeleteSplittingEnabled(final boolean enabled)
    {
        this.enableBulkDeleteSplitting = enabled;
        return this;
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up JDA.
     * <br>When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly close connections.
     *
     * <p>Default: <b>true (enabled)</b>
     *
     * @param  enable
     *         True (default) - use shutdown hook to clean up JDA if the Java program is closed.
     *
     * @return Return the {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setEnableShutdownHook(final boolean enable)
    {
        this.enableShutdownHook = enable;
        return this;
    }

    /**
     * Changes the internally used EventManager.
     * <br>There are 2 provided Implementations:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager} which uses the Interface
     *     {@link net.dv8tion.jda.core.hooks.EventListener EventListener} (tip: use the {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter}).
     *     <br>This is the default EventManager.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager} which uses the Annotation
     *         {@link net.dv8tion.jda.core.hooks.SubscribeEvent @SubscribeEvent} to mark the methods that listen for events.</li>
     * </ul>
     * <br>You can also create your own EventManager (See {@link net.dv8tion.jda.core.hooks.IEventManager}).
     *
     * @param  manager
     *         The new {@link net.dv8tion.jda.core.hooks.IEventManager} to use.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setEventManager(final IEventManager manager)
    {
        this.eventManager = manager;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for our session.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Hint:</b> You can create a {@link net.dv8tion.jda.core.entities.Game Game} object using
     * {@link net.dv8tion.jda.core.entities.Game#of(String) Game.of(String)} or
     * {@link net.dv8tion.jda.core.entities.Game#of(String, String) Game.of(String, String)}.
     *
     * @param  game
     *         An instance of {@link net.dv8tion.jda.core.entities.Game Game} (null allowed)
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setGame(Game)
     */
    public ShardManagerBuilder setGame(final Game game)
    {
        this.game = game;
        return this;
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br>This value can be changed at any time using
     * {@link net.dv8tion.jda.bot.sharding.ShardManager#setIdle(boolean) ShardManager#setIdle(boolean)}.
     *
     * @param  idle
     *         boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. <b>(default false)</b>
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setIdle(boolean)
     */
    public ShardManagerBuilder setIdle(final boolean idle)
    {
        this.idle = idle;
        return this;
    }

    /**
     * Sets the maximum amount of time that JDA will back off to wait when attempting to reconnect the MainWebsocket.
     * <br>Provided value must be 32 or greater.
     *
     * @param  maxReconnectDelay
     *         The maximum amount of time that JDA will wait between reconnect attempts in seconds.
     *
     * @throws java.lang.IllegalArgumentException
     *         Thrown if the provided {@code maxReconnectDelay} is less than 32.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setMaxReconnectDelay(final int maxReconnectDelay)
    {
        Args.check(maxReconnectDelay >= 32, "Max reconnect delay must be 32 seconds or greater. You provided %d.", maxReconnectDelay);

        this.maxReconnectDelay = maxReconnectDelay;
        return this;
    }

    /**
     * Sets the proxy that will be used by <b>ALL</b> JDA instances.
     * <br>Once this is set <b>IT CANNOT BE CHANGED.</b>
     * <br>After a JDA instance as been created, this method can never be called again, even if you are creating a new JDA object.
     * <br><b>Note:</b> currently this only supports HTTP proxies.
     *
     * @param  proxy
     *         The proxy to use.
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this method is called after proxy settings have already been set or after at least 1 JDA object has been created.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.core.JDABuilder#setProxy(HttpHost)
     */
    public ShardManagerBuilder setProxy(final HttpHost proxy)
    {
        if (jdaCreated)
            throw new UnsupportedOperationException("You cannot change the proxy after a JDA object has been created. Proxy settings are global among all instances!");
        ShardManagerBuilder.proxy = proxy;
        return this;
    }

    /**
     * Sets the range of shards the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} should contain.
     * This is usefull if you want to split your shards between multiple JVMs or servers.
     * 
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     * 
     * @param  minShardId
     *         The lowest shard id the ShardManager should contain
     * 
     * @param  maxShardId
     *         The highest shard id the ShardManager should contain
     *
     * @throws IllegalArgumentException
     *         If either minShardId is negative, maxShardId is lower than shardsTotal or
     *         minShardId is lower than or equal to maxShardId
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setShardRange(final int minShardId, final int maxShardId)
    {
        Args.notNegative(minShardId, "minShardId");
        Args.check(maxShardId < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        Args.check(minShardId <= maxShardId, "minShardId must be lower than or equal to maxShardId");

        this.minShardId = minShardId;
        this.maxShardId = maxShardId;
        return this;
    }

    /**
     * This will set the total amount of shards the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} should use. 
     * 
     * <p> Of this is set to {@code -1}
     * 
     * @param  shardTotal
     *         The number of overall shards or {@code -1} if JDA should use the recommended amount from discord.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    #setShardRange(int, int)
     */
    public ShardManagerBuilder setShardTotal(final int shardsTotal)
    {
        Args.check(shardsTotal == -1 || shardsTotal > 0, "shardsTotal must either be -1 or more than 0");
        this.shardsTotal = shardsTotal;

        if (this.minShardId == -1 && this.maxShardId == -1)
            this.setShardRange(0, shardsTotal - 1);

        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} our connection will display.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Note:</b>This will not take affect for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     * if the status specified in the user_settings is not "online" as it is overriding our identify status.
     *
     * @param  status
     *         Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is null or {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setStatus(OnlineStatus) Presence.setStatus(OnlineStatus)
     */
    public ShardManagerBuilder setStatus(final OnlineStatus status)
    {
        Args.check(status != null, "OnlineStatus cannot be null!");
        Args.check(status != OnlineStatus.UNKNOWN, "OnlineStatus cannot be unknown!");
        this.status = status;
        return this;
    }

    /**
     * Sets the token that will be used by the {@link net.dv8tion.jda.core.JDA} instance to log in when
     * {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildBlocking() buildBlocking()}
     * is called.
     *
     * <p>To get a bot token:
     * <ol>
     *     <li>Go to your <a href="https://discordapp.com/developers/applications/me">Discord Applications</a></li>
     *     <li>Create or select an already existing application</li>
     *     <li>Verify that it has already been turned into a Bot. If you see the "Create a Bot User" button, click it.</li>
     *     <li>Click the <i>click to reveal</i> link beside the <b>Token</b> label to show your Bot's {@code token}</li>
     * </ol>
     *
     * @param  token
     *         The token of the account that you would like to login with.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the token is either null or empty 
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setToken(final String token)
    {
        this.token = Args.notBlank(token, "token");
        return this;
    }

    /**
     * Sets the timeout (in milliseconds) for all Websockets created by JDA (MainWS and AudioWS's) for this instance.
     *
     * <p>By default, this is set to <b>0</b> which is supposed to represent infinite-timeout, however due to how the JVM
     * is implemented at the lower level (typically C), an infinite timeout will usually not be respected, and as such
     * providing an explicitly defined timeout will typically work better.
     *
     * <p>Default: <b>0 - Infinite-Timeout (maybe?)</b>
     *
     * @param  websocketTimeout
     *         Non-negative int representing Websocket timeout in milliseconds.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setWebSocketTimeout(final int websocketTimeout)
    {
        this.websocketTimeout = Args.notNegative(websocketTimeout, "Provided WebSocket timeout cannot be negative!");;
        return this;
    }

    private static class ShardsReadyListener implements EventListener
    {
        private final AtomicInteger numReady = new AtomicInteger(0);
        private final int shardsTotal;

        public ShardsReadyListener(final int shardsTotal)
        {
            this.shardsTotal = shardsTotal;
        }

        public boolean allReady()
        {
            return this.numReady.get() == this.shardsTotal;
        }

        @Override
        @SubscribeEvent
        public void onEvent(final Event event)
        {
            if (event instanceof ReadyEvent)
            {
                event.getJDA().removeEventListener(this);
                if (this.numReady.incrementAndGet() == this.shardsTotal)
                    synchronized (this)
                    {
                        this.notifyAll();
                    }
            }
        }
    }
}
