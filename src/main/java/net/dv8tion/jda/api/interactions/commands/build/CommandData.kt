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
package net.dv8tion.jda.api.interactions.commands.build

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils
import javax.annotation.Nonnull

/**
 * Builder for Application Commands.
 * <br></br>Use the factory methods provided by [Commands] to create instances of this interface.
 *
 * @see Commands
 */
interface CommandData : SerializableData {
    /**
     * Sets the [LocalizationFunction] for this command
     * <br></br>This enables you to have the entirety of this command to be localized.
     *
     * @param  localizationFunction
     * The localization function
     *
     * @throws IllegalArgumentException
     * If the localization function is null
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun setLocalizationFunction(@Nonnull localizationFunction: LocalizationFunction?): CommandData?

    /**
     * Configure the command name.
     *
     * @param  name
     * The name, 1-{@value #MAX_NAME_LENGTH} characters (lowercase and alphanumeric for [Command.Type.SLASH])
     *
     * @throws IllegalArgumentException
     * If the name is not between 1-{@value #MAX_NAME_LENGTH} characters long, or not lowercase and alphanumeric for slash commands
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun setName(@Nonnull name: String?): CommandData?

    /**
     * Sets a [language-specific][DiscordLocale] localization of this command's name.
     *
     * @param  locale
     * The locale to associate the translated name with
     *
     * @param  name
     * The translated name to put
     *
     * @throws IllegalArgumentException
     *
     *  * If the locale is null
     *  * If the name is null
     *  * If the locale is [DiscordLocale.UNKNOWN]
     *  * If the name does not pass the corresponding [name check][.setName]
     *
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): CommandData?

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this command's name.
     *
     * @param  map
     * The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *
     *  * If the map is null
     *  * If the map contains an [DiscordLocale.UNKNOWN] key
     *  * If the map contains a name which does not pass the corresponding [name check][.setName]
     *
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): CommandData?

    /**
     * Sets the [Permissions][net.dv8tion.jda.api.Permission] that a user must have in a specific channel to be able to use this command.
     * <br></br>By default, everyone can use this command ([DefaultMemberPermissions.ENABLED]). Additionally, a command can be disabled for everyone but admins via [DefaultMemberPermissions.DISABLED].
     *
     * These configurations can be overwritten by moderators in each guild. See [Command.retrievePrivileges] to get moderator defined overrides.
     *
     * @param  permission
     * [DefaultMemberPermissions] representing the default permissions of this command.
     *
     * @return The builder instance, for chaining
     *
     * @see DefaultMemberPermissions.ENABLED
     *
     * @see DefaultMemberPermissions.DISABLED
     */
    @Nonnull
    fun setDefaultPermissions(@Nonnull permission: DefaultMemberPermissions?): CommandData?

    /**
     * Sets whether this command is only usable in a guild (Default: false).
     * <br></br>This only has an effect if this command is registered globally.
     *
     * @param  guildOnly
     * Whether to restrict this command to guilds
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun setGuildOnly(guildOnly: Boolean): CommandData?

    /**
     * Sets whether this command should only be usable in NSFW (age-restricted) channels.
     * <br></br>Default: false
     *
     *
     * Note: Age-restricted commands will not show up in direct messages by default unless the user enables them in their settings.
     *
     * @param  nsfw
     * True, to make this command nsfw
     *
     * @return The builder instance, for chaining
     *
     * @see [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007)
     */
    @Nonnull
    fun setNSFW(nsfw: Boolean): CommandData?

    @JvmField
    @get:Nonnull
    val name: String?

    @JvmField
    @get:Nonnull
    val nameLocalizations: LocalizationMap?

    @JvmField
    @get:Nonnull
    val type: Command.Type?

    @get:Nonnull
    val defaultPermissions: DefaultMemberPermissions?

    /**
     * Whether the command can only be used inside a guild.
     * <br></br>Always true for guild commands.
     *
     * @return True, if this command is restricted to guilds.
     */
    val isGuildOnly: Boolean

    /**
     * Whether this command should only be usable in NSFW (age-restricted) channels
     *
     * @return True, if this command is restricted to NSFW channels
     *
     * @see [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007)
     */
    val isNSFW: Boolean

    companion object {
        /**
         * Converts the provided [Command] into a CommandData instance.
         *
         * @param  command
         * The command to convert
         *
         * @throws IllegalArgumentException
         * If null is provided or the command has illegal configuration
         *
         * @return An instance of CommandData
         *
         * @see SlashCommandData.fromCommand
         */
        @Nonnull
        fun fromCommand(@Nonnull command: Command): CommandData? {
            Checks.notNull(command, "Command")
            if (command.type != Command.Type.SLASH) {
                val data = CommandDataImpl(command.type, command.name)
                return data.setDefaultPermissions(command.defaultPermissions)
                    .setGuildOnly(command.isGuildOnly)
                    .setNSFW(command.isNSFW)
                    .setNameLocalizations(command.nameLocalizations.toMap())
                    .setDescriptionLocalizations(command.descriptionLocalizations.toMap())
            }
            return SlashCommandData.Companion.fromCommand(command)
        }

        /**
         * Parses the provided serialization back into an CommandData instance.
         * <br></br>This is the reverse function for [CommandData.toData].
         *
         * @param  object
         * The serialized [DataObject] representing the command
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the serialized object is missing required fields
         * @throws IllegalArgumentException
         * If any of the values are failing the respective checks such as length
         *
         * @return The parsed CommandData instance, which can be further configured through setters
         *
         * @see SlashCommandData.fromData
         * @see Commands.fromList
         */
        @JvmStatic
        @Nonnull
        fun fromData(@Nonnull `object`: DataObject): CommandData? {
            Checks.notNull(`object`, "DataObject")
            val name = `object`.getString("name")
            val commandType = Command.Type.fromId(`object`.getInt("type", 1))
            if (commandType != Command.Type.SLASH) {
                val data = CommandDataImpl(commandType, name)
                if (!`object`.isNull("default_member_permissions")) {
                    val defaultPermissions = `object`.getLong("default_member_permissions")
                    data.setDefaultPermissions(
                        if (defaultPermissions == 0L) DefaultMemberPermissions.DISABLED else DefaultMemberPermissions.enabledFor(
                            defaultPermissions
                        )
                    )
                }
                data.setGuildOnly(!`object`.getBoolean("dm_permission", true))
                data.setNSFW(`object`.getBoolean("nsfw"))
                data.setNameLocalizations(LocalizationUtils.mapFromProperty(`object`, "name_localizations"))
                data.setDescriptionLocalizations(
                    LocalizationUtils.mapFromProperty(
                        `object`,
                        "description_localizations"
                    )
                )
                return data
            }
            return SlashCommandData.Companion.fromData(`object`)
        }

        /**
         * The maximum length the name of a command can be. ({@value})
         */
        const val MAX_NAME_LENGTH = 32

        /**
         * The maximum length the description of a command can be. ({@value})
         */
        const val MAX_DESCRIPTION_LENGTH = 100

        /**
         * The maximum amount of options/subcommands/groups that can be added to a command or subcommand. ({@value})
         */
        const val MAX_OPTIONS = 25
    }
}
