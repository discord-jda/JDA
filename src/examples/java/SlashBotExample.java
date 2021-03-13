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
import net.dv8tion.jda.api.commands.CommandHook;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.OptionData;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

import static net.dv8tion.jda.api.entities.Command.OptionType.*;

public class SlashBotExample extends ListenerAdapter
{
    public static void main(String[] args) throws LoginException
    {
        JDA jda = JDABuilder.createLight("BOT_TOKEN_HERE", EnumSet.noneOf(GatewayIntent.class)) // slash commands don't need any intents
                .addEventListeners(new SlashBotExample())
                .build();

        // These commands take up to an hour to be activated after creation/update/delete
        CommandUpdateAction commands = jda.updateCommands();

        // Moderation commands with required options
        commands.addCommands(
            new CommandData("ban", "Ban a user from this server. Requires permission to ban users.")
                .addOption(new OptionData(USER, "user", "The user to ban") // USER type allows to include members of the server or other users by id
                    .setRequired(true)) // This command requires a parameter
                .addOption(new OptionData(INTEGER, "delDays", "Delete messages from the past days.")) // This is optional
        );

        // Simple reply commands
        commands.addCommands(
            new CommandData("say", "Makes the bot say what you tell it to")
                .addOption(new OptionData(STRING, "content", "What the bot should say")
                    .setRequired(true))
        );

        // Commands without any inputs
        commands.addCommands(
            new CommandData("leave", "Make the bot leave the server")
        );

        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        commands.queue();
    }


    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        // Only accept commands from guilds
        if (event.getGuild() == null)
            return;
        Member member;
        User user;
        switch (event.getName())
        {
        case "ban":
            member = event.getOption("user").getAsMember(); // the "user" option is required so it doesn't need a null-check here
            user = event.getOption("user").getAsUser();
            ban(event, user, member);
            break;
        case "kick":
            member = event.getOption("user").getAsMember(); // the "user" option is required so it doesn't need a null-check here
            user = event.getOption("user").getAsUser();
            kick(event, user, member);
            break;
        case "leave":
            leave(event);
            break;
        default:
            event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    public void ban(SlashCommandEvent event, User user, Member member)
    {
        event.acknowledge(true).queue(); // Let the user know we received the command before doing anything else
        CommandHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
        hook.setEphemeral(true); // All messages here will now be ephemeral implicitly
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS))
        {
            hook.sendMessage("You do not have the required permissions to ban users from this server.").queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.BAN_MEMBERS))
        {
            hook.sendMessage("I don't have the required permissions to ban users from this server.").queue();
            return;
        }

        if (member != null && !selfMember.canInteract(member))
        {
            hook.sendMessage("This user is too powerful for me to ban.").queue();
            return;
        }

        int delDays = 0;
        SlashCommandEvent.OptionData option = event.getOption("delDays");
        if (option != null) // null = not provided
            delDays = (int) Math.max(0, Math.min(7, option.getAsLong()));
        // Ban the user and send a success response
        event.getGuild().ban(user, delDays)
            .flatMap(v -> hook.sendMessage("Banned user " + user.getAsTag()))
            .queue();
    }

    public void kick(SlashCommandEvent event, User user, Member member)
    {
        if (member == null)
        {
            event.reply("The provided user is not a member of this server.").setEphemeral(true).queue();
            return;
        }

        event.acknowledge(true).queue(); // Let the user know we received the command before doing anything else
        CommandHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
        hook.setEphemeral(true); // All messages here will now be ephemeral implicitly
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS))
        {
            hook.sendMessage("You do not have the required permissions to kick users from this server.").queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.KICK_MEMBERS))
        {
            hook.sendMessage("I don't have the required permissions to kick users from this server.").queue();
            return;
        }

        if (!selfMember.canInteract(member))
        {
            hook.sendMessage("This user is too powerful for me to kick.").queue();
            return;
        }

        // Kick the user and send a success response
        member.kick()
            .flatMap(v -> hook.sendMessage("Kicked user " + user.getAsTag()))
            .queue();
    }

    public void leave(SlashCommandEvent event)
    {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS))
            event.reply("You do not have permissions to kick me.").setEphemeral(true).queue();
        else
            event.reply("Leaving the server... :wave:") // Yep we received it
                 .flatMap(v -> event.getGuild().leave()) // Leave server after acknowledging the command
                 .queue();
    }
}
