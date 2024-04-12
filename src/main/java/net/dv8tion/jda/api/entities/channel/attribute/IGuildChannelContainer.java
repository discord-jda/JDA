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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.ChannelCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

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
public interface IGuildChannelContainer<C extends Channel>
{
    /**
     * Unified cache of all channels associated with this shard or guild.
     *
     * <p>This {@link ChannelCacheView} stores all channels in individually typed maps based on {@link ChannelType}.
     * You can use {@link ChannelCacheView#getElementById(ChannelType, long)} or {@link ChannelCacheView#ofType(Class)} to filter
     * out more specific types.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link ChannelCacheView}
     */
    @Nonnull
    ChannelCacheView<C> getChannelCache();

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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
     */
    @Nullable
    default <T extends C> T getChannelById(@Nonnull Class<T> type, @Nonnull String id)
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
     */
    @Nullable
    default <T extends C> T getChannelById(@Nonnull Class<T> type, long id)
    {
        Checks.notNull(type, "Class");
        return getChannelCache().ofType(type).getElementById(id);
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
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
     *     <li>{@link #getForumChannelById(long)}</li>
     * </ul>
     *
     * @param  id
     *         The ID of the channel
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(long id)
    {
        C channel = getChannelCache().getElementById(id);
        return channel instanceof GuildChannel ? (GuildChannel) channel : null;
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
     * <br>This is meant for systems that use a dynamic {@link ChannelType} and can
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
     *     <li>{@link #getForumChannelById(String)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
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
     * <br>This is meant for systems that use a dynamic {@link ChannelType} and can
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
     *     <li>{@link #getForumChannelById(long)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@Nonnull ChannelType type, long id)
    {
        C channel = getChannelCache().getElementById(type, id);
        return channel instanceof GuildChannel ? (GuildChannel) channel : null;
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<StageChannel> getStageChannelCache();

    /**
     * Gets a list of all {@link StageChannel StageChannels}
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
     *         The name used to filter the returned {@link StageChannel StageChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all StageChannel names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<StageChannel> getStageChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getStageChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link StageChannel StageChannel} that has the same id as the one provided.
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
     *         The id of the {@link StageChannel StageChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link StageChannel StageChannel} with matching id.
     */
    @Nullable
    default StageChannel getStageChannelById(@Nonnull String id)
    {
        return (StageChannel) getChannelCache().getElementById(ChannelType.STAGE, id);
    }

    /**
     * Gets a {@link StageChannel StageChannel} that has the same id as the one provided.
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
     *         The id of the {@link StageChannel StageChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link StageChannel StageChannel} with matching id.
     */
    @Nullable
    default StageChannel getStageChannelById(long id)
    {
        return (StageChannel) getChannelCache().getElementById(ChannelType.STAGE, id);
    }

    /**
     * Gets all {@link StageChannel StageChannels} in the cache.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of {@link StageChannel StageChannels}.
     */
    @Nonnull
    @Unmodifiable
    default List<StageChannel> getStageChannels()
    {
        return getStageChannelCache().asList();
    }


    // Threads


    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link ThreadChannel}.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<ThreadChannel> getThreadChannelCache();

    /**
     * Gets a list of all {@link ThreadChannel ThreadChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all ThreadChannel names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<ThreadChannel> getThreadChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getThreadChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link ThreadChannel ThreadChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link ThreadChannel ThreadChannel} with matching id.
     */
    @Nullable
    default ThreadChannel getThreadChannelById(@Nonnull String id)
    {
        return (ThreadChannel) getChannelCache().getElementById(ChannelType.GUILD_PUBLIC_THREAD, id);
    }

    /**
     * Gets a {@link ThreadChannel ThreadChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link ThreadChannel ThreadChannel} with matching id.
     */
    @Nullable
    default ThreadChannel getThreadChannelById(long id)
    {
        return (ThreadChannel) getChannelCache().getElementById(ChannelType.GUILD_PUBLIC_THREAD, id);
    }

    /**
     * Gets all {@link ThreadChannel ThreadChannel} in the cache.
     *
     * <p>These threads can also represent posts in {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of {@link ThreadChannel ThreadChannels}.
     */
    @Nonnull
    @Unmodifiable
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<Category> getCategoryCache();

    /**
     * Gets a list of all {@link Category Categories}
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Immutable list of all categories matching the provided name
     */
    @Nonnull
    @Unmodifiable
    default List<Category> getCategoriesByName(@Nonnull String name, boolean ignoreCase)
    {
        return getCategoryCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link Category Category} that has the same id as the one provided.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(@Nonnull String id)
    {
        return (Category) getChannelCache().getElementById(ChannelType.CATEGORY, id);
    }

    /**
     * Gets a {@link Category Category} that has the same id as the one provided.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(long id)
    {
        return (Category) getChannelCache().getElementById(ChannelType.CATEGORY, id);
    }

    /**
     * Gets all {@link Category Categories} in the cache.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable list of all {@link Category Categories} in this Guild.
     */
    @Nonnull
    @Unmodifiable
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<TextChannel> getTextChannelCache();

    /**
     * Gets a list of all {@link TextChannel TextChannels}
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
     *         The name used to filter the returned {@link TextChannel TextChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all TextChannels names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<TextChannel> getTextChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getTextChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link TextChannel TextChannel} that has the same id as the one provided.
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
     *         The id of the {@link TextChannel TextChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(@Nonnull String id)
    {
        return (TextChannel) getChannelCache().getElementById(ChannelType.TEXT, id);
    }

    /**
     * Gets a {@link TextChannel TextChannel} that has the same id as the one provided.
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
     *         The id of the {@link TextChannel TextChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(long id)
    {
        return (TextChannel) getChannelCache().getElementById(ChannelType.TEXT, id);
    }

    /**
     * Gets all {@link TextChannel TextChannels} in the cache.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of all {@link TextChannel TextChannels} in this Guild.
     */
    @Nonnull
    @Unmodifiable
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<NewsChannel> getNewsChannelCache();

    /**
     * Gets a list of all {@link NewsChannel NewsChannels}
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
     *         The name used to filter the returned {@link NewsChannel NewsChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all NewsChannels names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<NewsChannel> getNewsChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getNewsChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link NewsChannel NewsChannel} that has the same id as the one provided.
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
     *         The id of the {@link NewsChannel NewsChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link NewsChannel NewsChannel} with matching id.
     */
    @Nullable
    default NewsChannel getNewsChannelById(@Nonnull String id)
    {
        return (NewsChannel) getChannelCache().getElementById(ChannelType.NEWS, id);
    }

    /**
     * Gets a {@link NewsChannel NewsChannel} that has the same id as the one provided.
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
     *         The id of the {@link NewsChannel NewsChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link NewsChannel NewsChannel} with matching id.
     */
    @Nullable
    default NewsChannel getNewsChannelById(long id)
    {
        return (NewsChannel) getChannelCache().getElementById(ChannelType.NEWS, id);
    }

    /**
     * Gets all {@link NewsChannel NewsChannels} in the cache.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of all {@link NewsChannel NewsChannels} in this Guild.
     */
    @Nonnull
    @Unmodifiable
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<VoiceChannel> getVoiceChannelCache();

    /**
     * Gets a list of all {@link VoiceChannel VoiceChannels}
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
     *         The name used to filter the returned {@link VoiceChannel VoiceChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all VoiceChannel names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<VoiceChannel> getVoiceChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getVoiceChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link VoiceChannel VoiceChannel} that has the same id as the one provided.
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
     *         The id of the {@link VoiceChannel VoiceChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(@Nonnull String id)
    {
        return (VoiceChannel) getChannelCache().getElementById(ChannelType.VOICE, id);
    }

    /**
     * Gets a {@link VoiceChannel VoiceChannel} that has the same id as the one provided.
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
     *         The id of the {@link VoiceChannel VoiceChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(long id)
    {
        return (VoiceChannel) getChannelCache().getElementById(ChannelType.VOICE, id);
    }

    /**
     * Gets all {@link VoiceChannel VoiceChannels} in the cache.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of {@link VoiceChannel VoiceChannels}.
     */
    @Nonnull
    @Unmodifiable
    default List<VoiceChannel> getVoiceChannels()
    {
        return getVoiceChannelCache().asList();
    }


    // ForumChannels


    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of {@link ForumChannel}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<ForumChannel> getForumChannelCache();

    /**
     * Gets a list of all {@link ForumChannel ForumChannels}
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
     *         The name used to filter the returned {@link ForumChannel ForumChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all ForumChannel names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<ForumChannel> getForumChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getForumChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link ForumChannel} that has the same id as the one provided.
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
     *         The id of the {@link ForumChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link ForumChannel} with matching id.
     */
    @Nullable
    default ForumChannel getForumChannelById(@Nonnull String id)
    {
        return (ForumChannel) getChannelCache().getElementById(ChannelType.FORUM, id);
    }

    /**
     * Gets a {@link ForumChannel} that has the same id as the one provided.
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
     *         The id of the {@link ForumChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link ForumChannel} with matching id.
     */
    @Nullable
    default ForumChannel getForumChannelById(long id)
    {
        return (ForumChannel) getChannelCache().getElementById(ChannelType.FORUM, id);
    }

    /**
     * Gets all {@link ForumChannel} in the cache.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getForumChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of {@link ForumChannel}.
     */
    @Nonnull
    @Unmodifiable
    default List<ForumChannel> getForumChannels()
    {
        return getForumChannelCache().asList();
    }


    // MediaChannels


    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of {@link MediaChannel}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Nonnull
    SnowflakeCacheView<MediaChannel> getMediaChannelCache();

    /**
     * Gets a list of all {@link MediaChannel MediaChannels}
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
     *         The name used to filter the returned {@link MediaChannel MediaChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-empty immutable list of all ForumChannel names that match the provided name.
     */
    @Nonnull
    @Unmodifiable
    default List<MediaChannel> getMediaChannelsByName(@Nonnull String name, boolean ignoreCase)
    {
        return getMediaChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link MediaChannel} that has the same id as the one provided.
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
     *         The id of the {@link MediaChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link MediaChannel} with matching id.
     */
    @Nullable
    default MediaChannel getMediaChannelById(@Nonnull String id)
    {
        return (MediaChannel) getChannelCache().getElementById(ChannelType.MEDIA, id);
    }

    /**
     * Gets a {@link MediaChannel} that has the same id as the one provided.
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
     *         The id of the {@link MediaChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return Possibly-null {@link MediaChannel} with matching id.
     */
    @Nullable
    default MediaChannel getMediaChannelById(long id)
    {
        return (MediaChannel) getChannelCache().getElementById(ChannelType.MEDIA, id);
    }

    /**
     * Gets all {@link MediaChannel} in the cache.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getForumChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot isn't in the guild.
     *
     * @return An immutable List of {@link MediaChannel}.
     */
    @Nonnull
    @Unmodifiable
    default List<MediaChannel> getMediaChannels()
    {
        return getMediaChannelCache().asList();
    }
}
