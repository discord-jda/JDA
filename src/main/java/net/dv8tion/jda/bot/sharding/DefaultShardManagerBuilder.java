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
package net.dv8tion.jda.bot.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.SessionController;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Used to create new instances of JDA's default {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} implementation.
 *
 * <p>A single DefaultShardManagerBuilder can be reused multiple times. Each call to {@link #build()}
 * creates a new {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance using the same information.
 *
 * @since  3.4
 * @author Aljoscha Grebe
 */
public class DefaultShardManagerBuilder
{
    protected final List<Object> listeners = new ArrayList<>();
    protected final List<IntFunction<Object>> listenerProviders = new ArrayList<>();
    protected SessionController sessionController = null;
    protected IntFunction<ConcurrentMap<String, String>> contextProvider = null;
    protected boolean enableContext = true;
    protected boolean enableBulkDeleteSplitting = true;
    protected boolean enableShutdownHook = true;
    protected boolean enableVoice = true;
    protected boolean autoReconnect = true;
    protected boolean retryOnTimeout = true;
    protected boolean useShutdownNow = false;
    protected boolean enableCompression = true;
    protected int shardsTotal = -1;
    protected int maxReconnectDelay = 900;
    protected int corePoolSize = 2;
    protected String token = null;
    protected IntFunction<Boolean> idleProvider = null;
    protected IntFunction<Game> gameProvider = null;
    protected IntFunction<OnlineStatus> statusProvider = null;
    protected Collection<Integer> shards = null;
    protected IEventManager eventManager = null;
    protected OkHttpClient.Builder httpClientBuilder = null;
    protected WebSocketFactory wsFactory = null;
    protected IAudioSendFactory audioSendFactory = null;
    protected ThreadFactory threadFactory = null;

    /**
     * Creates a completely empty DefaultShardManagerBuilder.
     * <br>You need to set the token using
     * {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setToken(String) setToken(String)}
     * before calling {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#build() build()}.
     */
    public DefaultShardManagerBuilder() {}

    /**
     * Sets the {@link net.dv8tion.jda.core.utils.SessionController SessionController}
     * for the resulting ShardManager instance. This can be used to sync behaviour and state between shards
     * of a bot and should be one and the same instance on all builders for the shards.
     *
     * @param  controller
     *         The {@link net.dv8tion.jda.core.utils.SessionController SessionController} to use
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.utils.SessionControllerAdapter SessionControllerAdapter
     */
    public DefaultShardManagerBuilder setSessionController(SessionController controller)
    {
        this.sessionController = controller;
        return this;
    }

    /**
     * Sets the {@link org.slf4j.MDC MDC} mappings provider to use in JDA.
     * <br>If sharding is enabled JDA will automatically add a {@code jda.shard} context with the format {@code [SHARD_ID / TOTAL]}
     * where {@code SHARD_ID} and {@code TOTAL} are the shard configuration.
     * Additionally it will provide context for the id via {@code jda.shard.id} and the total via {@code jda.shard.total}.
     *
     * <p><b>The manager will call this with a shardId and it is recommended to provide a different context map for each shard!</b>
     * <br>This automatically switches {@link #setContextEnabled(boolean)} to true if the provided function is not null!
     *
     * @param  provider
     *         The provider for <b>modifiable</b> context maps to use in JDA, or {@code null} to reset
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    <a href="https://www.slf4j.org/api/org/slf4j/MDC.html" target="_blank">MDC Javadoc</a>
     */
    public DefaultShardManagerBuilder setContextMap(IntFunction<ConcurrentMap<String, String>> provider)
    {
        this.contextProvider = provider;
        if (provider != null)
            this.enableContext = true;
        return this;
    }


    /**
     * Whether JDA should use a synchronized MDC context for all of its controlled threads.
     * <br>Default: {@code true}
     *
     * @param  enable
     *         True, if JDA should provide an MDC context map
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    <a href="https://www.slf4j.org/api/org/slf4j/MDC.html" target="_blank">MDC Javadoc</a>
     * @see    #setContextMap(java.util.function.IntFunction)
     */
    public DefaultShardManagerBuilder setContextEnabled(boolean enable)
    {
        this.enableContext = enable;
        return this;
    }

    /**
     * Enable stream-compression on the gateway connection,
     * this will decrease the amount of used bandwidth for the running bot instance
     * for the cost of a few extra cycles for decompression.
     * <br><b>Default: true</b>
     *
     * <p><b>We recommend to keep this enabled unless you have issues with the decompression</b>
     * <br>This mode might become obligatory in a future version, do not rely on this switch to stay.
     *
     * @param  enable
     *         True, if the gateway connection should use compression
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    <a href="https://discordapp.com/developers/docs/topics/gateway#transport-compression" target="_blank">Official Discord Documentation - Transport Compression</a>
     */
    public DefaultShardManagerBuilder setCompressionEnabled(boolean enable)
    {
        this.enableCompression = enable;
        return this;
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link DefaultShardManager DefaultShardManager} object.
     * <br>This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManager(net.dv8tion.jda.core.hooks.IEventManager) setEventManager(new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) to add to the list.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    DefaultShardManager#addEventListener(Object...) JDA.addEventListener(Object...)
     */
    public DefaultShardManagerBuilder addEventListeners(final Object... listeners)
    {
        return this.addEventListeners(Arrays.asList(listeners));
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link DefaultShardManager DefaultShardManager} object.
     * <br>This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManager(net.dv8tion.jda.core.hooks.IEventManager) setEventManager(new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) to add to the list.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    DefaultShardManager#addEventListener(Object...) JDA.addEventListener(Object...)
     */
    public DefaultShardManagerBuilder addEventListeners(final Collection<Object> listeners)
    {
        Checks.noneNull(listeners, "listeners");

        this.listeners.addAll(listeners);
        return this;
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
    public DefaultShardManagerBuilder removeEventListeners(final Object... listeners)
    {
        return this.removeEventListeners(Arrays.asList(listeners));
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
    public DefaultShardManagerBuilder removeEventListeners(final Collection<Object> listeners)
    {
        Checks.noneNull(listeners, "listeners");

        this.listeners.removeAll(listeners);
        return this;
    }

    /**
     * Adds the provided listener provider to the list of listener providers that will be used to create listeners.
     * On shard creation (including shard restarts) the provider will have the shard id applied and must return a listener,
     * which will be used, along all other listeners, to populate the listeners of the JDA object of that shard.
     *
     * <br>This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManager(net.dv8tion.jda.core.hooks.IEventManager) setEventManager(new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  listenerProvider
     *         The listener provider to add to the list of listener providers.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder addEventListenerProvider(final IntFunction<Object> listenerProvider)
    {
        return this.addEventListenerProviders(Collections.singleton(listenerProvider));
    }

    /**
     * Adds the provided listener providers to the list of listener providers that will be used to create listeners.
     * On shard creation (including shard restarts) each provider will have the shard id applied and must return a listener,
     * which will be used, along all other listeners, to populate the listeners of the JDA object of that shard.
     *
     * <br>This uses the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManager(net.dv8tion.jda.core.hooks.IEventManager) setEventManager(new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  listenerProviders
     *         The listener provider to add to the list of listener providers.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder addEventListenerProviders(final Collection<IntFunction<Object>> listenerProviders)
    {
        Checks.noneNull(listenerProviders, "listener providers");

        this.listenerProviders.addAll(listenerProviders);
        return this;
    }

    /**
     * Removes the provided listener provider from the list of listener providers.
     *
     * @param  listenerProvider
     *         The listener provider to remove from the list of listener providers.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder removeEventListenerProvider(final IntFunction<Object> listenerProvider)
    {
        return this.removeEventListenerProviders(Collections.singleton(listenerProvider));
    }

    /**
     * Removes all provided listener providers from the list of listener providers.
     *
     * @param  listenerProviders
     *         The listener provider(s) to remove from the list of listener providers.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder removeEventListenerProviders(final Collection<IntFunction<Object>> listenerProviders)
    {
        Checks.noneNull(listenerProviders, "listener providers");

        this.listenerProviders.removeAll(listenerProviders);
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
     * should instead handle the {@link net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}.
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
     * Sets the core pool size for the global JDA
     * {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} which is used
     * in various locations throughout the JDA instance created by this ShardManager. (Default: 2)
     *
     * @param  size
     *         The core pool size for the global JDA executor
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified core pool size is not positive
     *
     * @return The {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setCorePoolSize(int size)
    {
        Checks.positive(size, "Core pool size");
        this.corePoolSize = size;
        return this;
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up the ShardManager and it's JDA instances.
     * <br>When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly close connections.
     *
     * <p>Default: <b>true (enabled)</b>
     *
     * @param  enable
     *         True (default) - use shutdown hook to clean up the ShardManager and it's JDA instances if the Java program is closed.
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
        Checks.notNull(manager, "manager");

        this.eventManager = manager;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for our session.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Hint:</b> You can create a {@link net.dv8tion.jda.core.entities.Game Game} object using
     * {@link net.dv8tion.jda.core.entities.Game#playing(String) Game.playing(String)} or
     * {@link net.dv8tion.jda.core.entities.Game#streaming(String, String)} Game.streaming(String, String)}.
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
        return this.setGameProvider(id -> game);
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for our session.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Hint:</b> You can create a {@link net.dv8tion.jda.core.entities.Game Game} object using
     * {@link net.dv8tion.jda.core.entities.Game#playing(String) Game.playing(String)} or
     * {@link net.dv8tion.jda.core.entities.Game#streaming(String, String) Game.streaming(String, String)}.
     *
     * @param  gameProvider
     *         An instance of {@link net.dv8tion.jda.core.entities.Game Game} (null allowed)
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setGame(Game)
     */
    public DefaultShardManagerBuilder setGameProvider(final IntFunction<Game> gameProvider)
    {
        this.gameProvider = gameProvider;
        return this;
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br>This value can be changed at any time using
     * {@link DefaultShardManager#setIdle(boolean) DefaultShardManager#setIdleProvider(boolean)}.
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
        return this.setIdleProvider(id -> idle);
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br>This value can be changed at any time using
     * {@link DefaultShardManager#setIdle(boolean) DefaultShardManager#setIdleProvider(boolean)}.
     *
     * @param  idleProvider
     *         boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. <b>(default false)</b>
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setIdle(boolean)
     */
    public DefaultShardManagerBuilder setIdleProvider(final IntFunction<Boolean> idleProvider)
    {
        this.idleProvider = idleProvider;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} our connection will display.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Note:</b>This will not take affect for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     * if the statusProvider specified in the user_settings is not "online" as it is overriding our identify statusProvider.
     *
     * @param  status
     *         Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is null or {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setStatus(OnlineStatus) Presence.setStatusProvider(OnlineStatus)
     */
    public DefaultShardManagerBuilder setStatus(final OnlineStatus status)
    {
        Checks.notNull(status, "status");
        Checks.check(status != OnlineStatus.UNKNOWN, "OnlineStatus cannot be unknown!");

        return this.setStatusProvider(id -> status);
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} our connection will display.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Note:</b>This will not take affect for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     * if the statusProvider specified in the user_settings is not "online" as it is overriding our identify statusProvider.
     *
     * @param  statusProvider
     *         Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is null or {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setStatus(OnlineStatus) Presence.setStatusProvider(OnlineStatus)
     */
    public DefaultShardManagerBuilder setStatusProvider(final IntFunction<OnlineStatus> statusProvider)
    {
        this.statusProvider = statusProvider;
        return this;
    }

    /**
     * Sets the {@link java.util.concurrent.ThreadFactory ThreadFactory} that will be used by the internal executor
     * of the ShardManager.
     * <p>Note: This will not affect Threads created by any JDA instance.
     *
     * @param  threadFactory
     *         The ThreadFactory or {@code null} to reset to the default value.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setThreadFactory(final ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Sets the {@link okhttp3.OkHttpClient.Builder Builder} that will be used by JDA's requester.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  builder
     *         The new {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder} to use.
     *
     * @return Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setHttpClientBuilder(OkHttpClient.Builder builder)
    {
        this.httpClientBuilder = builder;
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
     * Whether the Requester should retry when
     * a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     * <br><b>Default</b>: {@code true}
     *
     * <p>This value can be changed at any time with {@link net.dv8tion.jda.core.JDA#setRequestTimeoutRetry(boolean) JDA.setRequestTimeoutRetry(boolean)}!
     *
     * @param  retryOnTimeout
     *         True, if the Request should retry once on a socket timeout
     *
     * @return The {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public DefaultShardManagerBuilder setRequestTimeoutRetry(boolean retryOnTimeout)
    {
        this.retryOnTimeout = retryOnTimeout;
        return this;
    }

    /**
     * Sets the list of shards the {@link DefaultShardManager DefaultShardManager} should contain.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  shardIds
     *         The list of shard ids
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
     * Sets the range of shards the {@link DefaultShardManager DefaultShardManager} should contain.
     * This is useful if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  minShardId
     *         The lowest shard id the DefaultShardManager should contain
     *
     * @param  maxShardId
     *         The highest shard id the DefaultShardManager should contain
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
     * Sets the range of shards the {@link DefaultShardManager DefaultShardManager} should contain.
     * This is useful if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  shardIds
     *         The list of shard ids
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
        for (Integer id : shardIds)
        {
            Checks.notNegative(id, "minShardId");
            Checks.check(id < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        }

        this.shards = new ArrayList<>(shardIds);

        return this;
    }

    /**
     * This will set the total amount of shards the {@link DefaultShardManager DefaultShardManager} should use.
     * <p> If this is set to {@code -1} JDA will automatically retrieve the recommended amount of shards from discord (default behavior).
     *
     * @param  shardsTotal
     *         The number of overall shards or {@code -1} if JDA should use the recommended amount from discord.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    #setShards(int, int)
     */
    public DefaultShardManagerBuilder setShardsTotal(final int shardsTotal)
    {
        Checks.check(shardsTotal == -1 || shardsTotal > 0, "shardsTotal must either be -1 or greater than 0");
        this.shardsTotal = shardsTotal;

        return this;
    }

    /**
     * Sets the token that will be used by the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance to log in when
     * {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#build() build()} is called.
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
     * Whether the {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} should use
     * {@link net.dv8tion.jda.core.JDA#shutdownNow() JDA#shutdownNow()} instead of
     * {@link net.dv8tion.jda.core.JDA#shutdown() JDA#shutdown()} to shutdown it's shards.
     * <br><b>Default</b>: {@code false}
     *
     * @param  useShutdownNow
     *         Whether the ShardManager should use JDA#shutdown() or not
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder} instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.core.JDA#shutdown()
     * @see net.dv8tion.jda.core.JDA#shutdownNow()
     */
    public DefaultShardManagerBuilder setUseShutdownNow(final boolean useShutdownNow)
    {
        this.useShutdownNow = useShutdownNow;
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
     * Builds a new {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance and uses the provided token to start the login process.
     * <br>The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} has not
     * finished loading, thus many {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} methods have the chance to return incorrect information.
     * <br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     * <p>Note that this method is async and as such will <b>not</b> block until all shards are started.
     *
     * @throws  LoginException
     *          If the provided token is invalid.
     * @throws  IllegalArgumentException
     *          If the provided token is empty or null.
     *
     * @return A {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instance that has started the login process. It is unknown as
     *         to whether or not loading has finished when this returns.
     */
    public ShardManager build() throws LoginException, IllegalArgumentException
    {
        final DefaultShardManager manager = new DefaultShardManager(
            this.shardsTotal, this.shards, this.sessionController,
            this.listeners, this.listenerProviders, this.token, this.eventManager,
            this.audioSendFactory, this.gameProvider, this.statusProvider,
            this.httpClientBuilder, this.wsFactory, this.threadFactory,
            this.maxReconnectDelay, this.corePoolSize, this.enableVoice, this.enableShutdownHook, this.enableBulkDeleteSplitting,
            this.autoReconnect, this.idleProvider, this.retryOnTimeout, this.useShutdownNow, this.enableContext, this.contextProvider, this.enableCompression);

        manager.login();

        return manager;
    }
}
