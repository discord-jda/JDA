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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Builder for a Slash-Command subcommand.
 */
public class SubcommandData implements SerializableData
{
    protected final List<OptionData> options = new ArrayList<>(CommandData.MAX_OPTIONS);
    protected String name, description;
    private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
    private final LocalizationMap descriptionLocalizations = new LocalizationMap(this::checkDescription);
    private boolean allowRequired = true;

    /**
     * Create a subcommand builder.
     *
     * @param  name
     *         The subcommand name, 1-32 lowercase alphanumeric characters
     * @param  description
     *         The subcommand description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If any of the following requirements are not met
     *         <ul>
     *             <li>The name must be lowercase alphanumeric (with dash), 1-32 characters long</li>
     *             <li>The description must be 1-100 characters long</li>
     *         </ul>
     */
    public SubcommandData(@Nonnull String name, @Nonnull String description)
    {
        setName(name);
        setDescription(description);
    }

    protected void checkName(@Nonnull String name)
    {
        Checks.inRange(name, 1, 32, "Name");
        Checks.isLowercase(name, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
    }

    protected void checkDescription(@Nonnull String description)
    {
        Checks.inRange(description, 1, 100, "Description");
    }

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null, not alphanumeric, or not between 1-32 characters
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData setName(@Nonnull String name)
    {
        checkName(name);
        this.name = name;
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this subcommand's name.
     *
     * @param  locale
     *         The locale to associate the translated name with
     *
     * @param  name
     *         The translated name to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the name is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the name does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    public SubcommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale, name);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this subcommand's name.
     *
     * @param  map
     *         The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a name which does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    public SubcommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslations(map);
        return this;
    }

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-100 characters
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData setDescription(@Nonnull String description)
    {
        checkDescription(description);
        this.description = description;
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this subcommand's description.
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
    public SubcommandData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale, description);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this subcommand's description.
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
    public SubcommandData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslations(map);
        return this;
    }

    /**
     * Removes all options that evaluate to {@code true} under the provided {@code condition}.
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
    public boolean removeOptions(@Nonnull Predicate<? super OptionData> condition)
    {
        Checks.notNull(condition, "Condition");
        return options.removeIf(condition);
    }

    /**
     * Removes options by the provided name.
     *
     * @param  name
     *         The <b>case-sensitive</b> option name
     *
     * @return True, if any options were removed
     */
    public boolean removeOptionByName(@Nonnull String name)
    {
        return removeOptions(option -> option.getName().equals(name));
    }

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this subcommand.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOptions(@Nonnull OptionData... options)
    {
        Checks.noneNull(options, "Option");
        Checks.check(options.length + this.options.size() <= CommandData.MAX_OPTIONS, "Cannot have more than %d options for a subcommand!", CommandData.MAX_OPTIONS);
        boolean allowRequired = this.allowRequired;
        for (OptionData option : options)
        {
            Checks.check(option.getType() != OptionType.SUB_COMMAND, "Cannot add a subcommand to a subcommand!");
            Checks.check(option.getType() != OptionType.SUB_COMMAND_GROUP, "Cannot add a subcommand group to a subcommand!");
            Checks.check(allowRequired || !option.isRequired(), "Cannot add required options after non-required options!");
            allowRequired = option.isRequired(); // prevent adding required options after non-required options
        }

        Checks.checkUnique(Stream.concat(getOptions().stream(), Arrays.stream(options)).map(OptionData::getName),
            "Cannot have multiple options with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count });

        this.allowRequired = allowRequired;
        this.options.addAll(Arrays.asList(options));

        return this;
    }

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this subcommand.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOptions(@Nonnull Collection<? extends OptionData> options)
    {
        Checks.noneNull(options, "Options");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this subcommand.
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
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If the provided option type does not support auto-complete</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required, boolean autoComplete)
    {
        return addOptions(new OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autoComplete));
    }

    /**
     * Adds an option to this subcommand.
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
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(type, name, description, required, false);
    }

    /**
     * Adds an option to this subcommand.
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
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    /**
     * The options for this command.
     *
     * @return Immutable list of {@link OptionData}
     */
    @Nonnull
    public List<OptionData> getOptions()
    {
        return Collections.unmodifiableList(options);
    }

    /**
     * The configured name
     *
     * @return The name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The localizations of this subcommand's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    public LocalizationMap getNameLocalizations()
    {
        return nameLocalizations;
    }

    /**
     * The configured description
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The localizations of this subcommand's description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    public LocalizationMap getDescriptionLocalizations()
    {
        return descriptionLocalizations;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", OptionType.SUB_COMMAND.getKey())
                .put("name", name)
                .put("name_localizations", nameLocalizations)
                .put("description", description)
                .put("description_localizations", descriptionLocalizations)
                .put("options", DataArray.fromCollection(options));
    }

    /**
     * Parses the provided serialization back into an SubcommandData instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} representing the subcommand
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SubcommandData instance, which can be further configured through setters
     */
    @Nonnull
    public static SubcommandData fromData(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        SubcommandData sub = new SubcommandData(name, description);
        json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                        .map(OptionData::fromData)
                        .forEach(sub::addOptions)
        );
        sub.setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"));
        sub.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(json, "description_localizations"));

        return sub;
    }

    /**
     * Converts the provided {@link Command.Subcommand} into a SubCommandData instance.
     *
     * @param  subcommand
     *         The subcommand to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the subcommand has illegal configuration
     *
     * @return An instance of SubCommandData
     */
    @Nonnull
    public static SubcommandData fromSubcommand(@Nonnull Command.Subcommand subcommand)
    {
        Checks.notNull(subcommand, "Subcommand");
        SubcommandData data = new SubcommandData(subcommand.getName(), subcommand.getDescription());
        data.setNameLocalizations(subcommand.getNameLocalizations().toMap());
        data.setDescriptionLocalizations(subcommand.getDescriptionLocalizations().toMap());
        subcommand.getOptions()
                .stream()
                .map(OptionData::fromOption)
                .forEach(data::addOptions);
        return data;
    }
}
