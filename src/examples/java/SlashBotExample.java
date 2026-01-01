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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class SlashBotExample extends ListenerAdapter {
    public static void main(String[] args) {
        // slash commands don't need any intents
        EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
        JDA jda = JDABuilder.createLight("BOT_TOKEN_HERE", intents)
                .addEventListeners(new SlashBotExample())
                .build();

        // You might need to reload your Discord client if you don't see the commands
        CommandListUpdateAction commands = jda.updateCommands();

        // Moderation commands with required options
        commands.addCommands(Commands.slash("ban", "Ban a user from this server. Requires permission to ban users.")
                // USER type allows to include members of the server or other users by id
                .addOptions(new OptionData(USER, "user", "The user to ban")
                        // This must be filled out by the command user
                        .setRequired(true))
                // This is optional by default and does not need to be provided
                .addOptions(new OptionData(INTEGER, "del_days", "Delete messages from the past days.")
                        // Only allow values between 0 and 7 (inclusive)
                        .setRequiredRange(0, 7))
                // optional reason
                .addOptions(new OptionData(STRING, "reason", "The ban reason to use (default: Banned by <user>)"))
                // This way the command can only be executed from a guild, and not the DMs
                .setContexts(InteractionContextType.GUILD)
                // Only members with the BAN_MEMBERS permission are going to see this command
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)));

        // Simple reply commands
        commands.addCommands(Commands.slash("say", "Makes the bot say what you tell it to")
                // Allow the command to be used anywhere (Bot DMs, Guild, Friend DMs, Group DMs)
                .setContexts(InteractionContextType.ALL)
                // Allow the command to be installed anywhere (Guilds, Users)
                .setIntegrationTypes(IntegrationType.ALL)
                // you can add required options like this too
                .addOption(STRING, "content", "What the bot should say", true));

        // Commands without any inputs
        commands.addCommands(Commands.slash("leave", "Make the bot leave the server")
                // The default integration types are GUILD_INSTALL.
                // Can't use this in DMs, and in guilds the bot isn't in.
                .setContexts(InteractionContextType.GUILD)
                // only admins should be able to use this command.
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commands.addCommands(Commands.slash("prune", "Prune messages from this channel")
                // simple optional argument
                .addOption(INTEGER, "amount", "How many messages to prune (Default 100)")
                // The default integration types are GUILD_INSTALL.
                // Can't use this in DMs, and in guilds the bot isn't in.
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)));

        // Send the new set of commands to discord,
        // this will override any existing global commands with the new set provided here
        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands from guilds
        if (event.getGuild() == null) {
            return;
        }
        switch (event.getName()) {
            case "ban":
                // the "user" option is required, so it doesn't need a null-check here
                Member member = event.getOption("user").getAsMember();
                User user = event.getOption("user").getAsUser();
                ban(event, user, member);
                break;
            case "say":
                // content is required so no null-check here
                say(event, event.getOption("content").getAsString());
                break;
            case "leave":
                leave(event);
                break;
            case "prune":
                // 2 stage command with a button prompt
                prune(event);
                break;
            default:
                event.reply("I can't handle that command right now :(")
                        .setEphemeral(true)
                        .queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // this is the custom id we specified in our button
        String[] id = event.getComponentId().split(":");
        String authorId = id[0];
        String type = id[1];
        // Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
        if (!authorId.equals(event.getUser().getId())) {
            return;
        }

        // acknowledge the button was clicked, otherwise the interaction will fail
        event.deferEdit().queue();

        MessageChannel channel = event.getChannel();
        switch (type) {
            case "prune" -> {
                int amount = Integer.parseInt(id[2]);
                event.getChannel()
                        .getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(amount)
                        .thenAccept(channel::purgeMessages);

                // delete the prompt message with our buttons
                event.getHook().deleteOriginal().queue();
            }
            case "delete" -> {
                event.getHook().deleteOriginal().queue();
            }
        }
    }

    public void ban(SlashCommandInteractionEvent event, User user, Member member) {
        // Let the user know we received the command before doing anything else
        event.deferReply(true).queue();
        // This is a special webhook that allows you to send messages
        // without having permissions in the channel and also allows ephemeral messages
        InteractionHook hook = event.getHook();

        // All messages here will now be ephemeral implicitly
        hook.setEphemeral(true);
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            hook.sendMessage("You do not have the required permissions to ban users from this server.")
                    .queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
            hook.sendMessage("I don't have the required permissions to ban users from this server.")
                    .queue();
            return;
        }

        if (member != null && !selfMember.canInteract(member)) {
            hook.sendMessage("This user is too powerful for me to ban.").queue();
            return;
        }

        // optional command argument, fall back to 0 if not provided
        int delDays = event.getOption(
                "del_days",
                0,
                // this last part is a method reference used to "resolve" the option value
                OptionMapping::getAsInt);

        // optional ban reason with a lazy evaluated fallback (supplier)
        String reason = event.getOption(
                "reason",
                // used if getOption("reason") is null (not provided)
                () -> "Banned by " + event.getUser().getName(),
                // used if getOption("reason") is not null (provided)
                OptionMapping::getAsString);

        // Ban the user and send a success response
        event.getGuild()
                .ban(user, delDays, TimeUnit.DAYS)
                // audit-log ban reason (sets X-AuditLog-Reason header)
                .reason(reason)
                // chain a followup message after the ban is executed
                .flatMap(v -> hook.sendMessage("Banned user " + user.getName()))
                // execute the entire call chain
                .queue();
    }

    public void say(SlashCommandInteractionEvent event, String content) {
        // This requires no permissions!
        event.reply(content).queue();
    }

    public void leave(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.reply("You do not have permissions to kick me.")
                    .setEphemeral(true)
                    .queue();
        } else {
            // Yep we received it
            event.reply("Leaving the server... :wave:")
                    // Leave server after acknowledging the command
                    .flatMap(v -> event.getGuild().leave())
                    .queue();
        }
    }

    public void prune(SlashCommandInteractionEvent event) {
        OptionMapping amountOption = event.getOption("amount");

        // This is configured to be optional so check for null
        int amount = amountOption == null
                ? 100 // default 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // enforcement: must be between 2-200

        String userId = event.getUser().getId();

        // prompt the user with a button menu
        event.reply("This will delete " + amount + " messages.\nAre you sure?")
                .addComponents(ActionRow.of(
                        // this means "<style>(<id>, <label>)"
                        // you can encode anything you want in the id (up to 100 characters)
                        Button.secondary(userId + ":delete", "Nevermind!"),
                        Button.danger(
                                // the first parameter is the component id we use in onButtonInteraction above
                                userId + ":prune:" + amount, "Yes!")))
                .queue();
    }
}
