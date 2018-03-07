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

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.cache.CacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

/**
 * This class acts as a manager for multiple shards.
 * It contains several methods to make your life with sharding easier.
 *
 * <br>Custom implementations my not support all methods and throw
 * {@link java.lang.UnsupportedOperationException UnsupportedOperationExceptions} instead.
 *
 * @since  3.4
 * @author Aljoscha Grebe
 */
public interface ShardManager
{
    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     *
     * <p>Note: when using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    default void addEventListener(final Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");
        this.getShardCache().forEach(jda -> jda.addEventListener(listeners));
    }

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     *         The listener(s) to be removed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    default void removeEventListener(final Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");
        this.getShardCache().forEach(jda -> jda.removeEventListener(listeners));
    }

    /**
     * Adds listeners provided by the listener provider to each shard to the event-listeners that will be used to handle events.
     * The listener provider gets a shard id applied and is expected to return a listener.
     *
     * <p>Note: when using the {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.core.hooks.EventListener EventListener}!
     *
     * @param  eventListenerProvider
     *         The provider of listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided listener provider or any of the listeners or provides are {@code null}.
     */
    default void addEventListeners(final IntFunction<Object> eventListenerProvider)
    {
        Checks.notNull(eventListenerProvider, "event listener provider");
        this.getShardCache().forEach(jda ->
        {
            Object listener = eventListenerProvider.apply(jda.getShardInfo().getShardId());
            if (listener != null) jda.addEventListener(listener);
        });
    }

    /**
     * Remove listeners from shards by their id.
     * The provider takes shard ids, and returns a collection of listeners that shall be removed from the respective
     * shards.
     *
     * @param eventListenerProvider
     *        gets shard ids applied and is expected to return a collection of listeners that shall be removed from
     *        the respective shards
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided event listeners provider is {@code null}.
     */
    default void removeEventListeners(final IntFunction<Collection<Object>> eventListenerProvider)
    {
        Checks.notNull(eventListenerProvider, "event listener provider");
        this.getShardCache().forEach(jda ->
            jda.removeEventListener(eventListenerProvider.apply(jda.getShardInfo().getShardId()))
        );
    }

    /**
     * Remove a listener provider. This will stop further created / restarted shards from getting a listener added by
     * that provider.
     *
     * Default is a no-op for backwards compatibility, see implementations like
     * {@link DefaultShardManager#removeEventListenerProvider(IntFunction)} for actual code
     *
     * @param  eventListenerProvider
     *         The provider of listeners that shall be removed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided listener provider is {@code null}.
     */
    default void removeEventListenerProvider(IntFunction<Object> eventListenerProvider)
    {
    }

    /**
     * Returns the amount of shards queued for (re)connecting.
     *
     * @return The amount of shards queued for (re)connecting.
     */
    int getShardsQueued();

    /**
     * Returns the amount of running shards.
     *
     * @return The amount of running shards.
     */
    default int getShardsRunning()
    {
        return (int) this.getShardCache().size();
    }

    /**
     * Returns the amount of shards managed by this {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager}.
     * This includes shards currently queued for a restart.
     *
     * @return The managed amount of shards.
     */
    default int getShardsTotal()
    {
        return this.getShardsQueued() + this.getShardsRunning();
    }

    /**
     * Used to access Bot specific functions like OAuth information.
     *
     * @throws java.lang.IllegalStateException
     *         If there is no running shard
     *
     * @return The {@link net.dv8tion.jda.bot.JDABot} registry for this instance of JDA.
     */
    default RestAction<ApplicationInfo> getApplicationInfo()
    {
        return this.getShardCache().stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("no active shards"))
                .asBot()
                .getApplicationInfo();
    }

    /**
     * The average time in milliseconds between all shards that discord took to respond to our last heartbeat.
     * This roughly represents the WebSocket ping of this session. If there is no shard running this wil return {@code -1}.
     *
     * <p><b>{@link net.dv8tion.jda.core.requests.RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * @return The average time in milliseconds between heartbeat and the heartbeat ack response
     */
    default double getAveragePing()
    {
        return this.getShardCache()
                .stream()
                .mapToLong(JDA::getPing)
                .filter(ping -> ping != -1)
                .average()
                .orElse(-1D);
    }

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Category Categories} visible to the currently logged in account.
     *
     * @return An immutable list of all visible {@link net.dv8tion.jda.core.entities.Category Categories}.
     */
    default List<Category> getCategories()
    {
        return this.getCategoryCache().asList();
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Category Categories} that have the same name as the one
     * provided. <br>If there are no matching categories this will return an empty list.
     *
     * @param  name
     *         The name to check
     * @param  ignoreCase
     *         Whether to ignore case on name checking
     * @return Immutable list of all categories matching the provided name
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}
     */
    default List<Category> getCategoriesByName(final String name, final boolean ignoreCase)
    {
        return this.getCategoryCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Category Category} that matches the provided id. <br>If there is no
     * matching {@link net.dv8tion.jda.core.entities.Category Category} this returns {@code null}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Category Category} for the provided ID.
     */
    default Category getCategoryById(final long id)
    {
        return this.getCategoryCache().getElementById(id);
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Category Category} that matches the provided id. <br>If there is no
     * matching {@link net.dv8tion.jda.core.entities.Category Category} this returns {@code null}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Category Category} for the provided ID.
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is not a valid {@code long}
     */
    default Category getCategoryById(final String id)
    {
        return this.getCategoryCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.Category Categories} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<Category> getCategoryCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getCategoryCache));
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.Emote}.
     * @return An {@link net.dv8tion.jda.core.entities.Emote Emote} represented by this id or null if none is found in
     * our cache.
     */
    default Emote getEmoteById(final long id)
    {
        return this.getEmoteCache().getElementById(id);
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.Emote}.
     * @return An {@link net.dv8tion.jda.core.entities.Emote Emote} represented by this id or null if none is found in
     *         our cache.
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    default Emote getEmoteById(final String id)
    {
        return this.getEmoteCache().getElementById(id);
    }

    /**
     * Unified {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.Emote Emotes} visible to this ShardManager instance.
     *
     * @return Unified {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<Emote> getEmoteCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getEmoteCache));
    }

    /**
     * A collection of all to us known emotes (managed/restricted included).
     *
     * <p><b>Hint</b>: To check whether you can use an {@link net.dv8tion.jda.core.entities.Emote Emote} in a specific
     * context you can use {@link Emote#canInteract(net.dv8tion.jda.core.entities.Member)} or {@link
     * Emote#canInteract(net.dv8tion.jda.core.entities.User, net.dv8tion.jda.core.entities.MessageChannel)}
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @return An immutable list of Emotes (which may or may not be available to usage).
     */
    default List<Emote> getEmotes()
    {
        return this.getEmoteCache().asList();
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.Emote Emotes} that have the same name as the one
     * provided. <br>If there are no {@link net.dv8tion.jda.core.entities.Emote Emotes} with the provided name, then
     * this returns an empty list.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @param  name
     *         The name of the requested {@link net.dv8tion.jda.core.entities.Emote Emotes}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link
     *         net.dv8tion.jda.core.entities.Emote#getName()}.
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Emote Emotes} that all have the same
     *         name as the provided name.
     */
    default List<Emote> getEmotesByName(final String name, final boolean ignoreCase)
    {
        return this.getEmoteCache().getElementsByName(name, ignoreCase);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    default Guild getGuildById(final long id)
    {
        return this.getGuildCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    default Guild getGuildById(final String id)
    {
        return this.getGuildCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.Guild Guilds} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<Guild> getGuildCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getGuildCache));
    }
    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link net.dv8tion.jda.core.entities.Guild Guilds}, this will return
     * an empty list.
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Guild Guilds} that this account is connected to.
     */
    default List<Guild> getGuilds()
    {
        return this.getGuildCache().asList();
    }

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *        The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    default List<Guild> getMutualGuilds(final Collection<User> users)
    {
        Checks.noneNull(users, "users");
        return Collections.unmodifiableList(
                this.getGuildCache().stream()
                .filter(guild -> users.stream()
                        .allMatch(guild::isMember))
                .collect(Collectors.toList()));
    }

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    default List<Guild> getMutualGuilds(final User... users)
    {
        Checks.notNull(users, "users");
        return this.getMutualGuilds(Arrays.asList(users));
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with matching id.
     */
    default PrivateChannel getPrivateChannelById(final long id)
    {
        return this.getPrivateChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with matching id.
     */
    default PrivateChannel getPrivateChannelById(final String id)
    {
        return this.getPrivateChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<PrivateChannel> getPrivateChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getPrivateChannelCache));
    }

    /**
     * An unmodifiable list of all known {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}.
     *
     * @return Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}.
     */
    default List<PrivateChannel> getPrivateChannels()
    {
        return this.getPrivateChannelCache().asList();
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.core.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link net.dv8tion.jda.core.entities.Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Role Role} for the specified ID
     */
    default Role getRoleById(final long id)
    {
        return this.getRoleCache().getElementById(id);
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.core.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link net.dv8tion.jda.core.entities.Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Role Role} for the specified ID
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    default Role getRoleById(final String id)
    {
        return this.getRoleCache().getElementById(id);
    }

    /**
     * Unified {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.Role Roles} visible to this ShardManager instance.
     *
     * @return Unified {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<Role> getRoleCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getRoleCache));
    }

    /**
     * All {@link net.dv8tion.jda.core.entities.Role Roles} this ShardManager instance can see. <br>This will iterate over each
     * {@link net.dv8tion.jda.core.entities.Guild Guild} retrieved from {@link #getGuilds()} and collect its {@link
     * net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()}.
     *
     * @return Immutable List of all visible Roles
     */
    default List<Role> getRoles()
    {
        return this.getRoleCache().asList();
    }

    /**
     * Retrieves all {@link net.dv8tion.jda.core.entities.Role Roles} visible to this ShardManager instance.
     * <br>This simply filters the Roles returned by {@link #getRoles()} with the provided name, either using
     * {@link String#equals(Object)} or {@link String#equalsIgnoreCase(String)} on {@link net.dv8tion.jda.core.entities.Role#getName()}.
     *
     * @param  name
     *         The name for the Roles
     * @param  ignoreCase
     *         Whether to use {@link String#equalsIgnoreCase(String)}
     * @return Immutable List of all Roles matching the parameters provided.
     */
    default List<Role> getRolesByName(final String name, final boolean ignoreCase)
    {
        return this.getRoleCache().getElementsByName(name, ignoreCase);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.JDA JDA} instance which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the shard.
     *
     * @return The {@link net.dv8tion.jda.core.JDA JDA} instance with the given shardId or
     *         {@code null} if no shard has the given id
     */
    default JDA getShardById(final int id)
    {
        return this.getShardCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.JDA JDA} instance which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the shard.
     *
     * @return The {@link net.dv8tion.jda.core.JDA JDA} instance with the given shardId or
     *         {@code null} if no shard has the given id
     */
    default JDA getShardById(final String id)
    {
        return this.getShardCache().getElementById(id);
    }

    /**
     * Unified {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView} of
     * all cached {@link net.dv8tion.jda.core.JDA JDA} bound to this ShardManager instance.
     *
     * @return Unified {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView}
     */
    ShardCacheView getShardCache();

    /**
     * Gets all {@link net.dv8tion.jda.core.JDA JDA} instances bound to this ShardManager.
     *
     * @return An immutable list of all managed {@link net.dv8tion.jda.core.JDA JDA} instances.
     */
    default List<JDA> getShards()
    {
        return this.getShardCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.JDA.Status JDA.Status} of the shard which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  shardId
     *         The id of the shard.
     *
     * @return The  {@link net.dv8tion.jda.core.JDA.Status JDA.Status} of the shard with the given shardId or
     *         {@code null} if no shard has the given id
     */
    default JDA.Status getStatus(final int shardId)
    {
        final JDA jda = this.getShardCache().getElementById(shardId);
        return jda == null ? null : jda.getStatus();
    }

    /**
     * Gets the current {@link net.dv8tion.jda.core.JDA.Status Status} of all shards.
     *
     * @return All current shard statuses.
     */
    default Map<JDA, Status> getStatuses()
    {
        return Collections.unmodifiableMap(this.getShardCache().stream()
                .collect(Collectors.toMap(Function.identity(), JDA::getStatus)));
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     */
    default TextChannel getTextChannelById(final long id)
    {
        return this.getTextChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     */
    default TextChannel getTextChannelById(final String id)
    {
        return this.getTextChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getTextChannelCache));
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @return Possibly-empty list of all known {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     */
    default List<TextChannel> getTextChannels()
    {
        return this.getTextChannelCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    default User getUserById(final long id)
    {
        return this.getUserCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    default User getUserById(final String id)
    {
        return this.getUserCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.User Users} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<User> getUserCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getUserCache));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.User Users} that share a
     * {@link net.dv8tion.jda.core.entities.Guild Guild} with the currently logged in account.
     * <br>This list will never contain duplicates and represents all {@link net.dv8tion.jda.core.entities.User Users}
     * that JDA can currently see.
     *
     * <p>If the developer is sharding, then only users from guilds connected to the specifically logged in
     * shard will be returned in the List.
     *
     * @return List of all {@link net.dv8tion.jda.core.entities.User Users} that are visible to JDA.
     */
    default List<User> getUsers()
    {
        return this.getUserCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    default VoiceChannel getVoiceChannelById(final long id)
    {
        return this.getVoiceChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    default VoiceChannel getVoiceChannelById(final String id)
    {
        return this.getVoiceChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    default SnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getVoiceChannelCache));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     *
     * @return Possible-empty list of all known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    default List<VoiceChannel> getVoiceChannels()
    {
        return this.getVoiceChannelCache().asList();
    }

    /**
     * Restarts all shards, shutting old ones down first.
     * 
     * As all shards need to connect to discord again this will take equally long as the startup of a new ShardManager
     * (using the 5000ms + backoff as delay between starting new JDA instances). 
     */
    void restart();

    /**
     * Restarts the shards with the given id only.
     * <br> If there is no shard with the given Id this method acts like {@link #start(int)}.
     *
     * @param  id
     *         The id of the target shard
     *
     * @throws IllegalArgumentException
     *         if shardId is negative or higher than maxShardId
     */
    void restart(int id);

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for all shards.
     * <br>A Game can be retrieved via {@link net.dv8tion.jda.core.entities.Game#playing(String)}.
     * For streams you provide a valid streaming url as second parameter.
     *
     * <p>This will also change the game for shards that are created in the future.
     *
     * @param  game
     *         A {@link net.dv8tion.jda.core.entities.Game Game} instance or null to reset
     *
     * @see    net.dv8tion.jda.core.entities.Game#playing(String)
     * @see    net.dv8tion.jda.core.entities.Game#streaming(String, String)
     */
    default void setGame(final Game game)
    {
        this.setGameProvider(id -> game);
    }

    /**
     * Sets provider that provider the {@link net.dv8tion.jda.core.entities.Game Game} for all shards.
     * <br>A Game can be retrieved via {@link net.dv8tion.jda.core.entities.Game#playing(String)}.
     * For streams you provide a valid streaming url as second parameter.
     *
     * <p>This will also change the provider for shards that are created in the future.
     *
     * @param  gameProvider
     *         A {@link net.dv8tion.jda.core.entities.Game Game} instance or null to reset
     *
     * @see    net.dv8tion.jda.core.entities.Game#playing(String)
     * @see    net.dv8tion.jda.core.entities.Game#streaming(String, String)
     */
    default void setGameProvider(final IntFunction<Game> gameProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setGame(gameProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Sets whether all instances should be marked as afk or not
     *
     * <p>This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * <p>This will also change the value for shards that are created in the future.
     *
     * @param  idle
     *        boolean
     */
    default void setIdle(final boolean idle)
    {
        this.setIdleProvider(id -> idle);
    }

    /**
     * Sets the provider that decides for all shards whether they should be marked as afk or not.
     *
     * <p>This will also change the provider for shards that are created in the future.
     *
     * @param  idleProvider
     *        boolean
     */
    default void setIdleProvider(final IntFunction<Boolean> idleProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setIdle(idleProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for all shards.
     *
     * <p>This will also change the status for shards that are created in the future.
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  status
     *         the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     */
    default void setStatus(final OnlineStatus status)
    {
        this.setStatusProvider(id -> status);
    }

    /**
     * Sets the provider that provides the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for all shards.
     *
     * <p>This will also change the provider for shards that are created in the future.
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  statusProvider
     *         the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     */
    default void setStatusProvider(final IntFunction<OnlineStatus> statusProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setStatus(statusProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this method has been called the ShardManager instance can not be used anymore.
     */
    void shutdown();

    /**
     * Shuts down the shard with the given id only.
     * <br> This does nothing if there is no shard with the given id.
     *
     * @param  shardId
     *        The id of the shard that should be stopped
     */
    void shutdown(int shardId);

    /**
     * Adds a new shard with the given id to this ShardManager and starts it.
     *
     * @param  shardId
     *        The id of the shard that should be started
     */
    void start(int shardId);

}
