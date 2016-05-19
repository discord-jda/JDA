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
import net.dv8tion.jda.events.channel.text.*;
import net.dv8tion.jda.events.channel.voice.*;
import net.dv8tion.jda.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class ChannelListenerExample extends ListenerAdapter
{
    public static void main(String[] args)
    {
        try
        {
            new JDABuilder()
                    .setBotToken("TOKEN")
                    .addListener(new ChannelListenerExample())
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

    // ------------------------------
    // ----- TextChannel Events -----
    // ------------------------------
    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event)
    {
        System.out.println("A TextChannel named: " + event.getChannel().getName() + " was created in guild: " + event.getGuild().getName());
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event)
    {
        System.out.println("A TextChannel named: " + event.getChannel().getName() + " was deleted from guild: " + event.getGuild().getName());
    }

    @Override
    public void onTextChannelUpdateName(TextChannelUpdateNameEvent event)
    {
        System.out.println("TextChannel " + event.getOldName() + " was renamed: " + event.getChannel().getName() + " in guild " + event.getGuild().getName());
    }

    @Override
    public void onTextChannelUpdateTopic(TextChannelUpdateTopicEvent event)
    {
        System.out.println("The " + event.getChannel().getName() + " TextChannel's topic just from\n" + event.getOldTopic() + "\n to\n" + event.getChannel().getTopic());
    }

    @Override
    public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent event)
    {
        System.out.println("The position of " + event.getChannel().getName() + " TextChannl just moved from " + event.getOldPosition() + " to " + event.getChannel().getPosition());
        System.out.println("Be sure to update your channel lists!");
    }

    @Override
    public void onTextChannelUpdatePermissions(TextChannelUpdatePermissionsEvent event)
    {
        System.out.println("TextChannel Permissions changed. There are a lot of details in this event and I'm too lazy to show them all. Just read the Javadoc ;_;");
    }

    // ------------------------------
    // ---- VoiceChannel Events -----
    // ------------------------------
    @Override
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event)
    {
        System.out.println("A VoiceChannel named: " + event.getChannel().getName() + " was created in guild: " + event.getGuild().getName());
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event)
    {
        System.out.println("A VoiceChannel named: " + event.getChannel().getName() + " was deleted from guild: " + event.getGuild().getName());
    }

    @Override
    public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event)
    {
        System.out.println("VoiceChannel " + event.getOldName() + " was renamed: " + event.getChannel().getName() + " in guild " + event.getGuild().getName());
    }

    //No onVoiceChannelUpdateTopic method because VoiceChannels don't have Topics.

    @Override
    public void onVoiceChannelUpdatePosition(VoiceChannelUpdatePositionEvent event)
    {
        System.out.println("The position of " + event.getChannel().getName() + " VoiceChannl just moved from " + event.getOldPosition() + " to " + event.getChannel().getPosition());
        System.out.println("Be sure to update your channel lists!");
    }

    @Override
    public void onVoiceChannelUpdatePermissions(VoiceChannelUpdatePermissionsEvent event)
    {
        System.out.println("VoiceChannel Permissions changed. There are a lot of details in this event and I'm too lazy to show them all. Just read the Javadoc ;_;");
    }
}
