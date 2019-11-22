/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction} was added or removed in a TextChannel.
 *
 * <p>Can be used to detect when a reaction is added or removed in a TextChannel.
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
     * <br>This might be missing if the user was not previously cached or the member was removed.
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
}
