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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MessagePaginationAction extends PaginationAction<Message, MessagePaginationAction>
{
    private final MessageChannel channel;

    public MessagePaginationAction(MessageChannel channel)
    {
        super(channel.getJDA(), 1, 100, 100);

        if (channel.getType() == ChannelType.TEXT)
        {
            TextChannel textChannel = (TextChannel) channel;
            if (!textChannel.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_HISTORY))
                throw new PermissionException(Permission.MESSAGE_HISTORY);
        }

        this.channel = channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of
     * the targeted {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     *
     * @return {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     */
    public ChannelType getType()
    {
        return getChannel().getType();
    }

    /**
     * The targeted {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     *
     * @return The MessageChannel instance
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    protected void finalizeRoute()
    {
        final String limit = String.valueOf(this.getLimit());
        final Message last = this.last;

        if (last == null)
            route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId(), limit);
        else
            route = Route.Messages.GET_MESSAGE_HISTORY_BEFORE.compile(channel.getId(), limit, last.getId());
    }

    @Override
    protected void handleResponse(Response response, Request<List<Message>> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        JSONArray array = response.getArray();
        List<Message> messages = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            Message msg = builder.createMessage(array.getJSONObject(i), channel, false);
            messages.add(msg);
            if (useCache)
                cached.add(msg);
            last = msg;
        }

        request.onSuccess(messages);
    }
}
