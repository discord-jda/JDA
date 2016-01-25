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
package net.dv8tion.jda.managers;

import com.sun.jna.Platform;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.audio.AudioConnection;
import net.dv8tion.jda.audio.AudioReceiveHandler;
import net.dv8tion.jda.audio.AudioSendHandler;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.NativeUtils;
import net.dv8tion.jda.utils.ServiceUtil;
import org.json.JSONObject;

import java.io.IOException;

public class AudioManager
{
    //This value is set at the bottom of this file.
    private static boolean AUDIO_SUPPORTED;
    public static String OPUS_LIB_NAME;

    private final JDAImpl api;
    private AudioConnection audioConnection = null;
    private VoiceChannel queuedAudioConnection = null;

    private AudioSendHandler sendHandler;
    private AudioReceiveHandler receiveHandler;

    public AudioManager(JDAImpl api)
    {
        this.api = api;
    }
    public void openAudioConnection(VoiceChannel channel)
    {
        if (audioConnection != null)
            throw new IllegalStateException("Cannot have more than 1 audio connection at a time. Please close existing" +
                    " connection before attempting to open a new connection.");
        if (queuedAudioConnection != null)
            throw new IllegalStateException("Already attempting to start an AudioConnection with a VoiceChannel!\n" +
                    "Currently Attempting Channel ID: " + queuedAudioConnection.getId() + "  |  New Attempt Channel ID: " + channel.getId());
        queuedAudioConnection = channel;
        JSONObject obj = new JSONObject()
                .put("op", 4)
                .put("d", new JSONObject()
                        .put("guild_id", channel.getGuild().getId())
                        .put("channel_id", channel.getId())
                        .put("self_mute", false)
                        .put("self_deaf", false)
                );
        api.getClient().send(obj.toString());
    }

    public void closeAudioConnection()
    {
        if (audioConnection == null)
            return;
        this.audioConnection.close();
        this.audioConnection = null;
    }

    public JDA getJDA()
    {
        return api;
    }

    public boolean attemptingToConnect()
    {
        return queuedAudioConnection != null;
    }

    public VoiceChannel getQueuedAudioConnection()
    {
        return queuedAudioConnection;
    }

    public boolean connected()
    {
        return audioConnection != null;
    }

    //Consider finding a way to hide this? It shouldn't be able to be seen by JDA users.
    public void setAudioConnection(AudioConnection audioConnection)
    {
        this.queuedAudioConnection = null;
        this.audioConnection = audioConnection;
        audioConnection.setSendingHandler(sendHandler);
        audioConnection.setReceivingHandler(receiveHandler);
        audioConnection.ready();
    }

    public void setSendingHandler(AudioSendHandler handler)
    {
        sendHandler = handler;
        audioConnection.setSendingHandler(handler);
    }

    public void setReceivingHandler(AudioReceiveHandler handler)
    {
        receiveHandler = handler;
        audioConnection.setReceivingHandler(handler);
    }

    public static boolean isAudioSupported()
    {
        return AUDIO_SUPPORTED;
    }

    //Load the Opus library.
    static
    {
        ServiceUtil.loadServices();

        String lib = "/opus/" + Platform.RESOURCE_PREFIX;
        if (lib.contains("win"))
            lib += "/opus.dll";
        else if (lib.contains("darwin"))
            lib += "/libopus.dylib";
        else if (lib.contains("linux"))
            lib += "/libopus.so";
        else
            throw new RuntimeException("We don't support audio for this operating system. Sorry!");

        try
        {
            NativeUtils.loadLibraryFromJar(lib);
            OPUS_LIB_NAME = lib;
            AUDIO_SUPPORTED = true;
        }
        catch (IOException e)
        {
            OPUS_LIB_NAME = null;
            AUDIO_SUPPORTED = false;
            System.err.println("There was an IO Exception when setting up the temp files for audio.");
            e.printStackTrace();
        }
    }
}
