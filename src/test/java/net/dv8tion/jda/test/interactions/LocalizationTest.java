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

package net.dv8tion.jda.test.interactions;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.PrettyRepresentation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalizationTest
{
    private static SlashCommandData slashCommandData;
    private static DataObject data;

    @BeforeAll
    static void setup()
    {
        LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                .fromBundles("MyCommands", DiscordLocale.FRENCH)
                .build();

        slashCommandData = Commands.slash("ban", "Bans someone").addSubcommandGroups(
            new SubcommandGroupData("user", "Bans a member").addSubcommands(
                new SubcommandData("perm", "Bans a user permanently").addOptions(
                    new OptionData(OptionType.STRING, "user", "The user to ban"),
                    new OptionData(OptionType.INTEGER, "del_days", "The amount of days to delete messages")
                        .addChoices(
                            new Command.Choice("1 Day", "1"),
                            new Command.Choice("7 Days", "7"),
                            new Command.Choice("14 Days", "14")
                        )
                ),
                new SubcommandData("temp", "Bans a user temporarily").addOptions(
                    new OptionData(OptionType.STRING, "user", "The user to ban"),
                    new OptionData(OptionType.INTEGER, "del_days", "The amount of days to delete messages")
                        .addChoices(
                            new Command.Choice("1 Day", "1"),
                            new Command.Choice("7 Days", "7"),
                            new Command.Choice("14 Days", "14")
                        )
                )
            )
        ).setLocalizationFunction(localizationFunction);

        data = slashCommandData.toData();
    }

    @Test
    void commandLocalization()
    {
        assertThat(data.getString("name")).isEqualTo("ban");
        assertThat(data.getString("description")).isEqualTo("Bans someone");

        assertThat(data.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "ban"));
        assertThat(data.getObject("description_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "Bannis un utilisateur"));
    }

    @Test
    void subcommandLocalization()
    {
        DataObject subcommandGroup = getOption(data, "user");

        assertThat(subcommandGroup.getString("name")).isEqualTo("user");
        assertThat(subcommandGroup.getString("description")).isEqualTo("Bans a member");

        assertThat(subcommandGroup.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "utilisateur"));
        assertThat(subcommandGroup.getObject("description_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "Bannis un utilisateur"));
    }

    @Test
    void subcommandGroupLocalization()
    {
        DataObject subcommandGroup = getOption(data, "user");
        DataObject subcommand = getOption(subcommandGroup, "perm");

        assertThat(subcommand.getString("name")).isEqualTo("perm");
        assertThat(subcommand.getString("description")).isEqualTo("Bans a user permanently");

        assertThat(subcommand.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "permanent"));
        assertThat(subcommand.getObject("description_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "Bannis un utilisateur pour toujours"));
    }

    @Test
    void optionLocalization()
    {
        DataObject subcommandGroup = getOption(data, "user");
        DataObject subcommand = getOption(subcommandGroup, "perm");
        DataObject userOption = getOption(subcommand, "user");
        DataObject delDaysOption = getOption(subcommand, "del_days");

        assertThat(userOption.getString("name")).isEqualTo("user");
        assertThat(userOption.getString("description")).isEqualTo("The user to ban");

        assertThat(delDaysOption.getString("name")).isEqualTo("del_days");
        assertThat(delDaysOption.getString("description")).isEqualTo("The amount of days to delete messages");

        assertThat(userOption.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "utilisateur"));
        assertThat(userOption.getObject("description_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "L'utilisateur à bannir"));

        assertThat(delDaysOption.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "nb_jours"));
        assertThat(delDaysOption.getObject("description_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "Nombre de jours de messages à supprimer"));
    }

    @Test
    void choiceLocalization()
    {
        DataObject subcommandGroup = getOption(data, "user");
        DataObject subcommand = getOption(subcommandGroup, "perm");
        DataObject delDaysOption = getOption(subcommand, "del_days");

        DataObject days1 = getChoice(delDaysOption, "1 Day");
        assertThat(days1.getString("name")).isEqualTo("1 Day");
        assertThat(days1.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "1 jour"));

        DataObject days7 = getChoice(delDaysOption, "7 Days");
        assertThat(days7.getString("name")).isEqualTo("7 Days");
        assertThat(days7.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "7 jours"));

        DataObject days14 = getChoice(delDaysOption, "14 Days");
        assertThat(days14.getString("name")).isEqualTo("14 Days");
        assertThat(days14.getObject("name_localizations"))
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty().put("fr", "14 jours"));
    }

    @Test
    void reconstructData()
    {
        final DataObject data = slashCommandData.toData();
        final DataObject reconstitutedData = CommandData.fromData(data).toData();
        assertThat(reconstitutedData.toMap()).isEqualTo(data.toMap());
    }

    private static DataObject getOption(DataObject root, String name)
    {
        Stream<DataObject> options = root.getArray("options")
                .stream(DataArray::getObject)
                .filter(option -> option.getString("name").equals(name));
        return assertExactlyOne(options);
    }

    private static DataObject getChoice(DataObject root, String name)
    {
        Stream<DataObject> choices = root.getArray("choices")
                .stream(DataArray::getObject)
                .filter(choice -> choice.getString("name").equals(name));
        return assertExactlyOne(choices);
    }

    private static <T> T assertExactlyOne(Stream<T> stream)
    {
        List<T> results = stream.collect(Collectors.toList());
        assertThat(results)
            .withRepresentation(new PrettyRepresentation())
            .hasSize(1);
        return results.get(0);
    }
}
