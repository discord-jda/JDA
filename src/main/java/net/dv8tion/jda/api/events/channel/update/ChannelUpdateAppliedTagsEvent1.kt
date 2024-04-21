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
package net.dv8tion.jda.api.events.channel.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.ChannelField
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that the tags applied to a [forum post thread][ThreadChannel] have been updated.
 *
 *
 * Limited to [ThreadChannels][ThreadChannel] inside of [ForumChannels][ForumChannel]
 *
 * @see ThreadChannel.getAppliedTags
 * @see ChannelField.APPLIED_TAGS
 */
class ChannelUpdateAppliedTagsEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull channel: ThreadChannel,
    @Nonnull oldValue: List<Long?>?,
    @Nonnull newValue: List<Long?>?
) : GenericChannelUpdateEvent<List<Long?>?>(api, responseNumber, channel, FIELD, oldValue, newValue) {
    @get:Nonnull
    val addedTags: List<ForumTag?>
        /**
         * The newly added tags.
         *
         *
         * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
         *
         * @return The tags that were added to the post
         */
        get() {
            val newTags: MutableList<ForumTag?> = ArrayList(newTags)
            newTags.removeAll(oldTags)
            return newTags
        }

    @get:Nonnull
    val removedTags: List<ForumTag?>
        /**
         * The removed tags.
         *
         *
         * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
         *
         * @return The tags that were removed from the post
         */
        get() {
            val oldTags: MutableList<ForumTag?> = ArrayList(oldTags)
            oldTags.removeAll(newTags)
            return oldTags
        }

    @get:Nonnull
    val newTags: List<ForumTag?>
        /**
         * The new list of applied tags.
         *
         *
         * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
         *
         * @return The updated list of applied tags
         */
        get() {
            val cache = getChannel().asThreadChannel()!!.parentChannel.asForumChannel()!!.availableTagCache
            return getNewValue()!!.stream()
                .map { id: Long? ->
                    cache.getElementById(
                        id!!
                    )
                }
                .filter { obj: ForumTag? -> Objects.nonNull(obj) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @get:Nonnull
    val oldTags: List<ForumTag?>
        /**
         * The old list of applied tags.
         *
         *
         * This requires [CacheFlag.FORUM_TAGS][net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS] to be enabled.
         *
         * @return The previous list of applied tags
         */
        get() {
            val cache = getChannel().asThreadChannel()!!.parentChannel.asForumChannel()!!.availableTagCache
            return getOldValue()!!.stream()
                .map { id: Long? ->
                    cache.getElementById(
                        id!!
                    )
                }
                .filter { obj: ForumTag? -> Objects.nonNull(obj) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @Nonnull
    override fun getOldValue(): List<Long>? {
        return super.getOldValue()
    }

    @Nonnull
    override fun getNewValue(): List<Long>? {
        return super.getNewValue()
    }

    companion object {
        val FIELD = ChannelField.APPLIED_TAGS
        val IDENTIFIER = FIELD.fieldName
    }
}
