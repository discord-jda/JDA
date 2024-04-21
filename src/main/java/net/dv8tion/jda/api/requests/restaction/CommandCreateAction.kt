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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Specialized [RestAction] used to create or update commands.
 * <br></br>If a command with the specified name already exists, it will be replaced!
 *
 *
 * This operation is **not** idempotent!
 * Commands will persist between restarts of your bot, you only have to create a command once.
 */
interface CommandCreateAction : RestAction<Command?>, SlashCommandData {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): CommandCreateAction?
    @Nonnull
    override fun addCheck(@Nonnull checks: BooleanSupplier): CommandCreateAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun deadline(timestamp: Long): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setLocalizationFunction(@Nonnull localizationFunction: LocalizationFunction?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setName(@Nonnull name: String?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setDescription(@Nonnull description: String?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setDescriptionLocalization(
        @Nonnull locale: DiscordLocale?,
        @Nonnull description: String?
    ): CommandCreateAction?

    @Nonnull
    @CheckReturnValue
    override fun setDescriptionLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun addOptions(@Nonnull vararg options: OptionData): CommandCreateAction?

    @Nonnull
    @CheckReturnValue
    override fun addOptions(@Nonnull options: Collection<OptionData?>): CommandCreateAction? {
        return super<SlashCommandData>.addOptions(options) as CommandCreateAction?
    }

    @Nonnull
    @CheckReturnValue
    override fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?,
        required: Boolean,
        autoComplete: Boolean
    ): CommandCreateAction? {
        return super<SlashCommandData>.addOption(
            type,
            name,
            description,
            required,
            autoComplete
        ) as CommandCreateAction?
    }

    @Nonnull
    @CheckReturnValue
    override fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?,
        required: Boolean
    ): CommandCreateAction? {
        return super<SlashCommandData>.addOption(type, name, description, required) as CommandCreateAction?
    }

    @Nonnull
    @CheckReturnValue
    override fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?
    ): CommandCreateAction? {
        return super<SlashCommandData>.addOption(type, name, description, false) as CommandCreateAction?
    }

    @Nonnull
    @CheckReturnValue
    override fun addSubcommands(@Nonnull vararg subcommands: SubcommandData): CommandCreateAction?

    @Nonnull
    @CheckReturnValue
    override fun addSubcommands(@Nonnull subcommands: Collection<SubcommandData?>): CommandCreateAction? {
        return super<SlashCommandData>.addSubcommands(subcommands) as CommandCreateAction?
    }

    @Nonnull
    @CheckReturnValue
    override fun addSubcommandGroups(@Nonnull vararg groups: SubcommandGroupData): CommandCreateAction?

    @Nonnull
    @CheckReturnValue
    override fun addSubcommandGroups(@Nonnull groups: Collection<SubcommandGroupData?>): CommandCreateAction? {
        return super<SlashCommandData>.addSubcommandGroups(groups) as CommandCreateAction?
    }

    @Nonnull
    @CheckReturnValue
    override fun setDefaultPermissions(@Nonnull permission: DefaultMemberPermissions?): CommandCreateAction?
    @Nonnull
    @CheckReturnValue
    override fun setGuildOnly(guildOnly: Boolean): CommandCreateAction?
    @Nonnull
    override fun setNSFW(nsfw: Boolean): CommandCreateAction?
}
