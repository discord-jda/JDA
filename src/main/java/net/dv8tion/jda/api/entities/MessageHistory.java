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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents an access point to the {@link net.dv8tion.jda.api.entities.Message Message} history of a
 * {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
 * <br><b>Note:</b> Message order is always in recent to past order. I.e: A message at index 0
 * of a list is more recent than a message at index 1.
 *
 * @see MessageChannel#getHistory()
 * @see MessageChannel#getHistoryAfter(String, int)
 * @see MessageChannel#getHistoryBefore(String, int)
 * @see MessageChannel#getHistoryAround(String, int)
 * @see MessageChannel#getHistoryFromBeginning(int)
 */
public class MessageHistory
{
    protected final MessageChannel channel;
    protected static final Logger LOG = JDALogger.getLog(MessageHistory.class);

    protected final ListOrderedMap<Long, Message> history = new ListOrderedMap<>();

    /**
     * Creates a new MessageHistory object.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} to retrieval history from.
     */
    public MessageHistory(@Nonnull MessageChannel channel)
    {
        Checks.notNull(channel, "Channel");
        this.channel = channel;

        //TODO-v5: Fix permissions here.
        if (channel instanceof TextChannel)
        {
            TextChannel tc = (TextChannel) channel;
            Member selfMember = tc.getGuild().getSelfMember();
            if (!selfMember.hasAccess(tc))
                throw new MissingAccessException(tc, Permission.VIEW_CHANNEL);
            if (!selfMember.hasPermission(tc, Permission.MESSAGE_HISTORY))
                throw new InsufficientPermissionException(tc, Permission.MESSAGE_HISTORY);
        }
    }

    /**
     * The corresponding JDA instance for this MessageHistory
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    /**
     * The amount of retrieved {@link net.dv8tion.jda.api.entities.Message Messages}
     * by this MessageHistory.
     * <br>This returns {@code 0} until any call to retrieve messages has completed.
     * See {@link #retrievePast(int)} and {@link #retrieveFuture(int)}!
     *
     * @return Amount of retrieved messages
     */
    public int size()
    {
        return history.size();
    }

    /**
     * Whether this MessageHistory instance has retrieved any messages.
     *
     * @return True, If this MessageHistory instance has not retrieved any messages from discord.
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} that this MessageHistory
     * is related to.
     *
     * @return The MessageChannel of this history.
     */
    @Nonnull
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
     *     <br>This mode is what is used when no {@link net.dv8tion.jda.api.entities.Message Messages} have been retrieved
     *         yet ({@link #getRetrievedHistory()}'s size is 0). Initial retrieval starts from the most recent message sent
     *         to the channel and retrieves backwards from there. So, if 50 messages are retrieved during this mode, the
     *         most recent 50 messages will be retrieved.</li>
     *
     *     <li><b>Additional Retrieval</b>
     *     <br>This mode is used once some {@link net.dv8tion.jda.api.entities.Message Messages} have already been retrieved
     *         from Discord and are stored in MessageHistory's history ({@link #getRetrievedHistory()}). When retrieving
     *         messages in this mode, MessageHistory will retrieve previous messages starting from the oldest message
     *         stored in MessageHistory.
     *     <br>E.g: If you initially retrieved 10 messages, the next call to this method to retrieve 10 messages would
     *         retrieve the <i>next</i> 10 messages, starting from the oldest message of the 10 previously retrieved messages.</li>
     * </ul>
     * <p>
     * Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>Can occur if retrieving in Additional Mode and the Message being used as the marker for the last retrieved
     *         Message was deleted. Currently, to fix this, you need to create a new
     *         {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} instance.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost access to the Channel,
     *         typically due to the account being removed from the {@link net.dv8tion.jda.api.entities.Guild Guild}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost the
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY} permission.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  amount
     *         The amount of {@link net.dv8tion.jda.api.entities.Message Messages} to retrieve.
     *
     * @throws java.lang.IllegalArgumentException
     *         The the {@code amount} is less than {@code 1} or greater than {@code 100}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} -
     *         Type: {@link java.util.List List}{@literal <}{@link net.dv8tion.jda.api.entities.Message Message}{@literal >}
     *         <br>Retrieved Messages are placed in a List and provided in order of most recent to oldest with most recent
     *         starting at index 0. If the list is empty, there were no more messages left to retrieve.
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<List<Message>> retrievePast(int amount)
    {
        if (amount > 100 || amount < 1)
            throw new IllegalArgumentException("Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: " + amount);

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()).withQueryParams("limit", Integer.toString(amount));

        if (!history.isEmpty())
            route = route.withQueryParams("before", String.valueOf(history.lastKey()));

        JDAImpl jda = (JDAImpl) getJDA();
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            EntityBuilder builder = jda.getEntityBuilder();
            LinkedList<Message> messages  = new LinkedList<>();
            DataArray historyJson = response.getArray();

            for (int i = 0; i < historyJson.length(); i++)
            {
                try
                {
                    messages.add(builder.createMessage(historyJson.getObject(i), channel, false));
                }
                catch (Exception e)
                {
                    LOG.warn("Encountered exception when retrieving messages ", e);
                }
            }

            messages.forEach(msg -> history.put(msg.getIdLong(), msg));
            return messages;
        });
    }

    /**
     * Retrieves messages from Discord that were sent more recently than the most recently sent message in
     * MessageHistory's history cache ({@link #getRetrievedHistory()}).
     * Use case for this method is for getting more recent messages after jumping to a specific point in history
     * using something like {@link MessageChannel#getHistoryAround(String, int)}.
     * <br>This method works in the same way as {@link #retrievePast(int)}'s Additional Retrieval mode.
     * <p>
     * <b>Note:</b> This method can only be used after {@link net.dv8tion.jda.api.entities.Message Messages} have already
     * been retrieved from Discord.
     * <p>
     * Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>Can occur if retrieving in Additional Mode and the Message being used as the marker for the last retrieved
     *         Message was deleted. Currently, to fix this, you need to create a new
     *         {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} instance.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost access to the Channel,
     *         typically due to the account being removed from the {@link net.dv8tion.jda.api.entities.Guild Guild}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>Can occur if the request for history retrieval was executed <i>after</i> JDA lost the
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY} permission.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  amount
     *         The amount of {@link net.dv8tion.jda.api.entities.Message Messages} to retrieve.
     *
     * @throws java.lang.IllegalArgumentException
     *         The the {@code amount} is less than {@code 1} or greater than {@code 100}.
     * @throws java.lang.IllegalStateException
     *         If no messages have been retrieved by this MessageHistory.
     *
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} -
     *         Type: {@link java.util.List List}{@literal <}{@link net.dv8tion.jda.api.entities.Message Message}{@literal >}
     *         <br>Retrieved Messages are placed in a List and provided in order of most recent to oldest with most recent
     *         starting at index 0. If the list is empty, there were no more messages left to retrieve.
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<List<Message>> retrieveFuture(int amount)
    {
        if (amount > 100 || amount < 1)
            throw new IllegalArgumentException("Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: " + amount);

        if (history.isEmpty())
            throw new IllegalStateException("No messages have been retrieved yet, so there is no message to act as a marker to retrieve more recent messages based on.");

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()).withQueryParams("limit", Integer.toString(amount), "after", String.valueOf(history.firstKey()));
        JDAImpl jda = (JDAImpl) getJDA();
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            EntityBuilder builder = jda.getEntityBuilder();
            LinkedList<Message> messages  = new LinkedList<>();
            DataArray historyJson = response.getArray();

            for (int i = 0; i < historyJson.length(); i++)
            {
                try
                {
                    messages.add(builder.createMessage(historyJson.getObject(i), channel, false));
                }
                catch (Exception e)
                {
                    LOG.warn("Encountered exception when retrieving messages ", e);
                }
            }

            for (Iterator<Message> it = messages.descendingIterator(); it.hasNext();)
            {
                Message m = it.next();
                history.put(0, m.getIdLong(), m);
            }

            return messages;
        });
    }

    /**
     * The List of Messages, sorted starting from newest to oldest, of all message that have already been retrieved
     * from Discord with this MessageHistory object using the {@link #retrievePast(int)}, {@link #retrieveFuture(int)}, and
     * {@link net.dv8tion.jda.api.entities.MessageChannel#getHistoryAround(String, int)} methods.
     *
     * <p>This will be empty if it was just created using {@link MessageChannel#getHistory()} or similar
     * methods. You first have to retrieve messages.
     *
     * @return An immutable List of Messages, sorted newest to oldest.
     */
    @Nonnull
    public List<Message> getRetrievedHistory()
    {
        int size = size();
        if (size == 0)
            return Collections.emptyList();
        else if (size == 1)
            return Collections.singletonList(history.getValue(0));
        return Collections.unmodifiableList(new ArrayList<>(history.values()));
    }

    /**
     * Used to get a Message from the set of already retrieved message via it's message Id.
     * <br>If a Message with the provided id has not already been retrieved (thus, doesn't not exist in this MessageHistory
     * object), then this method returns null.
     * <p>
     * <b>Note:</b> This methods is not the same as {@link MessageChannel#retrieveMessageById(String)}, which itself queries
     * Discord. This method is for getting a message that has already been retrieved by this MessageHistory object.
     *
     * @param  id
     *         The id of the requested Message.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is null or empty.
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null Message with the same {@code id} as the one provided.
     */
    @Nullable
    public Message getMessageById(@Nonnull String id)
    {
        return getMessageById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Used to get a Message from the set of already retrieved message via it's message Id.
     * <br>If a Message with the provided id has not already been retrieved (thus, doesn't not exist in this MessageHistory
     * object), then this method returns null.
     * <p>
     * <b>Note:</b> This methods is not the same as {@link MessageChannel#retrieveMessageById(long)}, which itself queries
     * Discord. This method is for getting a message that has already been retrieved by this MessageHistory object.
     *
     * @param  id
     *         The id of the requested Message.
     *
     * @return Possibly-null Message with the same {@code id} as the one provided.
     */
    @Nullable
    public Message getMessageById(long id)
    {
        return history.get(id);
    }

    /**
     * Constructs a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} with the initially retrieved history
     * of messages sent after the mentioned message ID (exclusive).
     * <br>The provided ID need not be valid!
     *
     * <p>Alternatively you can use {@link net.dv8tion.jda.api.entities.MessageChannel#getHistoryAfter(String, int) MessageChannel.getHistoryAfter(...)}
     *
     * <p><b>Example</b>
     * <br>{@code MessageHistory history = MessageHistory.getHistoryAfter(channel, messageId).limit(60).complete()}
     * <br>Will return a MessageHistory instance with the first 60 messages sent after the provided message ID.
     *
     * <p>Alternatively you can provide an epoch millisecond timestamp using {@link TimeUtil#getDiscordTimestamp(long) MiscUtil.getDiscordTimestamp(long)}:
     * <br><pre><code>
     * long timestamp = System.currentTimeMillis(); // or any other epoch millis timestamp
     * String discordTimestamp = Long.toUnsignedString(MiscUtil.getDiscordTimestamp(timestamp));
     * MessageHistory history = MessageHistory.getHistoryAfter(channel, discordTimestamp).complete();
     * </code></pre>
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     * @param  messageId
     *         The pivot ID to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null};
     *         Or if the provided messageId contains whitespace
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and the currently logged in account does not
     *         have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageRetrieveAction}
     *
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryAfter(String, int)  MessageChannel.getHistoryAfter(String, int)
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryAfter(long, int)    MessageChannel.getHistoryAfter(long, int)
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryAfter(Message, int) MessageChannel.getHistoryAfter(Message, int)
     */
    @Nonnull
    @CheckReturnValue
    public static MessageRetrieveAction getHistoryAfter(@Nonnull MessageChannel channel, @Nonnull String messageId)
    {
        checkArguments(channel, messageId);
        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()).withQueryParams("after", messageId);
        return new MessageRetrieveAction(route, channel);
    }

    /**
     * Constructs a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} with the initially retrieved history
     * of messages sent before the mentioned message ID (exclusive).
     * <br>The provided ID need not be valid!
     *
     * <p>Alternatively you can use {@link net.dv8tion.jda.api.entities.MessageChannel#getHistoryBefore(String, int) MessageChannel.getHistoryBefore(...)}
     *
     * <p><b>Example</b>
     * <br>{@code MessageHistory history = MessageHistory.getHistoryBefore(channel, messageId).limit(60).complete()}
     * <br>Will return a MessageHistory instance with the first 60 messages sent before the provided message ID.
     *
     * <p>Alternatively you can provide an epoch millisecond timestamp using {@link TimeUtil#getDiscordTimestamp(long) MiscUtil.getDiscordTimestamp(long)}:
     * <br><pre><code>
     * long timestamp = System.currentTimeMillis(); // or any other epoch millis timestamp
     * String discordTimestamp = Long.toUnsignedString(MiscUtil.getDiscordTimestamp(timestamp));
     * MessageHistory history = MessageHistory.getHistoryBefore(channel, discordTimestamp).complete();
     * </code></pre>
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     * @param  messageId
     *         The pivot ID to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null};
     *         Or if the provided messageId contains whitespace
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and the currently logged in account does not
     *         have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageRetrieveAction}
     *
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryBefore(String, int)  MessageChannel.getHistoryBefore(String, int)
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryBefore(long, int)    MessageChannel.getHistoryBefore(long, int)
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryBefore(Message, int) MessageChannel.getHistoryBefore(Message, int)
     */
    @Nonnull
    @CheckReturnValue
    public static MessageRetrieveAction getHistoryBefore(@Nonnull MessageChannel channel, @Nonnull String messageId)
    {
        checkArguments(channel, messageId);
        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()).withQueryParams("before", messageId);
        return new MessageRetrieveAction(route, channel);
    }

    /**
     * Constructs a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} with the initially retrieved history
     * of messages sent around the mentioned message ID (inclusive).
     * <br>The provided ID need not be valid!
     *
     * <p>Alternatively you can use {@link net.dv8tion.jda.api.entities.MessageChannel#getHistoryAround(String, int) MessageChannel.getHistoryAround(...)}
     *
     * <p><b>Example</b>
     * <br>{@code MessageHistory history = MessageHistory.getHistoryAround(channel, messageId).limit(60).complete()}
     * <br>Will return a MessageHistory instance with the first 60 messages sent around the provided message ID.
     *
     * <p>Alternatively you can provide an epoch millisecond timestamp using {@link TimeUtil#getDiscordTimestamp(long) MiscUtil.getDiscordTimestamp(long)}:
     * <br><pre><code>
     * long timestamp = System.currentTimeMillis(); // or any other epoch millis timestamp
     * String discordTimestamp = Long.toUnsignedString(MiscUtil.getDiscordTimestamp(timestamp));
     * MessageHistory history = MessageHistory.getHistoryAround(channel, discordTimestamp).complete();
     * </code></pre>
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     * @param  messageId
     *         The pivot ID to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null};
     *         Or if the provided messageId contains whitespace
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and the currently logged in account does not
     *         have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageRetrieveAction}
     *
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryAround(String, int)  MessageChannel.getHistoryAround(String, int)
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryAround(long, int)    MessageChannel.getHistoryAround(long, int)
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryAround(Message, int) MessageChannel.getHistoryAround(Message, int)
     */
    @Nonnull
    @CheckReturnValue
    public static MessageRetrieveAction getHistoryAround(@Nonnull MessageChannel channel, @Nonnull String messageId)
    {
        checkArguments(channel, messageId);
        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()).withQueryParams("around", messageId);
        return new MessageRetrieveAction(route, channel);
    }

    /**
     * Constructs a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} with the initially retrieved history
     * of messages sent.
     *
     * <p>Alternatively you can use {@link net.dv8tion.jda.api.entities.MessageChannel#getHistoryFromBeginning(int) MessageChannel.getHistoryFromBeginning(...)}
     *
     * <h2>Example</h2>
     * <br>{@code MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).limit(60).complete()}
     * <br>Will return a MessageHistory instance with the first 60 messages of the given {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.

     *
     * @param  channel
     *         The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided MessageChannel is {@code null};
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and the currently logged in account does not
     *         have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageRetrieveAction}
     *
     * @see    net.dv8tion.jda.api.entities.MessageChannel#getHistoryFromBeginning(int)  MessageChannel.getHistoryFromBeginning(int)
     */
    @Nonnull
    @CheckReturnValue
    public static MessageRetrieveAction getHistoryFromBeginning(@Nonnull MessageChannel channel)
    {
        return getHistoryAfter(channel, "0");
    }

    private static void checkArguments(MessageChannel channel, String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(channel, "Channel");
        if (channel.getType() == ChannelType.TEXT)
        {
            TextChannel t = (TextChannel) channel;
            Member selfMember = t.getGuild().getSelfMember();
            if (!selfMember.hasAccess(t))
                throw new MissingAccessException(t, Permission.VIEW_CHANNEL);
            if (!selfMember.hasPermission(t, Permission.MESSAGE_HISTORY))
                throw new InsufficientPermissionException(t, Permission.MESSAGE_HISTORY);
        }
    }

    /**
     * Constructs a MessageHistory object with initially retrieved Messages before or after a certain pivot message id.
     * <br>Allows to {@link #limit(Integer) limit} the amount to retrieve for better performance!
     */
    public static class MessageRetrieveAction extends RestActionImpl<MessageHistory>
    {
        private final MessageChannel channel;
        private Integer limit;

        protected MessageRetrieveAction(Route.CompiledRoute route, MessageChannel channel)
        {
            super(channel.getJDA(), route);
            this.channel = channel;
        }

        /**
         * Limit between 1-100 messages that should be retrieved.
         *
         * @param  limit
         *         The limit to use, or {@code null} to use default 50
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided limit is not between 1-100
         *
         * @return The current MessageRetrieveAction for chaining convenience
         */
        @Nonnull
        @CheckReturnValue
        public MessageRetrieveAction limit(@Nullable Integer limit)
        {
            if (limit != null)
            {
                Checks.positive(limit, "Limit");
                Checks.check(limit <= 100, "Limit may not exceed 100!");
            }
            this.limit = limit;
            return this;
        }

        @Override
        protected Route.CompiledRoute finalizeRoute()
        {
            final Route.CompiledRoute route = super.finalizeRoute();
            return limit == null ? route : route.withQueryParams("limit", String.valueOf(limit));
        }

        @Override
        protected void handleSuccess(Response response, Request<MessageHistory> request)
        {
            final MessageHistory result = new MessageHistory(channel);
            final DataArray array = response.getArray();
            final EntityBuilder builder = api.getEntityBuilder();
            for (int i = 0; i < array.length(); i++)
            {
                try
                {
                    DataObject obj = array.getObject(i);
                    result.history.put(obj.getLong("id"), builder.createMessage(obj, channel, false));
                }
                catch (Exception e)
                {
                    LOG.warn("Encountered exception in MessagePagination", e);
                }
            }
            request.onSuccess(result);
        }
    }
}
