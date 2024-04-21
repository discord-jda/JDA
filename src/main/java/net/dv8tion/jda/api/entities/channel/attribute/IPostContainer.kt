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

import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.MediaChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.ForumPostAction
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * A channel which contains [Forum Posts][.createForumPost].
 * <br></br>Forum posts are simply [ThreadChannels][ThreadChannel] of type [ChannelType.GUILD_PUBLIC_THREAD].
 *
 *
 * The `CREATE POSTS` permission that is shown in the official Discord Client, is an alias for [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND].
 * [Permission.CREATE_PUBLIC_THREADS][net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS] is ignored for creating forum posts.
 *
 * @see ForumChannel
 *
 * @see MediaChannel
 *
 * @see .createForumPost
 */
interface IPostContainer : IThreadContainer {
    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?

    @JvmField
    @get:Nonnull
    val availableTagCache: SortedSnowflakeCacheView<ForumTag?>

    @get:Nonnull
    val availableTags: List<ForumTag?>?
        /**
         * The available [ForumTags][ForumTag] for this channel.
         * <br></br>Tags are sorted by their [position][ForumTag.getPosition] ascending.
         *
         *
         * This is a shortcut for [getAvailableTagCache().asList()][.getAvailableTagCache].
         * This method will copy the underlying cache into the list, running in `O(n)` time.
         *
         *
         * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
         *
         * @return Immutable [List] of [ForumTag]
         */
        get() = availableTagCache.asList()

    /**
     * The available [ForumTags][ForumTag] for this channel.
     * <br></br>Tags are sorted by their [position][ForumTag.getPosition] ascending.
     *
     *
     * This is a shortcut for [getAvailableTagCache().getElementsByName(name, ignoreCase)][.getAvailableTagCache].
     * This method will copy the underlying cache into the list, running in `O(n)` time.
     *
     *
     * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
     *
     * @param  name
     * The name of the tag
     * @param  ignoreCase
     * Whether to use [String.equalsIgnoreCase]
     *
     * @throws IllegalArgumentException
     * If the name is `null`
     *
     * @return Immutable [List] of [ForumTag] with the given name
     */
    @Nonnull
    fun getAvailableTagsByName(@Nonnull name: String?, ignoreCase: Boolean): List<ForumTag?>? {
        return availableTagCache.getElementsByName(name!!, ignoreCase)
    }

    /**
     * Retrieves the tag for the provided id.
     *
     *
     * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
     *
     * @param  id
     * The tag id
     *
     * @return The tag for the provided id, or `null` if no tag with that id exists
     *
     * @see net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake.fromId
     */
    fun getAvailableTagById(id: Long): ForumTag? {
        return availableTagCache.getElementById(id)
    }

    /**
     * Retrieves the tag for the provided id.
     *
     *
     * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
     *
     * @param  id
     * The tag id
     *
     * @throws IllegalArgumentException
     * If the provided id is null
     * @throws NumberFormatException
     * If the provided id is not a valid snowflake
     *
     * @return The tag for the provided id, or `null` if no tag with that id exists
     *
     * @see net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake.fromId
     */
    fun getAvailableTagById(@Nonnull id: String?): ForumTag? {
        return availableTagCache.getElementById(id!!)
    }

    /**
     * The topic set for this channel, this is referred to as *Guidelines* in the official Discord client.
     * <br></br>If no topic has been set, this returns null.
     *
     * @return Possibly-null String containing the topic of this channel.
     */
    @JvmField
    val topic: String?
    val isTagRequired: Boolean
        /**
         * Whether all new forum posts must have a tag.
         *
         * @return True, if all new posts must have a tag.
         */
        get() = flags.contains(ChannelFlag.REQUIRE_TAG)

    /**
     * The emoji which will show up on new forum posts as default reaction.
     *
     * @return The default reaction for new forum posts.
     */
    @JvmField
    val defaultReaction: EmojiUnion?

    @JvmField
    @get:Nonnull
    val defaultSortOrder: SortOrder?

    /**
     * Creates a new forum/media post (thread) in this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel was deleted
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If the total sum of uploaded bytes exceeds the guild's [upload limit][Guild.getMaxFileSize]
     *
     *  * [TITLE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.TITLE_BLOCKED_BY_AUTOMOD]
     * <br></br>If the forum post name was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *
     * @param  name
     * The name of the post (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  message
     * The starting message of the post (see [MessageCreateBuilder][net.dv8tion.jda.api.utils.messages.MessageCreateBuilder])
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the bot does not have [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND] in the channel
     * @throws IllegalArgumentException
     *
     *  * If null is provided
     *  * If the name is empty or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     *
     * @return [ForumPostAction]
     */
    @Nonnull
    @Incubating
    @CheckReturnValue
    fun createForumPost(@Nonnull name: String?, @Nonnull message: MessageCreateData?): ForumPostAction?

    /**
     * The order used to sort forum posts.
     */
    enum class SortOrder(
        /**
         * The underlying value as used by Discord.
         *
         * @return The raw order key
         */
        val key: Int
    ) {
        /**
         * Sort by recent activity, including unarchive, message, reaction, and thread creation.
         */
        RECENT_ACTIVITY(0),

        /**
         * Sort by the time the post was originally created.
         */
        CREATION_TIME(1),

        /**
         * Placeholder for possible future order modes.
         */
        UNKNOWN(-1);

        companion object {
            /**
             * The [SortOrder] for the provided key.
             *
             * @param  key
             * The key to get the [SortOrder] for
             *
             * @return The [SortOrder] for the provided key, or [.UNKNOWN] if the key is not known
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): SortOrder {
                for (order in entries) {
                    if (order.key == key) return order
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * The maximum length of a forum / media channel topic ({@value})
         */
        const val MAX_POST_CONTAINER_TOPIC_LENGTH = 4096

        /**
         * The maximum number of [tags][ForumPostAction.setTags] that can be applied to a forum post. ({@value})
         */
        const val MAX_POST_TAGS = 5
    }
}
