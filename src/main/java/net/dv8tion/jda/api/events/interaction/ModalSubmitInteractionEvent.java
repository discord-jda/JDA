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
import net.dv8tion.jda.api.interactions.ModalInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.internal.interactions.ModalInteractionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * Indicates that a {@link net.dv8tion.jda.api.interactions.components.text.Modal Modal} was submitted.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see net.dv8tion.jda.api.interactions.ModalInteraction
 */
public class ModalSubmitInteractionEvent extends GenericInteractionCreateEvent implements ModalInteraction
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

    @NotNull
    @Override
    public String getModalId()
    {
        return interaction.getModalId();
    }

    @NotNull
    @Override
    public List<ActionRow> getComponents()
    {
        return interaction.getComponents();
    }
}
