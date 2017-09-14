/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

import com.neovisionaries.ws.client.WebSocketFactory;
import java.util.*;
import java.util.stream.Collectors;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.OkHttpClient;

/**
 * Used to create new {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} instances.
 *
 * <p>A single DefaultShardManagerBuilder can be reused multiple times. Each call to {@link #buildAsync()} or {@link #buildBlocking()}
 * creates a new {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} instance using the same information.
 *
 * @since  3.1
 * @author Aljoscha Grebe
 */
public class DefaultShardManagerBuilder
{
    protected Collection<Integer> shards = null;
    protected int shardsTotal = 1;
    protected IAudioSendFactory audioSendFactory = null;
    protected OkHttpClient.Builder httpClientBuilder = null;
    protected WebSocketFactory wsFactory = null;
    protected boolean autoReconnect = true;
    protected int backoff = 200;
    protected int corePoolSize = 2;
    protected boolean enableBulkDeleteSplitting = true;
    protected boolean enableShutdownHook = true;
    protected boolean enableVoice = true;
    protected IEventManager eventManager = null;
    protected Game game = null;
    protected boolean idle = false;
    protected final List<Object> listeners = new ArrayList<>();
    protected int maxReconnectDelay = 900;
    protected OnlineStatus status = OnlineStatus.ONLINE;
    protected String token = null;
    private SessionReconnectQueue reconnectQueue;

    /**
     * Creates a completely empty DefaultShardManagerBuilder.
     * <br>You need to set the token using
     * {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setToken(String) setToken(String)}
     * before calling {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#buildBlocking() buildBlocking()}.
     */
    public DefaultShardManagerBuilder()
    {}

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} object.
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl#addEventListener(Object...) JDA.addEventListener(Object...)
     */
    public DefaultShardManagerBuilder addEventListener(final Object... listeners)
    {
        Collections.addAll(this.listeners, listeners);
        return this;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance and uses the provided token to start the login process.
     * <br>The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} has not
     * finished loading, thus many {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} methods have the chance to return incorrect information.
     * <br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     * <p>If you wish to be sure that the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} information is correct, please use
     * {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#buildBlocking() buildBlocking()}.
     *
     * @throws  LoginException
     *          If the provided token is invalid.
     * @throws  IllegalArgumentException
     *          If the provided token is empty or null.
     * @throws  RateLimitedException
     *          If we are being Rate limited.
     *
     * @return A {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance that has started the login process. It is unknown as
     *         to whether or not loading has finished when this returns.
     */
    public ShardManager buildAsync() throws LoginException, IllegalArgumentException, RateLimitedException
    {
        final DefaultShardManagerImpl manager = new DefaultShardManagerImpl(this.shardsTotal, this.shards, this.listeners, this.token, this.eventManager, this.audioSendFactory, this.game, this.status, this.httpClientBuilder, this.wsFactory, this.maxReconnectDelay, this.corePoolSize, this.enableVoice, this.enableShutdownHook, this.enableBulkDeleteSplitting, this.autoReconnect, this.idle, this.reconnectQueue, this.backoff);

        manager.login();

        return manager;
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     *         The listener(s) to remove from the list.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.JDA#removeEventListener(Object...) JDA.removeEventListener(Object...)
     */
    public DefaultShardManagerBuilder removeEventListener(final Object... listeners)
    {
        for (final Object listener : listeners)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setAudioEnabled(final boolean enabled)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setAudioSendFactory(final IAudioSendFactory factory)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setAutoReconnect(final boolean autoReconnect)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setBulkDeleteSplittingEnabled(final boolean enabled)
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
     * @return Return the {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setEnableShutdownHook(final boolean enable)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setEventManager(final IEventManager manager)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setGame(Game)
     */
    public DefaultShardManagerBuilder setGame(final Game game)
    {
        this.game = game;
        return this;
    }

    /**
     * Sets the {@link okhttp3.OkHttpClient.Builder Builder} that will be used by JDA's requester.
     * This can be used to set things such as connection timeout and proxy. 
     *
     * @param  builder
     *         The new {@link okhttp3.OkHttpClient.Builder Builder} to use.
     *
     * @return Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setHttpClientBuilder(OkHttpClient.Builder builder)
    {
        this.httpClientBuilder = builder;
        return this;
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br>This value can be changed at any time using
     * {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl#setIdle(boolean) DefaultShardManagerImpl#setIdle(boolean)}.
     *
     * @param  idle
     *         boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. <b>(default false)</b>
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setIdle(boolean)
     */
    public DefaultShardManagerBuilder setIdle(final boolean idle)
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setMaxReconnectDelay(final int maxReconnectDelay)
    {
        Checks.check(maxReconnectDelay >= 32, "Max reconnect delay must be 32 seconds or greater. You provided %d.", maxReconnectDelay);

        this.maxReconnectDelay = maxReconnectDelay;
        return this;
    }

    /**
     * THis can be used to set a custom queue that will be used to reconnect sessions.
     * <br>This will ensure that sessions do not reconnect at the same time!
     * 
     * <p>If none is provided the ShardManager will use fall back to JDA's default implementation!
     *
     * @param  queue
     *         {@link java.util.concurrent.BlockingQueue BlockingQueue} to use
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setReconnectQueue(SessionReconnectQueue queue)
    {
        this.reconnectQueue = queue;
        return this;
    }

    /**
     * Sets the range of shards the {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} should contain.
     * This is usefull if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  minShardId
     *         The lowest shard id the DefaultShardManagerImpl should contain
     *
     * @param  maxShardId
     *         The highest shard id the DefaultShardManagerImpl should contain
     *
     * @throws IllegalArgumentException
     *         If either minShardId is negative, maxShardId is lower than shardsTotal or
     *         minShardId is lower than or equal to maxShardId
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setShards(final int... shardIds)
    {
        Checks.notNull(shardIds, "shardIds");
        for (int id : shardIds)
        {
            Checks.notNegative(id, "minShardId");
            Checks.check(id < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        }

        this.shards = Arrays.stream(shardIds).boxed().collect(Collectors.toSet());

        return this;
    }

    /**
     * Sets the range of shards the {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} should contain.
     * This is usefull if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  minShardId
     *         The lowest shard id the DefaultShardManagerImpl should contain
     *
     * @param  maxShardId
     *         The highest shard id the DefaultShardManagerImpl should contain
     *
     * @throws IllegalArgumentException
     *         If either minShardId is negative, maxShardId is lower than shardsTotal or
     *         minShardId is lower than or equal to maxShardId
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setShards(final int minShardId, final int maxShardId)
    {
        Checks.notNegative(minShardId, "minShardId");
        Checks.check(maxShardId < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        Checks.check(minShardId <= maxShardId, "minShardId must be lower than or equal to maxShardId");

        List<Integer> shards = new ArrayList<>(maxShardId - minShardId + 1);
        for (int i = minShardId; i <= maxShardId; i++)
            shards.add(i);

        this.shards = shards;

        return this;
    }

    /**
     * Sets the range of shards the {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} should contain.
     * This is usefull if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  minShardId
     *         The lowest shard id the DefaultShardManagerImpl should contain
     *
     * @param  maxShardId
     *         The highest shard id the DefaultShardManagerImpl should contain
     *
     * @throws IllegalArgumentException
     *         If either minShardId is negative, maxShardId is lower than shardsTotal or
     *         minShardId is lower than or equal to maxShardId
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setShards(Collection<Integer> shardIds)
    {
        Checks.notNull(shardIds, "shardIds");
        Iterator<Integer> iterator = shards.iterator();
        while (iterator.hasNext())
        {
            int id = iterator.next();
            Checks.notNegative(id, "minShardId");
            Checks.check(id < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        }

        this.shards = new ArrayList<>(shardIds);

        return this;
    }

    /**
     * This will set the total amount of shards the {@link net.dv8tion.jda.bot.entities.impl.DefaultShardManagerImpl DefaultShardManagerImpl} should use.
     * <p> If this is set to {@code -1} JDA will automatically retrieve the recommended amount of shards from discord.
     *
     * @param  shardTotal
     *         The number of overall shards or {@code -1} if JDA should use the recommended amount from discord.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    #setShardRange(int, int)
     */
    public DefaultShardManagerBuilder setShardTotal(final int shardsTotal)
    {
        Checks.check(shardsTotal == -1 || shardsTotal > 0, "shardsTotal must either be -1 or more than 0");
        this.shardsTotal = shardsTotal;

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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setStatus(OnlineStatus) Presence.setStatus(OnlineStatus)
     */
    public DefaultShardManagerBuilder setStatus(final OnlineStatus status)
    {
        Checks.check(status != null, "OnlineStatus cannot be null!");
        Checks.check(status != OnlineStatus.UNKNOWN, "OnlineStatus cannot be unknown!");
        this.status = status;
        return this;
    }

    /**
     * Sets the token that will be used by the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance to log in when
     * {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#buildBlocking() buildBlocking()}
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
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setToken(final String token)
    {
        Checks.notBlank(token, "token");

        this.token = token;
        return this;
    }

    /**
     * Sets the {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory} that will be used by JDA's websocket client.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  factory
     *         The new {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory} to use.
     *
     * @return Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setWebsocketFactory(WebSocketFactory factory)
    {
        this.wsFactory = factory;
        return this;
    }

    /**
     * Sets the the backoff in milliseconds that is used as safety between logging into shards.
     * The final delay will be {@code ratelimit + backoff}, while the ratelimit currently being 5 seconds.
     * The default backoff is 200 ms. 
     *
     * @param  backoff
     *         The new backoff in ms to use.
     *
     * @return Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setLoginBackoff(int backoff)
    {
        this.backoff = backoff;
        return this;
    }
}
