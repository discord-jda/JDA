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
package net.dv8tion.jda.api.sharding

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.requests.*
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.cache.CacheView
import net.dv8tion.jda.api.utils.cache.ChannelCacheView
import net.dv8tion.jda.api.utils.cache.ShardCacheView
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.CompletedRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.cache.UnifiedChannelCacheView
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.IntFunction
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * This class acts as a manager for multiple shards.
 * It contains several methods to make your life with sharding easier.
 *
 * <br></br>Custom implementations may not support all methods and throw
 * [UnsupportedOperationExceptions][java.lang.UnsupportedOperationException] instead.
 *
 * @since  3.4
 * @author Aljoscha Grebe
 */
interface ShardManager : IGuildChannelContainer<Channel?> {
    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     *
     *
     * Note: when using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * the given listener **must** be an instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  listeners
     * The listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     * If either listeners or one of it's objects is `null`.
     */
    fun addEventListener(@Nonnull vararg listeners: Any?) {
        Checks.noneNull(listeners, "listeners")
        shardCache!!.forEach(Consumer { jda: JDA -> jda.addEventListener(*listeners) })
    }

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     * The listener(s) to be removed.
     *
     * @throws java.lang.IllegalArgumentException
     * If either listeners or one of it's objects is `null`.
     */
    fun removeEventListener(@Nonnull vararg listeners: Any?) {
        Checks.noneNull(listeners, "listeners")
        shardCache!!.forEach(Consumer { jda: JDA -> jda.removeEventListener(*listeners) })
    }

    /**
     * Adds listeners provided by the listener provider to each shard to the event-listeners that will be used to handle events.
     * The listener provider gets a shard id applied and is expected to return a listener.
     *
     *
     * Note: when using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * the given listener **must** be an instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  eventListenerProvider
     * The provider of listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided listener provider or any of the listeners or provides are `null`.
     */
    fun addEventListeners(@Nonnull eventListenerProvider: IntFunction<Any?>) {
        Checks.notNull(eventListenerProvider, "event listener provider")
        shardCache!!.forEach(Consumer { jda: JDA ->
            val listener = eventListenerProvider.apply(jda.getShardInfo().shardId)
            if (listener != null) jda.addEventListener(listener)
        })
    }

    /**
     * Remove listeners from shards by their id.
     * The provider takes shard ids, and returns a collection of listeners that shall be removed from the respective
     * shards.
     *
     * @param  eventListenerProvider
     * Gets shard ids applied and is expected to return a collection of listeners that shall be removed from
     * the respective shards
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided event listeners provider is `null`.
     */
    fun removeEventListeners(@Nonnull eventListenerProvider: IntFunction<Collection<Any?>?>) {
        Checks.notNull(eventListenerProvider, "event listener provider")
        shardCache!!.forEach(
            Consumer { jda: JDA -> jda.removeEventListener(eventListenerProvider.apply(jda.getShardInfo().shardId)) }
        )
    }

    /**
     * Remove a listener provider. This will stop further created / restarted shards from getting a listener added by
     * that provider.
     *
     *
     * Default is a no-op for backwards compatibility, see implementations like
     * [DefaultShardManager.removeEventListenerProvider] for actual code
     *
     * @param  eventListenerProvider
     * The provider of listeners that shall be removed.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided listener provider is `null`.
     */
    fun removeEventListenerProvider(@Nonnull eventListenerProvider: IntFunction<Any?>?) {}

    /**
     * Returns the amount of shards queued for (re)connecting.
     *
     * @return The amount of shards queued for (re)connecting.
     */
    val shardsQueued: Int
    val shardsRunning: Int
        /**
         * Returns the amount of running shards.
         *
         * @return The amount of running shards.
         */
        get() = shardCache!!.size().toInt()
    val shardsTotal: Int
        /**
         * Returns the amount of shards managed by this [ShardManager][net.dv8tion.jda.api.sharding.ShardManager].
         * This includes shards currently queued for a restart.
         *
         * @return The managed amount of shards.
         */
        get() = shardsQueued + shardsRunning

    @get:Nonnull
    val gatewayIntents: EnumSet<GatewayIntent?>
        /**
         * The [GatewayIntents][GatewayIntent] for the JDA sessions of this shard manager.
         *
         * @return [EnumSet] of active gateway intents
         */
        get() = shardCache!!.applyStream<EnumSet<GatewayIntent?>>(Function { stream: Stream<JDA> ->
            stream.map { obj: JDA -> obj.gatewayIntents }
                .findAny()
                .orElse(EnumSet.noneOf(GatewayIntent::class.java))
        })

    /**
     * Used to access application details of this bot.
     * <br></br>Since this is the same for every shard it picks [JDA.retrieveApplicationInfo] from any shard.
     *
     * @throws java.lang.IllegalStateException
     * If there is no running shard
     *
     * @return The Application registry for this bot.
     */
    @Nonnull
    fun retrieveApplicationInfo(): RestAction<ApplicationInfo?>? {
        return shardCache!!.stream()
            .findAny()
            .orElseThrow { IllegalStateException("no active shards") }
            .retrieveApplicationInfo()
    }

    val averageGatewayPing: Double
        /**
         * The average time in milliseconds between all shards that discord took to respond to our last heartbeat.
         * This roughly represents the WebSocket ping of this session. If there are no shards running, this will return `-1`.
         *
         *
         * **[RestAction][net.dv8tion.jda.api.requests.RestAction] request times do not
         * correlate to this value!**
         *
         * @return The average time in milliseconds between heartbeat and the heartbeat ack response
         */
        get() = shardCache
            .stream()
            .mapToLong { obj: JDA -> obj.gatewayPing }
            .filter { ping: Long -> ping != -1L }
            .average()
            .orElse(-1.0)

    @get:Nonnull
    override val categoryCache: SnowflakeCacheView<Category>?
        /**
         * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
         * all cached [Categories][net.dv8tion.jda.api.entities.channel.concrete.Category] visible to this ShardManager instance.
         *
         * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         */
        get() = CacheView.allSnowflakes<Category>(Supplier<Stream<out SnowflakeCacheView<Category>?>> {
            shardCache!!.stream().map<Any?>(JDA::getCategoryCache)
        })

    /**
     * Retrieves a custom emoji matching the specified `id` if one is available in our cache.
     *
     *
     * **Unicode emojis are not included as [RichCustomEmoji]!**
     *
     * @param  id
     * The id of the requested [RichCustomEmoji].
     *
     * @return An [RichCustomEmoji] represented by this id or null if none is found in
     * our cache.
     */
    fun getEmojiById(id: Long): RichCustomEmoji? {
        return emojiCache.getElementById(id)
    }

    /**
     * Retrieves a custom emoji matching the specified `id` if one is available in our cache.
     *
     *
     * **Unicode emojis are not included as [RichCustomEmoji]!**
     *
     * @param  id
     * The id of the requested [RichCustomEmoji].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return An [RichCustomEmoji] represented by this id or null if none is found in
     * our cache.
     */
    fun getEmojiById(@Nonnull id: String?): RichCustomEmoji? {
        return emojiCache.getElementById(id!!)
    }

    @get:Nonnull
    val emojiCache: SnowflakeCacheView<RichCustomEmoji?>
        /**
         * Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
         * all cached [RichCustomEmojis][RichCustomEmoji] visible to this ShardManager instance.
         *
         * @return Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         */
        get() = CacheView.allSnowflakes {
            shardCache!!.stream().map { obj: JDA -> obj.getEmojiCache() }
        }

    @get:Nonnull
    val emojis: List<RichCustomEmoji?>?
        /**
         * A collection of all known custom emojis (managed/restricted included).
         *
         *
         * **Hint**: To check whether you can use a [RichCustomEmoji] in a specific
         * context you can use [RichCustomEmoji.canInteract] or [ ][RichCustomEmoji.canInteract]
         *
         *
         * **Unicode emojis are not included as [RichCustomEmoji]!**
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getEmojiCache] and use its more efficient
         * versions of handling these values.
         *
         * @return An immutable list of custom emojis (which may or may not be available to usage).
         */
        get() = emojiCache.asList()

    /**
     * An unmodifiable list of all [RichCustomEmojis][RichCustomEmoji] that have the same name as the one
     * provided. <br></br>If there are no [RichCustomEmojis][RichCustomEmoji] with the provided name, this will
     * return an empty list.
     *
     *
     * **Unicode emojis are not included as [RichCustomEmoji]!**
     *
     * @param  name
     * The name of the requested [RichCustomEmojis][RichCustomEmoji]. Without colons.
     * @param  ignoreCase
     * Whether to ignore case or not when comparing the provided name to each [         ][RichCustomEmoji.getName].
     *
     * @return Possibly-empty list of all the [RichCustomEmojis][RichCustomEmoji] that all have the same
     * name as the provided name.
     */
    @Nonnull
    fun getEmojisByName(@Nonnull name: String?, ignoreCase: Boolean): List<RichCustomEmoji?>? {
        return emojiCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * This returns the [Guild][net.dv8tion.jda.api.entities.Guild] which has the same id as the one provided.
     * <br></br>If there is no connected guild with an id that matches the provided one, this will return `null`.
     *
     * @param  id
     * The id of the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @return Possibly-null [Guild][net.dv8tion.jda.api.entities.Guild] with matching id.
     */
    fun getGuildById(id: Long): Guild? {
        return guildCache.getElementById(id)
    }

    /**
     * This returns the [Guild][net.dv8tion.jda.api.entities.Guild] which has the same id as the one provided.
     * <br></br>If there is no connected guild with an id that matches the provided one, this will return `null`.
     *
     * @param  id
     * The id of the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @return Possibly-null [Guild][net.dv8tion.jda.api.entities.Guild] with matching id.
     */
    fun getGuildById(@Nonnull id: String?): Guild? {
        return getGuildById(MiscUtil.parseSnowflake(id))
    }

    /**
     * An unmodifiable list of all [Guilds][net.dv8tion.jda.api.entities.Guild] that have the same name as the one provided.
     * <br></br>If there are no [Guilds][net.dv8tion.jda.api.entities.Guild] with the provided name, this will return an empty list.
     *
     * @param  name
     * The name of the requested [Guilds][net.dv8tion.jda.api.entities.Guild].
     * @param  ignoreCase
     * Whether to ignore case or not when comparing the provided name to each [net.dv8tion.jda.api.entities.Guild.getName].
     *
     * @return Possibly-empty list of all the [Guilds][net.dv8tion.jda.api.entities.Guild] that all have the same name as the provided name.
     */
    @Nonnull
    fun getGuildsByName(@Nonnull name: String?, ignoreCase: Boolean): List<Guild>? {
        return guildCache.getElementsByName(name!!, ignoreCase)
    }

    @get:Nonnull
    val guildCache: SnowflakeCacheView<Guild>
        /**
         * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
         * all cached [Guilds][net.dv8tion.jda.api.entities.Guild] visible to this ShardManager instance.
         *
         * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         */
        get() = CacheView.allSnowflakes {
            shardCache!!.stream().map { obj: JDA -> obj.getGuildCache() }
        }

    @get:Nonnull
    val guilds: List<Guild>?
        /**
         * An unmodifiable List of all [Guilds][net.dv8tion.jda.api.entities.Guild] that the logged account is connected to.
         * <br></br>If this account is not connected to any [Guilds][net.dv8tion.jda.api.entities.Guild], this will return
         * an empty list.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getGuildCache] and use its more efficient
         * versions of handling these values.
         *
         * @return Possibly-empty list of all the [Guilds][net.dv8tion.jda.api.entities.Guild] that this account is connected to.
         */
        get() = guildCache.asList()

    /**
     * Gets all [Guilds][net.dv8tion.jda.api.entities.Guild] that contain all given users as their members.
     *
     * @param  users
     * The users which all the returned [Guilds][net.dv8tion.jda.api.entities.Guild] must contain.
     *
     * @return Unmodifiable list of all [Guild][net.dv8tion.jda.api.entities.Guild] instances which have all [Users][net.dv8tion.jda.api.entities.User] in them.
     */
    @Nonnull
    fun getMutualGuilds(@Nonnull users: Collection<User?>): List<Guild>? {
        Checks.noneNull(users, "users")
        return guildCache.stream()
            .filter { guild: Guild ->
                users.stream()
                    .allMatch { user: User? -> guild.isMember(user) }
            }
            .collect(Helpers.toUnmodifiableList())
    }

    /**
     * Gets all [Guilds][net.dv8tion.jda.api.entities.Guild] that contain all given users as their members.
     *
     * @param  users
     * The users which all the returned [Guilds][net.dv8tion.jda.api.entities.Guild] must contain.
     *
     * @return Unmodifiable list of all [Guild][net.dv8tion.jda.api.entities.Guild] instances which have all [Users][net.dv8tion.jda.api.entities.User] in them.
     */
    @Nonnull
    fun getMutualGuilds(@Nonnull vararg users: User?): List<Guild>? {
        Checks.notNull(users, "users")
        return this.getMutualGuilds(Arrays.asList(*users))
    }

    /**
     * Attempts to retrieve a [User][net.dv8tion.jda.api.entities.User] object based on the provided id.
     * <br></br>This first calls [.getUserById], and if the return is `null` then a request
     * is made to the Discord servers.
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
     * @throws java.lang.IllegalArgumentException
     * If the provided id String is not a valid snowflake.
     * @throws java.lang.IllegalStateException
     * If there isn't any active shards.
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [User][net.dv8tion.jda.api.entities.User]
     * <br></br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveUserById(@Nonnull id: String?): RestAction<User?>? {
        return retrieveUserById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Attempts to retrieve a [User][net.dv8tion.jda.api.entities.User] object based on the provided id.
     * <br></br>This first calls [.getUserById], and if the return is `null` then a request
     * is made to the Discord servers.
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
     * @throws java.lang.IllegalStateException
     * If there isn't any active shards.
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [User][net.dv8tion.jda.api.entities.User]
     * <br></br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveUserById(id: Long): RestAction<User?>? {
        var api: JDA? = null
        for (shard in shardCache!!) {
            api = shard
            val intents = shard.gatewayIntents
            val user = shard.getUserById(id)
            val isUpdated =
                intents.contains(GatewayIntent.GUILD_PRESENCES) || intents.contains(GatewayIntent.GUILD_MEMBERS)
            if (user != null && isUpdated) return CompletedRestAction(shard, user)
        }
        checkNotNull(api) { "no shards active" }
        val jda = api as JDAImpl
        val route = Route.Users.GET_USER.compile(java.lang.Long.toUnsignedString(id))
        return RestActionImpl(
            jda,
            route
        ) { response: Response, request: Request<User?>? -> jda.entityBuilder.createUser(response.`object`) }
    }

    /**
     * Searches for the first user that has the matching Discord Tag.
     * <br></br>Format has to be in the form `Username#Discriminator` where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     *
     * This will only check cached users!
     *
     *
     * This only checks users that are known to the currently logged in account (shards). If a user exists
     * with the tag that is not available in the [User-Cache][.getUserCache] it will not be detected.
     * <br></br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     * @param  tag
     * The Discord Tag in the format `Username#Discriminator`
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided tag is null or not in the described format
     *
     * @return The [net.dv8tion.jda.api.entities.User] for the discord tag or null if no user has the provided tag
     */
    fun getUserByTag(@Nonnull tag: String?): User? {
        return shardCache!!.applyStream { stream: Stream<JDA> ->
            stream.map { jda: JDA ->
                jda.getUserByTag(
                    tag!!
                )
            }
                .filter { obj: User? -> Objects.nonNull(obj) }
                .findFirst()
                .orElse(null)
        }
    }

    /**
     * Searches for the first user that has the matching Discord Tag.
     * <br></br>Format has to be in the form `Username#Discriminator` where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     *
     * This will only check cached users!
     *
     *
     * This only checks users that are known to the currently logged in account (shards). If a user exists
     * with the tag that is not available in the [User-Cache][.getUserCache] it will not be detected.
     * <br></br>Currently Discord does not offer a way to retrieve a user by their discord tag.
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
     */
    fun getUserByTag(@Nonnull username: String?, @Nonnull discriminator: String?): User? {
        return shardCache!!.applyStream { stream: Stream<JDA> ->
            stream.map { jda: JDA ->
                jda.getUserByTag(
                    username!!, discriminator!!
                )
            }
                .filter { obj: User? -> Objects.nonNull(obj) }
                .findFirst()
                .orElse(null)
        }
    }

    @get:Nonnull
    val privateChannels: List<PrivateChannel?>?
        /**
         * An unmodifiable list of all known [PrivateChannels][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel].
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getPrivateChannelCache] and use its more efficient
         * versions of handling these values.
         *
         * @return Possibly-empty list of all [PrivateChannels][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel].
         */
        get() = privateChannelCache.asList()

    /**
     * Retrieves the [Role][net.dv8tion.jda.api.entities.Role] associated to the provided id. <br></br>This iterates
     * over all [Guilds][net.dv8tion.jda.api.entities.Guild] and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     * The id of the searched Role
     *
     * @return Possibly-null [Role][net.dv8tion.jda.api.entities.Role] for the specified ID
     */
    fun getRoleById(id: Long): Role? {
        return roleCache.getElementById(id)
    }

    /**
     * Retrieves the [Role][net.dv8tion.jda.api.entities.Role] associated to the provided id. <br></br>This iterates
     * over all [Guilds][net.dv8tion.jda.api.entities.Guild] and check whether a Role from that Guild is assigned
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
        return roleCache.getElementById(id!!)
    }

    @get:Nonnull
    val roleCache: SnowflakeCacheView<Role?>
        /**
         * Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
         * all cached [Roles][net.dv8tion.jda.api.entities.Role] visible to this ShardManager instance.
         *
         * @return Unified [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         */
        get() = CacheView.allSnowflakes {
            shardCache!!.stream().map { obj: JDA -> obj.getRoleCache() }
        }

    @get:Nonnull
    val roles: List<Role?>?
        /**
         * All [Roles][net.dv8tion.jda.api.entities.Role] this ShardManager instance can see. <br></br>This will iterate over each
         * [Guild][net.dv8tion.jda.api.entities.Guild] retrieved from [.getGuilds] and collect its [ ][net.dv8tion.jda.api.entities.Guild.getRoles].
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getRoleCache] and use its more efficient
         * versions of handling these values.
         *
         * @return Immutable List of all visible Roles
         */
        get() = roleCache.asList()

    /**
     * Retrieves all [Roles][net.dv8tion.jda.api.entities.Role] visible to this ShardManager instance.
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
        return roleCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * This returns the [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] which has the same id as the one provided.
     * <br></br>If there is no known [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] with an id that matches the provided
     * one, then this will return `null`.
     *
     * @param  id
     * The id of the [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel].
     *
     * @return Possibly-null [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] with matching id.
     */
    fun getPrivateChannelById(id: Long): PrivateChannel? {
        return privateChannelCache.getElementById(id)
    }

    /**
     * This returns the [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] which has the same id as the one provided.
     * <br></br>If there is no known [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] with an id that matches the provided
     * one, this will return `null`.
     *
     * @param  id
     * The id of the [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] with matching id.
     */
    fun getPrivateChannelById(@Nonnull id: String?): PrivateChannel? {
        return privateChannelCache.getElementById(id!!)
    }

    @get:Nonnull
    val privateChannelCache: SnowflakeCacheView<PrivateChannel?>
        /**
         * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
         * all cached [PrivateChannels][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] visible to this ShardManager instance.
         *
         * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         */
        get() = CacheView.allSnowflakes {
            shardCache!!.stream().map { obj: JDA -> obj.getPrivateChannelCache() }
        }

    override fun getGuildChannelById(id: Long): GuildChannel? {
        var channel: GuildChannel
        for (shard in shards) {
            channel = shard.getGuildChannelById(id)
            if (channel != null) return channel
        }
        return null
    }

    override fun getGuildChannelById(@Nonnull type: ChannelType?, id: Long): GuildChannel? {
        Checks.notNull(type, "ChannelType")
        var channel: GuildChannel
        for (shard in shards) {
            channel = shard.getGuildChannelById(type, id)
            if (channel != null) return channel
        }
        return null
    }

    @get:Nonnull
    override val textChannelCache: SnowflakeCacheView<TextChannel>?
        get() = CacheView.allSnowflakes<TextChannel>(Supplier<Stream<out SnowflakeCacheView<TextChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getTextChannelCache)
        })

    @get:Nonnull
    override val voiceChannelCache: SnowflakeCacheView<VoiceChannel>?
        get() = CacheView.allSnowflakes<VoiceChannel>(Supplier<Stream<out SnowflakeCacheView<VoiceChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getVoiceChannelCache)
        })

    @get:Nonnull
    override val stageChannelCache: SnowflakeCacheView<StageChannel>?
        get() = CacheView.allSnowflakes<StageChannel>(Supplier<Stream<out SnowflakeCacheView<StageChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getStageChannelCache)
        })

    @get:Nonnull
    override val threadChannelCache: SnowflakeCacheView<ThreadChannel>?
        get() = CacheView.allSnowflakes<ThreadChannel>(Supplier<Stream<out SnowflakeCacheView<ThreadChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getThreadChannelCache)
        })

    @get:Nonnull
    override val newsChannelCache: SnowflakeCacheView<NewsChannel>?
        get() = CacheView.allSnowflakes<NewsChannel>(Supplier<Stream<out SnowflakeCacheView<NewsChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getNewsChannelCache)
        })

    @get:Nonnull
    override val forumChannelCache: SnowflakeCacheView<ForumChannel>?
        get() = CacheView.allSnowflakes<ForumChannel>(Supplier<Stream<out SnowflakeCacheView<ForumChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getForumChannelCache)
        })

    @get:Nonnull
    override val mediaChannelCache: SnowflakeCacheView<MediaChannel>?
        get() = CacheView.allSnowflakes<MediaChannel>(Supplier<Stream<out SnowflakeCacheView<MediaChannel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getMediaChannelCache)
        })

    @get:Nonnull
    override val channelCache: ChannelCacheView<Channel>?
        get() = UnifiedChannelCacheView<Channel>(Supplier<Stream<ChannelCacheView<Channel>?>> {
            shardCache!!.stream().map<Any?>(JDA::getChannelCache)
        })

    /**
     * This returns the [JDA][net.dv8tion.jda.api.JDA] instance which has the same id as the one provided.
     * <br></br>If there is no shard with an id that matches the provided one, this will return `null`.
     *
     * @param  id
     * The id of the shard.
     *
     * @return The [JDA][net.dv8tion.jda.api.JDA] instance with the given shardId or
     * `null` if no shard has the given id
     */
    fun getShardById(id: Int): JDA? {
        return shardCache!!.getElementById(id)
    }

    /**
     * This returns the [JDA][net.dv8tion.jda.api.JDA] instance which has the same id as the one provided.
     * <br></br>If there is no shard with an id that matches the provided one, this will return `null`.
     *
     * @param  id
     * The id of the shard.
     *
     * @return The [JDA][net.dv8tion.jda.api.JDA] instance with the given shardId or
     * `null` if no shard has the given id
     */
    fun getShardById(@Nonnull id: String?): JDA? {
        return shardCache!!.getElementById(id!!)
    }

    @get:Nonnull
    val shardCache: ShardCacheView?

    @get:Nonnull
    val shards: List<JDA>
        /**
         * Gets all [JDA][net.dv8tion.jda.api.JDA] instances bound to this ShardManager.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getShardCache] and use its more efficient
         * versions of handling these values.
         *
         * @return An immutable list of all managed [JDA][net.dv8tion.jda.api.JDA] instances.
         */
        get() = shardCache!!.asList()

    /**
     * This returns the [JDA.Status][net.dv8tion.jda.api.JDA.Status] of the shard which has the same id as the one provided.
     * <br></br>If there is no shard with an id that matches the provided one, this will return `null`.
     *
     * @param  shardId
     * The id of the shard.
     *
     * @return The [JDA.Status][net.dv8tion.jda.api.JDA.Status] of the shard with the given shardId or
     * `null` if no shard has the given id
     */
    fun getStatus(shardId: Int): JDA.Status? {
        val jda = shardCache!!.getElementById(shardId)
        return jda?.status
    }

    @get:Nonnull
    val statuses: Map<JDA, JDA.Status>?
        /**
         * Gets the current [Status][net.dv8tion.jda.api.JDA.Status] of all shards.
         *
         * @return All current shard statuses.
         */
        get() = Collections.unmodifiableMap(
            shardCache!!.stream()
                .collect(Collectors.toMap(Function.identity(), Function { obj: JDA -> obj.status }))
        )

    /**
     * This returns the [User][net.dv8tion.jda.api.entities.User] which has the same id as the one provided.
     * <br></br>If there is no visible user with an id that matches the provided one, this will return `null`.
     *
     * @param  id
     * The id of the requested [User][net.dv8tion.jda.api.entities.User].
     *
     * @return Possibly-null [User][net.dv8tion.jda.api.entities.User] with matching id.
     */
    fun getUserById(id: Long): User? {
        return userCache.getElementById(id)
    }

    /**
     * This returns the [User][net.dv8tion.jda.api.entities.User] which has the same id as the one provided.
     * <br></br>If there is no visible user with an id that matches the provided one, this will return `null`.
     *
     * @param  id
     * The id of the requested [User][net.dv8tion.jda.api.entities.User].
     *
     * @return Possibly-null [User][net.dv8tion.jda.api.entities.User] with matching id.
     */
    fun getUserById(@Nonnull id: String?): User? {
        return userCache.getElementById(id!!)
    }

    @get:Nonnull
    val userCache: SnowflakeCacheView<User?>
        /**
         * [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] of
         * all cached [Users][net.dv8tion.jda.api.entities.User] visible to this ShardManager instance.
         *
         * @return [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         */
        get() = CacheView.allSnowflakes {
            shardCache!!.stream().map { obj: JDA -> obj.userCache }
        }

    @get:Nonnull
    val users: List<User?>?
        /**
         * An unmodifiable list of all [Users][net.dv8tion.jda.api.entities.User] that share a
         * [Guild][net.dv8tion.jda.api.entities.Guild] with the currently logged in account.
         * <br></br>This list will never contain duplicates and represents all [Users][net.dv8tion.jda.api.entities.User]
         * that JDA can currently see.
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
         * @return List of all [Users][net.dv8tion.jda.api.entities.User] that are visible to JDA.
         */
        get() = userCache.asList()

    /**
     * Restarts all shards, shutting old ones down first.
     *
     *
     * As all shards need to connect to discord again this will take equally long as the startup of a new ShardManager
     * (using the 5000ms + backoff as delay between starting new JDA instances).
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If [.shutdown] has already been invoked
     */
    fun restart()

    /**
     * Restarts the shards with the given id only.
     * <br></br> If there is no shard with the given Id, this method acts like [.start].
     *
     * @param  id
     * The id of the target shard
     *
     * @throws java.lang.IllegalArgumentException
     * If shardId is negative or higher than maxShardId
     * @throws java.util.concurrent.RejectedExecutionException
     * If [.shutdown] has already been invoked
     */
    fun restart(id: Int)

    /**
     * Sets the [Activity][net.dv8tion.jda.api.entities.Activity] for all shards.
     * <br></br>An Activity can be retrieved via [net.dv8tion.jda.api.entities.Activity.playing].
     * For streams you provide a valid streaming url as second parameter.
     *
     *
     * This will also change the activity for shards that are created in the future.
     *
     * @param  activity
     * A [Activity][net.dv8tion.jda.api.entities.Activity] instance or null to reset
     *
     * @see net.dv8tion.jda.api.entities.Activity.playing
     * @see net.dv8tion.jda.api.entities.Activity.streaming
     */
    fun setActivity(activity: Activity?) {
        setActivityProvider { id: Int -> activity }
    }

    /**
     * Sets provider that provider the [Activity][net.dv8tion.jda.api.entities.Activity] for all shards.
     * <br></br>A Activity can be retrieved via [net.dv8tion.jda.api.entities.Activity.playing].
     * For streams you provide a valid streaming url as second parameter.
     *
     *
     * This will also change the provider for shards that are created in the future.
     *
     * @param  activityProvider
     * Provider for an [Activity][net.dv8tion.jda.api.entities.Activity] instance or null to reset
     *
     * @see net.dv8tion.jda.api.entities.Activity.playing
     * @see net.dv8tion.jda.api.entities.Activity.streaming
     */
    fun setActivityProvider(activityProvider: IntFunction<out Activity?>?) {
        shardCache!!.forEach(Consumer { jda: JDA ->
            jda.getPresence().activity = activityProvider?.apply(jda.getShardInfo().shardId)
        })
    }

    /**
     * Sets whether all instances should be marked as afk or not
     *
     *
     * This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     *
     * This will also change the value for shards that are created in the future.
     *
     * @param idle
     * boolean
     */
    fun setIdle(idle: Boolean) {
        setIdleProvider { id: Int -> idle }
    }

    /**
     * Sets the provider that decides for all shards whether they should be marked as afk or not.
     *
     *
     * This will also change the provider for shards that are created in the future.
     *
     * @param idleProvider
     * Provider for a boolean
     */
    fun setIdleProvider(@Nonnull idleProvider: IntFunction<Boolean?>) {
        shardCache!!.forEach(Consumer { jda: JDA ->
            jda.getPresence().isIdle = idleProvider.apply(jda.getShardInfo().shardId)!!
        })
    }

    /**
     * Sets the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] and [Activity][net.dv8tion.jda.api.entities.Activity] for all shards.
     *
     *
     * This will also change the status for shards that are created in the future.
     *
     * @param  status
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus]
     * to be used (OFFLINE/null -&gt; INVISIBLE)
     * @param  activity
     * A [Activity][net.dv8tion.jda.api.entities.Activity] instance or null to reset
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     *
     * @see net.dv8tion.jda.api.entities.Activity.playing
     * @see net.dv8tion.jda.api.entities.Activity.streaming
     */
    fun setPresence(status: OnlineStatus?, activity: Activity?) {
        setPresenceProvider({ id: Int -> status }) { id: Int -> activity }
    }

    /**
     * Sets the provider that provides the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] and
     * [Activity][net.dv8tion.jda.api.entities.Activity] for all shards.
     *
     *
     * This will also change the status for shards that are created in the future.
     *
     * @param  statusProvider
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus]
     * to be used (OFFLINE/null -&gt; INVISIBLE)
     * @param  activityProvider
     * A [Activity][net.dv8tion.jda.api.entities.Activity] instance or null to reset
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     *
     * @see net.dv8tion.jda.api.entities.Activity.playing
     * @see net.dv8tion.jda.api.entities.Activity.streaming
     */
    fun setPresenceProvider(
        statusProvider: IntFunction<OnlineStatus?>?,
        activityProvider: IntFunction<out Activity?>?
    ) {
        shardCache!!.forEach(Consumer { jda: JDA ->
            jda.getPresence().setPresence(
                statusProvider?.apply(jda.getShardInfo().shardId), activityProvider?.apply(jda.getShardInfo().shardId)
            )
        })
    }

    /**
     * Sets the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] for all shards.
     *
     *
     * This will also change the status for shards that are created in the future.
     *
     * @param  status
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus]
     * to be used (OFFLINE/null -&gt; INVISIBLE)
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     */
    fun setStatus(status: OnlineStatus?) {
        setStatusProvider { id: Int -> status }
    }

    /**
     * Sets the provider that provides the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] for all shards.
     *
     *
     * This will also change the provider for shards that are created in the future.
     *
     * @param  statusProvider
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus]
     * to be used (OFFLINE/null -&gt; INVISIBLE)
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     */
    fun setStatusProvider(statusProvider: IntFunction<OnlineStatus?>?) {
        shardCache!!.forEach(Consumer { jda: JDA ->
            jda.getPresence().status = statusProvider?.apply(jda.getShardInfo().shardId)
        })
    }

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this method has been called the ShardManager instance can not be used anymore.
     *
     * <br></br>This will shutdown the internal queue worker for (re-)starts of shards.
     * This means [.restart], [.restart], and [.start] will throw
     * [java.util.concurrent.RejectedExecutionException].
     *
     *
     * This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
     */
    fun shutdown()

    /**
     * Shuts down the shard with the given id only.
     * <br></br>If there is no shard with the given id, this will do nothing.
     *
     * @param shardId
     * The id of the shard that should be stopped
     */
    fun shutdown(shardId: Int)

    /**
     * Adds a new shard with the given id to this ShardManager and starts it.
     *
     * @param  shardId
     * The id of the shard that should be started
     *
     * @throws java.util.concurrent.RejectedExecutionException
     * If [.shutdown] has already been invoked
     */
    fun start(shardId: Int)

    /**
     * Initializes and starts all shards. This should only be called once.
     *
     * @throws InvalidTokenException
     * If the provided token is invalid.
     */
    fun login()
}
