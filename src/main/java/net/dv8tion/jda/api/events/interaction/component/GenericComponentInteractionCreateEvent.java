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

package net.dv8tion.jda.api.events.interaction.component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link ComponentInteraction} was created in a channel.
 * <br>Every component interaction event is derived from this event.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class GenericComponentInteractionCreateEvent extends GenericInteractionCreateEvent implements ComponentInteraction
{
    private final ComponentInteraction interaction;

    public GenericComponentInteractionCreateEvent(@Nonnull JDA api, long responseNumber, @Nonnull ComponentInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public ComponentInteraction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return interaction.getChannel();
    }

    @Nonnull
    @Override
    public String getComponentId()
    {
        return interaction.getComponentId();
    }

    @Nonnull
    @Override
    public ActionComponent getComponent()
    {
        return interaction.getComponent();
    }

    @Nonnull
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

    @Nonnull
    @Override
    public MessageEditCallbackAction deferEdit()
    {
        return interaction.deferEdit();
    }

    @Nonnull
    @Override
    public InteractionHook getHook()
    {
        return interaction.getHook();
    }

    @Nonnull
    @Override
    public ReplyCallbackAction deferReply()
    {
        return interaction.deferReply();
    }
}
