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
package net.dv8tion.jda.api

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.api.JDA.ShardInfo
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo.getName
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.entities.sticker.StickerPack
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.entities.sticker.StickerUnion
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.managers.AudioManager
import net.dv8tion.jda.api.managers.DirectAudioController
import net.dv8tion.jda.api.managers.Presence
import net.dv8tion.jda.api.requests.*
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.api.requests.restaction.*
import net.dv8tion.jda.api.requests.restaction.pagination.EntitlementPaginationAction
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.MiscUtil.parseSnowflake
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.cache.CacheView
import net.dv8tion.jda.api.utils.cache.CacheView.getElementsByName
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import net.dv8tion.jda.api.utils.data.DataArray.stream
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.dv8tion.jda.internal.requests.CompletedRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.Helpers
import okhttp3.OkHttpClient
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiFunction
import java.util.function.BooleanSupplier
import java.util.function.Predicate
import java.util.regex.Matcher
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * The core of JDA. Acts as a registry system of JDA. All parts of the API can be accessed starting from this class.
 *
 * @see JDABuilder
 */
open interface JDA : IGuildChannelContainer<Channel?> {
    /**
     * Represents the connection status of JDA and its Main WebSocket.
     */
    enum class Status {
        /**JDA is currently setting up supporting systems like the AudioSystem. */
        INITIALIZING(true),

        /**JDA has finished setting up supporting systems and is ready to log in. */
        INITIALIZED(true),

        /**JDA is currently attempting to log in. */
        LOGGING_IN(true),

        /**JDA is currently attempting to connect it's websocket to Discord. */
        CONNECTING_TO_WEBSOCKET(true),

        /**JDA has successfully connected it's websocket to Discord and is sending authentication */
        IDENTIFYING_SESSION(true),

        /**JDA has sent authentication to discord and is awaiting confirmation */
        AWAITING_LOGIN_CONFIRMATION(true),

        /**JDA is populating internal objects.
         * This process often takes the longest of all Statuses (besides CONNECTED) */
        LOADING_SUBSYSTEMS(true),

        /**JDA has finished loading everything, is receiving information from Discord and is firing events. */
        CONNECTED(true),

        /**JDA's main websocket has been disconnected. This **DOES NOT** mean JDA has shutdown permanently.
         * This is an in-between status. Most likely ATTEMPTING_TO_RECONNECT or SHUTTING_DOWN/SHUTDOWN will soon follow. */
        DISCONNECTED,

        /** JDA session has been added to [SessionController][net.dv8tion.jda.api.utils.SessionController]
         * and is awaiting to be dequeued for reconnecting. */
        RECONNECT_QUEUED,

        /**When trying to reconnect to Discord JDA encountered an issue, most likely related to a lack of internet connection,
         * and is waiting to try reconnecting again. */
        WAITING_TO_RECONNECT,

        /**JDA has been disconnected from Discord and is currently trying to reestablish the connection. */
        ATTEMPTING_TO_RECONNECT,

        /**JDA has received a shutdown request or has been disconnected from Discord and reconnect is disabled, thus,
         * JDA is in the process of shutting down */
        SHUTTING_DOWN,

        /**JDA has finished shutting down and this instance can no longer be used to communicate with the Discord servers. */
        SHUTDOWN,

        /**While attempting to authenticate, Discord reported that the provided authentication information was invalid. */
        FAILED_TO_LOGIN;

        @JvmField
        val isInit: Boolean

        constructor(isInit: Boolean) {
            this.isInit = isInit
        }

        constructor() {
            isInit = false
        }
    }

    /**
     * Represents the information used to create this shard.
     */
    class ShardInfo(
        /**
         * Represents the id of the shard of the current instance.
         * <br></br>This value will be between 0 and ([.getShardTotal] - 1).
         *
         * @return The id of the currently logged in shard.
         */
        @JvmField var shardId: Int,
        /**
         * The total amount of shards based on the value provided during JDA instance creation using
         * [JDABuilder.useSharding].
         * <br></br>This **does not** query Discord to determine the total number of shards.
         * <br></br>This **does not** represent the amount of logged in shards.
         * <br></br>It strictly represents the integer value provided to discord
         * representing the total amount of shards that the developer indicated that it was going to use when
         * initially starting JDA.
         *
         * @return The total of shards based on the total provided by the developer during JDA initialization.
         */
        @JvmField var shardTotal: Int
    ) {

        val shardString: String
            /**
             * Provides a shortcut method for easily printing shard info.
             * <br></br>Format: "[# / #]"
             * <br></br>Where the first # is shardId and the second # is shardTotal.
             *
             * @return A String representing the information used to build this shard.
             */
            get() {
                return "[" + shardId + " / " + shardTotal + "]"
            }

        public override fun toString(): String {
            return EntityString(this)
                .addMetadata("currentShard", shardString)
                .addMetadata("totalShards", shardTotal)
                .toString()
        }

        public override fun equals(o: Any?): Boolean {
            if (!(o is ShardInfo)) return false
            val oInfo: ShardInfo = o
            return shardId == oInfo.shardId && shardTotal == oInfo.shardTotal
        }

        companion object {
            /** Default sharding config with one shard  */
            @JvmField
            val SINGLE: ShardInfo = ShardInfo(0, 1)
        }
    }

    @get:Nonnull
    val status: Status?

    @get:Nonnull
    val gatewayIntents: EnumSet<GatewayIntent?>?

    @get:Nonnull
    val cacheFlags: EnumSet<CacheFlag?>?

    /**
     * Attempts to remove the user with the provided id from the cache.
     * <br></br>If you attempt to remove the [SelfUser][.getSelfUser] this will simply return `false`.
     *
     *
     * This should be used by an implementation of [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
     * as an upstream request to remove a member.
     *
     * @param  userId
     * The target user id
     *
     * @return True, if the cache was changed
     */
    fun unloadUser(userId: Long): Boolean

    /**
     * The time in milliseconds that discord took to respond to our last heartbeat
     * <br></br>This roughly represents the WebSocket ping of this session
     *
     *
     * **[RestAction][net.dv8tion.jda.api.requests.RestAction] request times do not
     * correlate to this value!**
     *
     *
     * The [GatewayPingEvent][net.dv8tion.jda.api.events.GatewayPingEvent] indicates an update to this value.
     *
     * @return time in milliseconds between heartbeat and the heartbeat ack response
     *
     * @see .getRestPing
     */
    val gatewayPing: Long

    @get:Nonnull
    val restPing: RestAction<Long?>?
        /**
         * The time in milliseconds that discord took to respond to a REST request.
         * <br></br>This will request the current user from the API and calculate the time the response took.
         *
         *
         * **Example**<br></br>
         * <pre>`
         * jda.getRestPing().queue( (time) ->
         * channel.sendMessageFormat("Ping: %d ms", time).queue()
         * );
        `</pre> *
         *
         * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: long
         *
         * @since 4.0.0
         *
         * @see .getGatewayPing
         */
        get() {
            val time: AtomicLong = AtomicLong()
            val route: CompiledRoute = Route.Self.GET_SELF.compile()
            val action: RestActionImpl<Long?> = RestActionImpl(
                this,
                route,
                BiFunction({ response: Response?, request: Request<Long?>? -> System.currentTimeMillis() - time.get() })
            )
            action.setCheck(BooleanSupplier({
                time.set(System.currentTimeMillis())
                true
            }))
            return action
        }

    /**
     * This method will block until JDA has reached the specified connection status.
     *
     *
     * **Login Cycle**<br></br>
     *
     *  1. [INITIALIZING][net.dv8tion.jda.api.JDA.Status.INITIALIZING]
     *  1. [INITIALIZED][net.dv8tion.jda.api.JDA.Status.INITIALIZED]
     *  1. [LOGGING_IN][net.dv8tion.jda.api.JDA.Status.LOGGING_IN]
     *  1. [CONNECTING_TO_WEBSOCKET][net.dv8tion.jda.api.JDA.Status.CONNECTING_TO_WEBSOCKET]
     *  1. [IDENTIFYING_SESSION][net.dv8tion.jda.api.JDA.Status.IDENTIFYING_SESSION]
     *  1. [AWAITING_LOGIN_CONFIRMATION][net.dv8tion.jda.api.JDA.Status.AWAITING_LOGIN_CONFIRMATION]
     *  1. [LOADING_SUBSYSTEMS][net.dv8tion.jda.api.JDA.Status.LOADING_SUBSYSTEMS]
     *  1. [CONNECTED][net.dv8tion.jda.api.JDA.Status.CONNECTED]
     *
     *
     * @param  status
     * The init status to wait for, once JDA has reached the specified
     * stage of the startup cycle this method will return.
     *
     * @throws InterruptedException
     * If this thread is interrupted while waiting
     * @throws IllegalArgumentException
     * If the provided status is null or not an init status ([Status.isInit])
     * @throws IllegalStateException
     * If JDA is shutdown during this wait period
     *
     * @return The current JDA instance, for chaining convenience
     */
    @Nonnull
    @Throws(InterruptedException::class)
    fun awaitStatus(@Nonnull status: Status?): JDA? {
        //This is done to retain backwards compatible ABI as it would otherwise change the signature of the method
        // which would require recompilation for all users (including extension libraries)
        return awaitStatus(status, *arrayOfNulls<Status>(0))
    }

    /**
     * This method will block until JDA has reached the specified connection status.
     *
     *
     * **Login Cycle**<br></br>
     *
     *  1. [INITIALIZING][net.dv8tion.jda.api.JDA.Status.INITIALIZING]
     *  1. [INITIALIZED][net.dv8tion.jda.api.JDA.Status.INITIALIZED]
     *  1. [LOGGING_IN][net.dv8tion.jda.api.JDA.Status.LOGGING_IN]
     *  1. [CONNECTING_TO_WEBSOCKET][net.dv8tion.jda.api.JDA.Status.CONNECTING_TO_WEBSOCKET]
     *  1. [IDENTIFYING_SESSION][net.dv8tion.jda.api.JDA.Status.IDENTIFYING_SESSION]
     *  1. [AWAITING_LOGIN_CONFIRMATION][net.dv8tion.jda.api.JDA.Status.AWAITING_LOGIN_CONFIRMATION]
     *  1. [LOADING_SUBSYSTEMS][net.dv8tion.jda.api.JDA.Status.LOADING_SUBSYSTEMS]
     *  1. [CONNECTED][net.dv8tion.jda.api.JDA.Status.CONNECTED]
     *
     *
     * @param  status
     * The init status to wait for, once JDA has reached the specified
     * stage of the startup cycle this method will return.
     * @param  failOn
     * Optional failure states that will force a premature return
     *
     * @throws InterruptedException
     * If this thread is interrupted while waiting
     * @throws IllegalArgumentException
     * If the provided status is null or not an init status ([Status.isInit])
     * @throws IllegalStateException
     * If JDA is shutdown during this wait period
     *
     * @return The current JDA instance, for chaining convenience
     */
    @Nonnull
    @Throws(InterruptedException::class)
    fun awaitStatus(@Nonnull status: Status?, @Nonnull vararg failOn: Status?): JDA?

    /**
     * This method will block until JDA has reached the status [Status.CONNECTED].
     * <br></br>This status means that JDA finished setting up its internal cache and is ready to be used.
     *
     * @throws InterruptedException
     * If this thread is interrupted while waiting
     * @throws IllegalStateException
     * If JDA is shutdown during this wait period
     *
     * @return The current JDA instance, for chaining convenience
     */
    @Nonnull
    @Throws(InterruptedException::class)
    fun awaitReady(): JDA? {
        return awaitStatus(Status.CONNECTED)
    }

    /**
     * Blocks the current thread until [.getStatus] returns [Status.SHUTDOWN].
     * <br></br>This can be useful in certain situations like disabling class loading.
     *
     *
     * Note that shutdown time depends on the length of the rate-limit queue.
     * You can use [.shutdownNow] to cancel all pending requests and immediately shutdown.
     *
     *
     * **Example**
     * <pre>`jda.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
     * jda.shutdownNow(); // Cancel all remaining requests
     * jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
    `</pre> *
     *
     *
     * **This will not implicitly call `shutdown()`, you are responsible to ensure that the shutdown process has started.**
     *
     * @param  duration
     * The maximum time to wait, or 0 to wait indefinitely
     * @param  unit
     * The time unit for the duration
     *
     * @throws IllegalArgumentException
     * If the provided unit is null
     * @throws InterruptedException
     * If the current thread is interrupted while waiting
     *
     * @return False, if the timeout has elapsed before the shutdown has completed, true otherwise.
     */
    @CheckReturnValue
    @Throws(InterruptedException::class)
    fun awaitShutdown(duration: Long, @Nonnull unit: TimeUnit?): Boolean

    /**
     * Blocks the current thread until [.getStatus] returns [Status.SHUTDOWN].
     * <br></br>This can be useful in certain situations like disabling class loading.
     *
     *
     * Note that shutdown time depends on the length of the rate-limit queue.
     * You can use [.shutdownNow] to cancel all pending requests and immediately shutdown.
     *
     *
     * **Example**
     * <pre>`jda.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
     * jda.shutdownNow(); // Cancel all remaining requests
     * jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
    `</pre> *
     *
     *
     * **This will not implicitly call `shutdown()`, you are responsible to ensure that the shutdown process has started.**
     *
     * @param  timeout
     * The maximum time to wait, or [Duration.ZERO] to wait indefinitely
     *
     * @throws IllegalArgumentException
     * If the provided timeout is null
     * @throws InterruptedException
     * If the current thread is interrupted while waiting
     *
     * @return False, if the timeout has elapsed before the shutdown has completed, true otherwise.
     */
    @CheckReturnValue
    @Throws(InterruptedException::class)
    fun awaitShutdown(@Nonnull timeout: Duration): Boolean {
        Checks.notNull(timeout, "Timeout")
        return awaitShutdown(timeout.toMillis(), TimeUnit.MILLISECONDS)
    }

    /**
     * Blocks the current thread until [.getStatus] returns [Status.SHUTDOWN].
     * <br></br>This can be useful in certain situations like disabling class loading.
     *
     *
     * This will wait indefinitely by default. Use [.awaitShutdown] to set a timeout.
     *
     *
     * Note that shutdown time depends on the length of the rate-limit queue.
     * You can use [.shutdownNow] to cancel all pending requests and immediately shutdown.
     *
     *
     * **Example**
     * <pre>`jda.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
     * jda.shutdownNow(); // Cancel all remaining requests
     * jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
    `</pre> *
     *
     *
     * **This will not implicitly call `shutdown()`, you are responsible to ensure that the shutdown process has started.**
     *
     * @throws IllegalArgumentException
     * If the provided timeout is null
     * @throws InterruptedException
     * If the current thread is interrupted while waiting
     *
     * @return Always true
     */
    @Throws(InterruptedException::class)
    fun awaitShutdown(): Boolean {
        return awaitShutdown(0, TimeUnit.MILLISECONDS)
    }

    /**
     * Cancels all currently scheduled [RestAction] requests.
     * <br></br>When a [RestAction] is cancelled, a [java.util.concurrent.CancellationException] will be provided
     * to the failure callback. This means [RestAction.queue] will invoke the second callback
     * and [RestAction.complete] will throw an exception.
     *
     *
     * **This is only recommended as an extreme last measure to avoid backpressure.**
     * If you want to stop requests on shutdown you should use [.shutdownNow] instead of this method.
     *
     * @return how many requests were cancelled
     *
     * @see RestAction.setCheck
     */
    fun cancelRequests(): Int

    @get:Nonnull
    val rateLimitPool: ScheduledExecutorService?

    @JvmField
    @get:Nonnull
    val gatewayPool: ScheduledExecutorService?

    @get:Nonnull
    val callbackPool: ExecutorService?

    @get:Nonnull
    val httpClient: OkHttpClient?

    @get:Nonnull
    val directAudioController: DirectAudioController?

    /**
     * Changes the internal EventManager.
     *
     *
     * The default EventManager is [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager].
     * <br></br>There is also an [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager] available.
     *
     * @param  manager
     * The new EventManager to use
     */
    fun setEventManager(manager: IEventManager?)

    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     * This uses the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] by default.
     * To switch to the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager], use [.setEventManager].
     *
     *
     * Note: when using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * given listener **must** be instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  listeners
     * The listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     * If either listeners or one of it's objects is `null`.
     */
    fun addEventListener(@Nonnull vararg listeners: Any?)

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     * The listener(s) to be removed.
     *
     * @throws java.lang.IllegalArgumentException
     * If either listeners or one of it's objects is `null`.
     */
    fun removeEventListener(@Nonnull vararg listeners: Any?)

    @get:Nonnull
    val registeredListeners: List<Any?>?

    /**
     * Retrieves the list of global commands.
     * <br></br>This list does not include guild commands! Use [Guild.retrieveCommands] for guild commands.
     * <br></br>This list does not include localization data. Use [.retrieveCommands] to get localization data
     *
     * @return [RestAction] - Type: [List] of [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommands(): RestAction<List<Command?>?>? {
        return retrieveCommands(false)
    }

    /**
     * Retrieves the list of global commands.
     * <br></br>This list does not include guild commands! Use [Guild.retrieveCommands] for guild commands.
     *
     * @param  withLocalizations
     * `true` if the localization data (such as name and description) should be included
     *
     * @return [RestAction] - Type: [List] of [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommands(withLocalizations: Boolean): RestAction<List<Command?>?>?

    /**
     * Retrieves the existing [Command] instance by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The command id
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     *
     * @return [RestAction] - Type: [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommandById(@Nonnull id: String?): RestAction<Command?>?

    /**
     * Retrieves the existing [Command] instance by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The command id
     *
     * @return [RestAction] - Type: [Command]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveCommandById(id: Long): RestAction<Command?>? {
        return retrieveCommandById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Creates or updates a global command.
     * <br></br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     *
     * To specify a complete list of all commands you can use [.updateCommands] instead.
     *
     *
     * You need the OAuth2 scope `"applications.commands"` in order to add commands to a guild.
     *
     * @param  command
     * The [CommandData] for the command
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [RestAction] - Type: [Command]
     * <br></br>The RestAction used to create or update the command
     *
     * @see Commands.slash
     * @see Commands.message
     * @see Commands.user
     * @see Guild.upsertCommand
     */
    @Nonnull
    @CheckReturnValue
    fun upsertCommand(@Nonnull command: CommandData?): RestAction<Command?>?

    /**
     * Creates or updates a global slash command.
     * <br></br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     *
     * To specify a complete list of all commands you can use [.updateCommands] instead.
     *
     *
     * You need the OAuth2 scope `"applications.commands"` in order to add commands to a guild.
     *
     * @param  name
     * The lowercase alphanumeric (with dash) name, 1-32 characters
     * @param  description
     * The description for the command, 1-100 characters
     *
     * @throws IllegalArgumentException
     * If null is provided or the name/description do not meet the requirements
     *
     * @return [CommandCreateAction]
     *
     * @see Guild.upsertCommand
     */
    @Nonnull
    @CheckReturnValue
    fun upsertCommand(@Nonnull name: String?, @Nonnull description: String?): CommandCreateAction? {
        return upsertCommand(CommandDataImpl((name)!!, (description)!!)) as CommandCreateAction?
    }

    /**
     * Configures the complete list of global commands.
     * <br></br>This will replace the existing command list for this bot. You should only use this once on startup!
     *
     *
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     *
     * You need the OAuth2 scope `"applications.commands"` in order to add commands to a guild.
     *
     *
     * **Examples**
     *
     *
     * Set list to 2 commands:
     * <pre>`jda.updateCommands()
     * .addCommands(Commands.slash("ping", "Gives the current ping"))
     * .addCommands(Commands.slash("ban", "Ban the target user")
     * .setGuildOnly(true)
     * .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
     * .addOption(OptionType.USER, "user", "The user to ban", true))
     * .queue();
    `</pre> *
     *
     *
     * Delete all commands:
     * <pre>`jda.updateCommands().queue();
    `</pre> *
     *
     * @return [CommandListUpdateAction]
     *
     * @see Guild.updateCommands
     */
    @Nonnull
    @CheckReturnValue
    fun updateCommands(): CommandListUpdateAction?

    /**
     * Edit an existing global command by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The id of the command to edit
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     *
     * @return [CommandEditAction] used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    fun editCommandById(@Nonnull id: String?): CommandEditAction?

    /**
     * Edit an existing global command by id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  id
     * The id of the command to edit
     *
     * @return [CommandEditAction] used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    fun editCommandById(id: Long): CommandEditAction? {
        return editCommandById(java.lang.Long.toUnsignedString(id))
    }

    /**
     * Delete the global command for this id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  commandId
     * The id of the command that should be deleted
     *
     * @throws IllegalArgumentException
     * If the provided id is not a valid snowflake
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteCommandById(@Nonnull commandId: String?): RestAction<Void?>?

    /**
     * Delete the global command for this id.
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  commandId
     * The id of the command that should be deleted
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteCommandById(commandId: Long): RestAction<Void?>? {
        return deleteCommandById(java.lang.Long.toUnsignedString(commandId))
    }

    /**
     * Retrieves the currently configured [RoleConnectionMetadata] records for this application.
     *
     * @return [RestAction] - Type: [List] of [RoleConnectionMetadata]
     *
     * @see [Configuring App Metadata for Linked Roles](https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles)
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveRoleConnectionMetadata(): RestAction<List<RoleConnectionMetadata?>?>?

    /**
     * Updates the currently configured [RoleConnectionMetadata] records for this application.
     *
     *
     * Returns the updated connection metadata records on success.
     *
     * @param  records
     * The new records to set
     *
     * @throws IllegalArgumentException
     * If null is provided or more than {@value RoleConnectionMetadata#MAX_RECORDS} records are configured.
     *
     * @return [RestAction] - Type: [List] of [RoleConnectionMetadata]
     *
     * @see [Configuring App Metadata for Linked Roles](https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles)
     */
    @Nonnull
    @CheckReturnValue
    fun updateRoleConnectionMetadata(@Nonnull records: Collection<RoleConnectionMetadata?>?): RestAction<List<RoleConnectionMetadata?>?>?

    /**
     * Constructs a new [Guild] with the specified name
     * <br></br>Use the returned [GuildAction] to provide
     * further details and settings for the resulting Guild!
     *
     *
     * This RestAction does not provide the resulting Guild!
     * It will be in a following [GuildJoinEvent][net.dv8tion.jda.api.events.guild.GuildJoinEvent].
     *
     * @param  name
     * The name of the resulting guild
     *
     * @throws java.lang.IllegalStateException
     * If the currently logged in account is in 10 or more guilds
     * @throws java.lang.IllegalArgumentException
     * If the provided name is empty, `null` or not between 2-100 characters
     *
     * @return [GuildAction]
     * <br></br>Allows for setting various details for the resulting Guild
     */
    @Nonnull
    @CheckReturnValue
    fun createGuild(@Nonnull name: String?): GuildAction?

    /**
     * Constructs a new [Guild] from the specified template code.
     *
     *
     * This RestAction does not provide the resulting Guild!
     * It will be in a following [GuildJoinEvent][net.dv8tion.jda.api.events.guild.GuildJoinEvent].
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [Unknown Guild Template][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_GUILD_TEMPLATE]
     * <br></br>The template doesn't exist.
     *
     *
     * @param  code
     * The template code to use to create a guild
     * @param  name
     * The name of the resulting guild
     * @param  icon
     * The [Icon][net.dv8tion.jda.api.entities.Icon] to use, or null to use no icon
     *
     * @throws java.lang.IllegalStateException
     * If the currently logged in account is in 10 or more guilds
     * @throws java.lang.IllegalArgumentException
     * If the provided name is empty, `null` or not between 2-100 characters
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun createGuildFromTemplate(@Nonnull code: String?, @Nonnull name: String?, icon: Icon?): RestAction<Void?>?

    @get:Nonnull
    val audioManagerCache: CacheView<AudioManager?>

    @get:Nonnull
    val audioManagers: List<AudioManager?>?
        /**
         * Immutable list of all created [AudioManagers][net.dv8tion.jda.api.managers.AudioManager] for this JDA instance!
         *
         * @return Immutable list of all created AudioManager instances
         */
        get() {
            return audioManagerCache.asList()
        }

    @get:Nonnull
    val userCache: SnowflakeCacheView<User?>

    @get:Nonnull
    val users: List<User?>?
        /**
         * An immutable list of all [Users][net.dv8tion.jda.api.entities.User] that share a
         * [Guild] with the currently logged in account.
         * <br></br>This list will never contain duplicates and represents all
         * [Users][net.dv8tion.jda.api.entities.User] that JDA can currently see.
         *
         *
         * **This will only check cached users!**
         *
         *
         * If the developer is sharding, then only users from guilds connected to the specifically logged in
         * shard will be returned in the List.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getUserCache] and use its more efficient
         * versions of handling these values.
         *
         * @return Immutable list of all [Users][net.dv8tion.jda.api.entities.User] that are visible to JDA.
         */
        get() {
            return userCache.asList()
        }

    /**
     * This returns the [User][net.dv8tion.jda.api.entities.User] which has the same id as the one provided.
     * <br></br>If there is no visible user with an id that matches the provided one, this returns `null`.
     *
     *
     * **This will only check cached users!**
     *
     * @param  id
     * The id of the requested [User][net.dv8tion.jda.api.entities.User].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [User][net.dv8tion.jda.api.entities.User] with matching id.
     *
     * @see .retrieveUserById
     */
    fun getUserById(@Nonnull id: String?): User? {
        return userCache.getElementById(id)
    }

    /**
     * This returns the [User][net.dv8tion.jda.api.entities.User] which has the same id as the one provided.
     * <br></br>If there is no visible user with an id that matches the provided one, this returns `null`.
     *
     *
     * **This will only check cached users!**
     *
     * @param  id
     * The id of the requested [User][net.dv8tion.jda.api.entities.User].
     *
     * @return Possibly-null [User][net.dv8tion.jda.api.entities.User] with matching id.
     *
     * @see .retrieveUserById
     */
    fun getUserById(id: Long): User? {
        return userCache.getElementById(id)
    }

    /**
     * Searches for a user that has the matching Discord Tag.
     * <br></br>Format has to be in the form `Username#Discriminator` where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     *
     * This only checks users that are known to the currently logged in account (shard). If a user exists
     * with the tag that is not available in the [User-Cache][.getUserCache] it will not be detected.
     * <br></br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     *
     * **This will only check cached users!**
     *
     * @param  tag
     * The Discord Tag in the format `Username#Discriminator`
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided tag is null or not in the described format
     *
     * @return The [net.dv8tion.jda.api.entities.User] for the discord tag or null if no user has the provided tag
     *
     */
    @ForRemoval
    @Deprecated(
        "This will become obsolete in the future.\n" + "                  Discriminators are being phased out and replaced by globally unique usernames.\n" + "                  For more information, see <a href=" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") open fun /*@@hqbcjt@@*/getUserByTag(@javax.annotation.Nonnull tag:/*@@wpmhhi@@*/kotlin.String?): /*@@awtetn@@*/net.dv8tion.jda.api.entities.User? {
                net . dv8tion . jda . internal . utils . Checks . notNull (tag, "Tag"
    )
    var matcher: Matcher = User.USER_TAG.matcher(tag)
    net.dv8tion.jda.internal .utils.Checks.check(matcher.matches(), "Invalid tag format!")
    var username: String = matcher.group(1)
    var discriminator: String = matcher.group(2)
    return getUserByTag(username, discriminator)
}
/**
 * Searches for a user that has the matching Discord Tag.
 * <br></br>Format has to be in the form `Username#Discriminator` where the
 * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
 * must be exactly 4 digits.
 *
 *
 * This only checks users that are known to the currently logged in account (shard). If a user exists
 * with the tag that is not available in the [User-Cache][.getUserCache] it will not be detected.
 * <br></br>Currently Discord does not offer a way to retrieve a user by their discord tag.
 *
 *
 * **This will only check cached users!**
 *
 * @param  username
 * The name of the user
 * @param  discriminator
 * The discriminator of the user
 *
 * @throws java.lang.IllegalArgumentException
 * If the provided arguments are null or not in the described format
 *
 * @return The [net.dv8tion.jda.api.entities.User] for the discord tag or null if no user has the provided tag
 *
 */
@ForRemoval @Deprecated(
    "This will become obsolete in the future.\n" + "                  Discriminators are being phased out and replaced by globally unique usernames.\n" + "                  For more information, see <a href=" https ://support.discord.com/hc/en-us/articles/12620128861463" target="_blank">New Usernames &amp; Display Names</a>.") open fun /*@@xjoowg@@*/getUserByTag(@javax.annotation.Nonnull username:/*@@wpmhhi@@*/kotlin.String?, @javax.annotation.Nonnull discriminator:/*@@wpmhhi@@*/kotlin.String?): /*@@awtetn@@*/net.dv8tion.jda.api.entities.User? {
            net . dv8tion . jda . internal . utils . Checks . notNull (username, "Username"
)
net.dv8tion.jda.internal .utils.Checks.notNull(discriminator, "Discriminator")
net.dv8tion.jda.internal .utils.Checks.check(discriminator.length == 4 && net.dv8tion.jda.internal .utils.Helpers.isNumeric(discriminator), "Invalid format for discriminator!")
var codePointLength: Int = Helpers.codePointLength(username)
net.dv8tion.jda.internal .utils.Checks.check(codePointLength >= 2 && codePointLength <= 32, "Username must be between 2 and 32 codepoints in length!")
return getUserCache().applyStream<net.dv8tion.jda.api.entities.User?>(java.util.function.Function<java.util.stream.Stream<net.dv8tion.jda.api.entities.User?>?, net.dv8tion.jda.api.entities.User?> ({
    stream:java.util.stream.Stream<net.dv8tion.jda.api.entities.User?>? -> stream.filter(java.util.function.Predicate<net.dv8tion.jda.api.entities.User?> ({ it: User ->
        it.getDiscriminator().equals(discriminator)
    }))
    .filter(Predicate<User>({ it: User -> it.name.equals(username) }))
        .findFirst()
        .orElse(null)
})
)
}
/**
 * This immutable returns all [Users][net.dv8tion.jda.api.entities.User] that have the same username as the one provided.
 * <br></br>If there are no [Users][net.dv8tion.jda.api.entities.User] with the provided name, then this returns an empty list.
 *
 *
 * **This will only check cached users!**
 *
 *
 * **Note: ** This does **not** consider nicknames, it only considers [net.dv8tion.jda.api.entities.User.getName]
 *
 * @param  name
 * The name of the requested [Users][net.dv8tion.jda.api.entities.User].
 * @param  ignoreCase
 * Whether to ignore case or not when comparing the provided name to each [net.dv8tion.jda.api.entities.User.getName].
 *
 * @return Possibly-empty immutable list of [Users][net.dv8tion.jda.api.entities.User] that all have the same name as the provided name.
 *
 * @incubating This will be replaced in the future when the rollout of globally unique usernames has been completed.
 */
@Nonnull
@Incubating
fun getUsersByName(@Nonnull name: String?, ignoreCase: Boolean): List<User?>? {
    return getUserCache().getElementsByName(name, ignoreCase)
}

/**
 * Gets all [Guilds][Guild] that contain all given users as their members.
 *
 * @param  users
 * The users which all the returned [Guilds][Guild] must contain.
 *
 * @return Immutable list of all [Guild] instances which have all [Users][net.dv8tion.jda.api.entities.User] in them.
 *
 * @see Guild.isMember
 */
@Nonnull
fun getMutualGuilds(@Nonnull vararg users: User?): List<Guild?>?

/**
 * Gets all [Guilds][Guild] that contain all given users as their members.
 *
 * @param users
 * The users which all the returned [Guilds][Guild] must contain.
 *
 * @return Immutable list of all [Guild] instances which have all [Users][net.dv8tion.jda.api.entities.User] in them.
 */
@Nonnull
fun getMutualGuilds(@Nonnull users: Collection<User?>?): List<Guild?>?

/**
 * Attempts to retrieve a [User][net.dv8tion.jda.api.entities.User] object based on the provided id.
 *
 *
 * If [.getUserById] is cached, this will directly return the user in a completed [RestAction] without making a request.
 * When both [GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] and [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intents
 * are disabled this will always make a request even if the user is cached.
 * You can use [action.useCache(false)][CacheRestAction.useCache] to force an update.
 *
 *
 * The returned [RestAction][net.dv8tion.jda.api.requests.RestAction] can encounter the following Discord errors:
 *
 *  * [ErrorResponse.UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>Occurs when the provided id does not refer to a [User][net.dv8tion.jda.api.entities.User]
 * known by Discord. Typically occurs when developers provide an incomplete id (cut short).
 *
 *
 * @param  id
 * The id of the requested [User][net.dv8tion.jda.api.entities.User].
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 * @throws java.lang.IllegalArgumentException
 *
 *  * If the provided id String is null.
 *  * If the provided id String is empty.
 *
 *
 * @return [CacheRestAction] - Type: [User]
 * <br></br>On request, gets the User with id matching provided id from Discord.
 */
@Nonnull
@CheckReturnValue
fun retrieveUserById(@Nonnull id: String?): CacheRestAction<User?>? {
    return retrieveUserById(parseSnowflake((id)!!))
}

/**
 * Attempts to retrieve a [User][net.dv8tion.jda.api.entities.User] object based on the provided id.
 *
 *
 * If [.getUserById] is cached, this will directly return the user in a completed [RestAction] without making a request.
 * When both [GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] and [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intents
 * are disabled this will always make a request even if the user is cached.
 * You can use [action.useCache(false)][CacheRestAction.useCache] to force an update.
 *
 *
 * The returned [RestAction][net.dv8tion.jda.api.requests.RestAction] can encounter the following Discord errors:
 *
 *  * [ErrorResponse.UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * <br></br>Occurs when the provided id does not refer to a [User][net.dv8tion.jda.api.entities.User]
 * known by Discord. Typically occurs when developers provide an incomplete id (cut short).
 *
 *
 * @param  id
 * The id of the requested [User].
 *
 * @return [CacheRestAction] - Type: [User]
 * <br></br>On request, gets the User with id matching provided id from Discord.
 */
@Nonnull
@CheckReturnValue
fun retrieveUserById(id: Long): CacheRestAction<User?>?

/**
 * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [Guilds][Guild] visible to this JDA session.
 *
 * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
 */
@Nonnull
fun getGuildCache(): SnowflakeCacheView<Guild?>

/**
 * An immutable List of all [Guilds][Guild] that the logged account is connected to.
 * <br></br>If this account is not connected to any [Guilds][Guild], this will return an empty list.
 *
 *
 * If the developer is sharding ([net.dv8tion.jda.api.JDABuilder.useSharding], then this list
 * will only contain the [Guilds][Guild] that the shard is actually connected to.
 * Discord determines which guilds a shard is connect to using the following format:
 * <br></br>Guild connected if shardId == (guildId &gt;&gt; 22) % totalShards;
 * <br></br>Source for formula: [Discord Documentation](https://discord.com/developers/docs/topics/gateway#sharding)
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getGuildCache] and use its more efficient
 * versions of handling these values.
 *
 * @return Possibly-empty immutable list of all the [Guilds][Guild] that this account is connected to.
 */
@Nonnull
fun getGuilds(): List<Guild?>? {
    return getGuildCache().asList()
}

/**
 * This returns the [Guild] which has the same id as the one provided.
 * <br></br>If there is no connected guild with an id that matches the provided one, then this returns `null`.
 *
 * @param  id
 * The id of the [Guild].
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return Possibly-null [Guild] with matching id.
 */
fun getGuildById(@Nonnull id: String?): Guild? {
    return getGuildCache().getElementById(id)
}

/**
 * This returns the [Guild] which has the same id as the one provided.
 * <br></br>If there is no connected guild with an id that matches the provided one, then this returns `null`.
 *
 * @param  id
 * The id of the [Guild].
 *
 * @return Possibly-null [Guild] with matching id.
 */
fun getGuildById(id: Long): Guild? {
    return getGuildCache().getElementById(id)
}

/**
 * An immutable list of all [Guilds][Guild] that have the same name as the one provided.
 * <br></br>If there are no [Guilds][Guild] with the provided name, then this returns an empty list.
 *
 * @param  name
 * The name of the requested [Guilds][Guild].
 * @param  ignoreCase
 * Whether to ignore case or not when comparing the provided name to each [Guild.getName].
 *
 * @return Possibly-empty immutable list of all the [Guilds][Guild] that all have the same name as the provided name.
 */
@Nonnull
fun getGuildsByName(@Nonnull name: String?, ignoreCase: Boolean): List<Guild?>? {
    return getGuildCache().getElementsByName(name, ignoreCase)
}

/**
 * Set of [Guild] IDs for guilds that were marked unavailable by the gateway.
 * <br></br>When a guild becomes unavailable a [GuildUnavailableEvent][net.dv8tion.jda.api.events.guild.GuildUnavailableEvent]
 * is emitted and a [GuildAvailableEvent][net.dv8tion.jda.api.events.guild.GuildAvailableEvent] is emitted
 * when it becomes available again. During the time a guild is unavailable it its not reachable through
 * cache such as [.getGuildById].
 *
 * @return Possibly-empty set of guild IDs for unavailable guilds
 */
@Nonnull
fun getUnavailableGuilds(): Set<String?>?

/**
 * Whether the guild is unavailable. If this returns true, the guild id should be in [.getUnavailableGuilds].
 *
 * @param  guildId
 * The guild id
 *
 * @return True, if this guild is unavailable
 */
fun isUnavailable(guildId: Long): Boolean

/**
 * Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [Roles][net.dv8tion.jda.api.entities.Role] visible to this JDA session.
 *
 * @return Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
 *
 * @see net.dv8tion.jda.api.utils.cache.CacheView.allSnowflakes
 */
@Nonnull
fun getRoleCache(): SnowflakeCacheView<Role?>

/**
 * All [Roles][net.dv8tion.jda.api.entities.Role] this JDA instance can see. <br></br>This will iterate over each
 * [Guild] retrieved from [.getGuilds] and collect its [ ][Guild.getRoles].
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getRoleCache] and use its more efficient
 * versions of handling these values.
 *
 * @return Immutable List of all visible Roles
 */
@Nonnull
fun getRoles(): List<Role?>? {
    return getRoleCache().asList()
}

/**
 * Retrieves the [Role][net.dv8tion.jda.api.entities.Role] associated to the provided id. <br></br>This iterates
 * over all [Guilds][Guild] and check whether a Role from that Guild is assigned
 * to the specified ID and will return the first that can be found.
 *
 * @param  id
 * The id of the searched Role
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return Possibly-null [Role][net.dv8tion.jda.api.entities.Role] for the specified ID
 */
fun getRoleById(@Nonnull id: String?): Role? {
    return getRoleCache().getElementById(id)
}

/**
 * Retrieves the [Role][net.dv8tion.jda.api.entities.Role] associated to the provided id. <br></br>This iterates
 * over all [Guilds][Guild] and check whether a Role from that Guild is assigned
 * to the specified ID and will return the first that can be found.
 *
 * @param  id
 * The id of the searched Role
 *
 * @return Possibly-null [Role][net.dv8tion.jda.api.entities.Role] for the specified ID
 */
fun getRoleById(id: Long): Role? {
    return getRoleCache().getElementById(id)
}

/**
 * Retrieves all [Roles][net.dv8tion.jda.api.entities.Role] visible to this JDA instance.
 * <br></br>This simply filters the Roles returned by [.getRoles] with the provided name, either using
 * [String.equals] or [String.equalsIgnoreCase] on [net.dv8tion.jda.api.entities.Role.getName].
 *
 * @param  name
 * The name for the Roles
 * @param  ignoreCase
 * Whether to use [String.equalsIgnoreCase]
 *
 * @return Immutable List of all Roles matching the parameters provided.
 */
@Nonnull
fun getRolesByName(@Nonnull name: String?, ignoreCase: Boolean): List<Role?>? {
    return getRoleCache().getElementsByName(name, ignoreCase)
}

/**
 * [SnowflakeCacheView] of
 * all cached [ScheduledEvents][ScheduledEvent] visible to this JDA session.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @return [SnowflakeCacheView]
 */
@Nonnull
fun getScheduledEventCache(): SnowflakeCacheView<ScheduledEvent?>

/**
 * An unmodifiable list of all [ScheduledEvents][ScheduledEvent] of all connected
 * [Guilds][net.dv8tion.jda.api.entities.Guild].
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getScheduledEventCache] and use its more efficient
 * versions of handling these values.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @return Possibly-empty immutable list of all known [ScheduledEvents][ScheduledEvent].
 */
@Nonnull
fun getScheduledEvents(): List<ScheduledEvent?>? {
    return getScheduledEventCache().asList()
}

/**
 * This returns the [ScheduledEvent] which has the same id as the one provided.
 * <br></br>If there is no known [ScheduledEvent] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @param  id
 * The id of the [ScheduledEvent].
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return Possibly-null [ScheduledEvent] with a matching id.
 */
fun getScheduledEventById(@Nonnull id: String?): ScheduledEvent? {
    return getScheduledEventCache().getElementById(id)
}

/**
 * This returns the [ScheduledEvent] which has the same id as the one provided.
 * <br></br>If there is no known [ScheduledEvent] with an id that matches the provided
 * one, then this returns `null`.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @param  id
 * The id of the [ScheduledEvent].
 *
 * @return Possibly-null [ScheduledEvent] with a matching id.
 */
fun getScheduledEventById(id: Long): ScheduledEvent? {
    return getScheduledEventCache().getElementById(id)
}

/**
 * An unmodifiable list of all [ScheduledEvents][ScheduledEvent] that have the same name as the one provided.
 * <br></br>If there are no [ScheduledEvents][ScheduledEvent] with the provided name, then this returns an empty list.
 *
 *
 * This requires [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 *
 * @param  name
 * The name of the requested [ScheduledEvent].
 * @param  ignoreCase
 * Whether to ignore case or not when comparing the provided name to each [ScheduledEvent.getName].
 *
 * @throws IllegalArgumentException
 * If the provided name is null.
 *
 * @return Possibly-empty immutable list of all the [ScheduledEvents][ScheduledEvent] that all have the
 * same name as the provided name.
 */
@Nonnull
fun getScheduledEventsByName(@Nonnull name: String?, ignoreCase: Boolean): List<ScheduledEvent?>? {
    return getScheduledEventCache().getElementsByName(name, ignoreCase)
}

/**
 * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [PrivateChannels][PrivateChannel] visible to this JDA session.
 *
 * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
 */
@Nonnull
fun getPrivateChannelCache(): SnowflakeCacheView<PrivateChannel?>

/**
 * An unmodifiable list of all known [PrivateChannels][PrivateChannel].
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getPrivateChannelCache] and use its more efficient
 * versions of handling these values.
 *
 * @return Possibly-empty list of all [PrivateChannels][PrivateChannel].
 */
@Nonnull
fun getPrivateChannels(): List<PrivateChannel?>? {
    return getPrivateChannelCache().asList()
}

/**
 * This returns the [PrivateChannel] which has the same id as the one provided.
 * <br></br>If there is no known [PrivateChannel] with an id that matches the provided
 * one, then this returns `null`.
 *
 * @param  id
 * The id of the [PrivateChannel].
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return Possibly-null [PrivateChannel] with matching id.
 */
fun getPrivateChannelById(@Nonnull id: String?): PrivateChannel? {
    return getPrivateChannelCache().getElementById(id)
}

/**
 * This returns the [PrivateChannel] which has the same id as the one provided.
 * <br></br>If there is no known [PrivateChannel] with an id that matches the provided
 * one, then this returns `null`.
 *
 * @param  id
 * The id of the [PrivateChannel].
 *
 * @return Possibly-null [PrivateChannel] with matching id.
 */
fun getPrivateChannelById(id: Long): PrivateChannel? {
    return getPrivateChannelCache().getElementById(id)
}

/**
 * Opens a [PrivateChannel] with the provided user by id.
 * <br></br>This will fail with [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * if the user does not exist.
 *
 *
 * If the channel is cached, this will directly return the channel in a completed [RestAction] without making a request.
 * You can use [action.useCache(false)][CacheRestAction.useCache] to force an update.
 *
 *
 * **Example**<br></br>
 * <pre>`public void sendMessage(JDA jda, long userId, String content) {
 * jda.openPrivateChannelById(userId)
 * .flatMap(channel -> channel.sendMessage(content))
 * .queue();
 * }
`</pre> *
 *
 * @param  userId
 * The id of the target user
 *
 * @throws UnsupportedOperationException
 * If the target user is the currently logged in account
 *
 * @return [CacheRestAction] - Type: [PrivateChannel]
 *
 * @see User.openPrivateChannel
 */
@Nonnull
@CheckReturnValue
fun openPrivateChannelById(userId: Long): CacheRestAction<PrivateChannel?>?

/**
 * Opens a [PrivateChannel] with the provided user by id.
 * <br></br>This will fail with [UNKNOWN_USER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_USER]
 * if the user does not exist.
 *
 *
 * If the channel is cached, this will directly return the channel in a completed [RestAction] without making a request.
 * You can use [action.useCache(false)][CacheRestAction.useCache] to force an update.
 *
 *
 * **Example**<br></br>
 * <pre>`public void sendMessage(JDA jda, String userId, String content) {
 * jda.openPrivateChannelById(userId)
 * .flatMap(channel -> channel.sendMessage(content))
 * .queue();
 * }
`</pre> *
 *
 * @param  userId
 * The id of the target user
 *
 * @throws UnsupportedOperationException
 * If the target user is the currently logged in account
 * @throws IllegalArgumentException
 * If the provided id is not a valid snowflake
 *
 * @return [RestAction] - Type: [PrivateChannel]
 *
 * @see User.openPrivateChannel
 */
@Nonnull
@CheckReturnValue
fun openPrivateChannelById(@Nonnull userId: String?): CacheRestAction<PrivateChannel?>? {
    return openPrivateChannelById(parseSnowflake((userId)!!))
}

/**
 * Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
 * all cached [Custom Emojis][RichCustomEmoji] visible to this JDA session.
 *
 * @return Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
 *
 * @see net.dv8tion.jda.api.utils.cache.CacheView.allSnowflakes
 */
@Nonnull
fun getEmojiCache(): SnowflakeCacheView<RichCustomEmoji?>

/**
 * A collection of all to us known custom emoji (managed/restricted included).
 * <br></br>This will be empty if [net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] is disabled.
 *
 *
 * **Hint**: To check whether you can use an [RichCustomEmoji] in a specific
 * context you can use [RichCustomEmoji.canInteract] or [ ][RichCustomEmoji.canInteract]
 *
 *
 * **Unicode emojis are not included as [Custom Emoji][RichCustomEmoji]!**
 *
 *
 * This copies the backing store into a list. This means every call
 * creates a new list with O(n) complexity. It is recommended to store this into
 * a local variable or use [.getEmojiCache] and use its more efficient
 * versions of handling these values.
 *
 * @return An immutable list of Custom Emojis (which may or may not be available to usage).
 */
@Nonnull
fun getEmojis(): List<RichCustomEmoji?>? {
    return getEmojiCache().asList()
}

/**
 * Retrieves a custom emoji matching the specified `id` if one is available in our cache.
 * <br></br>This will be null if [net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] is disabled.
 *
 *
 * **Unicode emojis are not included as [Custom Emoji][RichCustomEmoji]!**
 *
 * @param  id
 * The id of the requested [RichCustomEmoji].
 *
 * @throws java.lang.NumberFormatException
 * If the provided `id` cannot be parsed by [Long.parseLong]
 *
 * @return A [Custom Emoji][RichCustomEmoji] represented by this id or null if none is found in
 * our cache.
 */
fun getEmojiById(@Nonnull id: String?): RichCustomEmoji? {
    return getEmojiCache().getElementById(id)
}

/**
 * Retrieves a custom emoji matching the specified `id` if one is available in our cache.
 * <br></br>This will be null if [net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] is disabled.
 *
 *
 * **Unicode emojis are not included as [Custom Emoji][RichCustomEmoji]!**
 *
 * @param  id
 * The id of the requested [RichCustomEmoji].
 *
 * @return A [Custom Emoji][RichCustomEmoji] represented by this id or null if none is found in
 * our cache.
 */
fun getEmojiById(id: Long): RichCustomEmoji? {
    return getEmojiCache().getElementById(id)
}

/**
 * An unmodifiable list of all [Custom Emojis][RichCustomEmoji] that have the same name as the one
 * provided. <br></br>If there are no [Custom Emojis][RichCustomEmoji] with the provided name, then
 * this returns an empty list.
 * <br></br>This will be empty if [net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] is disabled.
 *
 *
 * **Unicode emojis are not included as [Custom Emoji][RichCustomEmoji]!**
 *
 * @param  name
 * The name of the requested [Custom Emojis][RichCustomEmoji]. Without colons.
 * @param  ignoreCase
 * Whether to ignore case or not when comparing the provided name to each [         ][RichCustomEmoji.getName].
 *
 * @return Possibly-empty list of all the [Custom Emojis][RichCustomEmoji] that all have the same
 * name as the provided name.
 */
@Nonnull
fun getEmojisByName(@Nonnull name: String?, ignoreCase: Boolean): List<RichCustomEmoji?>? {
    return getEmojiCache().getElementsByName(name, ignoreCase)
}

/**
 * Attempts to retrieve a [Sticker] object based on the provided snowflake reference.
 * <br></br>This works for both [StandardSticker] and [GuildSticker], and you can resolve them using the provided [StickerUnion].
 *
 *
 * If the sticker is not one of the supported [Types][Sticker.Type], the request fails with [IllegalArgumentException].
 *
 *
 * The returned [RestAction][net.dv8tion.jda.api.requests.RestAction] can encounter the following Discord errors:
 *
 *  * [UNKNOWN_STICKER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_STICKER]
 * <br></br>Occurs when the provided id does not refer to a sticker known by Discord.
 *
 *
 * @param  sticker
 * The reference of the requested [Sticker].
 * <br></br>Can be [RichSticker], [StickerItem], or [Sticker.fromId].
 *
 * @throws IllegalArgumentException
 * If the provided sticker is null
 *
 * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [StickerUnion]
 * <br></br>On request, gets the sticker with id matching provided id from Discord.
 */
@Nonnull
@CheckReturnValue
fun retrieveSticker(@Nonnull sticker: StickerSnowflake?): RestAction<StickerUnion?>?

/**
 * Retrieves a list of all the public [StickerPacks][StickerPack] used for nitro.
 *
 * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: List of [StickerPack]
 */
@Nonnull
@CheckReturnValue
fun retrieveNitroStickerPacks(): RestAction<List<StickerPack?>?>?

/**
 * The EventManager used by this JDA instance.
 *
 * @return The [net.dv8tion.jda.api.hooks.IEventManager]
 */
@Nonnull
fun getEventManager(): IEventManager?

/**
 * Returns the currently logged in account represented by [SelfUser][net.dv8tion.jda.api.entities.SelfUser].
 * <br></br>Account settings **cannot** be modified using this object. If you wish to modify account settings please
 * use the AccountManager which is accessible by [net.dv8tion.jda.api.entities.SelfUser.getManager].
 *
 * @return The currently logged in account.
 */
@Nonnull
fun getSelfUser(): SelfUser?

/**
 * The [Presence][net.dv8tion.jda.api.managers.Presence] controller for the current session.
 * <br></br>Used to set [net.dv8tion.jda.api.entities.Activity] and [net.dv8tion.jda.api.OnlineStatus] information.
 *
 * @return The never-null [Presence][net.dv8tion.jda.api.managers.Presence] for this session.
 */
@Nonnull
fun getPresence(): Presence?

/**
 * The shard information used when creating this instance of JDA.
 * <br></br>Represents the information provided to [net.dv8tion.jda.api.JDABuilder.useSharding].
 *
 * @return The shard information for this shard
 */
@Nonnull
fun getShardInfo(): ShardInfo?

/**
 * The login token that is currently being used for Discord authentication.
 *
 * @return Never-null, 18 character length string containing the auth token.
 */
@Nonnull
fun getToken(): String?

/**
 * This value is the total amount of JSON responses that discord has sent.
 * <br></br>This value resets every time the websocket has to perform a full reconnect (not resume).
 *
 * @return Never-negative long containing total response amount.
 */
fun getResponseTotal(): Long

/**
 * This value is the maximum amount of time, in seconds, that JDA will wait between reconnect attempts.
 * <br></br>Can be set using [JDABuilder.setMaxReconnectDelay(int)][net.dv8tion.jda.api.JDABuilder.setMaxReconnectDelay].
 *
 * @return The maximum amount of time JDA will wait between reconnect attempts in seconds.
 */
fun getMaxReconnectDelay(): Int

/**
 * Sets whether or not JDA should try to automatically reconnect if a connection-error is encountered.
 * <br></br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
 *
 *
 * Default is **true**.
 *
 * @param  reconnect If true - enables autoReconnect
 */
fun setAutoReconnect(reconnect: Boolean)

/**
 * Whether the Requester should retry when
 * a [SocketTimeoutException][java.net.SocketTimeoutException] occurs.
 *
 * @param  retryOnTimeout
 * True, if the Request should retry once on a socket timeout
 */
fun setRequestTimeoutRetry(retryOnTimeout: Boolean)

/**
 * USed to determine whether or not autoReconnect is enabled for JDA.
 *
 * @return True if JDA will attempt to automatically reconnect when a connection-error is encountered.
 */
fun isAutoReconnect(): Boolean

/**
 * Used to determine if JDA will process MESSAGE_DELETE_BULK messages received from Discord as a single
 * [MessageBulkDeleteEvent][net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent] or split
 * the deleted messages up and fire multiple [MessageDeleteEvents][net.dv8tion.jda.api.events.message.MessageDeleteEvent],
 * one for each deleted message.
 *
 *
 * By default, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
 * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
 * should instead handle the [MessageBulkDeleteEvent][net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent]
 *
 * @return Whether or not JDA currently handles the BULK_MESSAGE_DELETE event by splitting it into individual MessageDeleteEvents or not.
 */
fun isBulkDeleteSplittingEnabled(): Boolean

/**
 * Shuts down this JDA instance, closing all its connections.
 * After this command is issued the JDA Instance can not be used anymore.
 * Already enqueued [RestActions][net.dv8tion.jda.api.requests.RestAction] are still going to be executed.
 *
 *
 * If you want this instance to shutdown without executing, use [shutdownNow()][.shutdownNow]
 *
 *
 * This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
 *
 * @see .shutdownNow
 */
fun shutdown()

/**
 * Shuts down this JDA instance instantly, closing all its connections.
 * After this command is issued the JDA Instance can not be used anymore.
 * This will also cancel all queued [RestActions][net.dv8tion.jda.api.requests.RestAction].
 *
 *
 * If you want this instance to shutdown without cancelling enqueued RestActions use [shutdown()][.shutdown]
 *
 *
 * This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
 *
 * @see .shutdown
 */
fun shutdownNow()
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
 * Retrieves the [ApplicationInfo] for
 * the application that owns the logged in Bot-Account.
 * <br></br>This contains information about the owner of the currently logged in bot account!
 *
 * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [ApplicationInfo]
 * <br></br>The [ApplicationInfo] of the bot's application.
 */
@Nonnull
@CheckReturnValue
fun retrieveApplicationInfo(): RestAction<ApplicationInfo?>?

/**
 * A [PaginationAction][net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction] implementation
 * which allows you to [iterate][Iterable] over [Entitlement]s that are applicable to the logged in application.
 *
 * @return [EntitlementPaginationAction]
 */
@Nonnull
@CheckReturnValue
fun retrieveEntitlements(): EntitlementPaginationAction?

/**
 * Configures the required scopes applied to the [.getInviteUrl] and similar methods.
 * <br></br>To use slash commands you must add `"applications.commands"` to these scopes. The scope `"bot"` is always applied.
 *
 * @param  scopes
 * The scopes to use with [.getInviteUrl] and the likes
 *
 * @throws IllegalArgumentException
 * If null is provided
 *
 * @return The current JDA instance
 */
@Nonnull
fun setRequiredScopes(@Nonnull vararg scopes: String?): JDA? {
    Checks.noneNull(scopes, "Scopes")
    return setRequiredScopes(Arrays.asList(*scopes))
}

/**
 * Configures the required scopes applied to the [.getInviteUrl] and similar methods.
 * <br></br>To use slash commands you must add `"applications.commands"` to these scopes. The scope `"bot"` is always applied.
 *
 * @param  scopes
 * The scopes to use with [.getInviteUrl] and the likes
 *
 * @throws IllegalArgumentException
 * If null is provided
 *
 * @return The current JDA instance
 */
@Nonnull
fun setRequiredScopes(@Nonnull scopes: Collection<String?>?): JDA?

/**
 * Creates an authorization invite url for the currently logged in Bot-Account.
 * <br></br>Example Format:
 * `https://discord.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8`
 *
 *
 * **Hint:** To enable a pre-selected Guild of choice append the parameter `&guild_id=YOUR_GUILD_ID`
 *
 * @param  permissions
 * The permissions to use in your invite, these can be changed by the link user.
 * <br></br>If no permissions are provided the `permissions` parameter is omitted
 *
 * @return A valid OAuth2 invite url for the currently logged in Bot-Account
 */
@Nonnull
fun getInviteUrl(vararg permissions: Permission?): String?

/**
 * Creates an authorization invite url for the currently logged in Bot-Account.
 * <br></br>Example Format:
 * `https://discord.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8`
 *
 *
 * **Hint:** To enable a pre-selected Guild of choice append the parameter `&guild_id=YOUR_GUILD_ID`
 *
 * @param  permissions
 * The permissions to use in your invite, these can be changed by the link user.
 * <br></br>If no permissions are provided the `permissions` parameter is omitted
 *
 * @return A valid OAuth2 invite url for the currently logged in Bot-Account
 */
@Nonnull
fun getInviteUrl(permissions: Collection<Permission?>?): String?

/**
 * Returns the [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] that manages this JDA instances or null if this instance is not managed
 * by any [ShardManager][net.dv8tion.jda.api.sharding.ShardManager].
 *
 * @return The corresponding ShardManager or `null` if there is no such manager
 */
fun getShardManager(): ShardManager?

/**
 * Retrieves a [Webhook][net.dv8tion.jda.api.entities.Webhook] by its id.
 * <br></br>If the webhook does not belong to any known guild of this JDA session, it will be [partial][Webhook.isPartial].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>We do not have the required permissions
 *
 *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
 * <br></br>A webhook with this id does not exist
 *
 *
 * @param  webhookId
 * The webhook id
 *
 * @throws IllegalArgumentException
 * If the `webhookId` is null or empty
 *
 * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Webhook][net.dv8tion.jda.api.entities.Webhook]
 * <br></br>The webhook object.
 *
 * @see Guild.retrieveWebhooks
 * @see TextChannel.retrieveWebhooks
 */
@Nonnull
@CheckReturnValue
fun retrieveWebhookById(@Nonnull webhookId: String?): RestAction<Webhook?>?

/**
 * Retrieves a [Webhook][net.dv8tion.jda.api.entities.Webhook] by its id.
 * <br></br>If the webhook does not belong to any known guild of this JDA session, it will be [partial][Webhook.isPartial].
 *
 *
 * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
 * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
 *
 *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
 * <br></br>We do not have the required permissions
 *
 *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
 * <br></br>A webhook with this id does not exist
 *
 *
 * @param  webhookId
 * The webhook id
 *
 * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Webhook][net.dv8tion.jda.api.entities.Webhook]
 * <br></br>The webhook object.
 *
 * @see Guild.retrieveWebhooks
 * @see TextChannel.retrieveWebhooks
 */
@Nonnull
@CheckReturnValue
fun retrieveWebhookById(webhookId: Long): RestAction<Webhook?>? {
    return retrieveWebhookById(java.lang.Long.toUnsignedString(webhookId))
}

/**
 * Installs an auxiliary port for audio transfer.
 *
 * @throws IllegalStateException
 * If this is a headless environment or no port is available
 *
 * @return [AuditableRestAction] - Type: int
 * Provides the resulting used port
 */
@Nonnull
@CheckReturnValue
fun installAuxiliaryPort(): AuditableRestAction<Int>? {
    val port: Int = ThreadLocalRandom.current().nextInt()
    if (Desktop.isDesktopSupported()) {
        try {
            Desktop.getDesktop().browse(URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        } catch (e: IOException) {
            throw IllegalStateException("No port available")
        } catch (e: URISyntaxException) {
            throw IllegalStateException("No port available")
        }
    } else throw IllegalStateException("No port available")
    return CompletedRestAction(this, port)
}
}
