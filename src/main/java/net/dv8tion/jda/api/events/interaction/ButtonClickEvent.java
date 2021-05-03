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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.Component;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.interactions.button.ButtonInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ButtonClickEvent extends GenericInteractionCreateEvent implements ButtonInteraction
{
    private final ButtonInteraction interaction;

    public ButtonClickEvent(@Nonnull JDA api, long responseNumber, @Nonnull ButtonInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
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
    public UpdateAction deferEdit()
    {
        return interaction.deferEdit();
    }

    @Nonnull
    @Override
    public ButtonInteraction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public String getComponentId()
    {
        return interaction.getComponentId();
    }

    @Nullable
    @Override
    public Message getMessage()
    {
        return interaction.getMessage();
    }

    @Override
    public long getMessageIdLong()
    {
        return interaction.getMessageIdLong();
    }

    @Nonnull
    @Override
    public Component.Type getComponentType()
    {
        return interaction.getComponentType();
    }

    @Nullable
    @Override
    public Button getButton()
    {
        return interaction.getButton();
    }
}
