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
 * Used to create new {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} instances. This is also useful for making sure all of
 * your {@link net.dv8tion.jda.core.hooks.EventListener EventListeners} are registered
 * before {@link net.dv8tion.jda.core.JDA} attempts to log in.
 *
 * <p>A single JDABuilder can be reused multiple times. Each call to
 * {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildAsync() buildAsync()} or
 * {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildBlocking() buildBlocking()}
 * creates a new {@link net.dv8tion.jda.core.JDA} instance using the same information.
 * This means that you can have listeners easily registered to multiple {@link net.dv8tion.jda.core.JDA} instances.
 */
public class ShardManagerBuilder
{
    final JDABuilder builder = new JDABuilder(AccountType.BOT);
    private int maxShardId = -1;
    private int minShardId = -1;
    private int shardsTotal;

    /**
     * TODO: completely change docs
     * Creates a completely empty JDABuilder.
     * <br>If you use this, you need to set the  token using
     * {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#setToken(String) setToken(String)}
     * before calling {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder#buildBlocking() buildBlocking()}
     *
     * @param  accountType
     *         The {@link net.dv8tion.jda.core.AccountType AccountType}.
     */
    public ShardManagerBuilder(final int shardsTotal)
    {
        this.setShardTotal(shardsTotal);
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link net.dv8tion.jda.core.JDA} object.
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
     * @see    net.dv8tion.jda.core.JDA#addEventListener(Object...) JDA.addEventListener(Object...)
     */
    public ShardManagerBuilder addEventListener(final Object... listeners)
    {
        this.builder.addEventListener(listeners);
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
        final ShardManager manager = new ShardManager(this.builder, this.shardsTotal, this.minShardId, this.maxShardId);

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
        this.builder.removeEventListener(listeners);
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
        this.builder.setAudioEnabled(enabled);
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
        this.builder.setAudioSendFactory(factory);
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
        this.builder.setAutoReconnect(autoReconnect);
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
        this.builder.setBulkDeleteSplittingEnabled(enabled);
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
     * @return Return the {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder JDABuilder } instance. Useful for chaining.
     */
    public ShardManagerBuilder setEnableShutdownHook(final boolean enable)
    {
        this.builder.setEnableShutdownHook(enable);
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
        this.builder.setEventManager(manager);
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for our session.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Hint:</b> You can create a {@link net.dv8tion.jda.core.entities.Game Game} object using
     * {@link net.dv8tion.jda.core.entities.Game#of(String)} or {@link net.dv8tion.jda.core.entities.Game#of(String, String)}.
     *
     * @param  game
     *         An instance of {@link net.dv8tion.jda.core.entities.Game Game} (null allowed)
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setGame(Game)  Presence.setGame(Game)
     */
    public ShardManagerBuilder setGame(final Game game)
    {
        this.builder.setGame(game);
        return this;
    }

    /**
     * Sets whether or not we should mark our session as afk
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.core.managers.Presence Presence} from a JDA instance.
     *
     * @param  idle
     *         boolean value that will be provided with our IDENTIFY package to mark our session as afk or not. <b>(default false)</b>
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.managers.Presence#setIdle(boolean) Presence#setIdle(boolean)
     */
    public ShardManagerBuilder setIdle(final boolean idle)
    {
        this.builder.setIdle(idle);
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
     * @return Returns the {@link net.dv8tion.jda.core.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setMaxReconnectDelay(final int maxReconnectDelay)
    {
        this.builder.setMaxReconnectDelay(maxReconnectDelay);
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
     */
    public ShardManagerBuilder setProxy(final HttpHost proxy)
    {
        this.builder.setProxy(proxy);
        return this;
    }

    public ShardManagerBuilder setShardRange(final int lowerBound, final int upperBound)
    {
        Args.notNegative(lowerBound, "minShardId");
        Args.check(upperBound < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        Args.check(lowerBound <= upperBound, "minShardId must be lower than or equal to maxShardId");

        this.minShardId = lowerBound;
        this.maxShardId = upperBound;
        return this;
    }

    /**
     * TODO
     * This will enable sharding mode for JDA.
     * <br>In sharding mode, guilds are split up and assigned one of multiple shards (clients).
     * <br>The shardId that receives all stuff related to given bot is calculated as follows: shardId == (guildId {@literal >>} 22) % shardTotal;
     * <br><b>PMs are only sent to shard 0.</b>
     *
     * <p>Please note, that a shard will not even know about guilds not assigned to.
     *
     * @param  shardId
     *         The id of this shard (starting at 0).
     * @param  shardTotal
     *         The number of overall shards.
     *
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.JDA#getShardInfo() JDA.getShardInfo()
     */
    public ShardManagerBuilder setShardTotal(final int shardsTotal)
    {
        Args.positive(shardsTotal, "shardsTotal");
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
        this.builder.setStatus(status);
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
     * @return The {@link net.dv8tion.jda.bot.sharding.ShardManagerBuilder ShardManagerBuilder} instance. Useful for chaining.
     */
    public ShardManagerBuilder setToken(final String token)
    {
        this.builder.setToken(token);
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
        this.builder.setWebSocketTimeout(websocketTimeout);
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
