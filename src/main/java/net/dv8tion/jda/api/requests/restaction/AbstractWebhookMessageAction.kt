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

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.requests.FluentRestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Abstraction for requests related to webhook executions.
 * <br></br>This adds the ability to change the context in which the webhook is used, by setting a [thread id][.setThread].
 *
 * @param <T>
 * The result type
 * @param <R>
 * The action type
</R></T> */
interface AbstractWebhookMessageAction<T, R : AbstractWebhookMessageAction<T, R>?> : FluentRestAction<T, R> {
    /**
     * Set the target thread id for the webhook message.
     * <br></br>This allows sending webhook messages in the target thread,
     * however the webhook must be part of the thread parent channel.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  threadId
     * The target thread id or null to unset
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     * @throws IllegalArgumentException
     * If the provided ID is not a valid snowflake
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setThreadId(threadId: String?): R

    /**
     * Set the target thread id for the webhook message.
     * <br></br>This allows sending webhook messages in the target thread,
     * however the webhook must be part of the thread parent channel.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  threadId
     * The target thread id or 0 to unset
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setThreadId(threadId: Long): R {
        return setThreadId(if (threadId == 0L) null else java.lang.Long.toUnsignedString(threadId))
    }

    /**
     * Set the target thread for the webhook message.
     * <br></br>This allows sending webhook messages in the target thread,
     * however the webhook must be part of the thread parent channel.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  channel
     * The target thread channel
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setThread(channel: ThreadChannel?): R {
        return setThreadId(channel?.id)
    }
}
