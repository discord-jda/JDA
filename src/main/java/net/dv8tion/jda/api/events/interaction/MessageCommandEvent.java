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

package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.MessageCommandInteraction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.MessageCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

public class MessageCommandEvent extends GenericCommandEvent implements MessageCommandInteraction
{
    private final MessageCommandInteraction commandInteraction;
    public MessageCommandEvent(JDAImpl api, long responseNumber, MessageCommandInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.commandInteraction = interaction;
    }

    @Override
    public long getCommandIdLong()
    {
        return commandInteraction.getCommandIdLong();
    }

    @Override
    public long getInteractedIdLong()
    {
        return commandInteraction.getInteractedIdLong();
    }

    @NotNull
    @Override
    public Message getInteractedMessage()
    {
        return commandInteraction.getInteractedMessage();
    }
}
