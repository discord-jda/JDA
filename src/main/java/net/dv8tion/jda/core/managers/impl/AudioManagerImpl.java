/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audio.AudioConnection;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.audio.hooks.ListenerProxy;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.NativeUtil;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.io.IOException;

public class AudioManagerImpl implements AudioManager
{
    //These values are set at the bottom of this file.
    public static boolean AUDIO_SUPPORTED;
    public static String OPUS_LIB_NAME;

    protected static boolean initialized = false;

    public final Object CONNECTION_LOCK = new Object();

    protected final JDAImpl api;
    protected GuildImpl guild;
    protected AudioConnection audioConnection = null;
    protected VoiceChannel queuedAudioConnection = null;

    protected AudioSendHandler sendHandler;
    protected AudioReceiveHandler receiveHandler;
    protected ListenerProxy connectionListener = new ListenerProxy();
    protected long queueTimeout = 100;
    protected boolean shouldReconnect = true;

    protected boolean selfMuted = false;
    protected boolean selfDeafened = false;

    protected long timeout = DEFAULT_CONNECTION_TIMEOUT;

    public AudioManagerImpl(GuildImpl guild)
    {
        this.guild = guild;
        this.api = this.guild.getJDA();
        init(); //Just to make sure that the audio libs have been initialized.
    }

    public void setGuild(GuildImpl guild)
    {
        this.guild = guild;
    }

    @Override
    public void openAudioConnection(VoiceChannel channel)
    {
        Args.notNull(channel, "Provided VoiceChannel");

        if (!AUDIO_SUPPORTED)
            throw new UnsupportedOperationException("Sorry! Audio is disabled due to an internal JDA error! Contact Dev!");
        if (!guild.equals(channel.getGuild()))
            throw new IllegalArgumentException("The provided VoiceChannel is not a part of the Guild that this AudioManager handles." +
                    "Please provide a VoiceChannel from the proper Guild");
        if (!guild.isAvailable())
            throw new GuildUnavailableException("Cannot open an Audio Connection with an unavailable guild. " +
                    "Please wait until this Guild is available to open a connection.");
        final Member self = guild.getSelfMember();
        if (!self.hasPermission(channel, Permission.VOICE_CONNECT) && !self.hasPermission(channel, Permission.VOICE_MOVE_OTHERS))
            throw new PermissionException(Permission.VOICE_CONNECT);

        if (audioConnection == null)
        {
            //Start establishing connection, joining provided channel
            queuedAudioConnection = channel;
            api.getClient().queueAudioConnect(channel);
        }
        else
        {
            //Connection is already established, move to specified channel

            //If we are already connected to this VoiceChannel, then do nothing.
            if (channel.equals(audioConnection.getChannel()))
                return;

            final int userLimit = channel.getUserLimit(); // userLimit is 0 if no limit is set!
            if (!self.isOwner() && !self.hasPermission(Permission.ADMINISTRATOR))
            {
                final long perms = PermissionUtil.getExplicitPermission(channel, self);
                final long voicePerm = Permission.VOICE_MOVE_OTHERS.getRawValue();
                if (userLimit > 0                                               // If there is a userlimit
                    && userLimit <= channel.getMembers().size()                 // if that userlimit is reached
                    && (perms & voicePerm) != voicePerm)                        // If we don't have voice move others permissions
                    throw new PermissionException(Permission.VOICE_MOVE_OTHERS, // then throw exception!
                            "Unable to connect to VoiceChannel due to userlimit! Requires permission VOICE_MOVE_OTHERS to bypass");
            }

            api.getClient().queueAudioConnect(channel);
            audioConnection.setChannel(channel);
        }
    }

    @Override
    public void closeAudioConnection()
    {
        closeAudioConnection(ConnectionStatus.NOT_CONNECTED);
    }

    public void closeAudioConnection(ConnectionStatus reason)
    {
        synchronized (CONNECTION_LOCK)
        {
            api.getClient().getQueuedAudioConnectionMap().remove(guild.getIdLong());
            this.queuedAudioConnection = null;
            if (audioConnection == null)
                return;
            this.audioConnection.close(reason);
            this.audioConnection = null;
        }
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

    @Override
    public void setAutoReconnect(boolean shouldReconnect)
    {
        this.shouldReconnect = shouldReconnect;
        if (audioConnection != null)
            audioConnection.getWebSocket().setAutoReconnect(shouldReconnect);
    }

    @Override
    public boolean isAutoReconnect()
    {
        return shouldReconnect;
    }

    @Override
    public void setSelfMuted(boolean muted)
    {
        if (selfMuted != muted)
        {
            this.selfMuted = muted;
            updateVoiceState();
        }
    }

    @Override
    public boolean isSelfMuted()
    {
        return selfMuted;
    }

    @Override
    public void setSelfDeafened(boolean deafened)
    {
        if (selfDeafened != deafened)
        {
            this.selfDeafened = deafened;
            updateVoiceState();
        }

    }

    @Override
    public boolean isSelfDeafened()
    {
        return selfDeafened;
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
    }

    public void prepareForRegionChange()
    {
        VoiceChannel queuedChannel = audioConnection.getChannel();
        closeAudioConnection(ConnectionStatus.AUDIO_REGION_CHANGE);
        this.queuedAudioConnection = queuedChannel;
    }

    public void setQueuedAudioConnection(VoiceChannel channel)
    {
        queuedAudioConnection = channel;
    }

    public void setConnectedChannel(VoiceChannel channel)
    {
        if (audioConnection != null)
            audioConnection.setChannel(channel);
    }

    public void setQueueTimeout(long queueTimeout)
    {
        this.queueTimeout = queueTimeout;
        if (audioConnection != null)
            audioConnection.setQueueTimeout(queueTimeout);
    }

    protected void updateVoiceState()
    {
        if (isConnected() || isAttemptingToConnect())
        {
            VoiceChannel channel = isConnected() ? getConnectedChannel() : getQueuedAudioConnection();

            //This is technically equivalent to an audio open/move packet.
            JSONObject voiceStateChange = new JSONObject()
                    .put("op", 4)
                    .put("d", new JSONObject()
                            .put("guild_id", guild.getId())
                            .put("channel_id", channel.getId())
                            .put("self_mute", isSelfMuted())
                            .put("self_deaf", isSelfDeafened())
                    );
            api.getClient().send(voiceStateChange.toString());
        }
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
