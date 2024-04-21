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
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import javax.annotation.Nonnull

/**
 * Indicates that a [CommandInteraction] was used.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 */
open class GenericCommandInteractionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull interaction: CommandInteraction
) : GenericInteractionCreateEvent(api, responseNumber, interaction), CommandInteraction {
    @Nonnull
    override fun getInteraction(): CommandInteraction? {
        return super.getInteraction() as CommandInteraction
    }

    @Nonnull
    override fun getCommandType(): Command.Type {
        return interaction!!.getCommandType()
    }

    @Nonnull
    override fun getName(): String {
        return interaction!!.getName()
    }

    override fun getSubcommandName(): String? {
        return interaction!!.getSubcommandName()
    }

    override fun getSubcommandGroup(): String? {
        return interaction!!.getSubcommandGroup()
    }

    override fun getCommandIdLong(): Long {
        return interaction!!.getCommandIdLong()
    }

    override fun isGuildCommand(): Boolean {
        return interaction!!.isGuildCommand()
    }

    @Nonnull
    override fun getOptions(): List<OptionMapping> {
        return interaction!!.getOptions()
    }

    @Nonnull
    override fun getHook(): InteractionHook {
        return interaction!!.getHook()
    }

    @Nonnull
    override fun deferReply(): ReplyCallbackAction {
        return interaction!!.deferReply()
    }

    @Nonnull
    override fun replyModal(@Nonnull modal: Modal): ModalCallbackAction {
        return interaction!!.replyModal(modal)
    }

    @Nonnull
    override fun replyWithPremiumRequired(): PremiumRequiredCallbackAction {
        return interaction!!.replyWithPremiumRequired()
    }
}
