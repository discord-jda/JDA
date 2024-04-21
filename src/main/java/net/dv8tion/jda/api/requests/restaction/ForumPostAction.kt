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

import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumPost
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.requests.FluentRestAction
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest
import net.dv8tion.jda.internal.utils.*
import java.util.*
import javax.annotation.Nonnull

/**
 * Extension of [RestAction][net.dv8tion.jda.api.requests.RestAction] specifically
 * designed to create new Forum Post Threads.
 *
 *
 * On success, this provides a [ForumPost] object with the [starter message][ForumPost.getMessage]
 * and the [thread channel][ForumPost.getThreadChannel] of the post.
 *
 * @see IPostContainer.createForumPost
 */
interface ForumPostAction : AbstractThreadCreateAction<ForumPost?, ForumPostAction?>,
    MessageCreateRequest<ForumPostAction?>, FluentRestAction<ForumPost?, ForumPostAction?> {
    @get:Nonnull
    val channel: IPostContainer?

    /**
     * Configures that tags which should be applied to the new post.
     * <br></br>Some forums require setting at least one tag.
     *
     * @param  tags
     * Up to {@value ForumChannel#MAX_POST_TAGS} tags to apply
     *
     * @throws IllegalArgumentException
     * If null is provided or more than {@value ForumChannel#MAX_POST_TAGS} tags are provided,
     * or if at least one is [required][ForumChannel.isTagRequired] and none were provided.
     *
     * @return The current ForumPostAction for chaining convenience
     *
     * @see ForumTagSnowflake.fromId
     */
    @Nonnull
    fun setTags(@Nonnull tags: Collection<ForumTagSnowflake?>?): ForumPostAction?

    /**
     * Configures that tags which should be applied to the new post.
     * <br></br>Some forums require setting at least one tag.
     *
     * @param  tags
     * Up to {@value ForumChannel#MAX_POST_TAGS} tags to apply
     *
     * @throws IllegalArgumentException
     * If null is provided or more than {@value ForumChannel#MAX_POST_TAGS} tags are provided,
     * or if at least one is [required][ForumChannel.isTagRequired] and none were provided.
     *
     * @return The current ForumPostAction for chaining convenience
     *
     * @see ForumTagSnowflake.fromId
     */
    @Nonnull
    fun setTags(@Nonnull vararg tags: ForumTagSnowflake?): ForumPostAction? {
        Checks.noneNull(tags, "Tags")
        return setTags(Arrays.asList(*tags))
    }
}
