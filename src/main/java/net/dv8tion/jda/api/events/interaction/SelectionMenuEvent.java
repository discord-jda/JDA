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
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenuInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SelectionMenuEvent extends GenericInteractionCreateEvent implements SelectionMenuInteraction
{
    private final SelectionMenuInteraction menuInteraction;

    public SelectionMenuEvent(@Nonnull JDA api, long responseNumber, @Nonnull SelectionMenuInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.menuInteraction = interaction;
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return menuInteraction.getChannel();
    }

    @Nonnull
    @Override
    public String getComponentId()
    {
        return menuInteraction.getComponentId();
    }

    @Nullable
    @Override
    public Message getMessage()
    {
        return menuInteraction.getMessage();
    }

    @Override
    public long getMessageIdLong()
    {
        return menuInteraction.getMessageIdLong();
    }

    @Nonnull
    @Override
    public Component.Type getComponentType()
    {
        return menuInteraction.getComponentType();
    }

    @Nonnull
    @Override
    public UpdateInteractionAction deferEdit()
    {
        return menuInteraction.deferEdit();
    }

    @Nullable
    @Override
    public SelectionMenu getComponent()
    {
        return menuInteraction.getComponent();
    }

    @Nonnull
    @Override
    public List<String> getValues()
    {
        return menuInteraction.getValues();
    }
}
