/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageEmbedEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;

public class MessageListenerExample extends ListenerAdapter
{
    /**
     * Used for the internal test bot.
     *
     * @param args not used
     */
    public static void main(String[] args)
    {
        try
        {
            new JDABuilder()
                    .setBotToken("TOKEN")
                    .addListener(new MessageListenerExample())
                    .buildBlocking();
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("The config was not populated. Please enter a bot token.");
        }
        catch (LoginException e)
        {
            System.out.println("The provided bot token was incorrect. Please provide valid details.");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageEmbed(MessageEmbedEvent event)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Found embed(s) Types: ");
        event.getMessageEmbeds().stream().forEach(embed -> builder.append(embed.getType()).append(", "));
        System.out.println(builder.toString().substring(0, builder.length() - 2));
    }

    //Note: onMessageReceived combines both the PrivateMessageReceivedEvent and GuildMessageReceivedEvent.
    //If you do not want to capture both in one method, consider using
    // onPrivateMessageReceived(PrivateMessageReceivedEvent event)
    //    or
    // onGuildMessageReceived(GuildMessageReceivedEvent event)
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        boolean isPrivate = event.isPrivate();

        if (!isPrivate)
        {
            System.out.printf("[%s][%s]\t%s: %s\n", event.getGuild().getName(), event.getTextChannel().getName(),
                    event.getAuthor().getUsername(), event.getMessage().getContent());
        }
        else
        {
            System.out.printf("[PM]\t%s: %s\n", event.getAuthor().getUsername(), event.getMessage().getContent());
        }

        //Printing all mentions, if the Message was not private
        if (!isPrivate)
        {
            List<User> mentions = event.getMessage().getMentionedUsers();
            StringBuilder builder = new StringBuilder();
            for (User u : mentions)
            {
                builder.append(u.getUsername()).append(", ");
            }
            String mentionsMessage = builder.toString();
            if (!mentionsMessage.isEmpty())
            {
                mentionsMessage = mentionsMessage.substring(0, mentionsMessage.length() - 2);
                System.out.println("The follow users were mentioned: " + mentionsMessage);
            }
        }
    }
}
