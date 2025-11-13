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

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioEchoExample extends ListenerAdapter {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Unable to start without token!");
            System.exit(1);
        }
        String token = args[0];

        // We only need 3 gateway intents enabled for this example:
        EnumSet<GatewayIntent> intents = EnumSet.of(
                // We need messages in guilds to accept commands from users
                GatewayIntent.GUILD_MESSAGES,
                // We need voice states to connect to the voice channel
                GatewayIntent.GUILD_VOICE_STATES,
                // Enable access to message.getContentRaw()
                GatewayIntent.MESSAGE_CONTENT);

        // Start the JDA session with default mode (voice member cache)
        JDABuilder.createDefault(token, intents)
                // Start listening with this listener
                .addEventListeners(new AudioEchoExample())
                // Inform users that we are jammin' it out
                .setActivity(Activity.listening("to jams"))
                // Please don't disturb us while we're jammin'
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                // Enable the VOICE_STATE cache to find a user's connected voice channel
                .enableCache(CacheFlag.VOICE_STATE)
                .build(); // Login with these options
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor();
        String content = message.getContentRaw();
        Guild guild = event.getGuild();

        // Ignore message if bot
        if (author.isBot()) {
            return;
        }

        // We only want to handle message in Guilds
        if (!event.isFromGuild()) {
            return;
        }

        if (content.startsWith("!echo ")) {
            String arg = content.substring("!echo ".length());
            onEchoCommand(event, guild, arg);
        } else if (content.equals("!echo")) {
            onEchoCommand(event);
        }
    }

    /**
     * Handle command without arguments.
     *
     * @param event
     *        The event for this command
     */
    private void onEchoCommand(MessageReceivedEvent event) {
        // Note: None of these can be null due to our configuration with the JDABuilder!
        // Member is the context of the user for the specific guild, containing voice state and roles
        Member member = event.getMember();
        // Check the current voice state of the user
        GuildVoiceState voiceState = member.getVoiceState();
        // Use the channel the user is currently connected to
        AudioChannel channel = voiceState.getChannel();
        if (channel != null) {
            // Join the channel of the user
            connectTo(channel);
            // Tell the user about our success
            onConnecting(channel, event.getChannel());
        } else {
            // Tell the user about our failure
            onUnknownChannel(event.getChannel(), "your voice channel");
        }
    }

    /**
     * Handle command with arguments.
     *
     * @param event
     *        The event for this command
     * @param guild
     *        The guild where its happening
     * @param arg
     *        The input argument
     */
    private void onEchoCommand(MessageReceivedEvent event, Guild guild, String arg) {
        // This is a regular expression that ensures the input consists of digits
        boolean isNumber = arg.matches("\\d+");
        VoiceChannel channel = null;

        // The input is an id?
        if (isNumber) {
            channel = guild.getVoiceChannelById(arg);
        }

        // Then the input must be a name?
        if (channel == null) {
            List<VoiceChannel> channels = guild.getVoiceChannelsByName(arg, true);
            // Make sure we found at least one exact match
            if (!channels.isEmpty()) {
                // We found a channel! This cannot be null.
                channel = channels.get(0);
            }
        }

        MessageChannel messageChannel = event.getChannel();
        if (channel == null) {
            // Let the user know about our failure
            onUnknownChannel(messageChannel, arg);
            return;
        }

        // We found a channel to connect to!
        connectTo(channel);
        // Let the user know, we were successful!
        onConnecting(channel, messageChannel);
    }

    /**
     * Inform user about successful connection.
     *
     * @param channel
     *        The voice channel we connected to
     * @param messageChannel
     *        The text channel to send the message in
     */
    private void onConnecting(AudioChannel channel, MessageChannel messageChannel) {
        // never forget to queue()!
        messageChannel.sendMessage("Connecting to " + channel.getName()).queue();
    }

    /**
     * The channel to connect to is not known to us.
     *
     * @param channel
     *        The message channel (text channel abstraction) to send failure information to
     * @param comment
     *        The information of this channel
     */
    private void onUnknownChannel(MessageChannel channel, String comment) {
        // never forget to queue()!
        channel.sendMessage("Unable to connect to ``" + comment + "``, no such channel!")
                .queue();
    }

    /**
     * Connect to requested channel and start echo handler
     *
     * @param channel
     *        The channel to connect to
     */
    private void connectTo(AudioChannel channel) {
        Guild guild = channel.getGuild();
        // Get an audio manager for this guild, this will be created upon first use for each guild
        AudioManager audioManager = guild.getAudioManager();
        // Create our Send/Receive handler for the audio connection
        EchoHandler handler = new EchoHandler();

        // The order of the following instructions does not matter!

        // Set the sending handler to our echo system
        audioManager.setSendingHandler(handler);
        // Set the receiving handler to the same echo system, otherwise we can't echo anything
        audioManager.setReceivingHandler(handler);
        // Connect to the voice channel
        audioManager.openAudioConnection(channel);
    }

    public static class EchoHandler implements AudioSendHandler, AudioReceiveHandler {
        /*
           All methods in this class are called by JDA threads when resources are available/ready for processing.

           The receiver will be provided with the latest 20ms of PCM stereo audio
           Note you can receive even while setting yourself to deafened!

           The sender will provide 20ms of PCM stereo audio (pass-through) once requested by JDA
           When audio is provided JDA will automatically set the bot to speaking!
        */
        private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

        /* Receive Handling */

        // combine multiple user audio-streams into a single one
        @Override
        public boolean canReceiveCombined() {
            // limit queue to 10 entries, if that is exceeded we can not receive more until the send system catches up
            return queue.size() < 10;
        }

        @Override
        public void handleCombinedAudio(CombinedAudio combinedAudio) {
            // we only want to send data when a user actually sent something, otherwise we would just send silence
            if (combinedAudio.getUsers().isEmpty()) {
                return;
            }

            byte[] data = combinedAudio.getAudioData(1.0f); // volume at 100% = 1.0 (50% = 0.5 / 55% = 0.55)
            queue.add(data);
        }
        /*
                Disable per-user audio since we want to echo the entire channel and not specific users.

                @Override // give audio separately for each user that is speaking
                public boolean canReceiveUser()
                {
                    // this is not useful if we want to echo the audio of the voice channel, thus disabled for this purpose
                    return false;
                }

                @Override
                public void handleUserAudio(UserAudio userAudio) {} // per-user is not helpful in an echo system
        */

        /* Send Handling */

        @Override
        public boolean canProvide() {
            // If we have something in our buffer we can provide it to the send system
            return !queue.isEmpty();
        }

        @Override
        public ByteBuffer provide20MsAudio() {
            // use what we have in our buffer to send audio as PCM
            byte[] data = queue.poll();
            // Wrap this in a java.nio.ByteBuffer
            return data == null ? null : ByteBuffer.wrap(data);
        }

        @Override
        public boolean isOpus() {
            // since we send audio that is received from discord we don't have opus but PCM
            return false;
        }
    }
}
