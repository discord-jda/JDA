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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.internal.utils.*
import java.util.*
import javax.annotation.Nonnull

/**
 * Metadata used to create a thread through a [webhook message][WebhookMessageCreateAction].
 *
 * @see WebhookMessageCreateAction.createThread
 */
class ThreadCreateMetadata(@Nonnull name: String) {
    /**
     * The thread name.
     *
     * @return The thread name
     */
    @JvmField
    @get:Nonnull
    val name: String
    private val appliedTags: MutableList<ForumTagSnowflake?> = ArrayList(ForumChannel.MAX_POST_TAGS)

    /**
     * Create a new thread metadata instance.
     *
     * @param  name
     * The title of the thread (1-{@value ThreadChannel#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the provided name is null or not between 1 and {@value ThreadChannel#MAX_NAME_LENGTH} characters long
     */
    init {
        var name = name
        Checks.notBlank(name, "Name")
        name = name.trim { it <= ' ' }
        Checks.notLonger(name, ThreadChannel.MAX_NAME_LENGTH, "Name")
        this.name = name
    }

    /**
     * Apply the provided tags to the forum post.
     *
     * @param  tags
     * The tags to apply
     *
     * @throws IllegalArgumentException
     * If null or more than {@value ForumChannel#MAX_POST_TAGS} tags are provided
     *
     * @return The updated metadata instance
     */
    @Nonnull
    fun addTags(@Nonnull tags: Collection<ForumTagSnowflake?>): ThreadCreateMetadata {
        Checks.noneNull(tags, "Tags")
        Checks.check(
            tags.size <= ForumChannel.MAX_POST_TAGS,
            "Cannot have more than %d post tags. Provided: %d",
            ForumChannel.MAX_POST_TAGS,
            tags.size
        )
        appliedTags.addAll(tags)
        return this
    }

    /**
     * Apply the provided tags to the forum post.
     *
     * @param  tags
     * The tags to apply
     *
     * @throws IllegalArgumentException
     * If null or more than {@value ForumChannel#MAX_POST_TAGS} tags are provided
     *
     * @return The updated metadata instance
     */
    @Nonnull
    fun addTags(@Nonnull vararg tags: ForumTagSnowflake?): ThreadCreateMetadata {
        Checks.noneNull(tags, "Tags")
        Checks.check(
            tags.size <= ForumChannel.MAX_POST_TAGS,
            "Cannot have more than %d post tags. Provided: %d",
            ForumChannel.MAX_POST_TAGS,
            tags.size
        )
        Collections.addAll(appliedTags, *tags)
        return this
    }

    /**
     * The applied tags for the thread / forum post.
     *
     * @return The applied tags
     */
    @Nonnull
    fun getAppliedTags(): List<ForumTagSnowflake?> {
        return appliedTags
    }
}
