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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.audio.player.Player;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AudioExample extends ListenerAdapter
{

    /**
     * This map is used as cache and contains all player instances
     */
    private Map<String,Player> players = new HashMap<>();

    public static void main(String[] args)
    {
        try
        {
            JDA api = new JDABuilder()
                    .setBotToken("TOKEN")
                    .addListener(new AudioExample())
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

    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        String message = event.getMessage().getContent();
        Player player = players.get(event.getGuild().getId());

        //Start an audio connection with a VoiceChannel
        if (message.startsWith("join "))
        {
            //Separates the name of the channel so that we can search for it
            String chanName = message.substring(5);

            //Scans through the VoiceChannels in this Guild, looking for one with a case-insensitive matching name.
            VoiceChannel channel = event.getGuild().getVoiceChannels().stream().filter(
                    vChan -> vChan.getName().equalsIgnoreCase(chanName))
                    .findFirst().orElse(null);  //If there isn't a matching name, return null.
            if (channel == null)
            {
                event.getChannel().sendMessage("There isn't a VoiceChannel in this Guild with the name: '" + chanName + "'");
                return;
            }
            event.getGuild().getAudioManager().openAudioConnection(channel);
        }
        //Disconnect the audio connection with the VoiceChannel.
        if (message.equals("leave"))
            event.getGuild().getAudioManager().closeAudioConnection();

        //Start playing audio with our FilePlayer. If we haven't created and registered a FilePlayer yet, do that.
        if (message.equals("play"))
        {
            //If the player didn't exist, create it and start playback.
            if (player == null)
            {
                File audioFile = null;
                URL audioUrl = null;
                try
                {
                    audioFile = new File("BABYMETAL Gimme chocolate!!.wav");
//                    audioUrl = new URL("https://dl.dropboxusercontent.com/u/41124983/anime-48000.mp3?dl=1");

                    player = new FilePlayer(audioFile);
//                    player = new URLPlayer(event.getJDA(), audioUrl);

                    //Add the new player to the cache
                    players.put(event.getGuild().getId(), player);

                    //Provide the handler to send audio.
                    //NOTE: You don't have to set the handler each time you create an audio connection with the
                    // AudioManager. Handlers persist between audio connections. Furthermore, handler playback is also
                    // paused when a connection is severed (closeAudioConnection), however it would probably be better
                    // to pause the play back yourself before severing the connection (If you are using a player class
                    // you could just call the pause() method. Otherwise, make canProvide() return false).
                    // Once again, you don't HAVE to pause before severing an audio connection,
                    // but it probably would be good to do.
                    event.getGuild().getAudioManager().setSendingHandler(player);

                    //Start playback. This will only start after the AudioConnection has completely connected.
                    //NOTE: "completely connected" is not just joining the VoiceChannel. Think about when your Discord
                    // client joins a VoiceChannel. You appear in the channel lobby immediately, but it takes a few
                    // moments before you can start communicating.
                    player.play();
                }
                catch (IOException e)
                {
                    event.getChannel().sendMessage("Could not load the file. Does it exist?  File name: " + audioFile.getName());
                    e.printStackTrace();
                }
                catch (UnsupportedAudioFileException e)
                {
                    event.getChannel().sendMessage("Could not load file. It either isn't an audio file or isn't a" +
                            " recognized audio format.");
                    e.printStackTrace();
                }
            }
            else if (player.isStarted() && player.isStopped())  //If it did exist, has it been stop()'d before?
            {
                event.getChannel().sendMessage("The player has been stopped. To start playback, please use 'restart'");
                return;
            }
            else    //It exists and hasn't been stopped before, so play. Note: if it was already playing, this will have no effect.
            {
                player.play();
            }

        }

        //You can't pause, stop or restart before a player has even been created!
        if (player == null && (message.equals("pause") || message.equals("stop") || message.equals("restart")))
        {
            event.getChannel().sendMessage("You need to 'play' before you can preform that command.");
            return;
        }

        if (player != null)
        {
            if (message.equals("pause"))
                player.pause();
            if (message.equals("stop"))
                player.stop();
            if (message.equals("restart"))
                player.restart();
        }
    }

}
