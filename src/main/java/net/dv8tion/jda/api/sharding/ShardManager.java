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
package net.dv8tion.jda.api.sharding;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.ShardCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * This class acts as a manager for multiple shards.
 * It contains several methods to make your life with sharding easier.
 *
 * <br>Custom implementations may not support all methods and throw
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
     * <p>Note: when using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * the given listener <b>must</b> be an instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    default void addEventListener(@Nonnull final Object... listeners)
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
    default void removeEventListener(@Nonnull final Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");
        this.getShardCache().forEach(jda -> jda.removeEventListener(listeners));
    }

    /**
     * Adds listeners provided by the listener provider to each shard to the event-listeners that will be used to handle events.
     * The listener provider gets a shard id applied and is expected to return a listener.
     *
     * <p>Note: when using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * the given listener <b>must</b> be an instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  eventListenerProvider
     *         The provider of listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided listener provider or any of the listeners or provides are {@code null}.
     */
    default void addEventListeners(@Nonnull final IntFunction<Object> eventListenerProvider)
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
     * @param  eventListenerProvider
     *         Gets shard ids applied and is expected to return a collection of listeners that shall be removed from
     *         the respective shards
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided event listeners provider is {@code null}.
     */
    default void removeEventListeners(@Nonnull final IntFunction<Collection<Object>> eventListenerProvider)
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
    default void removeEventListenerProvider(@Nonnull IntFunction<Object> eventListenerProvider)
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
     * Returns the amount of shards managed by this {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager}.
     * This includes shards currently queued for a restart.
     *
     * @return The managed amount of shards.
     */
    default int getShardsTotal()
    {
        return this.getShardsQueued() + this.getShardsRunning();
    }

    /**
     * The {@link GatewayIntent GatewayIntents} for the JDA sessions of this shard manager.
     *
     * @return {@link EnumSet} of active gateway intents
     */
    @Nonnull
    default EnumSet<GatewayIntent> getGatewayIntents()
    {
        //noinspection ConstantConditions
        return getShardCache().applyStream((stream) ->
                stream.map(JDA::getGatewayIntents)
                      .findAny()
                      .orElse(EnumSet.noneOf(GatewayIntent.class)));
    }

    /**
     * Used to access application details of this bot.
     * <br>Since this is the same for every shard it picks {@link JDA#retrieveApplicationInfo()} from any shard.
     *
     * @throws java.lang.IllegalStateException
     *         If there is no running shard
     *
     * @return The Application registry for this bot.
     */
    @Nonnull
    default RestAction<ApplicationInfo> retrieveApplicationInfo()
    {
        return this.getShardCache().stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("no active shards"))
                .retrieveApplicationInfo();
    }

    /**
     * The average time in milliseconds between all shards that discord took to respond to our last heartbeat.
     * This roughly represents the WebSocket ping of this session. If there are no shards running, this will return {@code -1}.
     *
     * <p><b>{@link net.dv8tion.jda.api.requests.RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * @return The average time in milliseconds between heartbeat and the heartbeat ack response
     */
    default double getAverageGatewayPing()
    {
        return this.getShardCache()
                .stream()
                .mapToLong(JDA::getGatewayPing)
                .filter(ping -> ping != -1)
                .average()
                .orElse(-1D);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.Category Categories} visible to the currently logged in account.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getCategoryCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return An immutable list of all visible {@link net.dv8tion.jda.api.entities.Category Categories}.
     */
    @Nonnull
    default List<Category> getCategories()
    {
        return this.getCategoryCache().asList();
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Category Categories} that have the same name as the one
     * provided. <br>If there are no matching categories, this will return an empty list.
     *
     * @param  name
     *         The name to check
     * @param  ignoreCase
     *         Whether to ignore case on name checking
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}
     *
     * @return Immutable list of all categories matching the provided name
     */
    @Nonnull
    default List<Category> getCategoriesByName(@Nonnull final String name, final boolean ignoreCase)
    {
        return this.getCategoryCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets the {@link net.dv8tion.jda.api.entities.Category Category} that matches the provided id. <br>If there is no
     * matching {@link net.dv8tion.jda.api.entities.Category Category}, this will return {@code null}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(final long id)
    {
        return this.getCategoryCache().getElementById(id);
    }

    /**
     * Gets the {@link net.dv8tion.jda.api.entities.Category Category} that matches the provided id. <br>If there is no
     * matching {@link net.dv8tion.jda.api.entities.Category Category}, this will return {@code null}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is not a valid {@code long}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(@Nonnull final String id)
    {
        return this.getCategoryCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Category Categories} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<Category> getCategoryCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getCategoryCache));
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
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
    default Emote getEmoteById(final long id)
    {
        return this.getEmoteCache().getElementById(id);
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
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
    default Emote getEmoteById(@Nonnull final String id)
    {
        return this.getEmoteCache().getElementById(id);
    }

    /**
     * Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Emote Emotes} visible to this ShardManager instance.
     *
     *
     * @return Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<Emote> getEmoteCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getEmoteCache));
    }

    /**
     * A collection of all known emotes (managed/restricted included).
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
        return this.getEmoteCache().asList();
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.Emote Emotes} that have the same name as the one
     * provided. <br>If there are no {@link net.dv8tion.jda.api.entities.Emote Emotes} with the provided name, this will
     * return an empty list.
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
    default List<Emote> getEmotesByName(@Nonnull final String name, final boolean ignoreCase)
    {
        return this.getEmoteCache().getElementsByName(name, ignoreCase);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Guild Guild} with matching id.
     */
    @Nullable
    default Guild getGuildById(final long id)
    {
        return getGuildCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Guild Guild} with matching id.
     */
    @Nullable
    default Guild getGuildById(@Nonnull final String id)
    {
        return getGuildById(MiscUtil.parseSnowflake(id));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.Guild Guilds} that have the same name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Guild Guilds} with the provided name, this will return an empty list.
     *
     * @param  name
     *         The name of the requested {@link net.dv8tion.jda.api.entities.Guild Guilds}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.api.entities.Guild#getName()}.
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.api.entities.Guild Guilds} that all have the same name as the provided name.
     */
    @Nonnull
    default List<Guild> getGuildsByName(@Nonnull final String name, final boolean ignoreCase)
    {
        return this.getGuildCache().getElementsByName(name, ignoreCase);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Guild Guilds} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<Guild> getGuildCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getGuildCache));
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.api.entities.Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link net.dv8tion.jda.api.entities.Guild Guilds}, this will return
     * an empty list.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getGuildCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.api.entities.Guild Guilds} that this account is connected to.
     */
    @Nonnull
    default List<Guild> getGuilds()
    {
        return this.getGuildCache().asList();
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link net.dv8tion.jda.api.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.api.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.api.entities.User Users} in them.
     */
    @Nonnull
    default List<Guild> getMutualGuilds(@Nonnull final Collection<User> users)
    {
        Checks.noneNull(users, "users");
        return Collections.unmodifiableList(
                this.getGuildCache().stream()
                .filter(guild -> users.stream()
                        .allMatch(guild::isMember))
                .collect(Collectors.toList()));
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link net.dv8tion.jda.api.entities.Guild Guilds} must contain.
     *
     * @return Unmodifiable list of all {@link net.dv8tion.jda.api.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.api.entities.User Users} in them.
     */
    @Nonnull
    default List<Guild> getMutualGuilds(@Nonnull final User... users)
    {
        Checks.notNull(users, "users");
        return this.getMutualGuilds(Arrays.asList(users));
    }

    /**
     * Attempts to retrieve a {@link net.dv8tion.jda.api.entities.User User} object based on the provided id.
     * <br>This first calls {@link #getUserById(long)}, and if the return is {@code null} then a request
     * is made to the Discord servers.
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
     * @throws java.lang.IllegalArgumentException
     *         If the provided id String is not a valid snowflake.
     * @throws java.lang.IllegalStateException
     *         If there isn't any active shards.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.User User}
     *         <br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveUserById(@Nonnull String id)
    {
        return retrieveUserById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Attempts to retrieve a {@link net.dv8tion.jda.api.entities.User User} object based on the provided id.
     * <br>This first calls {@link #getUserById(long)}, and if the return is {@code null} then a request
     * is made to the Discord servers.
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
     * @throws java.lang.IllegalStateException
     *         If there isn't any active shards.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.User User}
     *         <br>On request, gets the User with id matching provided id from Discord.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveUserById(long id)
    {
        JDA api = null;
        for (JDA shard : getShardCache())
        {
            api = shard;
            EnumSet<GatewayIntent> intents = shard.getGatewayIntents();
            User user = shard.getUserById(id);
            boolean isUpdated = intents.contains(GatewayIntent.GUILD_PRESENCES) || intents.contains(GatewayIntent.GUILD_MEMBERS);
            if (user != null && isUpdated)
                return new CompletedRestAction<>(shard, user);
        }

        if (api == null)
            throw new IllegalStateException("no shards active");

        JDAImpl jda = (JDAImpl) api;
        Route.CompiledRoute route = Route.Users.GET_USER.compile(Long.toUnsignedString(id));
        return new RestActionImpl<>(jda, route, (response, request) -> jda.getEntityBuilder().createUser(response.getObject()));
    }

    /**
     * Searches for the first user that has the matching Discord Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     * <p>This will only check cached users!
     *
     * <p>This only checks users that are known to the currently logged in account (shards). If a user exists
     * with the tag that is not available in the {@link #getUserCache() User-Cache} it will not be detected.
     * <br>Currently Discord does not offer a way to retrieve a user by their discord tag.
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
        return getShardCache().applyStream(stream ->
            stream.map(jda -> jda.getUserByTag(tag))
                  .filter(Objects::nonNull)
                  .findFirst()
                  .orElse(null)
        );
    }

    /**
     * Searches for the first user that has the matching Discord Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     * <p>This will only check cached users!
     *
     * <p>This only checks users that are known to the currently logged in account (shards). If a user exists
     * with the tag that is not available in the {@link #getUserCache() User-Cache} it will not be detected.
     * <br>Currently Discord does not offer a way to retrieve a user by their discord tag.
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
        return getShardCache().applyStream(stream ->
            stream.map(jda -> jda.getUserByTag(username, discriminator))
                  .filter(Objects::nonNull)
                  .findFirst()
                  .orElse(null)
        );
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with matching id.
     */
    @Nullable
    default PrivateChannel getPrivateChannelById(final long id)
    {
        return this.getPrivateChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} with matching id.
     */
    @Nullable
    default PrivateChannel getPrivateChannelById(@Nonnull final String id)
    {
        return this.getPrivateChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<PrivateChannel> getPrivateChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getPrivateChannelCache));
    }

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
        return this.getPrivateChannelCache().asList();
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link net.dv8tion.jda.api.entities.Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Role Role} for the specified ID
     */
    @Nullable
    default Role getRoleById(final long id)
    {
        return this.getRoleCache().getElementById(id);
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link net.dv8tion.jda.api.entities.Guild Guilds} and check whether a Role from that Guild is assigned
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
    default Role getRoleById(@Nonnull final String id)
    {
        return this.getRoleCache().getElementById(id);
    }

    /**
     * Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Role Roles} visible to this ShardManager instance.
     *
     * @return Unified {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<Role> getRoleCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getRoleCache));
    }

    /**
     * All {@link net.dv8tion.jda.api.entities.Role Roles} this ShardManager instance can see. <br>This will iterate over each
     * {@link net.dv8tion.jda.api.entities.Guild Guild} retrieved from {@link #getGuilds()} and collect its {@link
     * net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()}.
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
        return this.getRoleCache().asList();
    }

    /**
     * Retrieves all {@link net.dv8tion.jda.api.entities.Role Roles} visible to this ShardManager instance.
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
    default List<Role> getRolesByName(@Nonnull final String name, final boolean ignoreCase)
    {
        return this.getRoleCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     * <br>This checks if any of the channel types in this guild have the provided ID and returns the first match.
     *
     * <br>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getTextChannelById(String)}</li>
     *     <li>{@link #getVoiceChannelById(String)}</li>
     *     <li>{@link #getStoreChannelById(String)}</li>
     *     <li>{@link #getCategoryById(String)}</li>
     * </ul>
     *
     * @param  id
     *         The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@Nonnull String id)
    {
        return getGuildChannelById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     * <br>This checks if any of the channel types in this guild have the provided ID and returns the first match.
     *
     * <br>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getTextChannelById(long)}</li>
     *     <li>{@link #getVoiceChannelById(long)}</li>
     *     <li>{@link #getStoreChannelById(long)}</li>
     *     <li>{@link #getCategoryById(long)}</li>
     * </ul>
     *
     * @param  id
     *         The ID of the channel
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(long id)
    {
        GuildChannel channel;
        for (JDA shard : getShards())
        {
            channel = shard.getGuildChannelById(id);
            if (channel != null)
                return channel;
        }

        return null;
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <br>This is meant for systems that use a dynamic {@link net.dv8tion.jda.api.entities.ChannelType} and can
     * profit from a simple function to get the channel instance.
     * To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getTextChannelById(String)}</li>
     *     <li>{@link #getVoiceChannelById(String)}</li>
     *     <li>{@link #getStoreChannelById(String)}</li>
     *     <li>{@link #getCategoryById(String)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@link net.dv8tion.jda.api.entities.ChannelType} is null
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@Nonnull ChannelType type, @Nonnull String id)
    {
        return getGuildChannelById(type, MiscUtil.parseSnowflake(id));
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <br>This is meant for systems that use a dynamic {@link net.dv8tion.jda.api.entities.ChannelType} and can
     * profit from a simple function to get the channel instance.
     * To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getTextChannelById(long)}</li>
     *     <li>{@link #getVoiceChannelById(long)}</li>
     *     <li>{@link #getStoreChannelById(long)}</li>
     *     <li>{@link #getCategoryById(long)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@link net.dv8tion.jda.api.entities.ChannelType} is null
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@Nonnull ChannelType type, long id)
    {
        Checks.notNull(type, "ChannelType");
        GuildChannel channel;
        for (JDA shard : getShards())
        {
            channel = shard.getGuildChannelById(type, id);
            if (channel != null)
                return channel;
        }

        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.JDA JDA} instance which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, this will return {@code null}.
     *
     * @param  id
     *         The id of the shard.
     *
     * @return The {@link net.dv8tion.jda.api.JDA JDA} instance with the given shardId or
     *         {@code null} if no shard has the given id
     */
    @Nullable
    default JDA getShardById(final int id)
    {
        return this.getShardCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.JDA JDA} instance which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, this will return {@code null}.
     *
     * @param  id
     *         The id of the shard.
     *
     * @return The {@link net.dv8tion.jda.api.JDA JDA} instance with the given shardId or
     *         {@code null} if no shard has the given id
     */
    @Nullable
    default JDA getShardById(@Nonnull final String id)
    {
        return this.getShardCache().getElementById(id);
    }

    /**
     * Unified {@link ShardCacheView ShardCacheView} of
     * all cached {@link net.dv8tion.jda.api.JDA JDA} bound to this ShardManager instance.
     *
     * @return Unified {@link ShardCacheView ShardCacheView}
     */
    @Nonnull
    ShardCacheView getShardCache();

    /**
     * Gets all {@link net.dv8tion.jda.api.JDA JDA} instances bound to this ShardManager.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getShardCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return An immutable list of all managed {@link net.dv8tion.jda.api.JDA JDA} instances.
     */
    @Nonnull
    default List<JDA> getShards()
    {
        return this.getShardCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.JDA.Status JDA.Status} of the shard which has the same id as the one provided.
     * <br>If there is no shard with an id that matches the provided one, this will return {@code null}.
     *
     * @param  shardId
     *         The id of the shard.
     *
     * @return The {@link net.dv8tion.jda.api.JDA.Status JDA.Status} of the shard with the given shardId or
     *         {@code null} if no shard has the given id
     */
    @Nullable
    default JDA.Status getStatus(final int shardId)
    {
        final JDA jda = this.getShardCache().getElementById(shardId);
        return jda == null ? null : jda.getStatus();
    }

    /**
     * Gets the current {@link net.dv8tion.jda.api.JDA.Status Status} of all shards.
     *
     * @return All current shard statuses.
     */
    @Nonnull
    default Map<JDA, Status> getStatuses()
    {
        return Collections.unmodifiableMap(this.getShardCache().stream()
                .collect(Collectors.toMap(Function.identity(), JDA::getStatus)));
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} permission in.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(final long id)
    {
        return this.getTextChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} permission in.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(@Nonnull final String id)
    {
        return this.getTextChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getTextChannelCache));
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} of all connected
     * {@link net.dv8tion.jda.api.entities.Guild Guilds}.
     *
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} permission in.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getTextChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty list of all known {@link net.dv8tion.jda.api.entities.TextChannel TextChannels}.
     */
    @Nonnull
    default List<TextChannel> getTextChannels()
    {
        return this.getTextChannelCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} with matching id.
     */
    @Nullable
    default StoreChannel getStoreChannelById(final long id)
    {
        return this.getStoreChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel} with matching id.
     */
    @Nullable
    default StoreChannel getStoreChannelById(@Nonnull final String id)
    {
        return this.getStoreChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<StoreChannel> getStoreChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getStoreChannelCache));
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannels} of all connected
     * {@link net.dv8tion.jda.api.entities.Guild Guilds}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getStoreChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty list of all known {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannels}.
     */
    @Nonnull
    default List<StoreChannel> getStoreChannels()
    {
        return this.getStoreChannelCache().asList();
    }
    
    /**
     * This returns the {@link net.dv8tion.jda.api.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this will return {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.User User} with matching id.
     */
    @Nullable
    default User getUserById(final long id)
    {
        return this.getUserCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this will return {@code null}.
     *
     * @param  id
     *         The id of the requested {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.User User} with matching id.
     */
    @Nullable
    default User getUserById(@Nonnull final String id)
    {
        return this.getUserCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.User Users} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<User> getUserCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getUserCache));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.User Users} that share a
     * {@link net.dv8tion.jda.api.entities.Guild Guild} with the currently logged in account.
     * <br>This list will never contain duplicates and represents all {@link net.dv8tion.jda.api.entities.User Users}
     * that JDA can currently see.
     *
     * <p>If the developer is sharding, then only users from guilds connected to the specifically logged in
     * shard will be returned in the List.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getUserCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return List of all {@link net.dv8tion.jda.api.entities.User Users} that are visible to JDA.
     */
    @Nonnull
    default List<User> getUsers()
    {
        return this.getUserCache().asList();
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(final long id)
    {
        return this.getVoiceChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, this will return {@code null}.
     *
     * @param  id The id of the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(@Nonnull final String id)
    {
        return this.getVoiceChannelCache().getElementById(id);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} visible to this ShardManager instance.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    default SnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return CacheView.allSnowflakes(() -> this.getShardCache().stream().map(JDA::getVoiceChannelCache));
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} of all connected
     * {@link net.dv8tion.jda.api.entities.Guild Guilds}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getVoiceChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possible-empty list of all known {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}.
     */
    @Nonnull
    default List<VoiceChannel> getVoiceChannels()
    {
        return this.getVoiceChannelCache().asList();
    }

    /**
     * Restarts all shards, shutting old ones down first.
     * 
     * <p>As all shards need to connect to discord again this will take equally long as the startup of a new ShardManager
     * (using the 5000ms + backoff as delay between starting new JDA instances).
     *
     * @throws java.util.concurrent.RejectedExecutionException
     *         If {@link #shutdown()} has already been invoked
     */
    void restart();

    /**
     * Restarts the shards with the given id only.
     * <br> If there is no shard with the given Id, this method acts like {@link #start(int)}.
     *
     * @param  id
     *         The id of the target shard
     *
     * @throws java.lang.IllegalArgumentException
     *         If shardId is negative or higher than maxShardId
     * @throws java.util.concurrent.RejectedExecutionException
     *         If {@link #shutdown()} has already been invoked
     */
    void restart(int id);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Activity Activity} for all shards.
     * <br>An Activity can be retrieved via {@link net.dv8tion.jda.api.entities.Activity#playing(String)}.
     * For streams you provide a valid streaming url as second parameter.
     *
     * <p>This will also change the activity for shards that are created in the future.
     *
     * @param  activity
     *         A {@link net.dv8tion.jda.api.entities.Activity Activity} instance or null to reset
     *
     * @see    net.dv8tion.jda.api.entities.Activity#playing(String)
     * @see    net.dv8tion.jda.api.entities.Activity#streaming(String, String)
     */
    default void setActivity(@Nullable final Activity activity)
    {
        this.setActivityProvider(id -> activity);
    }

    /**
     * Sets provider that provider the {@link net.dv8tion.jda.api.entities.Activity Activity} for all shards.
     * <br>A Activity can be retrieved via {@link net.dv8tion.jda.api.entities.Activity#playing(String)}.
     * For streams you provide a valid streaming url as second parameter.
     *
     * <p>This will also change the provider for shards that are created in the future.
     *
     * @param  activityProvider
     *         Provider for an {@link net.dv8tion.jda.api.entities.Activity Activity} instance or null to reset
     *
     * @see    net.dv8tion.jda.api.entities.Activity#playing(String)
     * @see    net.dv8tion.jda.api.entities.Activity#streaming(String, String)
     */
    default void setActivityProvider(@Nullable final IntFunction<? extends Activity> activityProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setActivity(activityProvider == null ? null : activityProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Sets whether all instances should be marked as afk or not
     *
     * <p>This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * <p>This will also change the value for shards that are created in the future.
     *
     * @param idle
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
     * @param idleProvider
     *        Provider for a boolean
     */
    default void setIdleProvider(@Nonnull final IntFunction<Boolean> idleProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setIdle(idleProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} and {@link net.dv8tion.jda.api.entities.Activity Activity} for all shards.
     *
     * <p>This will also change the status for shards that are created in the future.
     *
     * @param  status
     *         The {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     * @param  activity
     *         A {@link net.dv8tion.jda.api.entities.Activity Activity} instance or null to reset
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided OnlineStatus is {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @see    net.dv8tion.jda.api.entities.Activity#playing(String)
     * @see    net.dv8tion.jda.api.entities.Activity#streaming(String, String)
     */
    default void setPresence(@Nullable final OnlineStatus status, @Nullable final Activity activity)
    {
        this.setPresenceProvider(id -> status, id -> activity);
    }

    /**
     * Sets the provider that provides the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} and
     * {@link net.dv8tion.jda.api.entities.Activity Activity} for all shards.
     *
     * <p>This will also change the status for shards that are created in the future.
     *
     * @param  statusProvider
     *         The {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     * @param  activityProvider
     *         A {@link net.dv8tion.jda.api.entities.Activity Activity} instance or null to reset
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided OnlineStatus is {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @see    net.dv8tion.jda.api.entities.Activity#playing(String)
     * @see    net.dv8tion.jda.api.entities.Activity#streaming(String, String)
     */
    default void setPresenceProvider(@Nullable final IntFunction<OnlineStatus> statusProvider, @Nullable final IntFunction<? extends Activity> activityProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setPresence(statusProvider == null ? null : statusProvider.apply(jda.getShardInfo().getShardId()), activityProvider == null ? null : activityProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} for all shards.
     *
     * <p>This will also change the status for shards that are created in the future.
     *
     * @param  status
     *         The {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided OnlineStatus is {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}
     */
    default void setStatus(@Nullable final OnlineStatus status)
    {
        this.setStatusProvider(id -> status);
    }

    /**
     * Sets the provider that provides the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} for all shards.
     *
     * <p>This will also change the provider for shards that are created in the future.
     *
     * @param  statusProvider
     *         The {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided OnlineStatus is {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}
     */
    default void setStatusProvider(@Nullable final IntFunction<OnlineStatus> statusProvider)
    {
        this.getShardCache().forEach(jda -> jda.getPresence().setStatus(statusProvider == null ? null : statusProvider.apply(jda.getShardInfo().getShardId())));
    }

    /**
     * Shuts down all JDA shards, closing all their connections.
     * After this method has been called the ShardManager instance can not be used anymore.
     *
     * <br>This will shutdown the internal queue worker for (re-)starts of shards.
     * This means {@link #restart(int)}, {@link #restart()}, and {@link #start(int)} will throw
     * {@link java.util.concurrent.RejectedExecutionException}.
     *
     * <p>This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
     */
    void shutdown();

    /**
     * Shuts down the shard with the given id only.
     * <br>If there is no shard with the given id, this will do nothing.
     *
     * @param shardId
     *        The id of the shard that should be stopped
     */
    void shutdown(int shardId);

    /**
     * Adds a new shard with the given id to this ShardManager and starts it.
     *
     * @param  shardId
     *         The id of the shard that should be started
     *
     * @throws java.util.concurrent.RejectedExecutionException
     *         If {@link #shutdown()} has already been invoked
     */
    void start(int shardId);

    /**
     * Initializes and starts all shards. This should only be called once.
     *
     * @throws LoginException
     *         If the provided token is invalid.
     */
    void login() throws LoginException;

}
