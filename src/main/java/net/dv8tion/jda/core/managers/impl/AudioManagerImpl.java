/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

public class AudioManagerImpl implements AudioManager
{
    public static final ThreadGroup AUDIO_THREADS = new ThreadGroup("jda-audio");

    public final ReentrantLock CONNECTION_LOCK = new ReentrantLock();

    protected final JDAImpl api;
    protected final ListenerProxy connectionListener = new ListenerProxy();
    protected final GuildImpl guild;
    protected AudioConnection audioConnection = null;
    protected VoiceChannel queuedAudioConnection = null;

    protected AudioSendHandler sendHandler;
    protected AudioReceiveHandler receiveHandler;
    protected long queueTimeout = 100;
    protected boolean shouldReconnect = true;

    protected boolean selfMuted = false;
    protected boolean selfDeafened = false;

    protected long timeout = DEFAULT_CONNECTION_TIMEOUT;

    public AudioManagerImpl(GuildImpl guild)
    {
        this.guild = guild;
        this.api = this.guild.getJDA();
    }

    public AudioConnection getAudioConnection()
    {
        return audioConnection;
    }

    @Override
    public void openAudioConnection(VoiceChannel channel)
    {
        Checks.notNull(channel, "Provided VoiceChannel");

//        if (!AUDIO_SUPPORTED)
//            throw new UnsupportedOperationException("Sorry! Audio is disabled due to an internal JDA error! Contact Dev!");
        if (!guild.equals(channel.getGuild()))
            throw new IllegalArgumentException("The provided VoiceChannel is not a part of the Guild that this AudioManager handles." +
                    "Please provide a VoiceChannel from the proper Guild");
        if (!guild.isAvailable())
            throw new GuildUnavailableException("Cannot open an Audio Connection with an unavailable guild. " +
                    "Please wait until this Guild is available to open a connection.");
        final Member self = guild.getSelfMember();
        //if (!self.hasPermission(channel, Permission.VOICE_CONNECT))
        //    throw new InsufficientPermissionException(Permission.VOICE_CONNECT);

        if (audioConnection == null)
        {
            checkChannel(channel, self);
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

            checkChannel(channel, self);

            api.getClient().queueAudioConnect(channel);
            audioConnection.setChannel(channel);
        }
    }

    private void checkChannel(VoiceChannel channel, Member self)
    {
        EnumSet<Permission> perms = Permission.toEnumSet(PermissionUtil.getEffectivePermission(channel, self));
        if (!perms.contains(Permission.VOICE_CONNECT))
            throw new InsufficientPermissionException(Permission.VOICE_CONNECT);
        final int userLimit = channel.getUserLimit(); // userLimit is 0 if no limit is set!
        if (userLimit > 0 && !perms.contains(Permission.ADMINISTRATOR))
        {
            // Check if we can actually join this channel
            // - If there is a userlimit
            // - If that userlimit is reached
            // - If we don't have voice move others permissions
            // VOICE_MOVE_OTHERS allows access because you would be able to move people out to
            // open up a slot anyway
            if (userLimit <= channel.getMembers().size()
                && !perms.contains(Permission.VOICE_MOVE_OTHERS))
            {
                throw new InsufficientPermissionException(Permission.VOICE_MOVE_OTHERS,
                    "Unable to connect to VoiceChannel due to userlimit! Requires permission VOICE_MOVE_OTHERS to bypass");
            }
        }
    }

    @Override
    public void closeAudioConnection()
    {
        closeAudioConnection(ConnectionStatus.NOT_CONNECTED);
    }

    public void closeAudioConnection(ConnectionStatus reason)
    {
        MiscUtil.locked(CONNECTION_LOCK, () ->
        {
            this.queuedAudioConnection = null;
            if (audioConnection != null)
                this.audioConnection.close(reason);
            else
                this.api.getClient().queueAudioDisconnect(guild);
            this.audioConnection = null;
        });
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
            api.getClient().queueAudioConnect(channel);
        }
    }
}
