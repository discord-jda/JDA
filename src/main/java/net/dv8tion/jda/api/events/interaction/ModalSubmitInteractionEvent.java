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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.internal.interactions.ModalInteractionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.List;

public class ModalSubmitInteractionEvent extends GenericInteractionCreateEvent
{
    private final ModalInteractionImpl interaction;

    public ModalSubmitInteractionEvent(@NotNull JDA api, long responseNumber, @NotNull ModalInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @NotNull
    @Override
    public ModalInteractionImpl getInteraction()
    {
        return interaction;
    }

    @NotNull
    public String getModalId()
    {
        return interaction.getModalId();
    }

    @NotNull
    public List<ActionRow> getComponents()
    {
        return interaction.getComponents();
    }

    @Nullable
    public TextInput getInputField(String id)
    {
        for (ActionRow row : getComponents())
        {
            for (ItemComponent component : row)
            {
                if (component instanceof TextInput)
                {
                    TextInput textInput = (TextInput) component;
                    if (textInput.getId().equals(id))
                        return textInput;
                }
            }
        }
        return null;
    }

    @NotNull
    public InteractionHook getHook()
    {
        return interaction.getHook();
    }

    @NotNull
    @CheckReturnValue
    public ReplyCallbackAction deferReply()
    {
        return interaction.deferReply();
    }

    @NotNull
    @CheckReturnValue
    public ReplyCallbackAction deferReply(boolean ephemeral)
    {
        return interaction.deferReply(ephemeral);
    }

    @NotNull
    @CheckReturnValue
    public ReplyCallbackAction reply(String content)
    {
        return interaction.reply(content);
    }
}
