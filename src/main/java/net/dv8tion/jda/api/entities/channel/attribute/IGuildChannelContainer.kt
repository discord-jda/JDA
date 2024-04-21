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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.cache.ChannelCacheView
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Provides various channel cache getters for Guild channels.
 *
 *
 * These getters only check the caches with the relevant scoping of the implementing type.
 * For example, [Guild] returns channels that exist within the guild,
 * whereas [JDA] or [ShardManager] returns any channels that exist within the shard.
 *
 * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
 * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
 *
 *
 * For the most efficient usage, it is recommended to use [CacheView] getters such as [.getTextChannelCache].
 * List getters usually require making a snapshot copy of the underlying cache view, which may introduce an undesirable performance hit.
 */
interface IGuildChannelContainer<C : Channel?> {
    @JvmField
    @get:Nonnull
    val channelCache: ChannelCacheView<C>

    /**
     * Get a channel of the specified type by id.
     *
     *
     * This will automatically check for all channel types and cast to the specified class.
     * If a channel with the specified id does not exist,
     * or exists but is not an instance of the provided class, this returns null.
     *
     * @param  type
     * [Class] of a channel type
     * @param  id
     * The snowflake id of the channel
     * @param  <T>
     * The type argument for the class
     *
     * @throws IllegalArgumentException
     * If null is provided, or the id is not a valid snowflake
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
    </T> */
    fun <T : C?> getChannelById(@Nonnull type: Class<T>?, @Nonnull id: String?): T? {
        return getChannelById(type, MiscUtil.parseSnowflake(id))
    }

    /**
     * Get a channel of the specified type by id.
     *
     *
     * This will automatically check for all channel types and cast to the specified class.
     * If a channel with the specified id does not exist,
     * or exists but is not an instance of the provided class, this returns null.
     *
     * @param  type
     * [Class] of a channel type
     * @param  id
     * The snowflake id of the channel
     * @param  <T>
     * The type argument for the class
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
    </T> */
    fun <T : C?> getChannelById(@Nonnull type: Class<T>?, id: Long): T? {
        Checks.notNull(type, "Class")
        return channelCache.ofType(type).getElementById(id)
    }

    /**
     * Get [GuildChannel] for the provided ID.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     *
     * To get more specific channel types you can use one of the following:
     *
     *  * [.getChannelById]
     *  * [.getTextChannelById]
     *  * [.getNewsChannelById]
     *  * [.getStageChannelById]
     *  * [.getVoiceChannelById]
     *  * [.getCategoryById]
     *
     *
     * @param  id
     * The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided ID is null
     * @throws java.lang.NumberFormatException
     * If the provided ID is not a snowflake
     *
     * @return The GuildChannel or null
     */
    fun getGuildChannelById(@Nonnull id: String?): GuildChannel? {
        return getGuildChannelById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Get [GuildChannel] for the provided ID.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     *
     * To get more specific channel types you can use one of the following:
     *
     *  * [.getChannelById]
     *  * [.getTextChannelById]
     *  * [.getNewsChannelById]
     *  * [.getStageChannelById]
     *  * [.getVoiceChannelById]
     *  * [.getCategoryById]
     *  * [.getForumChannelById]
     *
     *
     * @param  id
     * The ID of the channel
     *
     * @return The GuildChannel or null
     */
    fun getGuildChannelById(id: Long): GuildChannel? {
        val channel = channelCache.getElementById(id)
        return if (channel is GuildChannel) channel else null
    }

    /**
     * Get [GuildChannel] for the provided ID.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * <br></br>This is meant for systems that use a dynamic [ChannelType] and can
     * profit from a simple function to get the channel instance.
     *
     *
     * To get more specific channel types you can use one of the following:
     *
     *  * [.getChannelById]
     *  * [.getTextChannelById]
     *  * [.getNewsChannelById]
     *  * [.getStageChannelById]
     *  * [.getVoiceChannelById]
     *  * [.getCategoryById]
     *  * [.getForumChannelById]
     *
     *
     * @param  type
     * The [ChannelType]
     * @param  id
     * The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided ID is null
     * @throws java.lang.NumberFormatException
     * If the provided ID is not a snowflake
     *
     * @return The GuildChannel or null
     */
    fun getGuildChannelById(@Nonnull type: ChannelType?, @Nonnull id: String?): GuildChannel? {
        return getGuildChannelById(type, MiscUtil.parseSnowflake(id))
    }

    /**
     * Get [GuildChannel] for the provided ID.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * <br></br>This is meant for systems that use a dynamic [ChannelType] and can
     * profit from a simple function to get the channel instance.
     *
     *
     * To get more specific channel types you can use one of the following:
     *
     *  * [.getChannelById]
     *  * [.getTextChannelById]
     *  * [.getNewsChannelById]
     *  * [.getStageChannelById]
     *  * [.getVoiceChannelById]
     *  * [.getCategoryById]
     *  * [.getForumChannelById]
     *
     *
     * @param  type
     * The [ChannelType]
     * @param  id
     * The ID of the channel
     *
     * @return The GuildChannel or null
     */
    fun getGuildChannelById(@Nonnull type: ChannelType?, id: Long): GuildChannel? {
        val channel = channelCache.getElementById(type!!, id)
        return if (channel is GuildChannel) channel else null
    }

    // Stages
    @JvmField
    @get:Nonnull
    val stageChannelCache: SnowflakeCacheView<StageChannel?>

    /**
     * Gets a list of all [StageChannels][StageChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [StageChannels][StageChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all StageChannel names that match the provided name.
     */
    @Nonnull
    fun getStageChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<StageChannel?>? {
        return stageChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [StageChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [StageChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [StageChannel] with matching id.
     */
    fun getStageChannelById(@Nonnull id: String?): StageChannel? {
        return channelCache.getElementById(ChannelType.STAGE, id!!) as StageChannel?
    }

    /**
     * Gets a [StageChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [StageChannel].
     *
     * @return Possibly-null [StageChannel] with matching id.
     */
    fun getStageChannelById(id: Long): StageChannel? {
        return channelCache.getElementById(ChannelType.STAGE, id) as StageChannel?
    }

    @get:Nonnull
    val stageChannels: List<StageChannel?>?
        /**
         * Gets all [StageChannels][StageChannel] in the cache.
         * <br></br>In [Guild] cache, channels are sorted according to their position and id.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getStageChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of [StageChannels][StageChannel].
         */
        get() = stageChannelCache.asList()

    // Threads
    @JvmField
    @get:Nonnull
    val threadChannelCache: SnowflakeCacheView<ThreadChannel?>

    /**
     * Gets a list of all [ThreadChannels][ThreadChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * These threads can also represent posts in [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [ThreadChannels][ThreadChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all ThreadChannel names that match the provided name.
     */
    @Nonnull
    fun getThreadChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<ThreadChannel?>? {
        return threadChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [ThreadChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * These threads can also represent posts in [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [ThreadChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [ThreadChannel] with matching id.
     */
    fun getThreadChannelById(@Nonnull id: String?): ThreadChannel? {
        return channelCache.getElementById(ChannelType.GUILD_PUBLIC_THREAD, id!!) as ThreadChannel?
    }

    /**
     * Gets a [ThreadChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * These threads can also represent posts in [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [ThreadChannel].
     *
     * @return Possibly-null [ThreadChannel] with matching id.
     */
    fun getThreadChannelById(id: Long): ThreadChannel? {
        return channelCache.getElementById(ChannelType.GUILD_PUBLIC_THREAD, id) as ThreadChannel?
    }

    @get:Nonnull
    val threadChannels: List<ThreadChannel?>?
        /**
         * Gets all [ThreadChannel] in the cache.
         *
         *
         * These threads can also represent posts in [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getThreadChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of [ThreadChannels][ThreadChannel].
         */
        get() = threadChannelCache.asList()

    // Categories
    @JvmField
    @get:Nonnull
    val categoryCache: SnowflakeCacheView<Category?>

    /**
     * Gets a list of all [Categories][Category]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name to check
     * @param  ignoreCase
     * Whether to ignore case on name checking
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is `null`
     *
     * @return Immutable list of all categories matching the provided name
     */
    @Nonnull
    fun getCategoriesByName(@Nonnull name: String?, ignoreCase: Boolean): List<Category?>? {
        return categoryCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [Category] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The snowflake ID of the wanted Category
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided ID is not a valid `long`
     *
     * @return Possibly-null [Category] for the provided ID.
     */
    fun getCategoryById(@Nonnull id: String?): Category? {
        return channelCache.getElementById(ChannelType.CATEGORY, id!!) as Category?
    }

    /**
     * Gets a [Category] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The snowflake ID of the wanted Category
     *
     * @return Possibly-null [Category] for the provided ID.
     */
    fun getCategoryById(id: Long): Category? {
        return channelCache.getElementById(ChannelType.CATEGORY, id) as Category?
    }

    @get:Nonnull
    val categories: List<Category?>?
        /**
         * Gets all [Categories][Category] in the cache.
         * <br></br>In [Guild] cache, channels are sorted according to their position and id.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getCategoryCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable list of all [Categories][Category] in this Guild.
         */
        get() = categoryCache.asList()

    // TextChannels
    @JvmField
    @get:Nonnull
    val textChannelCache: SnowflakeCacheView<TextChannel?>

    /**
     * Gets a list of all [TextChannels][TextChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [TextChannels][TextChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all TextChannels names that match the provided name.
     */
    @Nonnull
    fun getTextChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<TextChannel?>? {
        return textChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [TextChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [TextChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [TextChannel] with matching id.
     */
    fun getTextChannelById(@Nonnull id: String?): TextChannel? {
        return channelCache.getElementById(ChannelType.TEXT, id!!) as TextChannel?
    }

    /**
     * Gets a [TextChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [TextChannel].
     *
     * @return Possibly-null [TextChannel] with matching id.
     */
    fun getTextChannelById(id: Long): TextChannel? {
        return channelCache.getElementById(ChannelType.TEXT, id) as TextChannel?
    }

    @get:Nonnull
    val textChannels: List<TextChannel?>?
        /**
         * Gets all [TextChannels][TextChannel] in the cache.
         * <br></br>In [Guild] cache, channels are sorted according to their position and id.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getTextChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of all [TextChannels][TextChannel] in this Guild.
         */
        get() = textChannelCache.asList()

    // NewsChannels / AnnouncementChannels
    @JvmField
    @get:Nonnull
    val newsChannelCache: SnowflakeCacheView<NewsChannel?>

    /**
     * Gets a list of all [NewsChannels][NewsChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [NewsChannels][NewsChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all NewsChannels names that match the provided name.
     */
    @Nonnull
    fun getNewsChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<NewsChannel?>? {
        return newsChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [NewsChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [NewsChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [NewsChannel] with matching id.
     */
    fun getNewsChannelById(@Nonnull id: String?): NewsChannel? {
        return channelCache.getElementById(ChannelType.NEWS, id!!) as NewsChannel?
    }

    /**
     * Gets a [NewsChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [NewsChannel].
     *
     * @return Possibly-null [NewsChannel] with matching id.
     */
    fun getNewsChannelById(id: Long): NewsChannel? {
        return channelCache.getElementById(ChannelType.NEWS, id) as NewsChannel?
    }

    @get:Nonnull
    val newsChannels: List<NewsChannel?>?
        /**
         * Gets all [NewsChannels][NewsChannel] in the cache.
         * <br></br>In [Guild] cache, channels are sorted according to their position and id.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getNewsChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of all [NewsChannels][NewsChannel] in this Guild.
         */
        get() = newsChannelCache.asList()

    // VoiceChannels
    @JvmField
    @get:Nonnull
    val voiceChannelCache: SnowflakeCacheView<VoiceChannel?>

    /**
     * Gets a list of all [VoiceChannels][VoiceChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [VoiceChannels][VoiceChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all VoiceChannel names that match the provided name.
     */
    @Nonnull
    fun getVoiceChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<VoiceChannel?>? {
        return voiceChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [VoiceChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [VoiceChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [VoiceChannel] with matching id.
     */
    fun getVoiceChannelById(@Nonnull id: String?): VoiceChannel? {
        return channelCache.getElementById(ChannelType.VOICE, id!!) as VoiceChannel?
    }

    /**
     * Gets a [VoiceChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [VoiceChannel].
     *
     * @return Possibly-null [VoiceChannel] with matching id.
     */
    fun getVoiceChannelById(id: Long): VoiceChannel? {
        return channelCache.getElementById(ChannelType.VOICE, id) as VoiceChannel?
    }

    @get:Nonnull
    val voiceChannels: List<VoiceChannel?>?
        /**
         * Gets all [VoiceChannels][VoiceChannel] in the cache.
         * <br></br>In [Guild] cache, channels are sorted according to their position and id.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getVoiceChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of [VoiceChannels][VoiceChannel].
         */
        get() = voiceChannelCache.asList()

    // ForumChannels
    @JvmField
    @get:Nonnull
    val forumChannelCache: SnowflakeCacheView<ForumChannel?>

    /**
     * Gets a list of all [ForumChannels][ForumChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [ForumChannels][ForumChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all ForumChannel names that match the provided name.
     */
    @Nonnull
    fun getForumChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<ForumChannel?>? {
        return forumChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [ForumChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [ForumChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [ForumChannel] with matching id.
     */
    fun getForumChannelById(@Nonnull id: String?): ForumChannel? {
        return channelCache.getElementById(ChannelType.FORUM, id!!) as ForumChannel?
    }

    /**
     * Gets a [ForumChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [ForumChannel].
     *
     * @return Possibly-null [ForumChannel] with matching id.
     */
    fun getForumChannelById(id: Long): ForumChannel? {
        return channelCache.getElementById(ChannelType.FORUM, id) as ForumChannel?
    }

    @get:Nonnull
    val forumChannels: List<ForumChannel?>?
        /**
         * Gets all [ForumChannel] in the cache.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getForumChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of [ForumChannel].
         */
        get() = forumChannelCache.asList()

    // MediaChannels
    @JvmField
    @get:Nonnull
    val mediaChannelCache: SnowflakeCacheView<MediaChannel?>

    /**
     * Gets a list of all [MediaChannels][MediaChannel]
     * in this Guild that have the same name as the one provided.
     * <br></br>If there are no channels with the provided name, then this returns an empty list.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  name
     * The name used to filter the returned [MediaChannels][MediaChannel].
     * @param  ignoreCase
     * Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all ForumChannel names that match the provided name.
     */
    @Nonnull
    fun getMediaChannelsByName(@Nonnull name: String?, ignoreCase: Boolean): List<MediaChannel?>? {
        return mediaChannelCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Gets a [MediaChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [MediaChannel].
     *
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null [MediaChannel] with matching id.
     */
    fun getMediaChannelById(@Nonnull id: String?): MediaChannel? {
        return channelCache.getElementById(ChannelType.MEDIA, id!!) as MediaChannel?
    }

    /**
     * Gets a [MediaChannel] that has the same id as the one provided.
     * <br></br>If there is no channel with an id that matches the provided one, then this returns `null`.
     *
     *
     * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
     * For [Guild], [JDA], or [ShardManager],
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
     *
     * @param  id
     * The id of the [MediaChannel].
     *
     * @return Possibly-null [MediaChannel] with matching id.
     */
    fun getMediaChannelById(id: Long): MediaChannel? {
        return channelCache.getElementById(ChannelType.MEDIA, id) as MediaChannel?
    }

    @get:Nonnull
    val mediaChannels: List<MediaChannel?>?
        /**
         * Gets all [MediaChannel] in the cache.
         *
         *
         * This copies the backing store into a list. This means every call
         * creates a new list with O(n) complexity. It is recommended to store this into
         * a local variable or use [.getForumChannelCache] and use its more efficient
         * versions of handling these values.
         *
         *
         * This getter exists on any instance of [IGuildChannelContainer] and only checks the caches with the relevant scoping.
         * For [Guild], [JDA], or [ShardManager],
         * this returns the relevant channel with respect to the cache within each of those objects.
         * For a guild, this would mean it only returns channels within the same guild.
         * <br></br>If this is called on [JDA] or [ShardManager], this may return null immediately after building, because the cache isn't initialized yet.
         * To make sure the cache is initialized after building your [JDA] instance, you can use [JDA.awaitReady].
         *
         * @return An immutable List of [MediaChannel].
         */
        get() = mediaChannelCache.asList()
}
