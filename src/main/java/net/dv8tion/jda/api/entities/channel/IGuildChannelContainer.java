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

package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Provides various channel cache getters for Guild channels.
 *
 * <p>These getters only check the caches with the relevant scoping of the implementing type.
 * For example, {@link Guild} returns channels that exist within the guild,
 * whereas {@link JDA} or {@link ShardManager} returns any channels that exist within the shard.
 *
 * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
 * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
 *
 * <p>For the most efficient usage, it is recommended to use {@link CacheView} getters such as {@link #getTextChannelCache()}.
 * List getters usually require making a snapshot copy of the underlying cache view, which may introduce an undesirable performance hit.
 */
public interface IGuildChannelContainer
{
    /**
     * Get a channel of the specified type by id.
     *
     * <p>This will automatically check for all channel types and cast to the specified class.
     * If a channel with the specified id does not exist,
     * or exists but is not an instance of the provided class, this returns null.
     *
     * @param  type
     *         {@link Class} of a channel type
     * @param  id
     *         The snowflake id of the channel
     * @param  <T>
     *         The type argument for the class
     *
     * @throws IllegalArgumentException
     *         If null is provided, or the id is not a valid snowflake
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
     */
    @Nullable
    default <T extends Channel> T getChannelById(@Nonnull Class<T> type, @Nonnull String id)
    {
        return getChannelById(type, MiscUtil.parseSnowflake(id));
    }

    /**
     * Get a channel of the specified type by id.
     *
     * <p>This will automatically check for all channel types and cast to the specified class.
     * If a channel with the specified id does not exist,
     * or exists but is not an instance of the provided class, this returns null.
     *
     * @param  type
     *         {@link Class} of a channel type
     * @param  id
     *         The snowflake id of the channel
     * @param  <T>
     *         The type argument for the class
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
     */
    @Nullable
    default <T extends Channel> T getChannelById(@Nonnull Class<T> type, long id)
    {
        Checks.notNull(type, "Class");
        GuildChannel channel = getGuildChannelById(id);
        return type.isInstance(channel) ? type.cast(channel) : null;
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, String)}</li>
     *     <li>{@link #getTextChannelById(String)}</li>
     *     <li>{@link #getNewsChannelById(String)}</li>
     *     <li>{@link #getStageChannelById(String)}</li>
     *     <li>{@link #getVoiceChannelById(String)}</li>
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
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, long)}</li>
     *     <li>{@link #getTextChannelById(long)}</li>
     *     <li>{@link #getNewsChannelById(long)}</li>
     *     <li>{@link #getStageChannelById(long)}</li>
     *     <li>{@link #getVoiceChannelById(long)}</li>
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
        //TODO-v5-unified-channel-cache
        GuildChannel channel = getTextChannelById(id);
        if (channel == null)
            channel = getNewsChannelById(id);
        if (channel == null)
            channel = getVoiceChannelById(id);
        if (channel == null)
            channel = getStageChannelById(id);
        if (channel == null)
            channel = getCategoryById(id);
        if (channel == null)
            channel = getThreadChannelById(id);

        return channel;
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * <br>This is meant for systems that use a dynamic {@link net.dv8tion.jda.api.entities.ChannelType} and can
     * profit from a simple function to get the channel instance.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, String)}</li>
     *     <li>{@link #getTextChannelById(String)}</li>
     *     <li>{@link #getNewsChannelById(String)}</li>
     *     <li>{@link #getStageChannelById(String)}</li>
     *     <li>{@link #getVoiceChannelById(String)}</li>
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
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * <br>This is meant for systems that use a dynamic {@link net.dv8tion.jda.api.entities.ChannelType} and can
     * profit from a simple function to get the channel instance.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, long)}</li>
     *     <li>{@link #getTextChannelById(long)}</li>
     *     <li>{@link #getNewsChannelById(long)}</li>
     *     <li>{@link #getStageChannelById(long)}</li>
     *     <li>{@link #getVoiceChannelById(long)}</li>
     *     <li>{@link #getCategoryById(long)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@Nonnull ChannelType type, long id)
    {
        Checks.notNull(type, "ChannelType");
        switch (type)
        {
        case NEWS:
            return getNewsChannelById(id);
        case TEXT:
            return getTextChannelById(id);
        case VOICE:
            return getVoiceChannelById(id);
        case STAGE:
            return getStageChannelById(id);
        case CATEGORY:
            return getCategoryById(id);
        }

        if (type.isThread())
            return getThreadChannelById(id);

        return null;
    }


    // Stages


    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link StageChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<StageChannel> getStageChannelCache();

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.StageChannel StageChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.StageChannel StageChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all StageChannel names that match the provided name.
     */
    @Nonnull
    default List<StageChannel> getStageChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getStageChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.StageChannel StageChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} with matching id.
     */
    @Nullable
    default StageChannel getStageChannelById(@Nonnull String id)
    {
        return getStageChannelCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.StageChannel StageChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} with matching id.
     */
    @Nullable
    default StageChannel getStageChannelById(long id)
    {
        return getStageChannelCache().getElementById(id);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.StageChannel StageChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getStageChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.StageChannel StageChannels}.
     */
    @Nonnull
    default List<StageChannel> getStageChannels()
    {
        return getStageChannelCache().asList();
    }


    // Threads


    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link ThreadChannel}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<ThreadChannel> getThreadChannelCache();

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link ThreadChannel ThreadChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all ThreadChannel names that match the provided name.
     */
    @Nonnull
    default List<ThreadChannel> getThreadChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getThreadChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link ThreadChannel ThreadChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link ThreadChannel ThreadChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link ThreadChannel ThreadChannel} with matching id.
     */
    @Nullable
    default ThreadChannel getThreadChannelById(@Nonnull String id)
    {
        return getThreadChannelCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link ThreadChannel ThreadChannel}.
     *
     * @return Possibly-null {@link ThreadChannel ThreadChannel} with matching id.
     */
    @Nullable
    default ThreadChannel getThreadChannelById(long id)
    {
        return getThreadChannelCache().getElementById(id);
    }

    /**
     * Gets all {@link ThreadChannel ThreadChannel} in the cache.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getThreadChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return An immutable List of {@link ThreadChannel ThreadChannels}.
     */
    @Nonnull
    default List<ThreadChannel> getThreadChannels()
    {
        return getThreadChannelCache().asList();
    }


    // Categories


    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link Category}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<Category> getCategoryCache();

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Category Categories}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
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
    default List<Category> getCategoriesByName(@Nonnull String name, boolean ignoreCase)
    {
        return getCategoryCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.Category Category} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
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
    default Category getCategoryById(@Nonnull String id)
    {
        return getCategoryCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.Category Category} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(long id)
    {
        return getCategoryCache().getElementById(id);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.Category Categories} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getCategoryCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return An immutable list of all {@link net.dv8tion.jda.api.entities.Category Categories} in this Guild.
     */
    @Nonnull
    default List<Category> getCategories()
    {
        return getCategoryCache().asList();
    }


    // TextChannels


    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link TextChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<TextChannel> getTextChannelCache();

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.TextChannel TextChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.TextChannel TextChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all TextChannels names that match the provided name.
     */
    @Nonnull
    default List<TextChannel> getTextChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getTextChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(@Nonnull String id)
    {
        return getTextChannelCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(long id)
    {
        return getTextChannelCache().getElementById(id);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getTextChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return An immutable List of all {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} in this Guild.
     */
    @Nonnull
    default List<TextChannel> getTextChannels()
    {
        return getTextChannelCache().asList();
    }


    // NewsChannels / AnnouncementChannels


    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link NewsChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<NewsChannel> getNewsChannelCache();

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all NewsChannels names that match the provided name.
     */
    @Nonnull
    default List<NewsChannel> getNewsChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getNewsChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} with matching id.
     */
    @Nullable
    default NewsChannel getNewsChannelById(@Nonnull String id)
    {
        return getNewsChannelCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} with matching id.
     */
    @Nullable
    default NewsChannel getNewsChannelById(long id)
    {
        return getNewsChannelCache().getElementById(id);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getNewsChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return An immutable List of all {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannels} in this Guild.
     */
    @Nonnull
    default List<NewsChannel> getNewsChannels()
    {
        return getNewsChannelCache().asList();
    }


    // VoiceChannels


    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link VoiceChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<VoiceChannel> getVoiceChannelCache();

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all VoiceChannel names that match the provided name.
     */
    @Nonnull
    default List<VoiceChannel> getVoiceChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getVoiceChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(@Nonnull String id)
    {
        return getVoiceChannelCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(long id)
    {
        return getVoiceChannelCache().getElementById(id);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getVoiceChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}.
     */
    @Nonnull
    default List<VoiceChannel> getVoiceChannels()
    {
        return getVoiceChannelCache().asList();
    }
}
