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
 * Builder for a Slash-Command group.
 */
public class SubcommandGroupData implements SerializableData
{
    private final List<SubcommandData> subcommands = new ArrayList<>(CommandData.MAX_OPTIONS);
    private String name, description;
    private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
    private final LocalizationMap descriptionLocalizations = new LocalizationMap(this::checkDescription);

    /**
     * Create an group builder.
     *
     * @param name
     *        The group name, 1-32 lowercase alphanumeric characters
     * @param description
     *        The group description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If any of the following requirements are not met
     *         <ul>
     *             <li>The name must be lowercase alphanumeric (with dash), 1-32 characters long</li>
     *             <li>The description must be 1-100 characters long</li>
     *         </ul>
     */
    public SubcommandGroupData(@Nonnull String name, @Nonnull String description)
    {
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(description, "Description");
        Checks.notLonger(name, 32, "Name");
        Checks.notLonger(description, 100, "Description");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        Checks.isLowercase(name, "Name");
        this.name = name;
        this.description = description;
    }

    protected void checkName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.isLowercase(name, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
    }

    protected void checkDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
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
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData setName(@Nonnull String name)
    {
        checkName(name);
        this.name = name;
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this subcommand group's name.
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
    public SubcommandGroupData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale, name);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this subcommand group's name.
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
    public SubcommandGroupData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
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
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData setDescription(@Nonnull String description)
    {
        checkDescription(description);
        this.description = description;
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this subcommand group's description.
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
    public SubcommandGroupData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale, description);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this subcommand group's description.
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
    public SubcommandGroupData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslations(map);
        return this;
    }

    /**
     * The name for this subcommand group
     *
     * @return The name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The localizations of this subcommand's name for {@link DiscordLocale various languages} group.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    public LocalizationMap getNameLocalizations()
    {
        return nameLocalizations;
    }

    /**
     * The description for this  subcommand group
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The localizations of this subcommand's description for {@link DiscordLocale various languages} group.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    public LocalizationMap getDescriptionLocalizations()
    {
        return descriptionLocalizations;
    }

    /**
     * Removes all subcommands that evaluate to {@code true} under the provided {@code condition}.
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
    public boolean removeSubcommand(@Nonnull Predicate<? super SubcommandData> condition)
    {
        Checks.notNull(condition, "Condition");
        return subcommands.removeIf(condition);
    }

    /**
     * Removes subcommands by the provided name.
     *
     * @param  name
     *         The <b>case-sensitive</b> subcommand name
     *
     * @return True, if any subcommands were removed
     */
    public boolean removeSubcommandByName(@Nonnull String name)
    {
        return removeSubcommand(subcommand -> subcommand.getName().equals(name));
    }

    /**
     * The {@link SubcommandData Subcommands} in this group.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @Nonnull
    public List<SubcommandData> getSubcommands()
    {
        return Collections.unmodifiableList(subcommands);
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandData Subcommands} to this group.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData addSubcommands(@Nonnull SubcommandData... subcommands)
    {
        Checks.noneNull(subcommands, "Subcommand");
        Checks.check(subcommands.length + this.subcommands.size() <= CommandData.MAX_OPTIONS, "Cannot have more than %d subcommands in one group!", CommandData.MAX_OPTIONS);
        Checks.checkUnique(
            Stream.concat(getSubcommands().stream(), Arrays.stream(subcommands)).map(SubcommandData::getName),
            "Cannot have multiple subcommands with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );
        this.subcommands.addAll(Arrays.asList(subcommands));
        return this;
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this group.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than 25 subcommands, or duplicate subcommand names are provided.
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", OptionType.SUB_COMMAND_GROUP.getKey())
                .put("name", name)
                .put("name_localizations", nameLocalizations)
                .put("description", description)
                .put("description_localizations", descriptionLocalizations)
                .put("options", DataArray.fromCollection(subcommands));
    }

    /**
     * Parses the provided serialization back into an SubcommandGroupData instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} representing the group
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SubcommandGroupData instance, which can be further configured through setters
     */
    @Nonnull
    public static SubcommandGroupData fromData(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        SubcommandGroupData group = new SubcommandGroupData(name, description);
        json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                        .map(SubcommandData::fromData)
                        .forEach(group::addSubcommands)
        );
        group.setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"));
        group.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(json, "description_localizations"));

        return group;
    }

    /**
     * Converts the provided {@link Command.SubcommandGroup} into a SubcommandGroupData instance.
     *
     * @param  group
     *         The subcommand group to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the subcommand group has illegal configuration
     *
     * @return An instance of SubcommandGroupData
     */
    @Nonnull
    public static SubcommandGroupData fromGroup(@Nonnull Command.SubcommandGroup group)
    {
        Checks.notNull(group, "Subcommand Group");
        SubcommandGroupData data = new SubcommandGroupData(group.getName(), group.getDescription());
        data.setNameLocalizations(group.getNameLocalizations().toMap());
        data.setDescriptionLocalizations(group.getDescriptionLocalizations().toMap());
        group.getSubcommands()
                .stream()
                .map(SubcommandData::fromSubcommand)
                .forEach(data::addSubcommands);
        return data;
    }
}
