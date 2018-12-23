/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * {@link PaginationAction PaginationAction}
 * that paginates the endpoints {@link net.dv8tion.jda.internal.requests.Route.Messages#GET_MESSAGE_HISTORY Route.Messages.GET_MESSAGE_HISTORY}.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} to compile a valid
 * pagination route.</b>
 *
 * <h2>Limits:</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <h1>Example</h1>
 * <pre><code>
 * /**
 *  * Iterates messages in an async stream and stops once the limit has been reached.
 *  *&#47;
 * public static void onEachMessageAsync(MessageChannel channel, {@literal Consumer<Message>} consumer, int limit)
 * {
 *     if (limit{@literal <} 1)
 *         return;
 *     <u>MessagePaginationAction</u> action = channel.<u>getIterableHistory</u>();
 *     AtomicInteger counter = new AtomicInteger(limit);
 *     action.forEachAsync( (message){@literal ->}
 *     {
 *         consumer.accept(message);
 *         // if false the iteration is terminated; else it continues
 *         return counter.decrementAndGet() == 0;
 *     });
 * }
 * </code></pre>
 *
 * @since  3.1
 */
public interface MessagePaginationAction extends PaginationAction<Message, MessagePaginationAction>
{
    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} of
     * the targeted {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     *
     * @return {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     */
    default ChannelType getType()
    {
        return getChannel().getType();
    }

    /**
     * The targeted {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     *
     * @return The MessageChannel instance
     */
    MessageChannel getChannel();
}
