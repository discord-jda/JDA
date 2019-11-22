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

package net.dv8tion.jda.api.events.message.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a MessageReaction was added/removed.
 * <br>Every MessageReactionEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect both remove and add events.
 */
public class GenericMessageReactionEvent extends GenericMessageEvent
{
    protected final long userId;
    protected User issuer;
    protected Member member;
    protected MessageReaction reaction;

    public GenericMessageReactionEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user,
                                       @Nullable Member member, @Nonnull MessageReaction reaction, long userId)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), reaction.getChannel());
        this.userId = userId;
        this.issuer = user;
        this.member = member;
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
     * @return The reacting user or null if this information is missing
     */
    @Nullable
    public User getUser()
    {
        return issuer;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} instance for the reacting user
     * or {@code null} if the reaction was from a user not in this guild.
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
}
