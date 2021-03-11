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

package net.dv8tion.jda.api.events.message.guild.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction} was added or removed in a TextChannel.
 *
 * <p>Can be used to detect when a reaction is added or removed in a TextChannel.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGE_REACTIONS GUILD_MESSAGE_REACTIONS} intent to be enabled.
 */
public abstract class GenericGuildMessageReactionEvent extends GenericGuildMessageEvent
{
    protected final long userId;
    protected final Member issuer;
    protected final MessageReaction reaction;

    public GenericGuildMessageReactionEvent(@Nonnull JDA api, long responseNumber, @Nullable Member user, @Nonnull MessageReaction reaction, long userId)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), (TextChannel) reaction.getChannel());
        this.issuer = user;
        this.reaction = reaction;
        this.userId = userId;
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
     * The id for the user who owns the reaction.
     *
     * @return The user id
     */
    public long getUserIdLong()
    {
        return userId;
    }

    /**
     * The reacting {@link net.dv8tion.jda.api.entities.User User}
     * <br>This might be missing if the user was not previously cached or the member was removed.
     * Use {@link #retrieveUser()} to load the user.
     *
     * @return The reacting user or null if this information is missing
     *
     * @see    #getUserIdLong()
     */
    @Nullable
    public User getUser()
    {
        return issuer == null ? getJDA().getUserById(userId) : issuer.getUser();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} instance for the reacting user
     * <br>This might be missing if the user was not previously cached or the member was removed.
     * Use {@link #retrieveMember()} to load the member.
     *
     * @return The member instance for the reacting user or null if this information is missing
     */
    @Nullable
    public Member getMember()
    {
        return issuer;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction}
     *
     * @return The message reaction
     */
    @Nonnull
    public MessageReaction getReaction()
    {
        return reaction;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote ReactionEmote}
     * <br>Shortcut for {@code getReaction().getReactionEmote()}
     *
     * @return The reaction emote
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
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<User> retrieveUser()
    {
        if (issuer != null)
            return new CompletedRestAction<>(getJDA(), issuer.getUser());
        return getJDA().retrieveUserById(getUserIdLong());
    }

    /**
     * Retrieves the {@link Member} who owns the reaction.
     * <br>If a member is known, this will return {@link #getMember()}.
     *
     * <p>Note that banning a member will also fire {@link GuildMessageReactionRemoveEvent} and no member will be available
     * in those cases. An {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER} error response
     * should be the failure result.
     *
     * @return {@link RestAction} - Type: {@link Member}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Member> retrieveMember()
    {
        if (issuer != null)
            return new CompletedRestAction<>(getJDA(), issuer);
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
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Message> retrieveMessage()
    {
        return getChannel().retrieveMessageById(getMessageId());
    }
}
