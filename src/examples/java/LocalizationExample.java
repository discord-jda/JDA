import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;

import javax.security.auth.login.LoginException;

public class LocalizationExample
{
    public static void main(String[] args) throws LoginException
    {
        final String token = "BOT_TOKEN_HERE";

        //Pretty standard JDA instance creation
        final JDA jda = JDABuilder.createLight(token).build();

        //This is the function that will retrieve the localization strings from the specified bundles
        // The localized strings will be injected when the commands are sent to Discord
        // You can see the strings being used in the "resources" folder, in "MyCommands_es_ES.properties" and "MyCommands_fr.properties"
        // You can also provide a custom implementation of LocalizationFunction if you wish to
        // This would enable you for example to get localizations in other format, or, allow wildcards to save some space / remove duplicated localizations
        final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                .fromBundles("MyCommands", DiscordLocale.SPANISH, DiscordLocale.FRENCH)
                .build();

        //Your normal ban command, no need to do anything special here, except at the last line
        final SlashCommandData slashCommandData = Commands.slash("ban", "Bans someone")
                .setLocalizationFunction(localizationFunction) //Sets the localization function to use
                .addSubcommandGroups(new SubcommandGroupData("member", "Bans a member").addSubcommands(
                                new SubcommandData("perm", "Bans a member permanently").addOptions(
                                        new OptionData(OptionType.STRING, "user", "The user to ban"),
                                        new OptionData(OptionType.INTEGER, "del_days", "The amount of days to delete messages")
                                                .addChoices(
                                                        new Command.Choice("1 Day", "1"),
                                                        new Command.Choice("7 Days", "7"),
                                                        new Command.Choice("14 Days", "14")
                                                )
                                ),
                                new SubcommandData("temp", "Bans a member temporarily").addOptions(
                                        new OptionData(OptionType.STRING, "user", "The user to ban"),
                                        new OptionData(OptionType.INTEGER, "del_days", "The amount of days to delete messages")
                                                .addChoices(
                                                        new Command.Choice("1 Day", "1"),
                                                        new Command.Choice("7 Days", "7"),
                                                        new Command.Choice("14 Days", "14")
                                                )
                                )
                        )
                );

        //Manual approach to localizing commands
        final CommandData messageCommand = Commands.message("Show raw content")
                .setNameLocalization(DiscordLocale.SPANISH, "Mostrar el contenido en bruto")
                .setNameLocalization(DiscordLocale.FRENCH, "Afficher contenu brut");

        final CommandData userCommand = Commands.user("Show avatar")
                .setNameLocalization(DiscordLocale.SPANISH, "Mostrar avatar")
                .setNameLocalization(DiscordLocale.FRENCH, "Afficher avatar");

        //Manual description localization would work the exact same

        jda.updateCommands()
            .addCommands(slashCommandData, messageCommand, userCommand)
            .queue();
    }
}
