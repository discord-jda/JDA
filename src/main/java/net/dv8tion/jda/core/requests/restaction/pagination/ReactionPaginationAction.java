/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.restaction.pagination;

import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoints:
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Messages#GET_REACTION_USERS_LIMIT Route.Messages.GET_REACTION_USERS_LIMIT}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Messages#GET_REACTION_USERS_AFTER Route.Messages.GET_REACTION_USERS_AFTER}</li>
 * </ul>
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.MessageReaction MessageReaction} to compile a valid
 * pagination route.</b>
 *
 * @since  3.1
 * @author Florian Spieß
 */
public class ReactionPaginationAction extends PaginationAction<User, ReactionPaginationAction>
{

    protected final MessageReaction reaction;
    protected final String code;

    /**
     * Creates a new PaginationAction instance
     *
     * @param reaction
     *        The target {@link net.dv8tion.jda.core.entities.MessageReaction MessageReaction}
     */
    public ReactionPaginationAction(MessageReaction reaction)
    {
        super(reaction.getJDA(), 1, 100, 100);
        this.reaction = reaction;

        MessageReaction.ReactionEmote emote = reaction.getEmote();
        code = emote.isEmote()
            ? emote.getName() + ":" + emote.getId()
            : MiscUtil.encodeUTF8(emote.getName());
    }

    /**
     * The current target {@link net.dv8tion.jda.core.entities.MessageReaction MessageReaction}
     *
     * @return The current MessageReaction
     */
    public MessageReaction getReaction()
    {
        return reaction;
    }


    @Override
    protected void finalizeRoute()
    {
        String after = null;
        String limit = String.valueOf(getLimit());
        if (!isEmpty())
            after = getLast().getId();

        String channel = reaction.getChannel().getId();
        String message = reaction.getMessageId();

        if (after != null)
            route = Route.Messages.GET_REACTION_USERS_AFTER.compile(channel, message, code, limit, after);
        else
            route = Route.Messages.GET_REACTION_USERS_LIMIT.compile(channel, message, code, limit);
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        final EntityBuilder builder = EntityBuilder.get(api);
        final JSONArray array = response.getArray();
        final List<User> users = new LinkedList<>();
        for (int i = 0; i < array.length(); i++)
        {
            final User user = builder.createFakeUser(array.getJSONObject(i), false);
            cached.add(user);
            users.add(user);
        }

        request.onSuccess(users);
    }

}
