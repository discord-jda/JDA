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
package net.dv8tion.jda.api.events.interaction.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import javax.annotation.Nonnull

/**
 * Indicates that a slash command was used in a [MessageChannel].
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 *
 * @see SlashCommandInteraction
 */
class SlashCommandInteractionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val interaction: SlashCommandInteraction
) : GenericCommandInteractionEvent(api, responseNumber, interaction), SlashCommandInteraction {
    @Nonnull
    override fun getInteraction(): SlashCommandInteraction? {
        return interaction
    }

    @Nonnull
    override fun getChannel(): MessageChannelUnion {
        return interaction.getChannel()
    }
}
