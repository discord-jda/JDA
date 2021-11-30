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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interaction of a Slash-Command.
 *
 * @see net.dv8tion.jda.api.events.interaction.SlashCommandEvent
 */
public interface CommandInteraction extends Interaction
{
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

    @Nonnull
    @Override
    MessageChannel getChannel();

    /**
     * The command id
     *
     * @return The command id
     */
    long getCommandIdLong();

    /**
     * The command id
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
     * @return The options passed for this command
     */
    @Nonnull
    List<OptionMapping> getOptions();

    /**
     * Gets all options for the specified name.
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
     * @param  type
     *         The option type
     *
     * @throws IllegalArgumentException
     *         If the provided type is null
     *
     * @return The list of options
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
     * @param  name
     *         The option name
     *
     * @throws IllegalArgumentException
     *         If the name is null
     *
     * @return The option with the provided name, or null if that option is not provided
     */
    @Nullable
    default OptionMapping getOption(@Nonnull String name)
    {
        List<OptionMapping> options = getOptionsByName(name);
        return options.isEmpty() ? null : options.get(0);
    }

    /**
     * Gets the slash command String for this slash command.
     * <br>This is similar to the String you see when clicking the interaction name in the client.
     *
     * <p>Example return for an echo command: {@code /say echo phrase: Say this}
     *
     * @return The command String for this slash command
     */
    @Nonnull
    default String getCommandString()
    {
        //Get text like the text that appears when you hover over the interaction in discord
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
}
