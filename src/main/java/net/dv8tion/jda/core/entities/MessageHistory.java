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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.http.util.Args;
import org.json.JSONArray;

import java.util.*;

/**
 * Represents an access point to the {@link net.dv8tion.jda.core.entities.Message Message} history of a
 * {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
 * <br><b>Note:</b> Message order is always in recent to past order. I.e: A message at index 0
 * of a list is more recent than a message at index 1.
 */
public class MessageHistory
{
    protected final MessageChannel channel;

    protected ListOrderedMap<String, Message> history = new ListOrderedMap<>();

    /**
     * Creates a new MessageHistory object.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} to retrieval history from.
     */
    public MessageHistory(MessageChannel channel)
    {
        this.channel = channel;
        if (channel instanceof TextChannel &&
                !((TextChannel) channel).getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY))
            throw new PermissionException(Permission.MESSAGE_HISTORY);
    }

    /**
     * The corresponding JDA instance for this MessageHistory
     *
     * @return The corresponding JDA instance
     */
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that this MessageHistory
     * is related to.
     *
     * @return The MessageChannel of this history.
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * Retrieves messages from Discord that were sent before the oldest sent message in MessageHistory's history cache
     * ({@link #getRetrievedHistory()}).
     * <br>Can only retrieve a <b>maximum</b> of {@code 100} messages at a time.
     * <br>This method has 2 modes of operation: initial retrieval and additional retrieval.
     * <ul>
     *     <li><b>Initial Retrieval</b>
     *     <br>This mode is what is used when no {@link net.dv8tion.jda.core.entities.Message Messages} have been retrieved
     *         yet ({@link #getRetrievedHistory()}'s size is 0). Initial retrieval starts from the most recent message sent
     *         to the channel and retrieves backwards from there. So, if 50 messages are retrieved during this mode, the
     *         most recent 50 messages will be retrieved.</li>
     *
     *     <li><b>Additional Retrieval</b>
     *     <br>This mode is used once some {@link net.dv8tion.jda.core.entities.Message Messages} have already been retrieved
     *         from Discord and are stored in MessageHistory's history ({@link #getRetrievedHistory()}). When retrieving
     *         messages in this mode, MessageHistory will retrieve previous messages starting from the oldest message
     *         stored in MessageHistory.
     *     <br>E.g: If you initially retrieved 10 messages, the next call to this method to retrieve 10 messages would
     *         retrieve the <i>next</i> 10 messages, starting from the oldest message of the 10 previously retrieved messages.</li>
     * </ul>
     * <p>
     * Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>Can occur if retrieving in Additional Mode and the Message being used as the marker for the last retrieved
     *         Message was deleted. Currently, to fix this, you need to create a new
     *         {@link net.dv8tion.jda.core.entities.MessageHistory MessageHistory} instance.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost access to the Channel,
     *         typically due to the account being removed from the {@link net.dv8tion.jda.core.entities.Guild Guild} or
     *         {@link net.dv8tion.jda.client.entities.Group Group}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost the
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY} permission.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  amount
     *         The amount of {@link net.dv8tion.jda.core.entities.Message Messages} to retrieve.
     *
     * @throws java.lang.IllegalArgumentException
     *         The the {@code amount} is less than {@code 1} or greater than {@code 100}.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link java.util.List List}{@literal <}{@link net.dv8tion.jda.core.entities.Message Message}{@literal >}
     *         <br>Retrieved Messages are placed in a List and provided in order of most recent to oldest with most recent
     *         starting at index 0. If the list is empty, there were no more messages left to retrieve.
     */
    public RestAction<List<Message>> retrievePast(int amount)
    {
        if (amount > 100 || amount < 1)
            throw new IllegalArgumentException("Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: " + amount);

        Route.CompiledRoute route;
        if (history.isEmpty())
            route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId(), Integer.toString(amount));
        else
            route = Route.Messages.GET_MESSAGE_HISTORY_BEFORE.compile(channel.getId(), Integer.toString(amount), history.lastKey());
        return new RestAction<List<Message>>(getJDA(), route, null)
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

    /**
     * Retrieves messages from Discord that were sent more recently than the most recently sent message in
     * MessageHistory's history cache ({@link #getRetrievedHistory()}).
     * Use case for this method is for getting more recent messages after jumping to a specific point in history
     * using something like {@link MessageChannel#getHistoryAround(String, int)}.
     * <br>This method works in the same way as {@link #retrievePast(int)}'s Additional Retrieval mode.
     * <p>
     * <b>Note:</b> This method can only be used after {@link net.dv8tion.jda.core.entities.Message Messages} have already
     * been retrieved from Discord.
     * <p>
     * Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>Can occur if retrieving in Additional Mode and the Message being used as the marker for the last retrieved
     *         Message was deleted. Currently, to fix this, you need to create a new
     *         {@link net.dv8tion.jda.core.entities.MessageHistory MessageHistory} instance.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost access to the Channel,
     *         typically due to the account being removed from the {@link net.dv8tion.jda.core.entities.Guild Guild} or
     *         {@link net.dv8tion.jda.client.entities.Group Group}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost the
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY} permission.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  amount
     *         The amount of {@link net.dv8tion.jda.core.entities.Message Messages} to retrieve.
     *
     * @throws java.lang.IllegalArgumentException
     *         The the {@code amount} is less than {@code 1} or greater than {@code 100}.
     * @throws java.lang.IllegalStateException
     *         If no messages have been retrieved by this MessageHistory.
     *
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} -
     *         Type: {@link java.util.List List}{@literal <}{@link net.dv8tion.jda.core.entities.Message Message}{@literal >}
     *         <br>Retrieved Messages are placed in a List and provided in order of most recent to oldest with most recent
     *         starting at index 0. If the list is empty, there were no more messages left to retrieve.
     */
    public RestAction<List<Message>> retrieveFuture(int amount)
    {
        if (amount > 100 || amount < 1)
            throw new IllegalArgumentException("Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: " + amount);

        if (history.isEmpty())
            throw new IllegalStateException("No messages have been retrieved yet, so there is no message to act as a marker to retrieve more recent messages based on.");

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY_AFTER.compile(channel.getId(), Integer.toString(amount), history.firstKey());
        return new RestAction<List<Message>>(getJDA(), route, null)
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

    /**
     * Returns a List of Messages, sorted starting from newest to oldest, of all message that have already been retrieved
     * from Discord with this MessageHistory object using the {@link #retrievePast(int)}, {@link #retrieveFuture(int)}, and
     * {@link net.dv8tion.jda.core.entities.MessageChannel#getHistoryAround(String, int)} methods.
     *
     * @return A List of Messages, sorted newest to oldest.
     */
    public List<Message> getRetrievedHistory()
    {
        return Collections.unmodifiableList(new ArrayList<>(history.values()));
    }

    /**
     * Use {@link #getRetrievedHistory()} instead.
     *
     * @return List of already retrieved messages.
     *
     * @deprecated Use {@link #getRetrievedHistory()}
     */
    @Deprecated
    public List<Message> getCachedHistory()
    {
        return getRetrievedHistory();
    }

    /**
     * Used to get a Message from the set of already retrieved message via it's message Id.
     * <br>If a Message with the provided id has not already been retrieved (thus, doesn't not exist in this MessageHistory
     * object), then this method returns null.
     * <p>
     * <b>Note:</b> This methods is not the same as {@link MessageChannel#getMessageById(String)}, which itself queries
     * Discord. This method is for getting a message that has already been retrieved by this MessageHistory object.
     *
     * @param  id
     *         The id of the requested Message.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is null or empty.
     *
     * @return Possibly-null Message with the same {@code id} as the one provided.
     */
    public Message getMessageById(String id)
    {
        Args.notEmpty(id, "Provided message id");

        return history.get(id);
    }
}