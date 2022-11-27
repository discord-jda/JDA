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

package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import javax.annotation.Nonnull;

/**
 * {@link PaginationAction PaginationAction} that paginates the reaction users endpoint.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction} to compile a valid
 * pagination route.</b>
 *
 * <p><b>Limits:</b><br>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * // Remove reactions for the specified emoji
 * public static void removeReaction(Message message, String emoji) {
 *     // get paginator
 *     ReactionPaginationAction users = message.retrieveReactionUsers(emoji);
 *     // remove reaction for every user
 *     users.forEachAsync((user) ->
 *         message.removeReaction(emoji, user).queue()
 *     );
 * }
 * }</pre>
 *
 * @see    MessageReaction#retrieveUsers()
 * @see    Message#retrieveReactionUsers(Emoji)
 * @see    MessageChannel#retrieveReactionUsersById(long, Emoji)
 * @see    MessageChannel#retrieveReactionUsersById(String, Emoji)
 */
public interface ReactionPaginationAction extends PaginationAction<User, ReactionPaginationAction>
{
    /**
     * The current target {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction}
     *
     * @throws IllegalStateException
     *         If this was created by {@link Message#retrieveReactionUsers(Emoji) Message.retrieveReactionUsers(...)} or {@link MessageChannel#retrieveReactionUsersById(long, Emoji) MessageChannel.retrieveReactionUsersById(...)}
     *
     * @return The current MessageReaction
     */
    @Nonnull
    MessageReaction getReaction();
}
