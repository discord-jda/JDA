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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.MessageCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;

public class MessageCommandInteractionImpl extends CommandInteractionImpl implements MessageCommandInteraction
{
    private final long messageId;
    private final Message message;

    public MessageCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);

        DataObject commandData = data.getObject("data");
        DataObject resolved = commandData.getObject("resolved");

        this.messageId = commandData.getUnsignedLong("target_id");

        this.message = jda.getEntityBuilder().createMessage(resolved.getObject("messages").getObject(Long.toUnsignedString(messageId)), true);
    }

    @Override
    public long getInteractedIdLong()
    {
        return messageId;
    }

    @NotNull
    @Override
    public Message getInteractedMessage()
    {
        return message;
    }
}
