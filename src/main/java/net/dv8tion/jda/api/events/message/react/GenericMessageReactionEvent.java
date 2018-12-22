/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.events.message.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

/**
 * Indicates that a MessageReaction was added/removed.
 * <br>Every MessageReactionEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect both remove and add events.
 */
public class GenericMessageReactionEvent extends GenericMessageEvent
{
    protected User issuer;
    protected MessageReaction reaction;

    public GenericMessageReactionEvent(JDA api, long responseNumber, User user, MessageReaction reaction)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), reaction.getChannel());
        this.issuer = user;
        this.reaction = reaction;
    }

    /**
     * The reacting {@link net.dv8tion.jda.api.entities.User User}
     *
     * @return The reacting user
     */
    public User getUser()
    {
        return issuer;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} instance for the reacting user
     * or {@code null} if the reaction was not in a guild.
     *
     * @return Member of the reacting user or null
     */
    public Member getMember()
    {
        Guild guild = getGuild();
        return guild != null ? guild.getMember(getUser()) : null;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction}
     *
     * @return The MessageReaction
     */
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
    public MessageReaction.ReactionEmote getReactionEmote()
    {
        return reaction.getReactionEmote();
    }
}
