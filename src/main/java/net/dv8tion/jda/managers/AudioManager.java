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

/**
 * AudioManager deals with creating, managing and severing audio connections to
 * {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}. Also controls audio handlers.
 */
public class AudioManager
{
    //These values are set at the bottom of this file.
    public static boolean AUDIO_SUPPORTED;
    public static String OPUS_LIB_NAME;

    private static boolean initialized = false;

    private final JDAImpl api;
    private AudioConnection audioConnection = null;
    private VoiceChannel queuedAudioConnection = null;

    private AudioSendHandler sendHandler;
    private AudioReceiveHandler receiveHandler;

    public AudioManager(JDAImpl api)
    {
        this.api = api;
        init();
    }

    /**
     * Starts the process to create an audio connection with a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}.<br>
     * Note: Currently you can only be connected to a single {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}
     * at a time.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} to open an audio connection with.
     *
     * @throws java.lang.IllegalStateException
     *          If JDA is already has an active audio connection with a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}
     *          this will be thrown. JDA can only have 1 audio connection at a time.<br>
     *          This will also be thrown if JDA is currently attempting to setup an audio connection.<br>
     *          For both of these situations, first checking {@link #isAttemptingToConnect()} and {@link #isConnected()}
     *          is advised.
     *
     * @throws java.lang.UnsupportedOperationException
     *          If {@link #AUDIO_SUPPORTED AUDIO_SUPPORTED} is false due to a problem when JDA initially set up the
     *          audio system then this will be thrown.<br>
     *          Consider checking the value of {@link #AUDIO_SUPPORTED AudioManager.AUDIO_SUPPORTED} first.
     */
    public void openAudioConnection(VoiceChannel channel)
    {
        if (!AUDIO_SUPPORTED)
            throw new UnsupportedOperationException("Sorry! Audio is disabled due to an internal JDA error! Contact Dev!");
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

    /**
     * Moves the audio connection from one {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} to a different
     * {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}. The destination channel MUST be in the same
     * {@link net.dv8tion.jda.entities.Guild Guild} as {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} that
     * the audio connection is currently connected to.<br>
     * Note: if the VoiceChannel provided is the same as the channel that the audio connection is currently connected
     * to, there will be no change.
     *
     * @param channel
     *          The destination {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} to which the audio connection
     *          will move to.
     * @throws java.lang.IllegalStateException
     *          If there is no open audio connection.
     * @throws java.lang.IllegalArgumentException
     *          <ul>
     *              <li>If the provided channel was <code>null</code>.</li>
     *              <li>If the provided channel is not part of the Guild that the current audio connection is connected to.</li>
     *          </ul>
     */
    public void moveAudioConnection(VoiceChannel channel)
    {
        if (!isConnected())
            throw new IllegalStateException("Cannot change to a different VoiceChannel when not currently connected. " +
                    "Please use openAudioConnection(VoiceChannel) to start an audio connection.");

        if (channel == null)
            throw new IllegalArgumentException("The provided VoiceChannel was null! Cannot determine which VoiceChannel " +
                    "to move to from a null VoiceChannel!");

        if (!audioConnection.getChannel().getGuild().getId().equals(channel.getGuild().getId()))
            throw new IllegalArgumentException("Cannot move to a VoiceChannel that isn't in the same Guild as the " +
                    "active VoiceChannel audio connection. If you wish to open an audio connection with a VoiceChannel " +
                    "on a different Guild, please close the active connection and start a new one.");

        //If we are already connected to this VoiceChannel, then do nothing.
        if (channel.getId().equals(audioConnection.getChannel().getId()))
            return;

        JSONObject obj = new JSONObject()
                .put("op", 4)
                .put("d", new JSONObject()
                        .put("guild_id", channel.getGuild().getId())
                        .put("channel_id", channel.getId())
                        .put("self_mute", false)
                        .put("self_deaf", false)
                );
        api.getClient().send(obj.toString());
        audioConnection.setChannel(channel);
    }

    /**
     * Used to close down the audio connection and disconnect from the {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}.<br>
     * As a note, if this is called when JDA doesn't have an audio connection, nothing happens.
     */
    public void closeAudioConnection()
    {
        if (audioConnection == null)
            return;
        this.audioConnection.close();
        this.audioConnection = null;
    }

    /**
     * Gets the {@link net.dv8tion.jda.JDA JDA} instance that this AudioManager is a part of.
     *
     * @return
     *      This AudioManager's JDA instance.
     */
    public JDA getJDA()
    {
        return api;
    }

    /**
     * This can be used to find out if JDA is currently attempting to setup an audio connection.<br>
     * If this returns true then {@link #getQueuedAudioConnection()} will return the
     * {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} that JDA is attempting to setup an audio connection to.
     *
     * @return
     *      True if JDA is currently attempting to create an audio connection.
     */
    public boolean isAttemptingToConnect()
    {
        return queuedAudioConnection != null;
    }

    /**
     * This can be used to find out what {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} JDA is currently
     * attempting to setup an audio connection for. If JDA isn't trying to create an audio connection this will return
     * null.<br>
     * Also, if JDA is already connected, this will return null. To determine if JDA is already has an audio connection
     * with a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} use {@link #isConnected()}
     *
     * @return
     *      The {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} that JDA is attempting to create an
     *      audio connection with, or null if JDA isn't attempting to create a connection.
     */
    public VoiceChannel getQueuedAudioConnection()
    {
        return queuedAudioConnection;
    }

    /**
     * Returns the {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} that JDA currently has an audio connection
     * to. If JDA currently doesn't have an audio connection to a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}
     * this will return null.
     *
     * @return
     *      The {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} the audio connection is connected to
     *      or <code>null</code> if not connected.
     */
    public VoiceChannel getConnectedChannel()
    {
        return audioConnection == null ? null : audioConnection.getChannel();
    }

    /**
     * This can be used to find out if JDA currently has an active audio connection with a
     * {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}. If this returns true, then
     * {@link #getConnectedChannel()} will return the {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} which
     * JDA is connected to.
     *
     * @return
     *      True if JDA currently has an active audio connection.
     */
    public boolean isConnected()
    {
        return audioConnection != null;
    }

    //Consider finding a way to hide this? It shouldn't be able to be seen by JDA users.
    /**
     * <b><u>Please don't touch this method. Bad Bad things will happen.</u></b><br>
     * If you would like to start an audio connection, please use
     * {@link #openAudioConnection(net.dv8tion.jda.entities.VoiceChannel)}
     *
     * @param audioConnection
     *          The audio connection to deal with. Once again, <b>don't use this method</b> ;_;
     */
    public void setAudioConnection(AudioConnection audioConnection)
    {
        this.queuedAudioConnection = null;
        this.audioConnection = audioConnection;
        if (audioConnection == null)
            return;

        audioConnection.setSendingHandler(sendHandler);
        audioConnection.setReceivingHandler(receiveHandler);
        audioConnection.ready();
    }

    /**
     * Sets the {@link net.dv8tion.jda.audio.AudioSendHandler}
     * that the manager will use to provide audio data to an audio connection.<br>
     * The handler provided here will persist between audio connection connect and disconnects. Furthermore, you don't
     * need to have an audio connection to set a handler. When JDA sets up a new audio connection it will use the
     * handler provided here.<br>
     * Setting this to null will remove the audio handler.
     * <p>
     * Example implementations of an {@link net.dv8tion.jda.audio.AudioSendHandler AudioSendHandler} can be seen in the
     * abstract class {@link net.dv8tion.jda.audio.player.Player Player} and its subclasses
     * {@link net.dv8tion.jda.audio.player.FilePlayer FilePlayer} and {@link net.dv8tion.jda.audio.player.URLPlayer URLPlayer}.
     *
     * @param handler
     *          The {@link net.dv8tion.jda.audio.AudioSendHandler AudioSendHandler} used to provide audio data.
     */
    public void setSendingHandler(AudioSendHandler handler)
    {
        sendHandler = handler;
        if (audioConnection != null)
            audioConnection.setSendingHandler(handler);
    }

    /**
     * Returns the currently set {@link net.dv8tion.jda.audio.AudioSendHandler AudioSendHandler}. If there is
     * no sender currently set, this method will return null.
     *
     * @return
     *      The currently active {@link net.dv8tion.jda.audio.AudioSendHandler AudioSendHandler} or <code>null</code>.
     */
    public AudioSendHandler getSendingHandler()
    {
        return sendHandler;
    }

    /**
     * Sets the {@link net.dv8tion.jda.audio.AudioReceiveHandler AudioReceiveHandler}
     * that the manager will use to process audio data received from an audio connection.
     * <p>
     * The handler provided here will persist between audio connection connect and disconnects. Furthermore, you don't
     * need to have an audio connection to set a handler. When JDA sets up a new audio connection it will use the
     * handler provided here.<br>
     * Setting this to null will remove the audio handler.
     *
     * @param handler
     *          The {@link net.dv8tion.jda.audio.AudioReceiveHandler AudioReceiveHandler} used to process
     *          received audio data.
     */
    public void setReceivingHandler(AudioReceiveHandler handler)
    {
        receiveHandler = handler;
        if (audioConnection != null)
            audioConnection.setReceivingHandler(handler);
    }

    /**
     * Returns the currently set {@link net.dv8tion.jda.audio.AudioReceiveHandler AudioReceiveHandler}. If there is
     * no receiver currently set, this method will return null.
     *
     * @return
     *      The currently active {@link net.dv8tion.jda.audio.AudioReceiveHandler AudioReceiveHandler} or <code>null</code>.
     */
    public AudioReceiveHandler getReceiveHandler()
    {
        return receiveHandler;
    }

    //Load the Opus library.
    private static synchronized void init()
    {
        if(initialized)
            return;
        initialized = true;
        ServiceUtil.loadServices();
        String lib = null;
        try
        {
            //The libraries that this is referencing are available in the src/main/resources/opus/ folder.
            //Of course, when JDA is compiled that just becomes /opus/
            lib = "/opus/" + Platform.RESOURCE_PREFIX;
            if (lib.contains("win"))
            {
                //windows server doesn't return -32 or -64
                if (lib.endsWith("x86"))
                    lib += "-32";
                lib += "/opus.dll";
            }
            else if (lib.contains("darwin"))
                lib += "/libopus.dylib";
            else if (lib.contains("linux"))
                lib += "/libopus.so";
            else
                throw new UnsupportedOperationException();

            NativeUtils.loadLibraryFromJar(lib);
        }
        catch (Exception e)
        {
            if (e instanceof UnsupportedOperationException)
                System.err.println("Sorry, JDA's audio system doesn't support this system.\n" +
                        "Supported Systems: Windows(x86, x64), Mac(x86, x64) and Linux(x86, x64)\n" +
                        "Operating system: " + Platform.RESOURCE_PREFIX);
            else if (e instanceof  IOException)
            {
                System.err.println("There was an IO Exception when setting up the temp files for audio.");
                e.printStackTrace();
            }
            else
            {
                System.err.println("An unknown error occurred while attempting to setup JDA's audio system!");
                e.printStackTrace();
            }

            lib = null;
        }

        finally
        {
            OPUS_LIB_NAME = lib;
            AUDIO_SUPPORTED = lib != null;
        }

    }
}
