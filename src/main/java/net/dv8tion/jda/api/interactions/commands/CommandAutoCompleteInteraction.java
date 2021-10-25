package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interaction of a command option autocomplete
 * @see net.dv8tion.jda.api.events.interaction.CommandAutoCompleteEvent
 */
public interface CommandAutoCompleteInteraction extends Interaction
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

    /**
     * Respond with a singular choice for this option autocompletion
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link OptionData#MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is of type {@link OptionType#STRING}</li>
     *         </ul>
     *
     * @return The ChoiceAction
     */
    @Nonnull
    ChoiceAction respondChoice(@Nonnull String name, @Nonnull String value);

    /**
     * Respond with a singular choice for this option autocompletion
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link OptionData#MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is of type {@link OptionType#NUMBER}</li>
     *         </ul>
     *
     * @return The ChoiceAction
     */
    @Nonnull
    ChoiceAction respondChoice(@Nonnull String name, double value);

    /**
     * Respond with a singular choice for this option autocompletion
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link OptionData#MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is of type {@link OptionType#INTEGER}</li>
     *         </ul>
     *
     * @return The ChoiceAction
     */
    @Nonnull
    ChoiceAction respondChoice(@Nonnull String name, long value);

    /**
     * Respond with up to 25 choices for this option autocompletion
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     *         The choices to add
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link OptionData#MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is of type {@link OptionType#STRING}, {@link OptionType#INTEGER}, or {@link OptionType#NUMBER}</li>
     *         </ul>
     *
     * @return The ChoiceAction
     */
    @Nonnull
    @CheckReturnValue
    ChoiceAction respondChoices(@Nonnull Command.Choice... choices);

    /**
     * Respond with up to 25 choices for this option autocompletion
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     *         The choices to add
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link OptionData#MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is of type {@link OptionType#STRING}, {@link OptionType#INTEGER}, or {@link OptionType#NUMBER}</li>
     *         </ul>
     *
     * @return The ChoiceAction
     */
    @Nonnull
    @CheckReturnValue
    default ChoiceAction respondChoices(@Nonnull Collection<? extends Command.Choice> choices) {
        Checks.noneNull(choices, "Choices");
        return respondChoices(choices.toArray(new Command.Choice[0]));
    }

    /**
     * Gets the option currently selected by the user.
     *
     * @return The focused option
     */
    @Nonnull
    OptionMapping getFocusedOption();
}
