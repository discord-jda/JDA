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
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.UpdateInteractionActionImpl;

import javax.annotation.Nonnull;

public class ButtonInteractionImpl extends InteractionImpl implements ButtonInteraction
{
    private final String customId;
    private final Message message;
    private final long messageId;
    private final Button button;

    public ButtonInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        customId = data.getObject("data").getString("custom_id");

        // message might be just id and flags for ephemeral messages in which case our "message" is null
        DataObject messageJson = data.getObject("message");
        messageId = messageJson.getUnsignedLong("id");

        message = messageJson.isNull("type") ? null : jda.getEntityBuilder().createMessage(messageJson);
        button = message != null ? this.message.getButtonById(customId) : null;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannel getChannel()
    {
        return (MessageChannel) super.getChannel();
    }

    @Nonnull
    @Override
    public UpdateInteractionAction deferEdit()
    {
        return new UpdateInteractionActionImpl(this.hook);
    }

    @Nonnull
    @Override
    public String getComponentId()
    {
        return customId;
    }

    @Override
    public Message getMessage()
    {
        return message;
    }

    @Override
    public long getMessageIdLong()
    {
        return messageId;
    }

    @Nonnull
    @Override
    public Component.Type getComponentType()
    {
        return Component.Type.BUTTON;
    }

    @Nonnull
    @Override
    public Button getButton()
    {
        return button;
    }
}
