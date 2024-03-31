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

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalizationTest
{
    private static SlashCommandData slashCommandData;
    private static DataObject data;

    @BeforeAll
    public static void setup()
    {
        final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                .fromBundles("MyCommands", DiscordLocale.FRENCH)
                .build();

        slashCommandData = Commands.slash("ban", "Bans someone").addSubcommandGroups(
                new SubcommandGroupData("user", "Bans a member").addSubcommands(
                        new SubcommandData("perm", "Bans an user permanently").addOptions(
                                new OptionData(OptionType.STRING, "user", "The user to ban"),
                                new OptionData(OptionType.INTEGER, "del_days", "The amount of days to delete messages")
                                        .addChoices(
                                                new Command.Choice("1 Day", "1"),
                                                new Command.Choice("7 Days", "7"),
                                                new Command.Choice("14 Days", "14")
                                        )
                        ),
                        new SubcommandData("temp", "Bans an user temporarily").addOptions(
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
    public void commandLocalization()
    {
        assertThat(DataPath.getString(data, "name_localizations.fr")).isEqualTo("ban");
        assertThat(DataPath.getString(data, "description_localizations.fr")).isEqualTo("Bannis un utilisateur");
    }

    @Test
    public void subcommandLocalization()
    {
        assertThat(navigateOptions("user").getObject("name_localizations").getString("fr")).isEqualTo("utilisateur");
        assertThat(navigateOptions("user").getObject("description_localizations").getString("fr")).isEqualTo("Bannis un utilisateur");
    }

    @Test
    public void subcommandGroupLocalization()
    {
        assertThat(navigateOptions("user", "perm").getObject("name_localizations").getString("fr")).isEqualTo("permanent");
        assertThat(navigateOptions("user", "perm").getObject("description_localizations").getString("fr")).isEqualTo("Bannis un utilisateur pour toujours");
    }

    @Test
    public void optionLocalization()
    {
        assertThat(navigateOptions("user", "perm", "user").getObject("name_localizations").getString("fr")).isEqualTo("utilisateur");
        assertThat(navigateOptions("user", "perm", "user").getObject("description_localizations").getString("fr")).isEqualTo("L'utilisateur à bannir");

        assertThat(navigateOptions("user", "perm", "del_days").getObject("name_localizations").getString("fr")).isEqualTo("nb_jours");
        assertThat(navigateOptions("user", "perm", "del_days").getObject("description_localizations").getString("fr")).isEqualTo("Nombre de jours de messages à supprimer");
    }

    @Test
    public void choiceLocalization()
    {
        assertThat(navigateChoice("1 Day", "user", "perm", "del_days").getObject("name_localizations").getString("fr")).isEqualTo("1 jour");
        assertThat(navigateChoice("7 Days", "user", "perm", "del_days").getObject("name_localizations").getString("fr")).isEqualTo("7 jours");
        assertThat(navigateChoice("14 Days", "user", "perm", "del_days").getObject("name_localizations").getString("fr")).isEqualTo("14 jours");
    }

    @Test
    public void reconstructData()
    {
        final DataObject data = slashCommandData.toData();
        final DataObject reconstitutedData = CommandData.fromData(data).toData();
        assertThat(reconstitutedData.toMap()).isEqualTo(data.toMap());
    }

    private DataObject navigateOptions(String... names)
    {
        DataObject o = data;
        for (String name : names)
        {
            o = o.getArray("options").stream(DataArray::getObject)
                    .filter(s -> s.getString("name").equals(name))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Could not find an option with path: " + Arrays.toString(names)));
        }
        return o;
    }

    private DataObject navigateChoice(String choiceName, String... names)
    {
        return navigateOptions(names)
                .getArray("choices")
                .stream(DataArray::getObject)
                .filter(s -> s.getString("name").equals(choiceName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find choice '" + choiceName + "' with path: " + Arrays.toString(names)));
    }
}
