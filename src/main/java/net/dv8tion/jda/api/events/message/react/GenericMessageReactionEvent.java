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

package net.dv8tion.jda.api.events.message.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a MessageReaction was added/removed.
 * <br>Every MessageReactionEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect both remove and add events.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGE_REACTIONS GUILD_MESSAGE_REACTIONS} to work in guild text channels</li>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGE_REACTIONS DIRECT_MESSAGE_REACTIONS} to work in private channels</li>
 * </ul>
 */
public class GenericMessageReactionEvent extends GenericMessageEvent
{
    protected final long userId;
    protected User issuer;
    protected Member member;
    protected MessageReaction reaction;

    public GenericMessageReactionEvent(@Nonnull JDA api, long responseNumber, @Nullable User user,
                                       @Nullable Member member, @Nonnull MessageReaction reaction, long userId)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), reaction.getChannel());
        this.userId = userId;
        this.issuer = user;
        this.member = member;
        this.reaction = reaction;
    }

    /**
     * The id for the user who owns the reaction.
     *
     * @return The user id
     */
    @Nonnull
    public String getUserId()
    {
        return Long.toUnsignedString(userId);
    }

    /**
     * The id for the user who owns reaction.
     *
     * @return The user id
     */
    public long getUserIdLong()
    {
        return userId;
    }

    /**
     * The reacting {@link net.dv8tion.jda.api.entities.User User}
     * <br>This might be missing if the user was not cached.
     * Use {@link #retrieveUser()} to load the user.
     *
     * @return The reacting user or null if this information is missing
     */
    @Nullable
    public User getUser()
    {
        return issuer == null && isFromType(ChannelType.PRIVATE)
                ? getPrivateChannel().getUser() // this can't be the self user because then issuer would be nonnull
                : issuer;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} instance for the reacting user
     * or {@code null} if the reaction was from a user not in this guild.
     * <br>This will also be {@code null} if the member is not available in the cache.
     * Use {@link #retrieveMember()} to load the member.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.TextChannel}.
     *
     * @return Member of the reacting user or null if they are no longer member of this guild
     *
     * @see    #isFromGuild()
     * @see    #getChannelType()
     */
    @Nullable
    public Member getMember()
    {
        return member;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction}
     *
     * @return The MessageReaction
     */
    @Nonnull
    public MessageReaction getReaction()
    {
        return reaction;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote ReactionEmote}
     * of the reaction, shortcut for {@code getReaction().getReactionEmote()}
     *
     * @return The ReactionEmote instance
     */
    @Nonnull
    public MessageReaction.ReactionEmote getReactionEmote()
    {
        return reaction.getReactionEmote();
    }

    /**
     * Retrieves the {@link User} who owns the reaction.
     * <br>If a user is known, this will return {@link #getUser()}.
     *
     * @return {@link RestAction} - Type: {@link User}
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<User> retrieveUser()
    {
        User user = getUser();
        if (user != null)
            return new CompletedRestAction<>(getJDA(), user);
        return getJDA().retrieveUserById(getUserIdLong());
    }

    /**
     * Retrieves the {@link Member} who owns the reaction.
     * <br>If a member is known, this will return {@link #getMember()}.
     *
     * <p>Note that banning a member will also fire {@link MessageReactionRemoveEvent} and no member will be available
     * in those cases. An {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER} error response
     * should be the failure result.
     *
     * @throws IllegalStateException
     *         If this event is not from a guild
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Member> retrieveMember()
    {
        if (member != null)
            return new CompletedRestAction<>(getJDA(), member);
        if (!getChannel().getType().isGuild())
            throw new IllegalStateException("Cannot retrieve member for a private reaction not from a guild");
        return getGuild().retrieveMemberById(getUserIdLong());
    }

    /**
     * Retrieves the message for this reaction event.
     * <br>Simple shortcut for {@code getChannel().retrieveMessageById(getMessageId())}.
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     * To retrieve the member you can use {@code getGuild().retrieveMember(message.getAuthor())}.
     *
     * @return {@link RestAction} - Type: {@link Message}
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Message> retrieveMessage()
    {
        return getChannel().retrieveMessageById(getMessageId());
    }
}
