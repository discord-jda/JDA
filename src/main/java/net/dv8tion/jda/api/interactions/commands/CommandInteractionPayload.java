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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Interactions which provide command data.
 * <br>This is an abstraction for {@link CommandAutoCompleteInteraction} and {@link CommandInteraction}.
 */
public interface CommandInteractionPayload extends Interaction
{
    /**
     * The {@link Command.Type Type} of command this interaction is for.
     *
     * @return The command type
     */
    @Nonnull
    Command.Type getCommandType();

    /**
     * The command name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * The subcommand name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The subcommand name, or null if this is not a subcommand
     */
    @Nullable
    String getSubcommandName();

    /**
     * The subcommand group name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The subcommand group name, or null if this is not a subcommand group
     */
    @Nullable
    String getSubcommandGroup();

    /**
     * Combination of {@link #getName()}, {@link #getSubcommandGroup()}, and {@link #getSubcommandName()}.
     * <br>This will format the command into a path such as {@code mod/mute} where {@code mod} would be the {@link #getName()} and {@code mute} the {@link #getSubcommandName()}.
     *
     * <p>Examples:
     * <ul>
     *     <li>{@code /mod ban -> "mod/ban"}</li>
     *     <li>{@code /admin config owner -> "admin/config/owner"}</li>
     *     <li>{@code /ban -> "ban"}</li>
     * </ul>
     *
     * @return The command path
     */
    @Nonnull
    default String getCommandPath()
    {
        StringBuilder builder = new StringBuilder(getName());
        if (getSubcommandGroup() != null)
            builder.append('/').append(getSubcommandGroup());
        if (getSubcommandName() != null)
            builder.append('/').append(getSubcommandName());
        return builder.toString();
    }

    /**
     * Gets the display string for this command.
     * <br>This is similar to the string you see when clicking the interaction name in the client.
     * For non-slash command types, this simply returns {@link #getName()} instead.
     *
     * <p>Example return for an echo command: {@code /say echo phrase: Say this}
     *
     * @return The display string for this command
     */
    @Nonnull
    default String getCommandString()
    {
        //Get text like the text that appears when you hover over the interaction in discord
        if (getCommandType() != Command.Type.SLASH)
            return getName();

        StringBuilder builder = new StringBuilder();
        builder.append("/").append(getName());
        if (getSubcommandGroup() != null)
            builder.append(" ").append(getSubcommandGroup());
        if (getSubcommandName() != null)
            builder.append(" ").append(getSubcommandName());
        for (OptionMapping o : getOptions())
        {
            builder.append(" ").append(o.getName()).append(": ");
            switch (o.getType())
            {
            case CHANNEL:
                builder.append("#").append(o.getAsGuildChannel().getName());
                break;
            case USER:
                builder.append("@").append(o.getAsUser().getName());
                break;
            case ROLE:
                builder.append("@").append(o.getAsRole().getName());
                break;
            case MENTIONABLE: //client only allows user or role mentionable as of Aug 4, 2021
                if (o.getAsMentionable() instanceof Role)
                    builder.append("@").append(o.getAsRole().getName());
                else if (o.getAsMentionable() instanceof Member)
                    builder.append("@").append(o.getAsUser().getName());
                else if (o.getAsMentionable() instanceof User)
                    builder.append("@").append(o.getAsUser().getName());
                else
                    builder.append("@").append(o.getAsMentionable().getIdLong());
                break;
            default:
                builder.append(o.getAsString());
                break;
            }
        }
        return builder.toString();
    }

    /**
     * The command id.
     * <br>This is the id generated when a command is created via {@link Guild#updateCommands()} or similar.
     *
     * <p>It is usually preferred to discriminate commands by the {@link #getName() command names} instead.
     *
     * @return The command id
     */
    long getCommandIdLong();

    /**
     * The command id
     * <br>This is the id generated when a command is created via {@link Guild#updateCommands()} or similar.
     *
     * <p>It is usually preferred to discriminate commands by the {@link #getName() command names} instead.
     *
     * @return The command id
     */
    @Nonnull
    default String getCommandId()
    {
        return Long.toUnsignedString(getCommandIdLong());
    }

    /**
     * The options provided by the user when this command was executed.
     * <br>Each option has a name and value.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * @return The options passed for this command
     *
     * @see    #getOption(String)
     */
    @Nonnull
    List<OptionMapping> getOptions();

    /**
     * Gets all options for the specified name.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * @param  name
     *         The option name
     *
     * @throws IllegalArgumentException
     *         If the provided name is null
     *
     * @return The list of options
     *
     * @see   #getOption(String)
     * @see   #getOptions()
     */
    @Nonnull
    default List<OptionMapping> getOptionsByName(@Nonnull String name)
    {
        Checks.notNull(name, "Name");
        return getOptions().stream()
                .filter(opt -> opt.getName().equals(name))
                .collect(Collectors.toList());
    }

    /**
     * Gets all options for the specified type.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * @param  type
     *         The option type
     *
     * @throws IllegalArgumentException
     *         If the provided type is null
     *
     * @return The list of options
     *
     * @see    #getOptions()
     */
    @Nonnull
    default List<OptionMapping> getOptionsByType(@Nonnull OptionType type)
    {
        Checks.notNull(type, "Type");
        return getOptions().stream()
                .filter(it -> it.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Finds the first option with the specified name.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * <p>You can use the second and third parameter overloads to handle optional arguments gracefully.
     * See {@link #getOption(String, Function)} and {@link #getOption(String, Object, Function)}.
     *
     * @param  name
     *         The option name
     *
     * @throws IllegalArgumentException
     *         If the name is null
     *
     * @return The option with the provided name, or null if that option is not provided
     *
     * @see    #getOption(String, Function)
     * @see    #getOption(String, Object, Function)
     * @see    #getOption(String, Supplier, Function)
     */
    @Nullable
    default OptionMapping getOption(@Nonnull String name)
    {
        List<OptionMapping> options = getOptionsByName(name);
        return options.isEmpty() ? null : options.get(0);
    }

    /**
     * Finds the first option with the specified name.
     * <br>A resolver is used to get the value if the option is provided.
     * If no option is provided for the given name, this will simply return null instead.
     * You can use {@link #getOption(String, Object, Function)} to provide a fallback for missing options.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * <p><b>Example</b>
     * <br>You can understand this as a shortcut for these lines of code:
     * <pre>{@code
     * OptionMapping opt = event.getOption("reason");
     * String reason = opt == null ? null : opt.getAsString();
     * }</pre>
     * Which can be written with this resolver as:
     * <pre>{@code
     * String reason = event.getOption("reason", OptionMapping::getAsString);
     * }</pre>
     *
     * @param  name
     *         The option name
     * @param  resolver
     *         The mapping resolver function to use if there is a mapping available,
     *         the provided mapping will never be null!
     * @param  <T>
     *         The type of the resolved option value
     *
     * @throws IllegalArgumentException
     *         If the name or resolver is null
     *
     * @return The resolved option with the provided name, or null if that option is not provided
     *
     * @see    #getOption(String, Object, Function)
     * @see    #getOption(String, Supplier, Function)
     */
    @Nullable
    default <T> T getOption(@Nonnull String name, @Nonnull Function<? super OptionMapping, ? extends T> resolver)
    {
        return getOption(name, null, resolver);
    }

    /**
     * Finds the first option with the specified name.
     * <br>A resolver is used to get the value if the option is provided.
     * If no option is provided for the given name, this will simply return your provided fallback instead.
     * You can use {@link #getOption(String, Function)} to fall back to {@code null}.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * <p><b>Example</b>
     * <br>You can understand this as a shortcut for these lines of code:
     * <pre>{@code
     * OptionMapping opt = event.getOption("reason");
     * String reason = opt == null ? "ban by mod" : opt.getAsString();
     * }</pre>
     * Which can be written with this resolver as:
     * <pre>{@code
     * String reason = event.getOption("reason", "ban by mod", OptionMapping::getAsString);
     * }</pre>
     *
     * @param  name
     *         The option name
     * @param  fallback
     *         The fallback to use if the option is not provided, meaning {@link #getOption(String)} returns null
     * @param  resolver
     *         The mapping resolver function to use if there is a mapping available,
     *         the provided mapping will never be null!
     * @param  <T>
     *         The type of the resolved option value
     *
     * @throws IllegalArgumentException
     *         If the name or resolver is null
     *
     * @return The resolved option with the provided name, or {@code fallback} if that option is not provided
     *
     * @see    #getOption(String, Function)
     * @see    #getOption(String, Supplier, Function)
     */
    default <T> T getOption(@Nonnull String name,
                            @Nullable T fallback,
                            @Nonnull Function<? super OptionMapping, ? extends T> resolver)
    {
        Checks.notNull(resolver, "Resolver");
        OptionMapping mapping = getOption(name);
        if (mapping != null)
            return resolver.apply(mapping);
        return fallback;
    }

    /**
     * Finds the first option with the specified name.
     * <br>A resolver is used to get the value if the option is provided.
     * If no option is provided for the given name, this will simply return your provided fallback instead.
     * You can use {@link #getOption(String, Function)} to fall back to {@code null}.
     *
     * <p>For {@link CommandAutoCompleteInteraction}, this might be incomplete and unvalidated.
     * Auto-complete interactions happen on incomplete command inputs and are not validated.
     *
     * <p><b>Example</b>
     * <br>You can understand this as a shortcut for these lines of code:
     * <pre>{@code
     * OptionMapping opt = event.getOption("reason");
     * String reason = opt == null ? context.getFallbackReason() : opt.getAsString();
     * }</pre>
     * Which can be written with this resolver as:
     * <pre>{@code
     * String reason = event.getOption("reason", context::getFallbackReason , OptionMapping::getAsString);
     * }</pre>
     *
     * @param  name
     *         The option name
     * @param  fallback
     *         The fallback supplier to use if the option is not provided, meaning {@link #getOption(String)} returns null
     * @param  resolver
     *         The mapping resolver function to use if there is a mapping available,
     *         the provided mapping will never be null!
     * @param  <T>
     *         The type of the resolved option value
     *
     * @throws IllegalArgumentException
     *         If the name or resolver is null
     *
     * @return The resolved option with the provided name, or {@code fallback} if that option is not provided
     *
     * @see    #getOption(String, Function)
     * @see    #getOption(String, Object, Function)
     */
    default <T> T getOption(@Nonnull String name,
                            @Nullable Supplier<? extends T> fallback,
                            @Nonnull Function<? super OptionMapping, ? extends T> resolver)
    {
        Checks.notNull(resolver, "Resolver");
        OptionMapping mapping = getOption(name);
        if (mapping != null)
            return resolver.apply(mapping);
        return fallback == null ? null : fallback.get();
    }
}
