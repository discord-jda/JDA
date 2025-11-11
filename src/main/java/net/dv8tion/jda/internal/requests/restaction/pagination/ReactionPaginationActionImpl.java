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

package net.dv8tion.jda.internal.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class ReactionPaginationActionImpl
        extends PaginationActionImpl<User, ReactionPaginationAction>
        implements ReactionPaginationAction {
    protected final MessageReaction reaction;

    /**
     * Creates a new PaginationAction instance
     *
     * @param reaction
     *        The target {@link MessageReaction MessageReaction}
     */
    public ReactionPaginationActionImpl(MessageReaction reaction) {
        this(reaction, MessageReaction.ReactionType.NORMAL);
    }

    /**
     * Creates a new PaginationAction instance
     *
     * @param reaction
     *        The target {@link MessageReaction MessageReaction}
     * @param type
     *        Type of {@link MessageReaction.ReactionType MessageReaction.ReactionType} to retrieve users for
     */
    public ReactionPaginationActionImpl(
            MessageReaction reaction, MessageReaction.ReactionType type) {
        super(
                reaction.getJDA(),
                getCompiledRoute(
                        reaction.getChannelId(), reaction.getMessageId(), getCode(reaction), type),
                1,
                100,
                100);
        super.order(PaginationOrder.FORWARD);
        this.reaction = reaction;
    }

    public ReactionPaginationActionImpl(
            Message message, String code, MessageReaction.ReactionType type) {
        super(
                message.getJDA(),
                getCompiledRoute(message.getChannelId(), message.getId(), code, type),
                1,
                100,
                100);
        super.order(PaginationOrder.FORWARD);
        this.reaction = null;
    }

    public ReactionPaginationActionImpl(
            MessageChannel channel,
            String messageId,
            String code,
            MessageReaction.ReactionType type) {
        super(
                channel.getJDA(),
                getCompiledRoute(channel.getId(), messageId, code, type),
                1,
                100,
                100);
        super.order(PaginationOrder.FORWARD);
        this.reaction = null;
    }

    private static Route.CompiledRoute getCompiledRoute(
            String channelId, String messageId, String code, MessageReaction.ReactionType type) {
        return Route.Messages.GET_REACTION_USERS
                .compile(channelId, messageId, code)
                .withQueryParams("type", String.valueOf(type.getId()));
    }

    protected static String getCode(MessageReaction reaction) {
        return reaction.getEmoji().getAsReactionCode();
    }

    @Nonnull
    @Override
    public MessageReaction getReaction() {
        if (reaction == null) {
            throw new IllegalStateException("Cannot get reaction for this action");
        }
        return reaction;
    }

    @Nonnull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders() {
        return EnumSet.of(PaginationOrder.FORWARD);
    }

    @Override
    protected void handleSuccess(Response response, Request<List<User>> request) {
        EntityBuilder builder = api.getEntityBuilder();
        DataArray array = response.getArray();
        List<User> users = new LinkedList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                User user = builder.createUser(array.getObject(i));
                users.add(user);
                if (useCache) {
                    cached.add(user);
                }
                last = user;
                lastKey = last.getIdLong();
            } catch (ParsingException | NullPointerException e) {
                LOG.warn("Encountered exception in ReactionPagination", e);
            }
        }

        request.onSuccess(users);
    }

    @Override
    protected long getKey(User it) {
        return it.getIdLong();
    }
}
