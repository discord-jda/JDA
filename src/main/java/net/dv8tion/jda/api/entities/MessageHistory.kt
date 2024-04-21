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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.*
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.TimeUtil
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.JDALogger
import org.apache.commons.collections4.map.ListOrderedMap
import java.util.*
import java.util.function.Consumer
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents an access point to the [Message][net.dv8tion.jda.api.entities.Message] history of a
 * [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
 * <br></br>**Note:** Message order is always in recent to past order. I.e: A message at index 0
 * of a list is more recent than a message at index 1.
 *
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistory
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAfter
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryBefore
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAround
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryFromBeginning
 */
class MessageHistory(@Nonnull channel: MessageChannel) {
    protected val channel: MessageChannel
    protected val history = ListOrderedMap<Long, Message>()

    /**
     * Creates a new MessageHistory object.
     *
     * @param  channel
     * The [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] to retrieval history from.
     */
    init {
        Checks.notNull(channel, "Channel")
        this.channel = channel
        if (channel is GuildChannel) {
            val guildChannel = channel as GuildChannel
            val selfMember = guildChannel.guild.getSelfMember()
            Checks.checkAccess(selfMember, guildChannel)
            if (!selfMember!!.hasPermission(
                    guildChannel,
                    Permission.MESSAGE_HISTORY
                )
            ) throw InsufficientPermissionException(guildChannel, Permission.MESSAGE_HISTORY)
        }
    }

    @get:Nonnull
    val jDA: JDA?
        /**
         * The corresponding JDA instance for this MessageHistory
         *
         * @return The corresponding JDA instance
         */
        get() = channel.jDA

    /**
     * The amount of retrieved [Messages][net.dv8tion.jda.api.entities.Message]
     * by this MessageHistory.
     * <br></br>This returns `0` until any call to retrieve messages has completed.
     * See [.retrievePast] and [.retrieveFuture]!
     *
     * @return Amount of retrieved messages
     */
    fun size(): Int {
        return history.size
    }

    val isEmpty: Boolean
        /**
         * Whether this MessageHistory instance has retrieved any messages.
         *
         * @return True, If this MessageHistory instance has not retrieved any messages from discord.
         */
        get() = size() == 0

    /**
     * Returns the [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] that this MessageHistory
     * is related to.
     *
     * @return The MessageChannel of this history.
     */
    @Nonnull
    fun getChannel(): MessageChannelUnion {
        return channel as MessageChannelUnion
    }

    /**
     * Retrieves messages from Discord that were sent before the oldest sent message in MessageHistory's history cache
     * ([.getRetrievedHistory]).
     * <br></br>Can only retrieve a **maximum** of `100` messages at a time.
     * <br></br>This method has 2 modes of operation: initial retrieval and additional retrieval.
     *
     *  * **Initial Retrieval**
     * <br></br>This mode is what is used when no [Messages][net.dv8tion.jda.api.entities.Message] have been retrieved
     * yet ([.getRetrievedHistory]'s size is 0). Initial retrieval starts from the most recent message sent
     * to the channel and retrieves backwards from there. So, if 50 messages are retrieved during this mode, the
     * most recent 50 messages will be retrieved.
     *
     *  * **Additional Retrieval**
     * <br></br>This mode is used once some [Messages][net.dv8tion.jda.api.entities.Message] have already been retrieved
     * from Discord and are stored in MessageHistory's history ([.getRetrievedHistory]). When retrieving
     * messages in this mode, MessageHistory will retrieve previous messages starting from the oldest message
     * stored in MessageHistory.
     * <br></br>E.g: If you initially retrieved 10 messages, the next call to this method to retrieve 10 messages would
     * retrieve the *next* 10 messages, starting from the oldest message of the 10 previously retrieved messages.
     *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>Can occur if retrieving in Additional Mode and the Message being used as the marker for the last retrieved
     * Message was deleted. Currently, to fix this, you need to create a new
     * [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] instance.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>Can occur if the request for history retrieval was executed *after* JDA lost access to the Channel,
     * typically due to the account being removed from the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>Can occur if the request for history retrieval was executed *after* JDA lost the
     * [net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] permission.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The send request was attempted after the channel was deleted.
     *
     *
     * @param  amount
     * The amount of [Messages][net.dv8tion.jda.api.entities.Message] to retrieve.
     *
     * @throws java.lang.IllegalArgumentException
     * The the `amount` is less than `1` or greater than `100`.
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] -
     * Type: [List][java.util.List]&lt;[Message][net.dv8tion.jda.api.entities.Message]&gt;
     * <br></br>Retrieved Messages are placed in a List and provided in order of most recent to oldest with most recent
     * starting at index 0. If the list is empty, there were no more messages left to retrieve.
     */
    @Nonnull
    @CheckReturnValue
    fun retrievePast(amount: Int): RestAction<List<Message>?> {
        require(!(amount > 100 || amount < 1)) { "Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: $amount" }
        var route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.id).withQueryParams("limit", amount.toString())
        if (!history.isEmpty()) route = route.withQueryParams("before", history.lastKey().toString())
        val jda = jDA as JDAImpl?
        return RestActionImpl(jda, route) { response: Response, request: Request<List<Message>?>? ->
            val builder = jda!!.entityBuilder
            val messages = LinkedList<Message>()
            val historyJson = response.array
            for (i in 0 until historyJson.length()) {
                try {
                    messages.add(builder.createMessageWithChannel(historyJson.getObject(i), channel, false))
                } catch (e: Exception) {
                    LOG.warn("Encountered exception when retrieving messages ", e)
                }
            }
            messages.forEach(Consumer { msg: Message -> history[msg.getIdLong()] = msg })
            messages
        }
    }

    /**
     * Retrieves messages from Discord that were sent more recently than the most recently sent message in
     * MessageHistory's history cache ([.getRetrievedHistory]).
     * Use case for this method is for getting more recent messages after jumping to a specific point in history
     * using something like [MessageChannel.getHistoryAround].
     * <br></br>This method works in the same way as [.retrievePast]'s Additional Retrieval mode.
     *
     *
     * **Note:** This method can only be used after [Messages][net.dv8tion.jda.api.entities.Message] have already
     * been retrieved from Discord.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>Can occur if retrieving in Additional Mode and the Message being used as the marker for the last retrieved
     * Message was deleted. Currently, to fix this, you need to create a new
     * [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] instance.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>Can occur if the request for history retrieval was executed *after* JDA lost access to the Channel,
     * typically due to the account being removed from the [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>Can occur if the request for history retrieval was executed *after* JDA lost the
     * [net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] permission.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The send request was attempted after the channel was deleted.
     *
     *
     * @param  amount
     * The amount of [Messages][net.dv8tion.jda.api.entities.Message] to retrieve.
     *
     * @throws java.lang.IllegalArgumentException
     * The the `amount` is less than `1` or greater than `100`.
     * @throws java.lang.IllegalStateException
     * If no messages have been retrieved by this MessageHistory.
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] -
     * Type: [List][java.util.List]&lt;[Message][net.dv8tion.jda.api.entities.Message]&gt;
     * <br></br>Retrieved Messages are placed in a List and provided in order of most recent to oldest with most recent
     * starting at index 0. If the list is empty, there were no more messages left to retrieve.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveFuture(amount: Int): RestAction<List<Message>?> {
        require(!(amount > 100 || amount < 1)) { "Message retrieval limit is between 1 and 100 messages. No more, no less. Limit provided: $amount" }
        check(!history.isEmpty()) { "No messages have been retrieved yet, so there is no message to act as a marker to retrieve more recent messages based on." }
        val route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.id)
            .withQueryParams("limit", amount.toString(), "after", history.firstKey().toString())
        val jda = jDA as JDAImpl?
        return RestActionImpl(jda, route) { response: Response, request: Request<List<Message>?>? ->
            val builder = jda!!.entityBuilder
            val messages = LinkedList<Message>()
            val historyJson = response.array
            for (i in 0 until historyJson.length()) {
                try {
                    messages.add(builder.createMessageWithChannel(historyJson.getObject(i), channel, false))
                } catch (e: Exception) {
                    LOG.warn("Encountered exception when retrieving messages ", e)
                }
            }
            val it = messages.descendingIterator()
            while (it.hasNext()) {
                val m = it.next()
                history.put(0, m.getIdLong(), m)
            }
            messages
        }
    }

    @get:Nonnull
    val retrievedHistory: List<Message>
        /**
         * The List of Messages, sorted starting from newest to oldest, of all message that have already been retrieved
         * from Discord with this MessageHistory object using the [.retrievePast], [.retrieveFuture], and
         * [net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAround] methods.
         *
         *
         * This will be empty if it was just created using [MessageChannel.getHistory] or similar
         * methods. You first have to retrieve messages.
         *
         * @return An immutable List of Messages, sorted newest to oldest.
         */
        get() {
            val size = size()
            if (size == 0) return emptyList() else if (size == 1) return listOf(history.getValue(0))
            return Collections.unmodifiableList(ArrayList(history.values))
        }

    /**
     * Used to get a Message from the set of already retrieved message via it's message Id.
     * <br></br>If a Message with the provided id has not already been retrieved (thus, doesn't not exist in this MessageHistory
     * object), then this method returns null.
     *
     *
     * **Note:** This methods is not the same as [MessageChannel.retrieveMessageById], which itself queries
     * Discord. This method is for getting a message that has already been retrieved by this MessageHistory object.
     *
     * @param  id
     * The id of the requested Message.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided `id` is null or empty.
     * @throws java.lang.NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return Possibly-null Message with the same `id` as the one provided.
     */
    fun getMessageById(@Nonnull id: String?): Message? {
        return getMessageById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Used to get a Message from the set of already retrieved message via it's message Id.
     * <br></br>If a Message with the provided id has not already been retrieved (thus, doesn't not exist in this MessageHistory
     * object), then this method returns null.
     *
     *
     * **Note:** This methods is not the same as [MessageChannel.retrieveMessageById], which itself queries
     * Discord. This method is for getting a message that has already been retrieved by this MessageHistory object.
     *
     * @param  id
     * The id of the requested Message.
     *
     * @return Possibly-null Message with the same `id` as the one provided.
     */
    fun getMessageById(id: Long): Message? {
        return history[id]
    }

    /**
     * Constructs a MessageHistory object with initially retrieved Messages before or after a certain pivot message id.
     * <br></br>Allows to [limit][.limit] the amount to retrieve for better performance!
     */
    class MessageRetrieveAction(route: CompiledRoute?, private val channel: MessageChannel) :
        RestActionImpl<MessageHistory?>(
            channel.jDA, route
        ) {
        private var limit: Int? = null

        /**
         * Limit between 1-100 messages that should be retrieved.
         *
         * @param  limit
         * The limit to use, or `null` to use default 50
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided limit is not between 1-100
         *
         * @return The current MessageRetrieveAction for chaining convenience
         */
        @Nonnull
        @CheckReturnValue
        fun limit(limit: Int?): MessageRetrieveAction {
            if (limit != null) {
                Checks.positive(limit, "Limit")
                Checks.check(limit <= 100, "Limit may not exceed 100!")
            }
            this.limit = limit
            return this
        }

        override fun finalizeRoute(): CompiledRoute {
            val route = super.finalizeRoute()
            return if (limit == null) route else route.withQueryParams("limit", limit.toString())
        }

        protected override fun handleSuccess(response: Response, request: Request<MessageHistory>) {
            val result = MessageHistory(channel)
            val array = response.array
            val builder = api.entityBuilder
            for (i in 0 until array.length()) {
                try {
                    val obj = array.getObject(i)
                    result.history[obj.getLong("id")] = builder.createMessageWithChannel(obj, channel, false)
                } catch (e: Exception) {
                    LOG.warn("Encountered exception in MessagePagination", e)
                }
            }
            request.onSuccess(result)
        }
    }

    companion object {
        protected val LOG = JDALogger.getLog(MessageHistory::class.java)

        /**
         * Constructs a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] with the initially retrieved history
         * of messages sent after the mentioned message ID (exclusive).
         * <br></br>The provided ID need not be valid!
         *
         *
         * Alternatively you can use [MessageChannel.getHistoryAfter(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAfter]
         *
         *
         * **Example**
         * <br></br>`MessageHistory history = MessageHistory.getHistoryAfter(channel, messageId).limit(60).complete()`
         * <br></br>Will return a MessageHistory instance with the first 60 messages sent after the provided message ID.
         *
         *
         * Alternatively you can provide an epoch millisecond timestamp using [MiscUtil.getDiscordTimestamp(long)][TimeUtil.getDiscordTimestamp]:
         * <br></br><pre>`
         * long timestamp = System.currentTimeMillis(); // or any other epoch millis timestamp
         * String discordTimestamp = Long.toUnsignedString(MiscUtil.getDiscordTimestamp(timestamp));
         * MessageHistory history = MessageHistory.getHistoryAfter(channel, discordTimestamp).complete();
        `</pre> *
         *
         * @param  channel
         * The [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel]
         * @param  messageId
         * The pivot ID to use
         *
         * @throws java.lang.IllegalArgumentException
         * If any of the provided arguments is `null`;
         * Or if the provided messageId contains whitespace
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If this is a TextChannel and the currently logged in account does not
         * have the permission [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
         *
         * @return [MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
         *
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAfter
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAfter
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAfter
         */
        @Nonnull
        @CheckReturnValue
        fun getHistoryAfter(@Nonnull channel: MessageChannel, @Nonnull messageId: String): MessageRetrieveAction {
            checkArguments(channel, messageId)
            val route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.id).withQueryParams("after", messageId)
            return MessageRetrieveAction(route, channel)
        }

        /**
         * Constructs a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] with the initially retrieved history
         * of messages sent before the mentioned message ID (exclusive).
         * <br></br>The provided ID need not be valid!
         *
         *
         * Alternatively you can use [MessageChannel.getHistoryBefore(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryBefore]
         *
         *
         * **Example**
         * <br></br>`MessageHistory history = MessageHistory.getHistoryBefore(channel, messageId).limit(60).complete()`
         * <br></br>Will return a MessageHistory instance with the first 60 messages sent before the provided message ID.
         *
         *
         * Alternatively you can provide an epoch millisecond timestamp using [MiscUtil.getDiscordTimestamp(long)][TimeUtil.getDiscordTimestamp]:
         * <br></br><pre>`
         * long timestamp = System.currentTimeMillis(); // or any other epoch millis timestamp
         * String discordTimestamp = Long.toUnsignedString(MiscUtil.getDiscordTimestamp(timestamp));
         * MessageHistory history = MessageHistory.getHistoryBefore(channel, discordTimestamp).complete();
        `</pre> *
         *
         * @param  channel
         * The [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel]
         * @param  messageId
         * The pivot ID to use
         *
         * @throws java.lang.IllegalArgumentException
         * If any of the provided arguments is `null`;
         * Or if the provided messageId contains whitespace
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If this is a TextChannel and the currently logged in account does not
         * have the permission [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
         *
         * @return [MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
         *
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryBefore
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryBefore
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryBefore
         */
        @Nonnull
        @CheckReturnValue
        fun getHistoryBefore(@Nonnull channel: MessageChannel, @Nonnull messageId: String): MessageRetrieveAction {
            checkArguments(channel, messageId)
            val route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.id).withQueryParams("before", messageId)
            return MessageRetrieveAction(route, channel)
        }

        /**
         * Constructs a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] with the initially retrieved history
         * of messages sent around the mentioned message ID (inclusive).
         * <br></br>The provided ID need not be valid!
         *
         *
         * Alternatively you can use [MessageChannel.getHistoryAround(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAround]
         *
         *
         * **Example**
         * <br></br>`MessageHistory history = MessageHistory.getHistoryAround(channel, messageId).limit(60).complete()`
         * <br></br>Will return a MessageHistory instance with the first 60 messages sent around the provided message ID.
         *
         *
         * Alternatively you can provide an epoch millisecond timestamp using [MiscUtil.getDiscordTimestamp(long)][TimeUtil.getDiscordTimestamp]:
         * <br></br><pre>`
         * long timestamp = System.currentTimeMillis(); // or any other epoch millis timestamp
         * String discordTimestamp = Long.toUnsignedString(MiscUtil.getDiscordTimestamp(timestamp));
         * MessageHistory history = MessageHistory.getHistoryAround(channel, discordTimestamp).complete();
        `</pre> *
         *
         * @param  channel
         * The [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel]
         * @param  messageId
         * The pivot ID to use
         *
         * @throws java.lang.IllegalArgumentException
         * If any of the provided arguments is `null`;
         * Or if the provided messageId contains whitespace
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If this is a TextChannel and the currently logged in account does not
         * have the permission [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
         *
         * @return [MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
         *
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAround
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAround
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryAround
         */
        @Nonnull
        @CheckReturnValue
        fun getHistoryAround(@Nonnull channel: MessageChannel, @Nonnull messageId: String): MessageRetrieveAction {
            checkArguments(channel, messageId)
            val route = Route.Messages.GET_MESSAGE_HISTORY.compile(channel.id).withQueryParams("around", messageId)
            return MessageRetrieveAction(route, channel)
        }

        /**
         * Constructs a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] with the initially retrieved history
         * of messages sent.
         *
         *
         * Alternatively you can use [MessageChannel.getHistoryFromBeginning(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryFromBeginning]
         *
         *
         * **Example**<br></br>
         * <br></br>`MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).limit(60).complete()`
         * <br></br>Will return a MessageHistory instance with the first 60 messages of the given [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
         *
         *
         * @param  channel
         * The [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel]
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided MessageChannel is `null`;
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If this is a TextChannel and the currently logged in account does not
         * have the permission [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
         *
         * @return [MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
         *
         * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.getHistoryFromBeginning
         */
        @JvmStatic
        @Nonnull
        @CheckReturnValue
        fun getHistoryFromBeginning(@Nonnull channel: MessageChannel): MessageRetrieveAction {
            return getHistoryAfter(channel, "0")
        }

        private fun checkArguments(channel: MessageChannel, messageId: String) {
            Checks.isSnowflake(messageId, "Message ID")
            Checks.notNull(channel, "Channel")
            if (channel is GuildChannel) {
                val guildChannel = channel as GuildChannel
                val selfMember = guildChannel.guild.getSelfMember()
                Checks.checkAccess(selfMember, guildChannel)
                if (!selfMember!!.hasPermission(
                        guildChannel,
                        Permission.MESSAGE_HISTORY
                    )
                ) throw InsufficientPermissionException(guildChannel, Permission.MESSAGE_HISTORY)
            }
        }
    }
}
