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

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.AutoArchiveDuration
import net.dv8tion.jda.api.requests.RestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Common features of all [RestActions][RestAction] that create a new thread.
 *
 * @param <T>
 * The success type given to the [.queue] success consumer
 * @param <R>
 * The common return type of setters, allowing for fluid interface design
</R></T> */
interface AbstractThreadCreateAction<T, R : AbstractThreadCreateAction<T, R>?> : RestAction<T> {
    @get:Nonnull
    val guild: Guild?

    @get:Nonnull
    val type: ChannelType?

    /**
     * Sets the name for the new GuildChannel.
     *
     * @param  name
     * The not-null name for the new GuildChannel (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the provided name is null, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return The current action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): R

    /**
     * Sets the [ThreadChannel.AutoArchiveDuration] for the new thread.
     * <br></br>This is primarily used to *hide* threads after the provided time of inactivity.
     * Threads are automatically archived after 7 days of inactivity regardless.
     *
     * @param  autoArchiveDuration
     * The new archive inactivity duration (which hides the thread)
     *
     * @throws IllegalArgumentException
     * If the provided duration is null
     *
     * @return The current action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAutoArchiveDuration(@Nonnull autoArchiveDuration: AutoArchiveDuration?): R
}
