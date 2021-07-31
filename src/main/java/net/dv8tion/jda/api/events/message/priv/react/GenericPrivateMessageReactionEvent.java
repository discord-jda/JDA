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

package net.dv8tion.jda.api.events.message.priv.react;

import javax.annotation.CheckReturnValue;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.priv.GenericPrivateMessageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction} was added or removed.
 *
 * <p>Can be used to detect when a message reaction is added or removed from a message.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGE_REACTIONS DIRECT_MESSAGE_REACTIONS} intent to be enabled.
 */
public class GenericPrivateMessageReactionEvent extends GenericPrivateMessageEvent
{
    protected final long userId;
    protected final MessageReaction reaction;

    public GenericPrivateMessageReactionEvent(@Nonnull JDA api, long responseNumber, @Nonnull MessageReaction reaction, long userId)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), (PrivateChannel) reaction.getChannel());
        this.userId = userId;
        this.reaction = reaction;
    }

    /**
     * The id for the user who added/removed their reaction.
     *
     * @return The user id
     */
    @Nonnull
    public String getUserId()
    {
        return Long.toUnsignedString(userId);
    }

    /**
     * The id for the user who added/removed their reaction.
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
     *
     * @return The reacting user
     *
     * @see #retrieveUser()
     */
    @Nullable
    public User getUser()
    {
        return userId == getJDA().getSelfUser().getIdLong()
                ? getJDA().getSelfUser()
                : getChannel().getUser();
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
     * @return The message reaction emote
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
     * @since  4.3.1
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
     * Retrieves the message for this reaction event.
     * <br>Simple shortcut for {@code getChannel().retrieveMessageById(getMessageId())}.
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     *
     * @return {@link RestAction} - Type: {@link Message}
     *
     * @since  4.3.1
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Message> retrieveMessage()
    {
        return getChannel().retrieveMessageById(getMessageId());
    }
}
