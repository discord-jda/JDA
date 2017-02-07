/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.json.JSONArray;

import java.util.*;

public class MessageHistory implements net.dv8tion.jda.core.hooks.EventListener
{
    protected final JDAImpl api;
    protected final MessageChannel channel;

    protected ListOrderedMap<Long, Message> history = new ListOrderedMap<>();
    protected long markerId;

    public MessageHistory(MessageChannel channel)
    {
        this.api = (JDAImpl) channel.getJDA();
        this.channel = channel;
        if (channel instanceof TextChannel &&
                !((TextChannel) channel).getGuild().getMember(api.getSelfUser()).hasPermission(Permission.MESSAGE_HISTORY))
            throw new PermissionException(Permission.MESSAGE_HISTORY);
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that this MessageHistory
     * is related to.
     *
     * @return
     *      The MessageChannel of this history.
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    public synchronized RestAction<List<Message>> retrievePast(int amount)
    {
        if (amount > 100 || amount < 0)
            throw new IllegalArgumentException("Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: " + amount);

        Route.CompiledRoute route;
        if (history.isEmpty())
            route = Route.Messages.GET_MESSAGE_HISTORY.compile(Long.toString(channel.getId()), Integer.toString(amount));
        else
            route = Route.Messages.GET_MESSAGE_HISTORY_BEFORE.compile(Long.toString(channel.getId()), Integer.toString(amount), Long.toString(history.lastKey()));
        return new RestAction<List<Message>>(api, route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = EntityBuilder.get(api);
                LinkedList<Message> msgs  = new LinkedList<>();
                JSONArray historyJson = response.getArray();

                for (int i = 0; i < historyJson.length(); i++)
                    msgs.add(builder.createMessage(historyJson.getJSONObject(i)));

                msgs.forEach(msg -> history.put(msg.getId(), msg));
                request.onSuccess(msgs);
            }
        };
    }

    public RestAction<List<Message>> retrieveFuture(int amount)
    {
        if (amount > 100 || amount < 0)
            throw new IllegalArgumentException("Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: " + amount);

        if (history.isEmpty())
            throw new IllegalStateException("No messageId  is stored to use as the marker between the future and past." +
                    "Either use MessageHistory(MessageChannel, String) or make a call to retrievePast(int) first.");

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY_AFTER.compile(Long.toString(channel.getId()), Integer.toString(amount), Long.toString(history.firstKey()));
        return new RestAction<List<Message>>(api, route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = EntityBuilder.get(api);
                LinkedList<Message> msgs  = new LinkedList<>();
                JSONArray historyJson = response.getArray();

                for (int i = 0; i < historyJson.length(); i++)
                    msgs.add(builder.createMessage(historyJson.getJSONObject(i)));

                for (Iterator<Message> it = msgs.descendingIterator(); it.hasNext();)
                {
                    Message m = it.next();
                    history.put(0, m.getId(), m);
                }

                request.onSuccess(msgs);
            }
        };
    }

    public List<Message> getCachedHistory()
    {
        return Collections.unmodifiableList(new ArrayList<>(history.values()));
    }

    public Message getMessageById(String id)
    {
        return history.get(id);
    }

    public Message informUpdate(Message msg)
    {
        if (msg.getChannel().equals(channel) && history.containsKey(msg.getId()))
            return history.put(msg.getId(), msg);

        return null;
    }

    public Message informDeletion(Message msg)
    {
        if (msg.getChannel().equals(channel))
            return informDeletion(msg.getId());

        return null;
    }


    public Message informDeletion(long id)
    {
        return history.remove(id);
    }

    @Override
    public void onEvent(Event event)
    {
        if (event instanceof MessageUpdateEvent)
        {
            informUpdate(((MessageUpdateEvent) event).getMessage());
        }
        else if (event instanceof MessageDeleteEvent)
        {
            MessageDeleteEvent mEvent = (MessageDeleteEvent) event;
            if (mEvent.getChannel().equals(channel))
            {
                history.remove(mEvent.getMessageId());
            }
        }
    }

    public static RestAction<MessageHistory> getHistoryAround(MessageChannel channel, Message message, int limit)
    {
        if (!message.getChannel().equals(channel))
            throw new IllegalArgumentException("The provided Message is not from the MessageChannel!");

        return getHistoryAround(channel, message.getId(), limit);
    }

    public static RestAction<MessageHistory> getHistoryAround(MessageChannel channel, final long markerMessageId, int limit)
    {
        if (limit > 100 || limit < 1)
            throw new IllegalArgumentException("Provided limit was out of bounds. Minimum: 1, Max: 100. Provided: " + limit);

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY_AROUND.compile(Long.toString(channel.getId()), Integer.toString(limit), Long.toString(markerMessageId));
        return new RestAction<MessageHistory>(channel.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                MessageHistory mHistory = new MessageHistory(channel);
                mHistory.markerId = markerMessageId;

                EntityBuilder builder = EntityBuilder.get(api);
                LinkedList<Message> msgs  = new LinkedList<>();
                JSONArray historyJson = response.getArray();

                for (int i = 0; i < historyJson.length(); i++)
                    msgs.add(builder.createMessage(historyJson.getJSONObject(i)));

                msgs.forEach(msg -> mHistory.history.put(msg.getId(), msg));
                request.onSuccess(mHistory);
            }
        };
    }
}