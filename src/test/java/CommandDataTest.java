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

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CommandDataTest
{
    @Test
    public void testNormal()
    {
        CommandData command = new CommandDataImpl("ban", "Ban a user from this server")
                .setDefaultEnabled(false)
                .addOption(OptionType.USER, "user", "The user to ban", true) // required before non-required
                .addOption(OptionType.STRING, "reason", "The ban reason") // test that default is false
                .addOption(OptionType.INTEGER, "days", "The duration of the ban", false); // test with explicit false

        DataObject data = command.toData();
        Assertions.assertEquals("ban", data.getString("name"));
        Assertions.assertEquals("Ban a user from this server", data.getString("description"));
        Assertions.assertFalse(data.getBoolean("default_permission"));

        DataArray options = data.getArray("options");

        DataObject option = options.getObject(0);
        Assertions.assertTrue(option.getBoolean("required"));
        Assertions.assertEquals("user", option.getString("name"));
        Assertions.assertEquals("The user to ban", option.getString("description"));

        option = options.getObject(1);
        Assertions.assertFalse(option.getBoolean("required"));
        Assertions.assertEquals("reason", option.getString("name"));
        Assertions.assertEquals("The ban reason", option.getString("description"));

        option = options.getObject(2);
        Assertions.assertFalse(option.getBoolean("required"));
        Assertions.assertEquals("days", option.getString("name"));
        Assertions.assertEquals("The duration of the ban", option.getString("description"));
    }

    @Test
    public void testSubcommand()
    {
        CommandDataImpl command = new CommandDataImpl("mod", "Moderation commands")
                .setDefaultEnabled(true)
                .addSubcommands(new SubcommandData("ban", "Ban a user from this server")
                    .addOption(OptionType.USER, "user", "The user to ban", true) // required before non-required
                    .addOption(OptionType.STRING, "reason", "The ban reason") // test that default is false
                    .addOption(OptionType.INTEGER, "days", "The duration of the ban", false)); // test with explicit false

        DataObject data = command.toData();
        Assertions.assertEquals("mod", data.getString("name"));
        Assertions.assertEquals("Moderation commands", data.getString("description"));
        Assertions.assertTrue(data.getBoolean("default_permission"));

        DataObject subdata = data.getArray("options").getObject(0);
        Assertions.assertEquals("ban", subdata.getString("name"));
        Assertions.assertEquals("Ban a user from this server", subdata.getString("description"));

        DataArray options = subdata.getArray("options");

        DataObject option = options.getObject(0);
        Assertions.assertTrue(option.getBoolean("required"));
        Assertions.assertEquals("user", option.getString("name"));
        Assertions.assertEquals("The user to ban", option.getString("description"));

        option = options.getObject(1);
        Assertions.assertFalse(option.getBoolean("required"));
        Assertions.assertEquals("reason", option.getString("name"));
        Assertions.assertEquals("The ban reason", option.getString("description"));

        option = options.getObject(2);
        Assertions.assertFalse(option.getBoolean("required"));
        Assertions.assertEquals("days", option.getString("name"));
        Assertions.assertEquals("The duration of the ban", option.getString("description"));
    }

    @Test
    public void testSubcommandGroup()
    {
        CommandDataImpl command = new CommandDataImpl("mod", "Moderation commands")
                .addSubcommandGroups(new SubcommandGroupData("ban", "Ban or unban a user from this server")
                    .addSubcommands(new SubcommandData("add", "Ban a user from this server")
                        .addOption(OptionType.USER, "user", "The user to ban", true) // required before non-required
                        .addOption(OptionType.STRING, "reason", "The ban reason") // test that default is false
                        .addOption(OptionType.INTEGER, "days", "The duration of the ban", false))); // test with explicit false

        DataObject data = command.toData();
        Assertions.assertEquals("mod", data.getString("name"));
        Assertions.assertEquals("Moderation commands", data.getString("description"));
        Assertions.assertTrue(data.getBoolean("default_permission"));

        DataObject group = data.getArray("options").getObject(0);
        Assertions.assertEquals("ban", group.getString("name"));
        Assertions.assertEquals("Ban or unban a user from this server", group.getString("description"));

        DataObject subdata = group.getArray("options").getObject(0);
        Assertions.assertEquals("add", subdata.getString("name"));
        Assertions.assertEquals("Ban a user from this server", subdata.getString("description"));
        DataArray options = subdata.getArray("options");

        DataObject option = options.getObject(0);
        Assertions.assertTrue(option.getBoolean("required"));
        Assertions.assertEquals("user", option.getString("name"));
        Assertions.assertEquals("The user to ban", option.getString("description"));

        option = options.getObject(1);
        Assertions.assertFalse(option.getBoolean("required"));
        Assertions.assertEquals("reason", option.getString("name"));
        Assertions.assertEquals("The ban reason", option.getString("description"));

        option = options.getObject(2);
        Assertions.assertFalse(option.getBoolean("required"));
        Assertions.assertEquals("days", option.getString("name"));
        Assertions.assertEquals("The duration of the ban", option.getString("description"));
    }

    @Test
    public void testRequiredThrows()
    {
        CommandDataImpl command = new CommandDataImpl("ban", "Simple ban command");
        command.addOption(OptionType.STRING, "opt", "desc");

        Assertions.assertThrows(IllegalArgumentException.class, () -> command.addOption(OptionType.STRING, "other", "desc", true));

        SubcommandData subcommand = new SubcommandData("sub", "Simple subcommand");
        subcommand.addOption(OptionType.STRING, "opt", "desc");
        Assertions.assertThrows(IllegalArgumentException.class, () -> subcommand.addOption(OptionType.STRING, "other", "desc", true));
    }

    @Test
    public void testNameChecks()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CommandDataImpl("invalid name", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CommandDataImpl("invalidName", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CommandDataImpl("valid_name", ""));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new SubcommandData("invalid name", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SubcommandData("invalidName", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SubcommandData("valid_name", ""));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new SubcommandGroupData("invalid name", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SubcommandGroupData("invalidName", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SubcommandGroupData("valid_name", ""));
    }

    @Test
    public void testChoices()
    {
        OptionData option = new OptionData(OptionType.INTEGER, "choice", "Option with choices!");
        Assertions.assertThrows(IllegalArgumentException.class, () -> option.addChoice("invalid name", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> option.addChoice("invalidName", "Valid description"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> option.addChoice("valid_name", ""));

        List<Command.Choice> choices = new ArrayList<>();
        for (int i = 0; i < 25; i++)
        {
            option.addChoice("choice_" + i, i);
            choices.add(new Command.Choice("choice_" + i, i));
        }
        Assertions.assertThrows(IllegalArgumentException.class, () -> option.addChoice("name", 100));
        Assertions.assertEquals(25, option.getChoices().size());
        Assertions.assertEquals(choices, option.getChoices());
    }
}
