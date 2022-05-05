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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessagePaginationActionImpl
    extends PaginationActionImpl<Message, MessagePaginationAction>
    implements MessagePaginationAction
{
    private final MessageChannel channel;

    public MessagePaginationActionImpl(MessageChannel channel)
    {
        super(channel.getJDA(), Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()), 1, 100, 100);

        //TODO-v5: Fix permissions here.
        if (channel.getType() == ChannelType.TEXT)
        {
            TextChannel textChannel = (TextChannel) channel;
            Member selfMember = textChannel.getGuild().getSelfMember();
            if (!selfMember.hasAccess(textChannel))
                throw new MissingAccessException(textChannel, Permission.VIEW_CHANNEL);
            if (!selfMember.hasPermission(textChannel, Permission.MESSAGE_HISTORY))
                throw new InsufficientPermissionException(textChannel, Permission.MESSAGE_HISTORY);
        }

        this.channel = channel;
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Message>> request)
    {
        DataArray array = response.getArray();
        List<Message> messages = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            try
            {
                Message msg = builder.createMessageWithChannel(array.getObject(i), channel, false);
                messages.add(msg);
            }
            catch (ParsingException | NullPointerException e)
            {
                LOG.warn("Encountered an exception in MessagePagination", e);
            }
            catch (IllegalArgumentException e)
            {
                if (EntityBuilder.UNKNOWN_MESSAGE_TYPE.equals(e.getMessage()))
                    LOG.warn("Skipping unknown message type during pagination", e);
                else
                    LOG.warn("Unexpected issue trying to parse message during pagination", e);
            }
        }

        if (order == PaginationOrder.FORWARD)
            Collections.reverse(messages);
        if (useCache)
            cached.addAll(messages);

        if (!messages.isEmpty())
        {
            last = messages.get(messages.size() - 1);
            lastKey = last.getIdLong();
        }

        request.onSuccess(messages);
    }

    @Override
    protected long getKey(Message it)
    {
        return it.getIdLong();
    }
}
