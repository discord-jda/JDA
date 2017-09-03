/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.message.guild.react;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

public class GenericGuildMessageReactionEvent extends GenericGuildMessageEvent
{
    protected final User issuer;
    protected final MessageReaction reaction;

    public GenericGuildMessageReactionEvent(JDA api, long responseNumber, User user, MessageReaction reaction)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), (TextChannel) reaction.getChannel());
        this.issuer = user;
        this.reaction = reaction;
    }

    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    public User getUser()
    {
        return issuer;
    }

    public Member getMember()
    {
        return getGuild().getMember(getUser());
    }

    public MessageReaction getReaction()
    {
        return reaction;
    }

    public MessageReaction.ReactionEmote getReactionEmote()
    {
        return reaction.getEmote();
    }
}
