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
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * An object representing a single MessageReaction from Discord.
 * This is an immutable object and is not updated by method calls or changes in Discord. A new snapshot instance
 * built from Discord is needed to see changes.
 *
 * @see Message.getReactions
 * @see Message.getReaction
 */
class MessageReaction
/**
 * Creates a new MessageReaction instance
 *
 * @param  channel
 * The [MessageChannel] this Reaction was used in
 * @param  emoji
 * The [Emoji] that was used
 * @param  channelId
 * The channel id for this reaction
 * @param  messageId
 * The message id this reaction is attached to
 * @param  self
 * Whether we already reacted with this Reaction,
 * as an array of `[normal, super]`
 * @param  counts
 * The amount of people that reacted with this Reaction,
 * as an array of `[total, normal, super]`
 */(
    /**
     * The JDA instance of this Reaction
     *
     * @return The JDA instance of this Reaction
     */
    @get:Nonnull
    @param:Nonnull val jDA: JDA, private val channel: MessageChannel?,
    /**
     * The [Emoji] of this Reaction.
     * <br></br>This includes both [custom emojis][Emoji.Type.CUSTOM] and [unicode emoji][Emoji.Type.UNICODE].
     *
     * @return The final instance of this Reaction's Emoji
     */
    @JvmField @get:Nonnull
    @param:Nonnull val emoji: EmojiUnion,
    /**
     * The ID for the channel this reaction happened in.
     *
     * @return The channel id
     */
    val channelIdLong: Long,
    /**
     * The message id this reaction is attached to
     *
     * @return The message id this reaction is attached to
     */
    val messageIdLong: Long, private val self: BooleanArray, private val counts: IntArray?
) {

    /**
     * Whether the currently logged in account has reacted with this reaction at all, including both super and normal.
     *
     *
     * **This will always be false for events. Discord does not provide this information for reaction events.**
     * You can use [MessageChannel.retrieveMessageById] to get this information on a complete message.
     *
     * @return True, if we reacted with this reaction
     *
     * @see .isSelf
     */
    fun isSelf(): Boolean {
        return self[0] || self[1]
    }

    /**
     * Whether the currently logged in account has reacted with this reaction as specifically a super or normal reaction.
     *
     *
     * **This will always be false for events. Discord does not provide this information for reaction events.**
     * You can use [MessageChannel.retrieveMessageById] to get this information on a complete message.
     *
     * @param  type
     * The specific type of reaction
     *
     * @return True, if we reacted with this reaction
     */
    fun isSelf(@Nonnull type: ReactionType): Boolean {
        Checks.notNull(type, "Type")
        return self[if (type == ReactionType.NORMAL) 0 else 1]
    }

    /**
     * Whether this reaction can provide a count via [.getCount].
     * <br></br>This is usually not provided for reactions coming from [net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent]
     * or similar.
     *
     * @return True, if a count is available
     *
     * @see .getCount
     */
    fun hasCount(): Boolean {
        return counts != null
    }

    /**
     * Whether this reaction instance has an available [.getChannel].
     *
     *
     * This can be `false` for messages sent via webhooks, or in the context of interactions.
     *
     * @return True, if [.getChannel] is available
     */
    fun hasChannel(): Boolean {
        return channel != null
    }

    val count: Int
        /**
         * The total amount of users that already reacted with this Reaction.
         * <br></br>**This is not updated, it is a `final int` per Reaction instance**
         *
         *
         * This value is not available in events such as [MessageReactionAddEvent][net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent]
         * and [MessageReactionRemoveEvent][net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent] in which case an
         * [IllegalStateException][java.lang.IllegalStateException] is thrown!
         *
         * @throws java.lang.IllegalStateException
         * If this MessageReaction is from an event which does not provide a count
         *
         * @return The amount of users that reacted with this Reaction
         *
         * @see .getCount
         */
        get() {
            check(hasCount()) { "Cannot retrieve count for this MessageReaction!" }
            return counts!![0]
        }

    /**
     * The specific amount of users that already reacted with this Reaction.
     * <br></br>**This is not updated, it is a `final int` per Reaction instance**
     *
     *
     * This value is not available in events such as [MessageReactionAddEvent][net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent]
     * and [MessageReactionRemoveEvent][net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent] in which case an
     * [IllegalStateException][java.lang.IllegalStateException] is thrown!
     *
     * @param  type
     * The specific type of reaction
     *
     * @throws java.lang.IllegalStateException
     * If this MessageReaction is from an event which does not provide a count
     *
     * @return The amount of users that reacted with this Reaction
     *
     * @see .getCount
     */
    fun getCount(@Nonnull type: ReactionType): Int {
        check(hasCount()) { "Cannot retrieve count for this MessageReaction!" }
        Checks.notNull(type, "Type")
        return counts!![if (type == ReactionType.NORMAL) 1 else 2]
    }

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType]
         * this Reaction was used in.
         *
         * @return The ChannelType
         */
        get() = if (channel != null) channel.type else ChannelType.UNKNOWN

    /**
     * Whether this Reaction was used in a [MessageChannel]
     * of the specified [ChannelType].
     *
     * @param  type
     * The ChannelType to compare
     *
     * @return True, if this Reaction was used in a MessageChannel from the specified ChannelType
     */
    fun isFromType(@Nonnull type: ChannelType): Boolean {
        return channelType == type
    }

    @get:Nonnull
    val guild: Guild
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] this Reaction was used in.
         *
         * @throws IllegalStateException
         * If [.getChannel] is not a guild channel or the channel is not provided
         *
         * @return [Guild][net.dv8tion.jda.api.entities.Guild] this Reaction was used in
         */
        get() = guildChannel!!.guild

    /**
     * The [MessageChannel]
     * this Reaction was used in.
     *
     * @throws IllegalStateException
     * If no channel instance is provided, this might be missing for messages sent from webhooks.
     *
     * @return The channel this Reaction was used in
     */
    @Nonnull
    fun getChannel(): MessageChannelUnion {
        if (channel != null) return channel as MessageChannelUnion
        throw IllegalStateException("Cannot provide channel instance for this reaction! Use getChannelId() instead.")
    }

    @get:Nonnull
    val guildChannel: GuildMessageChannelUnion?
        /**
         * The [channel][net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel] this Reaction was used in.
         *
         * @throws IllegalStateException
         * If [.getChannel] is not a guild channel or the channel is not provided
         *
         * @return The guild channel this Reaction was used in
         */
        get() = getChannel().asGuildMessageChannel() as GuildMessageChannelUnion?

    /**
     * The ID for the channel this reaction happened in.
     *
     * @return The channel id
     */
    @Nonnull
    fun getChannelId(): String {
        return java.lang.Long.toUnsignedString(channelIdLong)
    }

    /**
     * The message id this reaction is attached to
     *
     * @return The message id this reaction is attached to
     */
    @Nonnull
    fun getMessageId(): String {
        return java.lang.Long.toUnsignedString(messageIdLong)
    }

    /**
     * Retrieves the [Users][net.dv8tion.jda.api.entities.User] that
     * already reacted with this MessageReaction.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>If the message this reaction was attached to got deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel this reaction was used in got deleted.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the channel/guild
     *
     *
     * @return [ReactionPaginationAction]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveUsers(): ReactionPaginationAction {
        return ReactionPaginationActionImpl(this)
    }

    /**
     * Removes this Reaction from the Message.
     * <br></br>This will remove our own reaction as an overload
     * of [.removeReaction].
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>If the message this reaction was attached to got deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel this reaction was used in got deleted.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the channel/guild
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Void
     * Nothing is returned on success
     */
    @Nonnull
    @CheckReturnValue
    fun removeReaction(): RestAction<Void> {
        return removeReaction(jDA.getSelfUser())
    }

    /**
     * Removes this Reaction from the Message.
     * <br></br>This will remove the reaction of the [User][net.dv8tion.jda.api.entities.User]
     * provided.
     *
     *
     * If the provided User did not react with this Reaction this does nothing.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>If the message this reaction was attached to got deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel this reaction was used in got deleted.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the channel/guild
     *
     *
     * @param  user
     * The User of which to remove the reaction
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided `user` is null.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the provided User is not us and we do not have permission to
     * [manage messages][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     * in the channel this reaction was used in
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     * If the message is from another user in a [PrivateChannel]
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     * Nothing is returned on success
     */
    @Nonnull
    @CheckReturnValue
    fun removeReaction(@Nonnull user: User): RestAction<Void> {
        Checks.notNull(user, "User")
        val self = user == jDA.getSelfUser()
        if (!self && channel != null) {
            if (!channel.type!!.isGuild) throw PermissionException("Unable to remove Reaction of other user in non-guild channels!")
            val guildChannel = channel as GuildChannel
            if (!guildChannel.guild.getSelfMember()
                    .hasPermission(guildChannel, Permission.MESSAGE_MANAGE)
            ) throw InsufficientPermissionException(guildChannel, Permission.MESSAGE_MANAGE)
        }
        val code = emoji.asReactionCode
        val target = if (self) "@me" else user.id
        val route = Route.Messages.REMOVE_REACTION.compile(getChannelId(), getMessageId(), code, target)
        return RestActionImpl(jDA, route)
    }

    /**
     * Removes this entire reaction from the message.
     * <br></br>Unlike [.removeReaction], which removes the reaction of a single user, this will remove the reaction
     * completely.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The currently logged in account lost access to the channel by either being removed from the guild
     * or losing the [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided unicode emoji doesn't exist. Try using one of the example formats.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message was deleted.
     *
     *
     * @throws UnsupportedOperationException
     * If this reaction happened in a private channel
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MESSAGE_MANAGE] in the channel
     *
     * @return [RestAction]
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun clearReactions(): RestAction<Void?>? {
        if (channel == null) {
            val route =
                Route.Messages.CLEAR_EMOJI_REACTIONS.compile(getChannelId(), getMessageId(), emoji.asReactionCode)
            return RestActionImpl(jDA, route)
        }

        // Requires permission, only works in guilds
        if (!channelType!!.isGuild) throw UnsupportedOperationException("Cannot clear reactions on a message sent from a private channel")
        val guildChannel: GuildMessageChannel? = Objects.requireNonNull(guildChannel)
        return guildChannel!!.clearReactionsById(getMessageId(), emoji)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is MessageReaction) return false
        val r = obj
        return r.emoji == emoji && r.isSelf() == this.isSelf() && r.messageIdLong == messageIdLong
    }

    override fun toString(): String {
        return EntityString(this)
            .addMetadata("channelId", channelIdLong)
            .addMetadata("messageId", messageIdLong)
            .addMetadata("emoji", emoji)
            .toString()
    }

    /**
     * Type of reaction.
     */
    enum class ReactionType {
        NORMAL,
        SUPER
    }
}
