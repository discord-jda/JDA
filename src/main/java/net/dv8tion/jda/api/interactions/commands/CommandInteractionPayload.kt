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
package net.dv8tion.jda.api.interactions.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.internal.utils.Checks
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Interactions which provide command data.
 * <br></br>This is an abstraction for [CommandAutoCompleteInteraction] and [CommandInteraction].
 */
interface CommandInteractionPayload : Interaction {
    @JvmField
    @get:Nonnull
    val commandType: Command.Type

    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * The subcommand name.
     * <br></br>This can be useful for abstractions.
     *
     *
     * Note that commands can have these following structures:
     *
     *  * `/name subcommandGroup subcommandName`
     *  * `/name subcommandName`
     *  * `/name`
     *
     *
     * You can use [.getFullCommandName] to simplify your checks.
     *
     * @return The subcommand name, or null if this is not a subcommand
     */
    @JvmField
    val subcommandName: String?

    /**
     * The subcommand group name.
     * <br></br>This can be useful for abstractions.
     *
     *
     * Note that commands can have these following structures:
     *
     *  * `/name subcommandGroup subcommandName`
     *  * `/name subcommandName`
     *  * `/name`
     *
     *
     * You can use [.getFullCommandName] to simplify your checks.
     *
     * @return The subcommand group name, or null if this is not a subcommand group
     */
    @JvmField
    val subcommandGroup: String?

    @get:Nonnull
    val fullCommandName: String?
        /**
         * Combination of [.getName], [.getSubcommandGroup], and [.getSubcommandName].
         * <br></br>This will format the command into a path such as `mod mute` where `mod` would be the [.getName] and `mute` the [.getSubcommandName].
         *
         *
         * Examples:
         *
         *  * `/mod ban -> "mod ban"`
         *  * `/admin config owner -> "admin config owner"`
         *  * `/ban -> "ban"`
         *
         *
         * @return The command path
         */
        get() {
            val builder = StringBuilder(name)
            if (subcommandGroup != null) builder.append(' ').append(subcommandGroup)
            if (subcommandName != null) builder.append(' ').append(subcommandName)
            return builder.toString()
        }

    @get:Nonnull
    val commandString: String?
        /**
         * Gets the display string for this command.
         * <br></br>This is similar to the string you see when clicking the interaction name in the client.
         * For non-slash command types, this simply returns [.getName] instead.
         *
         *
         * Example return for an echo command: `/say echo phrase: Say this`
         *
         * @return The display string for this command
         */
        get() {
            //Get text like the text that appears when you hover over the interaction in discord
            if (commandType != Command.Type.SLASH) return name
            val builder = StringBuilder()
            builder.append("/").append(name)
            if (subcommandGroup != null) builder.append(" ").append(subcommandGroup)
            if (subcommandName != null) builder.append(" ").append(subcommandName)
            for (o in options) {
                builder.append(" ").append(o.name).append(": ")
                when (o.type) {
                    OptionType.CHANNEL -> builder.append("#").append(o.getAsChannel().name)
                    OptionType.USER -> builder.append("@").append(o.getAsUser().name)
                    OptionType.ROLE -> builder.append("@").append(o.getAsRole().name)
                    OptionType.MENTIONABLE -> if (o.getAsMentionable() is Role) builder.append("@")
                        .append(o.getAsRole().name) else if (o.getAsMentionable() is Member) builder.append("@")
                        .append(o.getAsUser().name) else if (o.getAsMentionable() is User) builder.append("@")
                        .append(o.getAsUser().name) else builder.append("@").append(o.getAsMentionable().idLong)

                    else -> builder.append(o.asString)
                }
            }
            return builder.toString()
        }

    /**
     * The command id.
     * <br></br>This is the id generated when a command is created via [Guild.updateCommands] or similar.
     *
     *
     * It is usually preferred to discriminate commands by the [command names][.getName] instead.
     *
     * @return The command id
     */
    @JvmField
    val commandIdLong: Long

    @get:Nonnull
    val commandId: String?
        /**
         * The command id
         * <br></br>This is the id generated when a command is created via [Guild.updateCommands] or similar.
         *
         *
         * It is usually preferred to discriminate commands by the [command names][.getName] instead.
         *
         * @return The command id
         */
        get() = java.lang.Long.toUnsignedString(commandIdLong)

    /**
     * Whether the used command is a guild command.
     *
     *
     * Guild commands can be created with [Guild.upsertCommand].
     *
     * @return True, if the used command is a guild command
     */
    @JvmField
    val isGuildCommand: Boolean
    val isGlobalCommand: Boolean
        /**
         * Whether the used command is a global command.
         *
         *
         * Global commands can be created with [JDA.upsertCommand].
         *
         * @return True, if the used command is a global command
         */
        get() = !isGuildCommand

    @JvmField
    @get:Nonnull
    val options: List<OptionMapping>

    /**
     * Gets all options for the specified name.
     *
     *
     * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * @param  name
     * The option name
     *
     * @throws IllegalArgumentException
     * If the provided name is null
     *
     * @return The list of options
     *
     * @see .getOption
     * @see .getOptions
     */
    @Nonnull
    fun getOptionsByName(@Nonnull name: String): List<OptionMapping> {
        Checks.notNull(name, "Name")
        return options.stream()
            .filter { opt: OptionMapping -> opt.name == name }
            .collect(Collectors.toList())
    }

    /**
     * Gets all options for the specified type.
     *
     *
     * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * @param  type
     * The option type
     *
     * @throws IllegalArgumentException
     * If the provided type is null
     *
     * @return The list of options
     *
     * @see .getOptions
     */
    @Nonnull
    fun getOptionsByType(@Nonnull type: OptionType): List<OptionMapping>? {
        Checks.notNull(type, "Type")
        return options.stream()
            .filter { it: OptionMapping -> it.type == type }
            .collect(Collectors.toList())
    }

    /**
     * Finds the first option with the specified name.
     *
     *
     * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     *
     * You can use the second and third parameter overloads to handle optional arguments gracefully.
     * See [.getOption] and [.getOption].
     *
     * @param  name
     * The option name
     *
     * @throws IllegalArgumentException
     * If the name is null
     *
     * @return The option with the provided name, or null if that option is not provided
     *
     * @see .getOption
     * @see .getOption
     * @see .getOption
     */
    fun getOption(@Nonnull name: String): OptionMapping? {
        val options = getOptionsByName(name)
        return if (options.isEmpty()) null else options[0]
    }

    /**
     * Finds the first option with the specified name.
     * <br></br>A resolver is used to get the value if the option is provided.
     * If no option is provided for the given name, this will simply return null instead.
     * You can use [.getOption] to provide a fallback for missing options.
     *
     *
     * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     *
     * **Example**
     * <br></br>You can understand this as a shortcut for these lines of code:
     * <pre>`OptionMapping opt = event.getOption("reason");
     * String reason = opt == null ? null : opt.getAsString();
    `</pre> *
     * Which can be written with this resolver as:
     * <pre>`String reason = event.getOption("reason", OptionMapping::getAsString);
    `</pre> *
     *
     * @param  name
     * The option name
     * @param  resolver
     * The mapping resolver function to use if there is a mapping available,
     * the provided mapping will never be null!
     * @param  <T>
     * The type of the resolved option value
     *
     * @throws IllegalArgumentException
     * If the name or resolver is null
     *
     * @return The resolved option with the provided name, or null if that option is not provided
     *
     * @see .getOption
     * @see .getOption
    </T> */
    fun <T> getOption(@Nonnull name: String, @Nonnull resolver: Function<in OptionMapping?, out T?>): T? {
        return getOption(name, null, resolver)
    }

    /**
     * Finds the first option with the specified name.
     * <br></br>A resolver is used to get the value if the option is provided.
     * If no option is provided for the given name, this will simply return your provided fallback instead.
     * You can use [.getOption] to fall back to `null`.
     *
     *
     * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     *
     * **Example**
     * <br></br>You can understand this as a shortcut for these lines of code:
     * <pre>`OptionMapping opt = event.getOption("reason");
     * String reason = opt == null ? "ban by mod" : opt.getAsString();
    `</pre> *
     * Which can be written with this resolver as:
     * <pre>`String reason = event.getOption("reason", "ban by mod", OptionMapping::getAsString);
    `</pre> *
     *
     * @param  name
     * The option name
     * @param  fallback
     * The fallback to use if the option is not provided, meaning [.getOption] returns null
     * @param  resolver
     * The mapping resolver function to use if there is a mapping available,
     * the provided mapping will never be null!
     * @param  <T>
     * The type of the resolved option value
     *
     * @throws IllegalArgumentException
     * If the name or resolver is null
     *
     * @return The resolved option with the provided name, or `fallback` if that option is not provided
     *
     * @see .getOption
     * @see .getOption
    </T> */
    fun <T> getOption(
        @Nonnull name: String,
        fallback: T?,
        @Nonnull resolver: Function<in OptionMapping?, out T>
    ): T? {
        Checks.notNull(resolver, "Resolver")
        val mapping = getOption(name)
        return if (mapping != null) resolver.apply(mapping) else fallback
    }

    /**
     * Finds the first option with the specified name.
     * <br></br>A resolver is used to get the value if the option is provided.
     * If no option is provided for the given name, this will simply return your provided fallback instead.
     * You can use [.getOption] to fall back to `null`.
     *
     *
     * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     *
     * **Example**
     * <br></br>You can understand this as a shortcut for these lines of code:
     * <pre>`OptionMapping opt = event.getOption("reason");
     * String reason = opt == null ? context.getFallbackReason() : opt.getAsString();
    `</pre> *
     * Which can be written with this resolver as:
     * <pre>`String reason = event.getOption("reason", context::getFallbackReason , OptionMapping::getAsString);
    `</pre> *
     *
     * @param  name
     * The option name
     * @param  fallback
     * The fallback supplier to use if the option is not provided, meaning [.getOption] returns null
     * @param  resolver
     * The mapping resolver function to use if there is a mapping available,
     * the provided mapping will never be null!
     * @param  <T>
     * The type of the resolved option value
     *
     * @throws IllegalArgumentException
     * If the name or resolver is null
     *
     * @return The resolved option with the provided name, or `fallback` if that option is not provided
     *
     * @see .getOption
     * @see .getOption
    </T> */
    fun <T> getOption(
        @Nonnull name: String,
        fallback: Supplier<out T>?,
        @Nonnull resolver: Function<in OptionMapping?, out T?>
    ): T? {
        Checks.notNull(resolver, "Resolver")
        val mapping = getOption(name)
        return if (mapping != null) resolver.apply(mapping) else fallback?.get()
    }
}
