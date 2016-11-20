/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.managers.impl;

import com.sun.jna.Platform;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.AudioConnection;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.audio.hooks.ListenerProxy;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.NativeUtil;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.io.IOException;

public class AudioManagerImpl implements AudioManager
{
    //These values are set at the bottom of this file.
    public static boolean AUDIO_SUPPORTED;
    public static String OPUS_LIB_NAME;

    protected static boolean initialized = false;

    protected final JDAImpl api;
    protected final Guild guild;
    protected AudioConnection audioConnection = null;
    protected VoiceChannel queuedAudioConnection = null;
    protected VoiceChannel unexpectedDisconnectedChannel = null;

    protected AudioSendHandler sendHandler;
    protected AudioReceiveHandler receiveHandler;
    protected ListenerProxy connectionListener = new ListenerProxy();
    protected long queueTimeout = 100;

    protected long timeout = DEFAULT_CONNECTION_TIMEOUT;

    public AudioManagerImpl(Guild guild)
    {
        this.guild = guild;
        this.api = (JDAImpl) guild.getJDA();
        init(); //Just to make sure that the audio libs have been initialized.
    }

    @Override
    public void openAudioConnection(VoiceChannel channel)
    {
        Args.notNull(channel, "Provided VoiceChannel");

        if (!AUDIO_SUPPORTED)
            throw new UnsupportedOperationException("Sorry! Audio is disabled due to an internal JDA error! Contact Dev!");
        if (queuedAudioConnection != null)
            throw new IllegalStateException("Already attempting to start an AudioConnection with a VoiceChannel!\n" +
                    "Currently Attempting Channel ID: " + queuedAudioConnection.getId() + "  |  New Attempt Channel ID: " + channel.getId());
        if (!guild.equals(channel.getGuild()))
            throw new IllegalArgumentException("The provided VoiceChannel is not a part of the Guild that this AudioManager handles." +
                    "Please provide a VoiceChannel from the proper Guild");
        if (!guild.isAvailable())
            throw new GuildUnavailableException("Cannot open an Audio Connection with an unavailable guild. " +
                    "Please wait until this Guild is available to open a connection.");

        if (audioConnection == null)
        {
            //Start establishing connection, joining provided channel
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
        else
        {
            //Connection is already established, move to specified channel

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
    }

    @Override
    public void closeAudioConnection()
    {
        if (audioConnection == null)
            return;
        this.audioConnection.close(ConnectionStatus.NOT_CONNECTED);
        this.audioConnection = null;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public boolean isAttemptingToConnect()
    {
        return queuedAudioConnection != null;
    }

    @Override
    public VoiceChannel getQueuedAudioConnection()
    {
        return queuedAudioConnection;
    }

    @Override
    public VoiceChannel getConnectedChannel()
    {
        return audioConnection == null ? null : audioConnection.getChannel();
    }

    @Override
    public boolean isConnected()
    {
        return audioConnection != null;
    }

    @Override
    public void setConnectTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    @Override
    public long getConnectTimeout()
    {
        return timeout;
    }

    @Override
    public void setSendingHandler(AudioSendHandler handler)
    {
        sendHandler = handler;
        if (audioConnection != null)
            audioConnection.setSendingHandler(handler);
    }

    @Override
    public AudioSendHandler getSendingHandler()
    {
        return sendHandler;
    }

    @Override
    public void setReceivingHandler(AudioReceiveHandler handler)
    {
        receiveHandler = handler;
        if (audioConnection != null)
            audioConnection.setReceivingHandler(handler);
    }

    @Override
    public AudioReceiveHandler getReceiveHandler()
    {
        return receiveHandler;
    }

    @Override
    public void setConnectionListener(ConnectionListener listener)
    {
        this.connectionListener.setListener(listener);
    }

    @Override
    public ConnectionListener getConnectionListener()
    {
        return connectionListener.getListener();
    }

    @Override
    public ConnectionStatus getConnectionStatus()
    {
        if (audioConnection != null)
            return audioConnection.getWebSocket().getConnectionStatus();
        else
            return ConnectionStatus.NOT_CONNECTED;
    }

    public ConnectionListener getListenerProxy()
    {
        return connectionListener;
    }

    public void setAudioConnection(AudioConnection audioConnection)
    {
        this.audioConnection = audioConnection;
        if (audioConnection == null)
            return;

        this.queuedAudioConnection = null;
        audioConnection.setSendingHandler(sendHandler);
        audioConnection.setReceivingHandler(receiveHandler);
        audioConnection.setQueueTimeout(queueTimeout);
        audioConnection.ready(timeout);
    }

    public void prepareForRegionChange()
    {
        VoiceChannel queuedChannel = audioConnection.getChannel();
        this.audioConnection.close(ConnectionStatus.NOT_CONNECTED);
        this.audioConnection = null;
        this.queuedAudioConnection = queuedChannel;
    }

    public boolean wasUnexpectedlyDisconnected()
    {
        return unexpectedDisconnectedChannel != null;
    }

    public void setUnexpectedDisconnectChannel(VoiceChannel channel)
    {
        this.unexpectedDisconnectedChannel = channel;
    }

    public VoiceChannel getUnexpectedDisconnectedChannel()
    {
        return unexpectedDisconnectedChannel;
    }

    public void setQueueTimeout(long queueTimeout)
    {
        this.queueTimeout = queueTimeout;
        if (audioConnection != null)
            audioConnection.setQueueTimeout(queueTimeout);
    }

    //Load the Opus library.
    public static synchronized boolean init()
    {
        if(initialized)
            return AUDIO_SUPPORTED;
        initialized = true;
        String nativesRoot  = null;
        try
        {
            //The libraries that this is referencing are available in the src/main/resources/opus/ folder.
            //Of course, when JDA is compiled that just becomes /opus/
            nativesRoot = "/natives/" + Platform.RESOURCE_PREFIX + "/%s";
            if (nativesRoot.contains("darwin")) //Mac
                nativesRoot += ".dylib";
            else if (nativesRoot.contains("win"))
                nativesRoot += ".dll";
            else if (nativesRoot.contains("linux"))
                nativesRoot += ".so";
            else
                throw new UnsupportedOperationException();

            NativeUtil.loadLibraryFromJar(String.format(nativesRoot, "libopus"));
        }
        catch (Throwable e)
        {
            if (e instanceof UnsupportedOperationException)
                LOG.fatal("Sorry, JDA's audio system doesn't support this system.\n" +
                        "Supported Systems: Windows(x86, x64), Mac(x86, x64) and Linux(x86, x64)\n" +
                        "Operating system: " + Platform.RESOURCE_PREFIX);
            else if (e instanceof  IOException)
            {
                LOG.fatal("There was an IO Exception when setting up the temp files for audio.");
                LOG.log(e);
            }
            else if (e instanceof UnsatisfiedLinkError)
            {
                LOG.fatal("JDA encountered a problem when attempting to load the Native libraries. Contact a DEV.");
                LOG.log(e);
            }
            else
            {
                LOG.fatal("An unknown error occurred while attempting to setup JDA's audio system!");
                LOG.log(e);
            }

            nativesRoot = null;
        }
        finally
        {
            OPUS_LIB_NAME = nativesRoot != null ? String.format(nativesRoot, "libopus") : null;
            AUDIO_SUPPORTED = nativesRoot != null;

            if (AUDIO_SUPPORTED)
                LOG.info("Audio System successfully setup!");
            else
                LOG.info("Audio System encountered problems while loading, thus, is disabled.");
            return AUDIO_SUPPORTED;
        }

    }
}
