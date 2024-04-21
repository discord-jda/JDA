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
package net.dv8tion.jda.api.events.message.react

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.requests.CompletedRestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Indicates that a MessageReaction was added/removed.
 * <br></br>Every MessageReactionEvent is derived from this event and can be casted.
 *
 *
 * Can be used to detect both remove and add events.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require at least one of the following intents (Will not fire at all if neither is enabled):
 *
 *  * [GUILD_MESSAGE_REACTIONS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS] to work in guild text channels
 *  * [DIRECT_MESSAGE_REACTIONS][net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGE_REACTIONS] to work in private channels
 *
 */
open class GenericMessageReactionEvent(
    @Nonnull api: JDA, responseNumber: Long, protected var issuer: User?,
    /**
     * The [Member][net.dv8tion.jda.api.entities.Member] instance for the reacting user
     * or `null` if the reaction was from a user not in this guild.
     * <br></br>This will also be `null` if the member is not available in the cache.
     * Use [.retrieveMember] to load the member.
     *
     * @throws java.lang.IllegalStateException
     * If this was not sent in a [net.dv8tion.jda.api.entities.Guild].
     *
     * @return Member of the reacting user or null if they are no longer member of this guild
     *
     * @see .isFromGuild
     * @see .getChannelType
     */
    var member: Member?,
    /**
     * The [MessageReaction][net.dv8tion.jda.api.entities.MessageReaction]
     *
     * @return The MessageReaction
     */
    @get:Nonnull
    @param:Nonnull var reaction: MessageReaction,
    /**
     * The id for the user who owns reaction.
     *
     * @return The user id
     */
    val userIdLong: Long
) : GenericMessageEvent(api, responseNumber, reaction.messageIdLong, reaction.getChannel()) {
    /**
     * The id for the user who owns the reaction.
     *
     * @return The user id
     */
    @Nonnull
    fun getUserId(): String {
        return java.lang.Long.toUnsignedString(userIdLong)
    }

    val user: User?
        /**
         * The reacting [User][net.dv8tion.jda.api.entities.User]
         * <br></br>This might be missing if the user was not cached.
         * Use [.retrieveUser] to load the user.
         *
         * @return The reacting user or null if this information is missing
         */
        get() = if (issuer == null && isFromType(ChannelType.PRIVATE)) getChannel().asPrivateChannel()!!.user // this can't be the self user because then issuer would be nonnull
        else issuer

    @get:Nonnull
    val emoji: EmojiUnion
        /**
         * The [Emoji] of the reaction, shortcut for `getReaction().getEmoji()`
         *
         * @return The Emoji instance
         */
        get() = reaction.emoji

    /**
     * Retrieves the [User] who owns the reaction.
     * <br></br>If a user is known, this will return [.getUser].
     *
     * @return [RestAction] - Type: [User]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveUser(): RestAction<User> {
        val user = issuer
        return user?.let { CompletedRestAction(jda, it) } ?: jda.retrieveUserById(
            userIdLong
        )
    }

    /**
     * Retrieves the [Member] who owns the reaction.
     * <br></br>If a member is known, this will return [.getMember].
     *
     *
     * Note that banning a member will also fire [MessageReactionRemoveEvent] and no member will be available
     * in those cases. An [UNKNOWN_MEMBER][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MEMBER] error response
     * should be the failure result.
     *
     * @throws IllegalStateException
     * If this event is not from a guild
     *
     * @return [RestAction] - Type: [Member]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMember(): RestAction<Member> {
        if (member != null) return CompletedRestAction(jda, member)
        check(getChannel().type!!.isGuild) { "Cannot retrieve member for a private reaction not from a guild" }
        return getGuild().retrieveMemberById(userIdLong)
    }

    /**
     * Retrieves the message for this reaction event.
     * <br></br>Simple shortcut for `getChannel().retrieveMessageById(getMessageId())`.
     *
     *
     * The [Message.getMember()][Message.getMember] method will always return null for the resulting message.
     * To retrieve the member you can use `getGuild().retrieveMember(message.getAuthor())`.
     *
     * @return [RestAction] - Type: [Message]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMessage(): RestAction<Message?>? {
        return getChannel().retrieveMessageById(getMessageId())
    }
}
