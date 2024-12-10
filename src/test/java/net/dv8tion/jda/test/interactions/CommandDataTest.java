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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.test.PrettyRepresentation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.test.ChecksHelper.assertStringChecks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class CommandDataTest
{
    private static DataObject defaultCommand()
    {
        return DataObject.empty()
                .put("type", 1)
                .put("contexts", DataArray.empty().add(InteractionContextType.GUILD.getType()).add(InteractionContextType.BOT_DM.getType()))
                .put("integration_types", DataArray.empty().add(IntegrationType.GUILD_INSTALL.getType()))
                .put("name_localizations", DataObject.empty())
                .put("description_localizations", DataObject.empty())
                .put("nsfw", false)
                .put("default_member_permissions", null)
                .put("options", DataArray.empty());
    }

    private static DataObject defaultOption(OptionType type, String name, String description)
    {
        return DataObject.empty()
                .put("type", type.getKey())
                .put("name", name)
                .put("description", description)
                .put("required", false)
                .put("autocomplete", false)
                .put("name_localizations", DataObject.empty())
                .put("description_localizations", DataObject.empty());
    }

    @Test
    void testNormal()
    {
        CommandData command = new CommandDataImpl("ban", "Ban a user from this server")
                .setContexts(InteractionContextType.GUILD)
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .addOption(OptionType.USER, "user", "The user to ban", true) // required before non-required
                .addOption(OptionType.STRING, "reason", "The ban reason") // test that default is false
                .addOption(OptionType.INTEGER, "days", "The duration of the ban", false); // test with explicit false

        DataObject data = command.toData();

        assertThat(data)
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(defaultCommand()
                .put("type", 1)
                .put("name", "ban")
                .put("description", "Ban a user from this server")
                .put("contexts", DataArray.empty().add(InteractionContextType.GUILD.getType()))
                .put("integration_types", DataArray.empty().add(IntegrationType.USER_INSTALL.getType()))
                .put("default_member_permissions", "4")
                .put("options", DataArray.empty()
                    .add(defaultOption(OptionType.USER, "user", "The user to ban").put("required", true))
                    .add(defaultOption(OptionType.STRING, "reason", "The ban reason"))
                    .add(defaultOption(OptionType.INTEGER, "days", "The duration of the ban"))));
    }

    @Test
    void testDeprecatedGuildOnly()
    {
        CommandDataImpl command = new CommandDataImpl("ban", "Ban a user from this server")
                .setGuildOnly(true);

        assertThat(command.toData())
                .withRepresentation(new PrettyRepresentation())
                .isEqualTo(defaultCommand()
                        .put("type", 1)
                        .put("name", "ban")
                        .put("description", "Ban a user from this server")
                        .put("contexts", DataArray.empty().add(InteractionContextType.GUILD.getType())));

        command.setGuildOnly(false);

        assertThat(command.toData())
                .withRepresentation(new PrettyRepresentation())
                .isEqualTo(defaultCommand()
                        .put("type", 1)
                        .put("name", "ban")
                        .put("description", "Ban a user from this server")
                        .put("contexts", DataArray.empty().add(InteractionContextType.GUILD.getType()).add(InteractionContextType.BOT_DM.getType())));
    }

    @Test
    void testDefaultMemberPermissions()
    {
        CommandData command = new CommandDataImpl("ban", "Ban a user from this server")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);

        assertThat(command.toData().get("default_member_permissions")).isEqualTo("0");

        command.setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        assertThat(command.toData().opt("default_member_permissions")).isEmpty();
    }

    @Test
    void testSubcommand()
    {
        CommandDataImpl command = new CommandDataImpl("mod", "Moderation commands")
                .addSubcommands(new SubcommandData("ban", "Ban a user from this server")
                    .addOption(OptionType.USER, "user", "The user to ban", true) // required before non-required
                    .addOption(OptionType.STRING, "reason", "The ban reason") // test that default is false
                    .addOption(OptionType.INTEGER, "days", "The duration of the ban", false)); // test with explicit false

        assertThat(command.toData())
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(defaultCommand()
                .put("name", "mod")
                .put("description", "Moderation commands")
                    .put("options", DataArray.empty()
                        .add(defaultOption(OptionType.SUB_COMMAND, "ban", "Ban a user from this server")
                            .remove("autocomplete")
                            .remove("required")
                            .put("options", DataArray.empty()
                                .add(defaultOption(OptionType.USER, "user", "The user to ban").put("required", true))
                                .add(defaultOption(OptionType.STRING, "reason", "The ban reason"))
                                .add(defaultOption(OptionType.INTEGER, "days", "The duration of the ban"))))));
    }

    @Test
    void testSubcommandGroup()
    {
        CommandDataImpl command = new CommandDataImpl("mod", "Moderation commands")
                .addSubcommandGroups(new SubcommandGroupData("ban", "Ban or unban a user from this server")
                    .addSubcommands(new SubcommandData("add", "Ban a user from this server")
                        .addOption(OptionType.USER, "user", "The user to ban", true) // required before non-required
                        .addOption(OptionType.STRING, "reason", "The ban reason") // test that default is false
                        .addOption(OptionType.INTEGER, "days", "The duration of the ban", false))); // test with explicit false

        assertThat(command.toData())
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(defaultCommand()
                .put("name", "mod")
                .put("description", "Moderation commands")
                .put("options", DataArray.empty()
                    .add(defaultOption(OptionType.SUB_COMMAND_GROUP, "ban", "Ban or unban a user from this server")
                        .remove("autocomplete")
                        .remove("required")
                        .put("options", DataArray.empty()
                            .add(defaultOption(OptionType.SUB_COMMAND, "add", "Ban a user from this server")
                                .remove("autocomplete")
                                .remove("required")
                                .put("options", DataArray.empty()
                                    .add(defaultOption(OptionType.USER, "user", "The user to ban").put("required", true))
                                    .add(defaultOption(OptionType.STRING, "reason", "The ban reason"))
                                    .add(defaultOption(OptionType.INTEGER, "days", "The duration of the ban"))))))));
    }

    @Test
    void testRequiredThrows()
    {
        CommandDataImpl command = new CommandDataImpl("ban", "Simple ban command");
        command.addOption(OptionType.STRING, "opt", "desc");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> command.addOption(OptionType.STRING, "other", "desc", true))
            .withMessage("Cannot add required options after non-required options!");

        SubcommandData subcommand = new SubcommandData("sub", "Simple subcommand");
        subcommand.addOption(OptionType.STRING, "opt", "desc");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> subcommand.addOption(OptionType.STRING, "other", "desc", true))
            .withMessage("Cannot add required options after non-required options!");
    }

    @Test
    void testNameChecks()
    {
        assertStringChecks("Name", input -> new CommandDataImpl(input, "Valid description"))
            .checksNotNull()
            .checksRange(1, 32)
            .checksLowercaseOnly()
            .checksRegex("invalid name", Checks.ALPHANUMERIC_WITH_DASH);

        assertStringChecks("Name", input -> new SubcommandData(input, "Valid description"))
            .checksNotNull()
            .checksRange(1, 32)
            .checksLowercaseOnly()
            .checksRegex("invalid name", Checks.ALPHANUMERIC_WITH_DASH);

        assertStringChecks("Name", input -> new SubcommandGroupData(input, "Valid description"))
            .checksNotNull()
            .checksRange(1, 32)
            .checksLowercaseOnly()
            .checksRegex("invalid name", Checks.ALPHANUMERIC_WITH_DASH);

        assertStringChecks("Description", input -> new CommandDataImpl("valid_name", input))
            .checksNotNull()
            .checksRange(1, 100);

        assertStringChecks("Description", input -> new SubcommandData("valid_name", input))
            .checksNotNull()
            .checksRange(1, 100);

        assertStringChecks("Description", input -> new SubcommandGroupData("valid_name", input))
            .checksNotNull()
            .checksRange(1, 100);
    }

    @Test
    void testChoices()
    {
        OptionData stringOption = new OptionData(OptionType.STRING, "choice", "Option with choices!");

        assertStringChecks("Value", value -> stringOption.addChoice("valid_name", value))
            .checksNotEmpty();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> stringOption.addChoice("invalid name", 0))
            .withMessage("Cannot add long choice for OptionType.STRING");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> stringOption.addChoice("invalidName", 0.0))
            .withMessage("Cannot add double choice for OptionType.STRING");

        OptionData intOption = new OptionData(OptionType.INTEGER, "choice", "Option with choices!");
        List<Command.Choice> choices = new ArrayList<>();
        for (int i = 0; i < 25; i++)
        {
            intOption.addChoice("choice_" + i, i);
            choices.add(new Command.Choice("choice_" + i, i));
        }
        assertThatIllegalArgumentException()
            .isThrownBy(() -> intOption.addChoice("name", 100))
            .withMessage("Cannot have more than 25 choices for an option!");
        assertThat(intOption.getChoices())
            .hasSize(25);
        assertThat(intOption.getChoices())
            .isEqualTo(choices);
    }
}
