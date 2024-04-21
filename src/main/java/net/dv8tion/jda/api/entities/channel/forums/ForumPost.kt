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
package net.dv8tion.jda.api.entities.channel.forums

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import javax.annotation.Nonnull

/**
 * Result of creating a post in a [ForumChannel].
 * <br></br>This is a utility class for [ForumChannel.createForumPost],
 * the actual post itself is only the [thread channel][.getThreadChannel].
 *
 * @see ForumChannel.createForumPost
 * @see .getThreadChannel
 * @see .getMessage
 */
class ForumPost(
    /**
     * The starter message of the post.
     * <br></br>This is created from the [MessageCreateData] passed to [ForumChannel.createForumPost].
     *
     * @return [Message]
     */
    @JvmField @get:Nonnull
    @param:Nonnull val message: Message,
    /**
     * The [ThreadChannel] representing the post.
     * <br></br>This will use the name provided to [ForumChannel.createForumPost].
     *
     * @return The forum post thread channel
     */
    @get:Nonnull
    @param:Nonnull val threadChannel: ThreadChannel
)
