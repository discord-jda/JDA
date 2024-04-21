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
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.AutoCompleteQuery
import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction
import javax.annotation.Nonnull

/**
 * Indicates that a user is typing in an [option][net.dv8tion.jda.api.interactions.commands.build.OptionData] which
 * supports [auto-complete][net.dv8tion.jda.api.interactions.commands.build.OptionData.setAutoComplete].
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 *
 * @see CommandAutoCompleteInteraction
 *
 * @see IAutoCompleteCallback
 */
class CommandAutoCompleteInteractionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val interaction: CommandAutoCompleteInteraction
) : GenericAutoCompleteInteractionEvent(api, responseNumber, interaction), CommandAutoCompleteInteraction {
    @Nonnull
    override fun getInteraction(): CommandAutoCompleteInteraction? {
        return interaction
    }

    @Nonnull
    override fun getFocusedOption(): AutoCompleteQuery {
        return interaction.focusedOption
    }

    @Nonnull
    override fun getCommandType(): Command.Type {
        return interaction.commandType
    }

    @Nonnull
    override fun getName(): String {
        return interaction.name
    }

    override fun getSubcommandName(): String? {
        return interaction.subcommandName
    }

    override fun getSubcommandGroup(): String? {
        return interaction.subcommandGroup
    }

    override fun getCommandIdLong(): Long {
        return interaction.commandIdLong
    }

    override fun isGuildCommand(): Boolean {
        return interaction.isGuildCommand
    }

    @Nonnull
    override fun getOptions(): List<OptionMapping> {
        return interaction.options
    }

    @Nonnull
    override fun replyChoices(@Nonnull choices: Collection<Command.Choice>): AutoCompleteCallbackAction {
        return interaction.replyChoices(choices)
    }

    @Nonnull
    override fun getChannel(): MessageChannelUnion {
        return interaction.getChannel()
    }
}
