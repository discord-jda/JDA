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
package net.dv8tion.jda.api.requests.restaction.pagination

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import javax.annotation.Nonnull

/**
 * [PaginationAction] that paginates the message history endpoint.
 * <br></br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 *
 * **Must provide not-null [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] to compile a valid
 * pagination route.**
 *
 *
 * **Limits:**<br></br>
 * Minimum - 1
 * <br></br>Maximum - 100
 *
 *
 * **Example**<br></br>
 * <pre>`
 * / **
 * * Iterates messages in an async stream and stops once the limit has been reached.
 * *&#47;
 * public static void onEachMessageAsync(MessageChannel channel, Consumer<Message> consumer, int limit)
 * {
 * if (limit< 1)
 * return;
 * MessagePaginationAction action = channel.getIterableHistory();
 * AtomicInteger counter = new AtomicInteger(limit);
 * action.forEachAsync( (message)->
 * {
 * consumer.accept(message);
 * // if false the iteration is terminated; else it continues
 * return counter.decrementAndGet() == 0;
 * });
 * }
`</pre> *
 *
 * @since  3.1
 *
 * @see MessageChannel.getIterableHistory
 */
interface MessagePaginationAction : PaginationAction<Message?, MessagePaginationAction?> {
    @get:Nonnull
    val type: ChannelType?
        /**
         * The [ChannelType] of
         * the targeted [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
         *
         * @return [ChannelType]
         */
        get() = channel.type

    @get:Nonnull
    val channel: MessageChannelUnion
}
