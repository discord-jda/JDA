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

package net.dv8tion.jda.api.events.interaction.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import javax.annotation.Nonnull;

/**
 * Indicates that a slash command was used in a {@link MessageChannel}.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see SlashCommandInteraction
 */
public class SlashCommandInteractionEvent extends GenericCommandInteractionEvent implements SlashCommandInteraction
{
    private final SlashCommandInteraction interaction;

    public SlashCommandInteractionEvent(@Nonnull JDA api, long responseNumber, @Nonnull SlashCommandInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public SlashCommandInteraction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public MessageChannelUnion getChannel()
    {
        return (MessageChannelUnion) interaction.getChannel();
    }
}
