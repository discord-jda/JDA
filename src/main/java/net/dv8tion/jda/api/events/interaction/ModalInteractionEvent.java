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
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.LaunchActivityCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Indicates that a {@link Modal} was submitted.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see    ModalInteraction
 */
public class ModalInteractionEvent extends GenericInteractionCreateEvent implements ModalInteraction
{
    private final ModalInteraction interaction;

    public ModalInteractionEvent(@Nonnull JDA api, long responseNumber, @Nonnull ModalInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public ModalInteraction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public String getModalId()
    {
        return interaction.getModalId();
    }

    @Nonnull
    @Override
    public List<ModalMapping> getValues()
    {
        return interaction.getValues();
    }

    @Nullable
    @Override
    public Message getMessage()
    {
        return interaction.getMessage();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ReplyCallbackAction deferReply()
    {
        return interaction.deferReply();
    }

    @Nonnull
    @Override
    public InteractionHook getHook()
    {
        return interaction.getHook();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageEditCallbackAction deferEdit()
    {
        return interaction.deferEdit();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public LaunchActivityCallbackAction replyWithLaunchedActivity()
    {
        return interaction.replyWithLaunchedActivity();
    }

    @Nonnull
    @Override
    public MessageChannelUnion getChannel()
    {
        return interaction.getChannel();
    }
}
