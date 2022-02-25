/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.IGuildChannelContainer;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import okhttp3.OkHttpClient;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * The core of JDA. Acts as a registry system of JDA. All parts of the the API can be accessed starting from this class.
 *
 * @see JDABuilder
 */
public interface JDA extends IGuildChannelContainer
{
    /**
     * Represents the connection status of JDA and its Main WebSocket.
     */
    enum Status
    {
        /**JDA is currently setting up supporting systems like the AudioSystem.*/
        INITIALIZING(true),
        /**JDA has finished setting up supporting systems and is ready to log in.*/
        INITIALIZED(true),
        /**JDA is currently attempting to log in.*/
        LOGGING_IN(true),
        /**JDA is currently attempting to connect it's websocket to Discord.*/
        CONNECTING_TO_WEBSOCKET(true),
        /**JDA has successfully connected it's websocket to Discord and is sending authentication*/
        IDENTIFYING_SESSION(true),
        /**JDA has sent authentication to discord and is awaiting confirmation*/
        AWAITING_LOGIN_CONFIRMATION(true),
        /**JDA is populating internal objects.
         * This process often takes the longest of all Statuses (besides CONNECTED)*/
        LOADING_SUBSYSTEMS(true),
        /**JDA has finished loading everything, is receiving information from Discord and is firing events.*/
        CONNECTED(true),
        /**JDA's main websocket has been disconnected. This <b>DOES NOT</b> mean JDA has shutdown permanently.
         * This is an in-between status. Most likely ATTEMPTING_TO_RECONNECT or SHUTTING_DOWN/SHUTDOWN will soon follow.*/
        DISCONNECTED,
        /** JDA session has been added to {@link net.dv8tion.jda.api.utils.SessionController SessionController}
         * and is awaiting to be dequeued for reconnecting.*/
        RECONNECT_QUEUED,
        /**When trying to reconnect to Discord JDA encountered an issue, most likely related to a lack of internet connection,
         * and is waiting to try reconnecting again.*/
        WAITING_TO_RECONNECT,
        /**JDA has been disconnected from Discord and is currently trying to reestablish the connection.*/
        ATTEMPTING_TO_RECONNECT,
        /**JDA has received a shutdown request or has been disconnected from Discord and reconnect is disabled, thus,
         * JDA is in the process of shutting down*/
        SHUTTING_DOWN,
        /**JDA has finished shutting down and this instance can no longer be used to communicate with the Discord servers.*/
        SHUTDOWN,
        /**While attempting to authenticate, Discord reported that the provided authentication information was invalid.*/
        FAILED_TO_LOGIN;

        private final boolean isInit;

        Status(boolean isInit)
        {
            this.isInit = isInit;
        }

        Status()
        {
            this.isInit = false;
        }

        public boolean isInit()
        {
            return isInit;
        }
    }

    /**
     * Represents the information used to create this shard.
     */
    class ShardInfo
    {
        /** Default sharding config with one shard */
        public static final ShardInfo SINGLE = new ShardInfo(0, 1);

        int shardId;
        int shardTotal;

        public ShardInfo(int shardId, int shardTotal)
        {
            this.shardId = shardId;
            this.shardTotal = shardTotal;
        }

        /**
         * Represents the id of the shard of the current instance.
         * <br>This value will be between 0 and ({@link #getShardTotal()} - 1).
         *
         * @return The id of the currently logged in shard.
         */
        public int getShardId()
        {
            return shardId;
        }

        /**
         * The total amount of shards based on the value provided during JDA instance creation using
         * {@link JDABuilder#useSharding(int, int)}.
         * <br>This <b>does not</b> query Discord to determine the total number of shards.
         * <br>This <b>does not</b> represent the amount of logged in shards.
         * <br>It strictly represents the integer value provided to discord
         * representing the total amount of shards that the developer indicated that it was going to use when
         * initially starting JDA.
         *
         * @return The total of shards based on the total provided by the developer during JDA initialization.
         */
        public int getShardTotal()
        {
            return shardTotal;
        }

        /**
         * Provides a shortcut method for easily printing shard info.
         * <br>Format: "[# / #]"
         * <br>Where the first # is shardId and the second # is shardTotal.
         *
         * @return A String representing the information used to build this shard.
         */
        public String getShardString()
        {
            return "[" + shardId + " / " + shardTotal + "]";
        }

        @Override
        public String toString()
        {
            return "Shard " + getShardString();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof ShardInfo))
                return false;

            ShardInfo oInfo = (ShardInfo) o;
            return shardId == oInfo.getShardId() && shardTotal == oInfo.getShardTotal();
        }
    }

    /**
     * Gets the current {@link net.dv8tion.jda.api.JDA.Status Status} of the JDA instance.
     *
     * @return Current JDA status.
     */
    @Nonnull
    Status getStatus();

    /**
     * The {@link GatewayIntent GatewayIntents} for this JDA session.
     *
     * @return {@link EnumSet} of active gateway intents
     */
    @Nonnull
    EnumSet<GatewayIntent> getGatewayIntents();

    /**
     * The {@link CacheFlag cache flags} that have been enabled for this JDA session.
     *
     * @return Copy of the EnumSet of cache flags for this session
     */
    @Nonnull
    EnumSet<CacheFlag> getCacheFlags();

    /**
     * Attempts to remove the user with the provided id from the cache.
     * <br>If you attempt to remove the {@link #getSelfUser() SelfUser} this will simply return {@code false}.
     *
     * <p>This should be used by an implementation of {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * as an upstream request to remove a member.
     *
     * @param  userId
     *         The target user id
     *
     * @return True, if the cache was changed
     */
    boolean unloadUser(long userId);

    /**
     * The time in milliseconds that discord took to respond to our last heartbeat
     * <br>This roughly represents the WebSocket ping of this session
     *
     * <p><b>{@link net.dv8tion.jda.api.requests.RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * <p>The {@link net.dv8tion.jda.api.events.GatewayPingEvent GatewayPingEvent} indicates an update to this value.
     *
     * @return time in milliseconds between heartbeat and the heartbeat ack response
     *
     * @see    #getRestPing() Getting RestAction ping
     */
    long getGatewayPing();

    /**
     * The time in milliseconds that discord took to respond to a REST request.
     * <br>This will request the current user from the API and calculate the time the response took.
     *
     * <h4>Example</h4>
     * <pre><code>
     * jda.getRestPing().queue( (time) {@literal ->}
     *     channel.sendMessageFormat("Ping: %d ms", time).queue()
     * );
     * </code></pre>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: long
     *
     * @since 4.0.0
     *
     * @see    #getGatewayPing()
     */
    @Nonnull
    default RestAction<Long> getRestPing()
    {
        AtomicLong time = new AtomicLong();
        Route.CompiledRoute route = Route.Self.GET_SELF.compile();
        RestActionImpl<Long> action = new RestActionImpl<>(this, route, (response, request) -> System.currentTimeMillis() - time.get());
        action.setCheck(() ->
        {
            time.set(System.currentTimeMillis());
            return true;
        });
        return action;
    }

    /**
     * This method will block until JDA has reached the specified connection status.
     *
     * <h4>Login Cycle</h4>
     * <ol>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#INITIALIZING INITIALIZING}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#INITIALIZED INITIALIZED}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#LOGGING_IN LOGGING_IN}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#CONNECTING_TO_WEBSOCKET CONNECTING_TO_WEBSOCKET}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#IDENTIFYING_SESSION IDENTIFYING_SESSION}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#AWAITING_LOGIN_CONFIRMATION AWAITING_LOGIN_CONFIRMATION}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#LOADING_SUBSYSTEMS LOADING_SUBSYSTEMS}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#CONNECTED CONNECTED}</li>
     * </ol>
     *
     * @param  status
     *         The init status to wait for, once JDA has reached the specified
     *         stage of the startup cycle this method will return.
     *
     * @throws InterruptedException
     *         If this thread is interrupted while waiting
     * @throws IllegalArgumentException
     *         If the provided status is null or not an init status ({@link Status#isInit()})
     * @throws IllegalStateException
     *         If JDA is shutdown during this wait period
     *
     * @return The current JDA instance, for chaining convenience
     */
    @Nonnull
    default JDA awaitStatus(@Nonnull JDA.Status status) throws InterruptedException
    {
        //This is done to retain backwards compatible ABI as it would otherwise change the signature of the method
        // which would require recompilation for all users (including extension libraries)
        return awaitStatus(status, new JDA.Status[0]);
    }

    /**
     * This method will block until JDA has reached the specified connection status.
     *
     * <h4>Login Cycle</h4>
     * <ol>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#INITIALIZING INITIALIZING}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#INITIALIZED INITIALIZED}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#LOGGING_IN LOGGING_IN}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#CONNECTING_TO_WEBSOCKET CONNECTING_TO_WEBSOCKET}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#IDENTIFYING_SESSION IDENTIFYING_SESSION}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#AWAITING_LOGIN_CONFIRMATION AWAITING_LOGIN_CONFIRMATION}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#LOADING_SUBSYSTEMS LOADING_SUBSYSTEMS}</li>
     *  <li>{@link net.dv8tion.jda.api.JDA.Status#CONNECTED CONNECTED}</li>
     * </ol>
     *
     * @param  status
     *         The init status to wait for, once JDA has reached the specified
     *         stage of the startup cycle this method will return.
     * @param  failOn
     *         Optional failure states that will force a premature return
     *
     * @throws InterruptedException
     *         If this thread is interrupted while waiting
     * @throws IllegalArgumentException
     *         If the provided status is null or not an init status ({@link Status#isInit()})
     * @throws IllegalStateException
     *         If JDA is shutdown during this wait period
     *
     * @return The current JDA instance, for chaining convenience
     */
    @Nonnull
    JDA awaitStatus(@Nonnull JDA.Status status, @Nonnull JDA.Status... failOn) throws InterruptedException;

    /**
     * This method will block until JDA has reached the status {@link Status#CONNECTED}.
     * <br>This status means that JDA finished setting up its internal cache and is ready to be used.
     *
     * @throws InterruptedException
     *         If this thread is interrupted while waiting
     * @throws IllegalStateException
     *         If JDA is shutdown during this wait period
     *
     * @return The current JDA instance, for chaining convenience
     */
    @Nonnull
    default JDA awaitReady() throws InterruptedException
    {
        return awaitStatus(Status.CONNECTED);
    }

    /**
     * Cancels all currently scheduled {@link RestAction} requests.
     * <br>When a {@link RestAction} is cancelled, a {@link java.util.concurrent.CancellationException} will be provided
     * to the failure callback. This means {@link RestAction#queue(Consumer, Consumer)} will invoke the second callback
     * and {@link RestAction#complete()} will throw an exception.
     *
     * <p><b>This is only recommended as an extreme last measure to avoid backpressure.</b>
     * If you want to stop requests on shutdown you should use {@link #shutdownNow()} instead of this method.
     *
     * @return how many requests were cancelled
     *
     * @see    RestAction#setCheck(BooleanSupplier)
     */
    int cancelRequests();

    /**
     * {@link ScheduledExecutorService} used to handle rate-limits for {@link RestAction}
     * executions. This is also used in other parts of JDA related to http requests.
     *
     * @return The {@link ScheduledExecutorService} used for http request handling
     *
     * @since 4.0.0
     */
    @Nonnull
    ScheduledExecutorService getRateLimitPool();

    /**
     * {@link ScheduledExecutorService} used to send WebSocket messages to discord.
     * <br>This involves initial setup of guilds as well as keeping the connection alive.
     *
     * @return The {@link ScheduledExecutorService} used for WebSocket transmissions
     *
     * @since 4.0.0
     */
    @Nonnull
    ScheduledExecutorService getGatewayPool();

    /**
     * {@link ExecutorService} used to handle {@link RestAction} callbacks
     * and completions. This is also used for handling {@link net.dv8tion.jda.api.entities.Message.Attachment} downloads
     * when needed.
     * <br>By default this uses the {@link ForkJoinPool#commonPool() CommonPool} of the runtime.
     *
     * @return The {@link ExecutorService} used for callbacks
     *
     * @since 4.0.0
     */
    @Nonnull
    ExecutorService getCallbackPool();

    /**
     * The {@link OkHttpClient} used for handling http requests from {@link RestAction RestActions}.
     *
     * @return The http client
     *
     * @since 4.0.0
     */
    @Nonnull
    OkHttpClient getHttpClient();

    /**
     * Direct access to audio (dis-)connect requests.
     * <br>This should not be used when normal audio operation is desired.
     *
     * <p>The correct way to open and close an audio connection is through the {@link Guild Guild's}
     * {@link AudioManager}.
     *
     * @throws IllegalStateException
     *         If {@link GatewayIntent#GUILD_VOICE_STATES} is disabled
     *
     * @return The {@link DirectAudioController} for this JDA instance
     *
     * @since 4.0.0
     */
    @Nonnull
    DirectAudioController getDirectAudioController();

    /**
     * Changes the internal EventManager.
     *
     * <p>The default EventManager is {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener}.
     * <br>There is also an {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager} available.
     *
     * @param  manager
     *         The new EventManager to use
     */
    void setEventManager(@Nullable IEventManager manager);

    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     * This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager}, use {@link #setEventManager(IEventManager)}.
     *
     * Note: when using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    void addEventListener(@Nonnull Object... listeners);

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     *         The listener(s) to be removed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    void removeEventListener(@Nonnull Object... listeners);

    /**
     * Immutable List of Objects that have been registered as EventListeners.
     *
     * @return List of currently registered Objects acting as EventListeners.
     */
    @Nonnull
    List<Object> getRegisteredListeners();

    /**
     * Retrieves the list of global commands.
     * <br>This list does not include guild commands! Use {@link Guild#retrieveCommands()} for guild commands.
     *
     * @return {@link RestAction} - Type: {@link List} of {@link Command}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Command>> retrieveCommands();

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The command id
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link Command}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Command> retrieveCommandById(@Nonnull String id);

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The command id
     *
     * @return {@link RestAction} - Type: {@link Command}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Command> retrieveCommandById(long id)
    {
        return retrieveCommandById(Long.toUnsignedString(id));
    }

    /**
     * Creates or updates a global command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * @param  command
     *         The {@link CommandData} for the command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link RestAction} - Type: {@link Command}
     *         <br>The RestAction used to create or update the command
     *
     * @see    Commands#slash(String, String) Commands.slash(...)
     * @see    Commands#message(String) Commands.message(...)
     * @see    Commands#user(String) Commands.user(...)
     * @see    Guild#upsertCommand(CommandData) Guild.upsertCommand(...)
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Command> upsertCommand(@Nonnull CommandData command);

    /**
     * Creates or updates a global slash command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     * @param  description
     *         The description for the command, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name/description do not meet the requirements
     *
     * @return {@link CommandCreateAction}
     *
     * @see Guild#upsertCommand(String, String)
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction upsertCommand(@Nonnull String name, @Nonnull String description)
    {
        return (CommandCreateAction) upsertCommand(new CommandDataImpl(name, description));
    }

    /**
     * Configures the complete list of global commands.
     * <br>This will replace the existing command list for this bot. You should only use this once on startup!
     *
     * <p>This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * <h4>Examples</h4>
     * <pre>{@code
     * // Set list to 2 commands
     * jda.updateCommands()
     *   .addCommands(Commands.slash("ping", "Gives the current ping"))
     *   .addCommands(Commands.slash("ban", "Ban the target user")
     *     .addOption(OptionType.USER, "user", "The user to ban", true))
     *   .queue();
     * // Delete all commands
     * jda.updateCommands().queue();
     * }</pre>
     *
     * @return {@link CommandListUpdateAction}
     *
     * @see    Guild#updateCommands()
     */
    @Nonnull
    @CheckReturnValue
    CommandListUpdateAction updateCommands();

    /**
     * Edit an existing global command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * @param  id
     *         The id of the command to edit
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link CommandEditAction} used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction editCommandById(@Nonnull String id);

    /**
     * Edit an existing global command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * @param  id
     *         The id of the command to edit
     *
     * @return {@link CommandEditAction} used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction editCommandById(long id)
    {
        return editCommandById(Long.toUnsignedString(id));
    }

    /**
     * Delete the global command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * @param  commandId
     *         The id of the command that should be deleted
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> deleteCommandById(@Nonnull String commandId);

    /**
     * Delete the global command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * <p><b>Global commands can take up to <u>1 hour</u> to propagate to the clients.</b>
     * For testing, it is recommended to use a test guild with guild commands.
     *
     * @param  commandId
     *         The id of the command that should be deleted
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteCommandById(long commandId)
    {
        return deleteCommandById(Long.toUnsignedString(commandId));
    }

    /**
     * Constructs a new {@link Guild Guild} with the specified name
     * <br>Use the returned {@link GuildAction GuildAction} to provide
     * further details and settings for the resulting Guild!
     *
     * <p>This RestAction does not provide the resulting Guild!
     * It will be in a following {@link net.dv8tion.jda.api.events.guild.GuildJoinEvent GuildJoinEvent}.
     *
     * @param  name
     *         The name of the resulting guild
     *
     * @throws java.lang.IllegalStateException
     *         If the currently logged in account is in 10 or more guilds
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is empty, {@code null} or not between 2-100 characters
     *
     * @return {@link GuildAction GuildAction}
     *         <br>Allows for setting various details for the resulting Guild
     */
    @Nonnull
    @CheckReturnValue
    GuildAction createGuild(@Nonnull String name);

    /**
     * Constructs a new {@link Guild Guild} from the specified template code.
     *
     * <p>This RestAction does not provide the resulting Guild!
     * It will be in a following {@link net.dv8tion.jda.api.events.guild.GuildJoinEvent GuildJoinEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD_TEMPLATE Unknown Guild Template}
     *     <br>The template doesn't exist.</li>
     * </ul>
     *
     * @param  code
     *         The template code to use to create a guild
     * @param  name
     *         The name of the resulting guild
     * @param  icon
     *         The {@link net.dv8tion.jda.api.entities.Icon Icon} to use, or null to use no icon
     *
     * @throws java.lang.IllegalStateException
     *         If the currently logged in account is in 10 or more guilds
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is empty, {@code null} or not between 2-100 characters
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> createGuildFromTemplate(@Nonnull String code, @Nonnull String name, @Nullable Icon icon);

    /**
     * {@link net.dv8tion.jda.api.utils.cache.CacheView CacheView} of
     * all cached {@link net.dv8tion.jda.api.managers.AudioManager AudioManagers} created for this JDA instance.
     * <br>AudioManagers are created when first retrieved via {@link Guild#getAudioManager() Guild.getAudioManager()}.
     * <u>Using this will perform better than calling {@code Guild.getAudioManager()} iteratively as that would cause many useless audio managers to be created!</u>
     *
     * <p>AudioManagers are cross-session persistent!
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.CacheView CacheView}
     */
    @Nonnull
    CacheView<AudioManager> getAudioManagerCache();

    /**
     * Immutable list of all created {@link net.dv8tion.jda.api.managers.AudioManager AudioManagers} for this JDA instance!
     *
     * @return Immutable list of all created AudioManager instances
     */
    @Nonnull
    default List<AudioManager> getAudioManagers()
    {
        return getAudioManagerCache().asList();
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all <b>cached</b> {@link net.dv8tion.jda.api.entities.User Users} visible to this JDA session.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<User> getUserCache();

    /**
     * An immutable list of all {@link net.dv8tion.jda.api.entities.User Users} that share a
     * {@link Guild Guild} with the currently logged in account.
     * <br>This list will never contain duplicates and represents all
     * {@link net.dv8tion.jda.api.entities.User Users} that JDA can currently see.
     *
     * <p><b>This will only check cached users!</b>
     *
     * <p>If the developer is sharding, then only users from guilds connected to the specifically logged in
     * shard will be returned in the List.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getUserCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Immutable list of all {@link net.dv8tion.jda.api.entities.User Users} that are visible to JDA.
     */
    @Nonnull
    default List<User> getUsers()
    {
        return getUserCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.User User} with matching id.
     *
     * @see    #retrieveUserById(String)
     */
    @Nullable
    default User getUserById(@Nonnull String id)
    {
        return getUserCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.User User} with matching id.
     *
     * @see    #retrieveUserById(long)
     */
    @Nullable
    default User getUserById(long id)
    {
        return getUserCache().getElementById(id);
    }

    /**
     * Searches for a user that has the matching Discord Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     * <p>This only checks users that are known to the currently logged in account (shard). If a user exists
     * with the tag that is not available in the {@link #getUserCache() User-Cache} it will not be detected.
     * <br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  tag
     *         The Discord Tag in the format {@code Username#Discriminator}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided tag is null or not in the described format
     *
     * @return The {@link net.dv8tion.jda.api.entities.User} for the discord tag or null if no user has the provided tag
     */
    @Nullable
    default User getUserByTag(@Nonnull String tag)
    {
        Checks.notNull(tag, "Tag");
        Matcher matcher = User.USER_TAG.matcher(tag);
        Checks.check(matcher.matches(), "Invalid tag format!");
        String username = matcher.group(1);
        String discriminator = matcher.group(2);
        return getUserByTag(username, discriminator);
    }

    /**
     * Searches for a user that has the matching Discord Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     * <p>This only checks users that are known to the currently logged in account (shard). If a user exists
     * with the tag that is not available in the {@link #getUserCache() User-Cache} it will not be detected.
     * <br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  username
     *         The name of the user
     * @param  discriminator
     *         The discriminator of the user
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided arguments are null or not in the described format
     *
     * @return The {@link net.dv8tion.jda.api.entities.User} for the discord tag or null if no user has the provided tag
     */
    @Nullable
    default User getUserByTag(@Nonnull String username, @Nonnull String discriminator)
    {
        Checks.notNull(username, "Username");
        Checks.notNull(discriminator, "Discriminator");
        Checks.check(discriminator.length() == 4 && Helpers.isNumeric(discriminator), "Invalid format for discriminator!");
        int codePointLength = Helpers.codePointLength(username);
        Checks.check(codePointLength >= 2 && codePointLength <= 32, "Username must be between 2 and 32 codepoints in length!");
        return getUserCache().applyStream(stream ->
            stream.filter(it -> it.getDiscriminator().equals(discriminator))
                  .filter(it -> it.getName().equals(username))
                  .findFirst()
                  .orElse(null)
        );
    }

    /**
     * This immutable returns all {@link net.dv8tion.jda.api.entities.User Users} that have the same username as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.User Users} with the provided name, then this returns an empty list.
     *
     * <p><b>This will only check cached users!</b>
     *
     * <p><b>Note: </b> This does **not** consider nicknames, it only considers {@link net.dv8tion.jda.api.entities.User#getName()}
     *
     * @param  name
     *         The name of the requested {@link net.dv8tion.jda.api.entities.User Users}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.api.entities.User#getName()}.
     *
     * @return Possibly-empty immutable list of {@link net.dv8tion.jda.api.entities.User Users} that all have the same name as the provided name.
     */
    @Nonnull
    default List<User> getUsersByName(@Nonnull String name, boolean ignoreCase)
    {
        return getUserCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets all {@link Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link Guild Guilds} must contain.
     *
     * @return Immutable list of all {@link Guild Guild} instances which have all {@link net.dv8tion.jda.api.entities.User Users} in them.
     *
     * @see    Guild#isMember(net.dv8tion.jda.api.entities.User)
     */
    @Nonnull
    List<Guild> getMutualGuilds(@Nonnull User... users);

    /**
     * Gets all {@link Guild Guilds} that contain all given users as their members.
     *
     * @param users
     *        The users which all the returned {@link Guild Guilds} must contain.
     *
     * @return Immutable list of all {@link Guild Guild} instances which have all {@link net.dv8tion.jda.api.entities.User Users} in them.
     */
    @Nonnull
    List<Guild> getMutualGuilds(@Nonnull Collection<User> users);

    /**
     * Attempts to retrieve a {@link net.dv8tion.jda.api.entities.User User} object based on the provided id.
     * <br>This first calls {@link #getUserById(long)}, and if that returns {@code null} or the cache is inconsistent due to disabled intents then a request
     * is made to the Discord servers.
     *
     * <p>When the both {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} and {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intents
     * are disabled this will always make a request even if the user is cached. You can use {@link #retrieveUserById(String, boolean)} to disable this behavior.
     *
     * <p>The returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} can encounter the following Discord errors:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link net.dv8tion.jda.api.entities.User User}
     *     known by Discord. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         This endpoint is {@link AccountType#BOT} only.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided id String is null.</li>
     *             <li>If the provided id String is empty.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.User User}
     *         <br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveUserById(@Nonnull String id)
    {
        return retrieveUserById(id, true);
    }

    /**
     * Attempts to retrieve a {@link net.dv8tion.jda.api.entities.User User} object based on the provided id.
     * <br>This first calls {@link #getUserById(long)}, and if that returns {@code null} or the cache is inconsistent due to disabled intents then a request
     * is made to the Discord servers.
     *
     * <p>When the both {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} and {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intents
     * are disabled this will always make a request even if the user is cached. You can use {@link #retrieveUserById(String, boolean)} to disable this behavior.
     *
     * <p>The returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} can encounter the following Discord errors:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link net.dv8tion.jda.api.entities.User User}
     *     known by Discord. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         This endpoint is {@link AccountType#BOT} only.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.User User}
     *         <br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveUserById(long id)
    {
        return retrieveUserById(id, true);
    }

    /**
     * Attempts to retrieve a {@link net.dv8tion.jda.api.entities.User User} object based on the provided id.
     * <br>If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intents are disabled
     * this method will update the cached user unless the {@code update} parameter is {@code false}.
     * <br>If either of those intents is enabled, this will immediately provide the cached user if possible.
     *
     * <p>The returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} can encounter the following Discord errors:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link net.dv8tion.jda.api.entities.User User}
     *     known by Discord. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     * @param  update
     *         Whether JDA should perform a request even if the member is already cached to update properties such as the name
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         This endpoint is {@link AccountType#BOT} only.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided id String is null.</li>
     *             <li>If the provided id String is empty.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.User User}
     *         <br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveUserById(@Nonnull String id, boolean update)
    {
        return retrieveUserById(MiscUtil.parseSnowflake(id), update);
    }

    /**
     * Attempts to retrieve a {@link net.dv8tion.jda.api.entities.User User} object based on the provided id.
     * <br>If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intents are disabled
     * this method will update the cached user unless the {@code update} parameter is {@code false}.
     * <br>If either of those intents is enabled, this will immediately provide the cached user if possible.
     *
     * <p>The returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} can encounter the following Discord errors:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link net.dv8tion.jda.api.entities.User User}
     *     known by Discord. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     * @param  update
     *         Whether JDA should perform a request even if the member is already cached to update properties such as the name
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         This endpoint is {@link AccountType#BOT} only.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.User User}
     *         <br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<User> retrieveUserById(long id, boolean update);

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link Guild Guilds} visible to this JDA session.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<Guild> getGuildCache();

    /**
     * An immutable List of all {@link Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link Guild Guilds}, this will return an empty list.
     *
     * <p>If the developer is sharding ({@link net.dv8tion.jda.api.JDABuilder#useSharding(int, int)}, then this list
     * will only contain the {@link Guild Guilds} that the shard is actually connected to.
     * Discord determines which guilds a shard is connect to using the following format:
     * <br>Guild connected if shardId == (guildId {@literal >>} 22) % totalShards;
     * <br>Source for formula: <a href="https://discord.com/developers/docs/topics/gateway#sharding">Discord Documentation</a>
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getGuildCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty immutable list of all the {@link Guild Guilds} that this account is connected to.
     */
    @Nonnull
    default List<Guild> getGuilds()
    {
        return getGuildCache().asList();
    }

    /**
     * This returns the {@link Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link Guild Guild}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link Guild Guild} with matching id.
     */
    @Nullable
    default Guild getGuildById(@Nonnull String id)
    {
        return getGuildCache().getElementById(id);
    }

    /**
     * This returns the {@link Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link Guild Guild}.
     *
     * @return Possibly-null {@link Guild Guild} with matching id.
     */
    @Nullable
    default Guild getGuildById(long id)
    {
        return getGuildCache().getElementById(id);
    }

    /**
     * An immutable list of all {@link Guild Guilds} that have the same name as the one provided.
     * <br>If there are no {@link Guild Guilds} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name of the requested {@link Guild Guilds}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link Guild#getName()}.
     *
     * @return Possibly-empty immutable list of all the {@link Guild Guilds} that all have the same name as the provided name.
     */
    @Nonnull
    default List<Guild> getGuildsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getGuildCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Set of {@link Guild} IDs for guilds that were marked unavailable by the gateway.
     * <br>When a guild becomes unavailable a {@link net.dv8tion.jda.api.events.guild.GuildUnavailableEvent GuildUnavailableEvent}
     * is emitted and a {@link net.dv8tion.jda.api.events.guild.GuildAvailableEvent GuildAvailableEvent} is emitted
     * when it becomes available again. During the time a guild is unavailable it its not reachable through
     * cache such as {@link #getGuildById(long)}.
     *
     * @return Possibly-empty set of guild IDs for unavailable guilds
     */
    @Nonnull
    Set<String> getUnavailableGuilds();

    /**
     * Whether the guild is unavailable. If this returns true, the guild id should be in {@link #getUnavailableGuilds()}.
     *
     * @param  guildId
     *         The guild id
     *
     * @return True, if this guild is unavailable
     */
    boolean isUnavailable(long guildId);

    /**
     * Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Role Roles} visible to this JDA session.
     *
     * @return Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     *
     * @see    net.dv8tion.jda.api.utils.cache.CacheView#allSnowflakes(java.util.function.Supplier) CacheView.allSnowflakes(...)
     */
    @Nonnull
    SnowflakeCacheView<Role> getRoleCache();

    /**
     * All {@link net.dv8tion.jda.api.entities.Role Roles} this JDA instance can see. <br>This will iterate over each
     * {@link Guild Guild} retrieved from {@link #getGuilds()} and collect its {@link
     * Guild#getRoles() Guild.getRoles()}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getRoleCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Immutable List of all visible Roles
     */
    @Nonnull
    default List<Role> getRoles()
    {
        return getRoleCache().asList();
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Role Role} for the specified ID
     */
    @Nullable
    default Role getRoleById(@Nonnull String id)
    {
        return getRoleCache().getElementById(id);
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Role Role} for the specified ID
     */
    @Nullable
    default Role getRoleById(long id)
    {
        return getRoleCache().getElementById(id);
    }

    /**
     * Retrieves all {@link net.dv8tion.jda.api.entities.Role Roles} visible to this JDA instance.
     * <br>This simply filters the Roles returned by {@link #getRoles()} with the provided name, either using
     * {@link String#equals(Object)} or {@link String#equalsIgnoreCase(String)} on {@link net.dv8tion.jda.api.entities.Role#getName()}.
     *
     * @param  name
     *         The name for the Roles
     * @param  ignoreCase
     *         Whether to use {@link String#equalsIgnoreCase(String)}
     *
     * @return Immutable List of all Roles matching the parameters provided.
     */
    @Nonnull
    default List<Role> getRolesByName(@Nonnull String name, boolean ignoreCase)
    {
        return getRoleCache().getElementsByName(name, ignoreCase);
    }


    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} visible to this JDA session.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<GuildScheduledEvent> getGuildScheduledEventCache();
    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} of all connected
     * {@link net.dv8tion.jda.api.entities.Guild Guilds}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getGuildScheduledEventCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possible-empty list of all known {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents}.
     */
    @Nonnull
    default List<GuildScheduledEvent> getGuildScheduledEvents()
    {
        return getGuildScheduledEventCache().asList();
    }
    /**
     * This returns the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} with a matching id.
     */
    @Nullable
    default GuildScheduledEvent getGuildScheduledEventById(@Nonnull String id)
    {
        return getGuildScheduledEventCache().getElementById(id);
    }
    /**
     * This returns the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} with a matching id.
     */
    @Nullable
    default GuildScheduledEvent getGuildScheduledEventById(long id)
    {
        return getGuildScheduledEventCache().getElementById(id);
    }
    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} that have the same name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name of the requested {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.api.entities.GuildScheduledEvent#getName()}.
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} that all have the
     *         same name as the provided name.
     */
    @Nonnull
    default List<GuildScheduledEvent> getGuildScheduledEventsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getGuildScheduledEventCache().getElementsByName(name, ignoreCase);
    }

    @Nullable
    @Override
    default <T extends Channel> T getChannelById(@Nonnull Class<T> type, long id)
    {
        Checks.notNull(type, "Class");
        Channel channel = getPrivateChannelById(id);
        if (channel != null)
            return type.isInstance(channel) ? type.cast(channel) : null;
        return IGuildChannelContainer.super.getChannelById(type, id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannels} visible to this JDA session.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<PrivateChannel> getPrivateChannelCache();

    /**
     * An unmodifiable list of all known {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannels}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getPrivateChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty list of all {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannels}.
     */
    @Nonnull
    default List<PrivateChannel> getPrivateChannels()
    {
        return getPrivateChannelCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with matching id.
     */
    @Nullable
    default PrivateChannel getPrivateChannelById(@Nonnull String id)
    {
        return getPrivateChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with matching id.
     */
    @Nullable
    default PrivateChannel getPrivateChannelById(long id)
    {
        return getPrivateChannelCache().getElementById(id);
    }

    /**
     * Opens a {@link PrivateChannel} with the provided user by id.
     * <br>This will fail with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     * if the user does not exist.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public void sendMessage(JDA jda, long userId, String content) {
     *     jda.openPrivateChannelById(userId)
     *        .flatMap(channel -> channel.sendMessage(content))
     *        .queue();
     * }
     * }</pre>
     *
     * @param  userId
     *         The id of the target user
     *
     * @throws UnsupportedOperationException
     *         If the target user is the currently logged in account
     *
     * @return {@link RestAction} - Type: {@link PrivateChannel}
     *
     * @see    User#openPrivateChannel()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<PrivateChannel> openPrivateChannelById(long userId);

    /**
     * Opens a {@link PrivateChannel} with the provided user by id.
     * <br>This will fail with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     * if the user does not exist.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public void sendMessage(JDA jda, String userId, String content) {
     *     jda.openPrivateChannelById(userId)
     *        .flatMap(channel -> channel.sendMessage(content))
     *        .queue();
     * }
     * }</pre>
     *
     * @param  userId
     *         The id of the target user
     *
     * @throws UnsupportedOperationException
     *         If the target user is the currently logged in account
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link PrivateChannel}
     *
     * @see    User#openPrivateChannel()
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<PrivateChannel> openPrivateChannelById(@Nonnull String userId)
    {
        return openPrivateChannelById(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Emote Emotes} visible to this JDA session.
     *
     * @return Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     *
     * @see    net.dv8tion.jda.api.utils.cache.CacheView#allSnowflakes(java.util.function.Supplier) CacheView.allSnowflakes(...)
     */
    @Nonnull
    SnowflakeCacheView<Emote> getEmoteCache();

    /**
     * A collection of all to us known emotes (managed/restricted included).
     * <br>This will be empty if {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE} is disabled.
     *
     * <p><b>Hint</b>: To check whether you can use an {@link net.dv8tion.jda.api.entities.Emote Emote} in a specific
     * context you can use {@link Emote#canInteract(net.dv8tion.jda.api.entities.Member)} or {@link
     * Emote#canInteract(net.dv8tion.jda.api.entities.User, net.dv8tion.jda.api.entities.MessageChannel)}
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getEmoteCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return An immutable list of Emotes (which may or may not be available to usage).
     */
    @Nonnull
    default List<Emote> getEmotes()
    {
        return getEmoteCache().asList();
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
     * <br>This will be null if {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE} is disabled.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.Emote}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return An {@link net.dv8tion.jda.api.entities.Emote Emote} represented by this id or null if none is found in
     *         our cache.
     */
    @Nullable
    default Emote getEmoteById(@Nonnull String id)
    {
        return getEmoteCache().getElementById(id);
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
     * <br>This will be null if {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE} is disabled.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.Emote}.
     *
     * @return An {@link net.dv8tion.jda.api.entities.Emote Emote} represented by this id or null if none is found in
     *         our cache.
     */
    @Nullable
    default Emote getEmoteById(long id)
    {
        return getEmoteCache().getElementById(id);
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.Emote Emotes} that have the same name as the one
     * provided. <br>If there are no {@link net.dv8tion.jda.api.entities.Emote Emotes} with the provided name, then
     * this returns an empty list.
     * <br>This will be empty if {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE} is disabled.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * @param  name
     *         The name of the requested {@link net.dv8tion.jda.api.entities.Emote Emotes}. Without colons.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link
     *         net.dv8tion.jda.api.entities.Emote#getName()}.
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.api.entities.Emote Emotes} that all have the same
     *         name as the provided name.
     */
    @Nonnull
    default List<Emote> getEmotesByName(@Nonnull String name, boolean ignoreCase)
    {
        return getEmoteCache().getElementsByName(name, ignoreCase);
    }

    /**
     * The EventManager used by this JDA instance.
     *
     * @return The {@link net.dv8tion.jda.api.hooks.IEventManager}
     */
    @Nonnull
    IEventManager getEventManager();

    /**
     * Returns the currently logged in account represented by {@link net.dv8tion.jda.api.entities.SelfUser SelfUser}.
     * <br>Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     * use the AccountManager which is accessible by {@link net.dv8tion.jda.api.entities.SelfUser#getManager()}.
     *
     * @return The currently logged in account.
     */
    @Nonnull
    SelfUser getSelfUser();

    /**
     * The {@link net.dv8tion.jda.api.managers.Presence Presence} controller for the current session.
     * <br>Used to set {@link net.dv8tion.jda.api.entities.Activity} and {@link net.dv8tion.jda.api.OnlineStatus} information.
     *
     * @return The never-null {@link net.dv8tion.jda.api.managers.Presence Presence} for this session.
     */
    @Nonnull
    Presence getPresence();

    /**
     * The shard information used when creating this instance of JDA.
     * <br>Represents the information provided to {@link net.dv8tion.jda.api.JDABuilder#useSharding(int, int)}.
     *
     * @return The shard information for this shard
     */
    @Nonnull
    ShardInfo getShardInfo();

    /**
     * The login token that is currently being used for Discord authentication.
     *
     * @return Never-null, 18 character length string containing the auth token.
     */
    @Nonnull
    String getToken();

    /**
     * This value is the total amount of JSON responses that discord has sent.
     * <br>This value resets every time the websocket has to perform a full reconnect (not resume).
     *
     * @return Never-negative long containing total response amount.
     */
    long getResponseTotal();

    /**
     * This value is the maximum amount of time, in seconds, that JDA will wait between reconnect attempts.
     * <br>Can be set using {@link net.dv8tion.jda.api.JDABuilder#setMaxReconnectDelay(int) JDABuilder.setMaxReconnectDelay(int)}.
     *
     * @return The maximum amount of time JDA will wait between reconnect attempts in seconds.
     */
    int getMaxReconnectDelay();

    /**
     * Sets whether or not JDA should try to automatically reconnect if a connection-error is encountered.
     * <br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * <p>Default is <b>true</b>.
     *
     * @param  reconnect If true - enables autoReconnect
     */
    void setAutoReconnect(boolean reconnect);

    /**
     * Whether the Requester should retry when
     * a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     *
     * @param  retryOnTimeout
     *         True, if the Request should retry once on a socket timeout
     */
    void setRequestTimeoutRetry(boolean retryOnTimeout);

    /**
     * USed to determine whether or not autoReconnect is enabled for JDA.
     *
     * @return True if JDA will attempt to automatically reconnect when a connection-error is encountered.
     */
    boolean isAutoReconnect();

    /**
     * Used to determine if JDA will process MESSAGE_DELETE_BULK messages received from Discord as a single
     * {@link net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent} or split
     * the deleted messages up and fire multiple {@link net.dv8tion.jda.api.events.message.MessageDeleteEvent MessageDeleteEvents},
     * one for each deleted message.
     *
     * <p>By default, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}
     *
     * @return Whether or not JDA currently handles the BULK_MESSAGE_DELETE event by splitting it into individual MessageDeleteEvents or not.
     */
    boolean isBulkDeleteSplittingEnabled();

    /**
     * Shuts down this JDA instance, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * Already enqueued {@link net.dv8tion.jda.api.requests.RestAction RestActions} are still going to be executed.
     *
     * <p>If you want this instance to shutdown without executing, use {@link #shutdownNow() shutdownNow()}
     *
     * <p>This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
     *
     * @see #shutdownNow()
     */
    void shutdown();

    /**
     * Shuts down this JDA instance instantly, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * This will also cancel all queued {@link net.dv8tion.jda.api.requests.RestAction RestActions}.
     *
     * <p>If you want this instance to shutdown without cancelling enqueued RestActions use {@link #shutdown() shutdown()}
     *
     * <p>This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
     *
     * @see #shutdown()
     */
    void shutdownNow();

    ///**
    // * Installs an auxiliary cable into the given port of your system.
    // *
    // * @param  port
    // *         The port in which the cable should be installed.
    // *
    // * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}{@literal <}{@link Void}{@literal >}
    // */
    //AuditableRestAction<Void> installAuxiliaryCable(int port);

    /**
     * The {@link net.dv8tion.jda.api.AccountType} of the currently logged in account.
     * <br>Used when determining functions that are restricted based on the type of account.
     *
     * @return The current AccountType.
     */
    @Nonnull
    AccountType getAccountType();

    /**
     * Retrieves the {@link ApplicationInfo ApplicationInfo} for
     * the application that owns the logged in Bot-Account.
     * <br>This contains information about the owner of the currently logged in bot account!
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link ApplicationInfo ApplicationInfo}
     *         <br>The {@link ApplicationInfo ApplicationInfo} of the bot's application.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<ApplicationInfo> retrieveApplicationInfo();

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>To use slash commands you must add {@code "applications.commands"} to these scopes. The scope {@code "bot"} is always applied.
     *
     * @param  scopes
     *         The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current JDA instance
     */
    @Nonnull
    default JDA setRequiredScopes(@Nonnull String... scopes)
    {
        Checks.noneNull(scopes, "Scopes");
        return setRequiredScopes(Arrays.asList(scopes));
    }

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>To use slash commands you must add {@code "applications.commands"} to these scopes. The scope {@code "bot"} is always applied.
     *
     * @param  scopes
     *         The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current JDA instance
     */
    @Nonnull
    JDA setRequiredScopes(@Nonnull Collection<String> scopes);

    /**
     * Creates an authorization invite url for the currently logged in Bot-Account.
     * <br>Example Format:
     * {@code https://discord.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8}
     *
     * <p><b>Hint:</b> To enable a pre-selected Guild of choice append the parameter {@code &guild_id=YOUR_GUILD_ID}
     *
     * @param  permissions
     *         The permissions to use in your invite, these can be changed by the link user.
     *         <br>If no permissions are provided the {@code permissions} parameter is omitted
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT}
     *
     * @return A valid OAuth2 invite url for the currently logged in Bot-Account
     */
    @Nonnull
    String getInviteUrl(@Nullable Permission... permissions);

    /**
     * Creates an authorization invite url for the currently logged in Bot-Account.
     * <br>Example Format:
     * {@code https://discord.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8}
     *
     * <p><b>Hint:</b> To enable a pre-selected Guild of choice append the parameter {@code &guild_id=YOUR_GUILD_ID}
     *
     * @param  permissions
     *         The permissions to use in your invite, these can be changed by the link user.
     *         <br>If no permissions are provided the {@code permissions} parameter is omitted
     *
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT}
     *
     * @return A valid OAuth2 invite url for the currently logged in Bot-Account
     */
    @Nonnull
    String getInviteUrl(@Nullable Collection<Permission> permissions);

    /**
     * Returns the {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} that manages this JDA instances or null if this instance is not managed
     * by any {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager}.
     *
     * @return The corresponding ShardManager or {@code null} if there is no such manager
     */
    @Nullable
    ShardManager getShardManager();

    /**
     * Retrieves a {@link net.dv8tion.jda.api.entities.Webhook Webhook} by its id.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>We do not have the required permissions</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>A webhook with this id does not exist</li>
     * </ul>
     *
     * @param  webhookId
     *         The webhook id
     *
     * @throws IllegalArgumentException
     *         If the {@code webhookId} is null or empty
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     *          <br>The webhook object.
     *
     * @see    Guild#retrieveWebhooks()
     * @see    TextChannel#retrieveWebhooks()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Webhook> retrieveWebhookById(@Nonnull String webhookId);

    /**
     * Retrieves a {@link net.dv8tion.jda.api.entities.Webhook Webhook} by its id.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>We do not have the required permissions</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>A webhook with this id does not exist</li>
     * </ul>
     *
     * @param  webhookId
     *         The webhook id
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     *          <br>The webhook object.
     *
     * @see    Guild#retrieveWebhooks()
     * @see    TextChannel#retrieveWebhooks()
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Webhook> retrieveWebhookById(long webhookId)
    {
        return retrieveWebhookById(Long.toUnsignedString(webhookId));
    }

    /**
     * Installs an auxiliary port for audio transfer.
     *
     * @throws IllegalStateException
     *         If this is a headless environment or no port is available
     *
     * @return {@link AuditableRestAction} - Type: int
     *         Provides the resulting used port
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Integer> installAuxiliaryPort()
    {
        int port = ThreadLocalRandom.current().nextInt();
        if (Desktop.isDesktopSupported())
        {
            try
            {
                Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
            }
            catch (IOException | URISyntaxException e)
            {
                throw new IllegalStateException("No port available");
            }
        }
        else throw new IllegalStateException("No port available");
        return new CompletedRestAction<>(this, port);
    }
}
