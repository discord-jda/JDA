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

package net.dv8tion.jda.api.events.message.poll;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Indicates that a poll vote was added/removed.
 * <br>Every MessagePollVoteEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect both remove and add events.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGE_POLLS GUILD_MESSAGE_POLLS} to work in guild text channels</li>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGE_POLLS DIRECT_MESSAGE_POLLS} to work in private channels</li>
 * </ul>
 */
public class GenericMessagePollVoteEvent extends GenericMessageEvent
{
    protected final long userId;
    protected final long messageId;
    protected final long answerId;

    public GenericMessagePollVoteEvent(@Nonnull MessageChannel channel, long responseNumber, long messageId, long userId, long answerId)
    {
        super(channel.getJDA(), responseNumber, messageId, channel);
        this.userId = userId;
        this.messageId = messageId;
        this.answerId = answerId;
    }

    /**
     * The id of the voting user.
     *
     * @return The user id
     */
    @Nonnull
    public String getUserId()
    {
        return Long.toUnsignedString(userId);
    }

    /**
     * The id for the voting user.
     *
     * @return The user id
     */
    public long getUserIdLong()
    {
        return userId;
    }

    /**
     * The id of the answer, usually the ordinal position.
     * <br>The first answer options is usually 1.
     *
     * @return The answer id
     */
    public long getAnswerId()
    {
        return answerId;
    }

    /**
     * Retrieves the voting {@link User}.
     *
     * @return {@link RestAction} - Type: {@link User}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<User> retrieveUser()
    {
        return getJDA().retrieveUserById(getUserIdLong());
    }

    /**
     * Retrieves the voting {@link Member}.
     *
     * <p>Note that banning a member will also fire {@link MessagePollVoteRemoveEvent} and no member will be available
     * in those cases. An {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER} error response
     * should be the failure result.
     *
     * @throws IllegalStateException
     *         If this event is not from a guild
     *
     * @return {@link RestAction} - Type: {@link Member}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Member> retrieveMember()
    {
        if (!getChannel().getType().isGuild())
            throw new IllegalStateException("Cannot retrieve member for a vote that happened outside of a guild");
        return getGuild().retrieveMemberById(getUserIdLong());
    }

    /**
     * Retrieves the message for this event.
     * <br>Simple shortcut for {@code getChannel().retrieveMessageById(getMessageId())}.
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     * To retrieve the member you can use {@code getGuild().retrieveMember(message.getAuthor())}.
     *
     * @return {@link RestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Message> retrieveMessage()
    {
        return getChannel().retrieveMessageById(getMessageId());
    }
}
