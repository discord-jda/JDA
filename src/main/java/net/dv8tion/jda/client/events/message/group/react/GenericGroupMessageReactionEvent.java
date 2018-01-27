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

package net.dv8tion.jda.client.events.message.group.react;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.events.message.group.GenericGroupMessageEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

public class GenericGroupMessageReactionEvent extends GenericGroupMessageEvent
{
    protected final User issuer;
    protected final MessageReaction reaction;

    public GenericGroupMessageReactionEvent(JDA api, long responseNumber, User user, MessageReaction reaction)
    {
        super(api, responseNumber, reaction.getMessageIdLong(), (Group) reaction.getChannel());
        this.issuer = user;
        this.reaction = reaction;
    }

    public User getUser()
    {
        return issuer;
    }

    public MessageReaction getReaction()
    {
        return reaction;
    }

    public MessageReaction.ReactionEmote getReactionEmote()
    {
        return reaction.getReactionEmote();
    }
}
