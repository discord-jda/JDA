/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.requests;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.audio.ConnectionRequest;
import net.dv8tion.jda.internal.audio.ConnectionStage;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

//Helper class delegated to WebSocketClient
class WebSocketSendingThread implements Runnable
{
    private static final Logger LOG = WebSocketClient.LOG;

    private final WebSocketClient client;
    private final JDAImpl api;
    private final ReentrantLock queueLock;
    private final Queue<String> chunkSyncQueue;
    private final Queue<String> ratelimitQueue;
    private final TLongObjectMap<ConnectionRequest> queuedAudioConnections;
    private final ScheduledExecutorService executor;
    private Future<?> handle;

    private boolean needRateLimit = false;
    private boolean attemptedToSend = false;
    private boolean shutdown = false;

    WebSocketSendingThread(WebSocketClient client)
    {
        this.client = client;
        this.api = client.api;
        this.queueLock = client.queueLock;
        this.chunkSyncQueue = client.chunkSyncQueue;
        this.ratelimitQueue = client.ratelimitQueue;
        this.queuedAudioConnections = client.queuedAudioConnections;
        this.executor = client.executor;
    }

    public void shutdown()
    {
        shutdown = true;
        if (handle != null)
            handle.cancel(false);
    }

    public void start()
    {
        shutdown = false;
        handle = executor.submit(this);
    }

    private void scheduleIdle()
    {
        if (shutdown)
            return;
        handle = executor.schedule(this, 500, TimeUnit.MILLISECONDS);
    }

    private void scheduleSentMessage()
    {
        if (shutdown)
            return;
        handle = executor.schedule(this, 10, TimeUnit.MILLISECONDS);
    }

    private void scheduleRateLimit()
    {
        if (shutdown)
            return;
        handle = executor.schedule(this, 1, TimeUnit.MINUTES);
    }

    @Override
    public void run()
    {
        //Make sure that we don't send any packets before sending auth info.
        if (!client.sentAuthInfo)
        {
            scheduleIdle();
            return;
        }

        try
        {
            api.setContext();
            attemptedToSend = false;
            needRateLimit = false;
            queueLock.lockInterruptibly();

            ConnectionRequest audioRequest = client.getNextAudioConnectRequest();
            String chunkOrSyncRequest = chunkSyncQueue.peek();
            if (chunkOrSyncRequest != null)
                handleChunkSync(chunkOrSyncRequest);
            else if (audioRequest != null)
                handleAudioRequest(audioRequest);
            else
                handleNormalRequest();

            if (needRateLimit)
                scheduleRateLimit();
            else if (!attemptedToSend)
                scheduleIdle();
            else
                scheduleSentMessage();
        }
        catch (InterruptedException ignored)
        {
            LOG.debug("Main WS send thread interrupted. Most likely JDA is disconnecting the websocket.");
        }
        finally
        {
            // on any exception that might cause this lock to not release
            client.maybeUnlock();
        }
    }

    private void handleChunkSync(String chunkOrSyncRequest)
    {
        LOG.debug("Sending chunk/sync request {}", chunkOrSyncRequest);
        if (send(chunkOrSyncRequest))
            chunkSyncQueue.remove();
    }

    private void handleAudioRequest(ConnectionRequest audioRequest)
    {
        long channelId = audioRequest.getChannelId();
        long guildId = audioRequest.getGuildIdLong();
        Guild guild = api.getGuildById(guildId);
        if (guild == null)
        {
            LOG.debug("Discarding voice request due to null guild {}", guildId);
            // race condition on guild delete, avoid NPE on DISCONNECT requests
            queuedAudioConnections.remove(guildId);
            return;
        }
        ConnectionStage stage = audioRequest.getStage();
        AudioManager audioManager = guild.getAudioManager();
        DataObject packet;
        switch (stage)
        {
            case RECONNECT:
            case DISCONNECT:
                packet = newVoiceClose(guildId);
                break;
            default:
            case CONNECT:
                packet = newVoiceOpen(audioManager, channelId, guild.getIdLong());
        }
        LOG.debug("Sending voice request {}", packet);
        if (send(packet.toString()))
        {
            //If we didn't get RateLimited, Next request attempt will be 2 seconds from now
            // we remove it in VoiceStateUpdateHandler once we hear that it has updated our status
            // in 2 seconds we will attempt again in case we did not receive an update
            audioRequest.setNextAttemptEpoch(System.currentTimeMillis() + 2000);
            //If we are already in the correct state according to voice state
            // we will not receive a VOICE_STATE_UPDATE that would remove it
            // thus we update it here
            final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();
            client.updateAudioConnection0(guild.getIdLong(), voiceState.getChannel());
        }
    }

    private void handleNormalRequest()
    {
        String message = ratelimitQueue.peek();
        if (message != null)
        {
            LOG.debug("Sending normal message {}", message);
            if (send(message))
                ratelimitQueue.remove();
        }
    }

    //returns true if send was successful
    private boolean send(String request)
    {
        needRateLimit = !client.send(request, false);
        attemptedToSend = true;
        return !needRateLimit;
    }

    protected DataObject newVoiceClose(long guildId)
    {
        return DataObject.empty()
            .put("op", WebSocketCode.VOICE_STATE)
            .put("d", DataObject.empty()
                .put("guild_id", Long.toUnsignedString(guildId))
                .putNull("channel_id")
                .put("self_mute", false)
                .put("self_deaf", false));
    }

    protected DataObject newVoiceOpen(AudioManager manager, long channel, long guild)
    {
        return DataObject.empty()
            .put("op", WebSocketCode.VOICE_STATE)
            .put("d", DataObject.empty()
                .put("guild_id", guild)
                .put("channel_id", channel)
                .put("self_mute", manager.isSelfMuted())
                .put("self_deaf", manager.isSelfDeafened()));
    }
}
