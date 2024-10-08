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

package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Extension of {@link CommandData} which allows setting slash-command specific settings such as options and subcommands.
 */
public interface SlashCommandData extends CommandData
{
    @Nonnull
    @Override
    SlashCommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    @Nonnull
    @Override
    SlashCommandData setName(@Nonnull String name);

    @Nonnull
    @Override
    SlashCommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    @Nonnull
    @Override
    SlashCommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    @Nonnull
    @Override
    SlashCommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    @Nonnull
    @Override
    @Deprecated
    SlashCommandData setGuildOnly(boolean guildOnly);

    @Nonnull
    @Override
    default SlashCommandData setContexts(@Nonnull InteractionContextType... contexts)
    {
        return (SlashCommandData) CommandData.super.setContexts(contexts);
    }

    @Nonnull
    @Override
    SlashCommandData setContexts(@Nonnull Collection<InteractionContextType> contexts);

    @Nonnull
    @Override
    default SlashCommandData setIntegrationTypes(@Nonnull IntegrationType... integrationTypes)
    {
        return (SlashCommandData) CommandData.super.setIntegrationTypes(integrationTypes);
    }

    @Nonnull
    @Override
    SlashCommandData setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes);

    @Nonnull
    @Override
    SlashCommandData setNSFW(boolean nsfw);

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The builder, for chaining
     */
    @Nonnull
    SlashCommandData setDescription(@Nonnull String description);

    /**
     * Sets a {@link DiscordLocale language-specific} localizations of this command's description.
     *
     * @param  locale
     *         The locale to associate the translated description with
     *
     * @param  description
     *         The translated description to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the description is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the description does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    SlashCommandData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description);

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this command's description.
     *
     * @param  map
     *         The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a description which does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    SlashCommandData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map);

    /**
     * The configured description
     *
     * @return The description
     */
    @Nonnull
    String getDescription();

    /**
     * The localizations of this command's description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    LocalizationMap getDescriptionLocalizations();

    /**
     * Removes all options that evaluate to {@code true} under the provided {@code condition}.
     * <br>This will not affect options within subcommands.
     * Use {@link SubcommandData#removeOptions(Predicate)} instead.
     *
     * <p><b>Example: Remove all options</b>
     * <pre>{@code
     * command.removeOptions(option -> true);
     * }</pre>
     * <p><b>Example: Remove all options that are required</b>
     * <pre>{@code
     * command.removeOptions(option -> option.isRequired());
     * }</pre>
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any options were removed
     */
    boolean removeOptions(@Nonnull Predicate<? super OptionData> condition);

    /**
     * Removes options by the provided name.
     * <br>This will not affect options within subcommands.
     * Use {@link SubcommandData#removeOptionByName(String)} instead.
     *
     * @param  name
     *         The <b>case-sensitive</b> option name
     *
     * @return True, if any options were removed
     */
    default boolean removeOptionByName(@Nonnull String name)
    {
        return removeOptions(option -> option.getName().equals(name));
    }

    /**
     * Removes all subcommands that evaluate to {@code true} under the provided {@code condition}.
     * <br>This will not apply to subcommands within subcommand groups.
     * Use {@link SubcommandGroupData#removeSubcommand(Predicate)} instead.
     *
     * <p><b>Example: Remove all subcommands</b>
     * <pre>{@code
     * command.removeSubcommands(subcommand -> true);
     * }</pre>
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any subcommands were removed
     */
    boolean removeSubcommands(@Nonnull Predicate<? super SubcommandData> condition);

    /**
     * Removes subcommands by the provided name.
     * <br>This will not apply to subcommands within subcommand groups.
     * Use {@link SubcommandGroupData#removeSubcommandByName(String)} instead.
     *
     * @param  name
     *         The <b>case-sensitive</b> subcommand name
     *
     * @return True, if any subcommands were removed
     */
    default boolean removeSubcommandByName(@Nonnull String name)
    {
        return removeSubcommands(subcommand -> subcommand.getName().equals(name));
    }

    /**
     * Removes all subcommand groups that evaluate to {@code true} under the provided {@code condition}.
     *
     * <p><b>Example: Remove all subcommand groups</b>
     * <pre>{@code
     * command.removeSubcommandGroups(group -> true);
     * }</pre>
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any subcommand groups were removed
     */
    boolean removeSubcommandGroups(@Nonnull Predicate<? super SubcommandGroupData> condition);

    /**
     * Removes subcommand groups by the provided name.
     *
     * @param  name
     *         The <b>case-sensitive</b> subcommand group name
     *
     * @return True, if any subcommand groups were removed
     */
    default boolean removeSubcommandGroupByName(@Nonnull String name)
    {
        return removeSubcommandGroups(group -> group.getName().equals(name));
    }

    /**
     * The {@link SubcommandData Subcommands} in this command.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @Nonnull
    @Unmodifiable
    List<SubcommandData> getSubcommands();

    /**
     * The {@link SubcommandGroupData Subcommand Groups} in this command.
     *
     * @return Immutable list of {@link SubcommandGroupData}
     */
    @Nonnull
    @Unmodifiable
    List<SubcommandGroupData> getSubcommandGroups();

    /**
     * The options for this command.
     *
     * @return Immutable list of {@link OptionData}
     */
    @Nonnull
    @Unmodifiable
    List<OptionData> getOptions();

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *          The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    SlashCommandData addOptions(@Nonnull OptionData... options);

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOptions(@Nonnull Collection<? extends OptionData> options)
    {
        Checks.noneNull(options, "Option");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     * @param  autoComplete
     *         Whether this option supports auto-complete via {@link CommandAutoCompleteInteractionEvent},
     *         only supported for option types which {@link OptionType#canSupportChoices() support choices}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If the provided option type does not support auto-complete</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required, boolean autoComplete)
    {
        return addOptions(new OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autoComplete));
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(type, name, description, required, false);
    }

    /**
     * Adds an option to this command.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandData Subcommands} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * <pre>{@code
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }</pre>
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     *         Also throws if you try adding subcommands when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    SlashCommandData addSubcommands(@Nonnull SubcommandData... subcommands);

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandData Subcommands} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * <pre>{@code
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }</pre>
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     *         Also throws if you try adding subcommands when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * <pre>{@code
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }</pre>
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommand groups, or duplicate group names are provided.
     *         Also throws if you try adding subcommand groups when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    SlashCommandData addSubcommandGroups(@Nonnull SubcommandGroupData... groups);

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * <pre>{@code
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }</pre>
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommand groups, or duplicate group names are provided.
     *         Also throws if you try adding subcommand groups when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups)
    {
        Checks.noneNull(groups, "SubcommandGroups");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }

    /**
     * Converts the provided {@link Command} into a SlashCommandData instance.
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of SlashCommandData
     */
    @Nonnull
    static SlashCommandData fromCommand(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        if (command.getType() != Command.Type.SLASH)
            throw new IllegalArgumentException("Cannot convert command of type " + command.getType() + " to SlashCommandData!");

        CommandDataImpl data = new CommandDataImpl(command.getName(), command.getDescription());
        data.setGuildOnly(command.isGuildOnly());
        data.setContexts(command.getContexts());
        data.setIntegrationTypes(command.getIntegrationTypes());
        data.setNSFW(command.isNSFW());
        data.setDefaultPermissions(command.getDefaultPermissions());
        //Command localizations are unmodifiable, make a copy
        data.setNameLocalizations(command.getNameLocalizations().toMap());
        data.setDescriptionLocalizations(command.getDescriptionLocalizations().toMap());
        command.getOptions()
                .stream()
                .map(OptionData::fromOption)
                .forEach(data::addOptions);
        command.getSubcommands()
                .stream()
                .map(SubcommandData::fromSubcommand)
                .forEach(data::addSubcommands);
        command.getSubcommandGroups()
                .stream()
                .map(SubcommandGroupData::fromGroup)
                .forEach(data::addSubcommandGroups);
        return data;
    }

    /**
     * Parses the provided serialization back into a SlashCommandData instance.
     * <br>This is the reverse function for {@link SlashCommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SlashCommandData instance, which can be further configured through setters
     *
     * @see    CommandData#fromData(DataObject)
     * @see    Commands#fromList(Collection)
     */
    @Nonnull
    static SlashCommandData fromData(@Nonnull DataObject object)
    {
        Checks.notNull(object, "DataObject");
        String name = object.getString("name");
        Command.Type commandType = Command.Type.fromId(object.getInt("type", 1));
        if (commandType != Command.Type.SLASH)
            throw new IllegalArgumentException("Cannot convert command of type " + commandType + " to SlashCommandData!");

        String description = object.getString("description");
        DataArray options = object.optArray("options").orElseGet(DataArray::empty);
        CommandDataImpl command = new CommandDataImpl(name, description);
        command.setContexts(object.getArray("contexts").stream(DataArray::getString)
                        .map(InteractionContextType::fromKey)
                        .collect(Helpers.toUnmodifiableEnumSet(InteractionContextType.class)));
        command.setIntegrationTypes(object.getArray("integration_types").stream(DataArray::getString)
                .map(IntegrationType::fromKey)
                .collect(Helpers.toUnmodifiableEnumSet(IntegrationType.class)));
        command.setNSFW(object.getBoolean("nsfw"));

        command.setDefaultPermissions(
                object.isNull("default_member_permissions")
                        ? DefaultMemberPermissions.ENABLED
                        : DefaultMemberPermissions.enabledFor(object.getLong("default_member_permissions"))
        );

        command.setNameLocalizations(LocalizationUtils.mapFromProperty(object, "name_localizations"));
        command.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(object, "description_localizations"));
        options.stream(DataArray::getObject).forEach(opt ->
        {
            OptionType type = OptionType.fromKey(opt.getInt("type"));
            switch (type)
            {
            case SUB_COMMAND:
                command.addSubcommands(SubcommandData.fromData(opt));
                break;
            case SUB_COMMAND_GROUP:
                command.addSubcommandGroups(SubcommandGroupData.fromData(opt));
                break;
            default:
                command.addOptions(OptionData.fromData(opt));
            }
        });
        return command;
    }
}
