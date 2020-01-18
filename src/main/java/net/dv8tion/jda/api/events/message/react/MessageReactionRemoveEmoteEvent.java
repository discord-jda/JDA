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
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import javax.annotation.Nonnull;

public class MessageReactionRemoveEmoteEvent extends GenericMessageEvent
{
    private final MessageReaction reaction;

    public MessageReactionRemoveEmoteEvent(@Nonnull JDA api, long responseNumber, long messageId, @Nonnull MessageChannel channel, @Nonnull MessageReaction reaction)
    {
        super(api, responseNumber, messageId, channel);
        this.reaction = reaction;
    }

    @Nonnull
    public MessageReaction getReaction()
    {
        return reaction;
    }

    @Nonnull
    public MessageReaction.ReactionEmote getReactionEmote()
    {
        return reaction.getReactionEmote();
    }
}
