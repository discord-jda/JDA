/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.audio.ConnectionRequest;
import net.dv8tion.jda.core.audio.ConnectionStage;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.AudioManager;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

//Helper class delegated to WebSocketClient
class WebSocketSendingThread implements Runnable
{
    private static final Logger LOG = WebSocketClient.LOG;

    private final AtomicBoolean needRateLimit = new AtomicBoolean(false);
    private final AtomicBoolean attemptedToSend = new AtomicBoolean(true);

    private final WebSocketClient client;
    private final JDAImpl api;
    private final ReentrantLock queueLock;
    private final Queue<String> chunkSyncQueue;
    private final Queue<String> ratelimitQueue;
    private final TLongObjectMap<ConnectionRequest> queuedAudioConnections;

    WebSocketSendingThread(WebSocketClient client)
    {
        this.client = client;
        this.api = client.api;
        this.queueLock = client.queueLock;
        this.chunkSyncQueue = client.chunkSyncQueue;
        this.ratelimitQueue = client.ratelimitQueue;
        this.queuedAudioConnections = client.queuedAudioConnections;
    }

    @Override
    public void run()
    {
        //Make sure that we don't send any packets before sending auth info.
        if (!client.sentAuthInfo || needRateLimit.getAndSet(false) || !attemptedToSend.getAndSet(true))
            return; // wait another 500 ms
        try
        {
            api.setContext();
            attemptedToSend.set(false);
            needRateLimit.set(false);
            queueLock.lockInterruptibly();

            ConnectionRequest audioRequest = client.getNextAudioConnectRequest();
            String chunkOrSyncRequest = chunkSyncQueue.peek();
            if (chunkOrSyncRequest != null)
                handleChunkSync(chunkOrSyncRequest);
            else if (audioRequest != null)
                handleAudioRequest(audioRequest);
            else
                handleNormalRequest();
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
        needRateLimit.set(!client.send(chunkOrSyncRequest, false));
        if (!needRateLimit.get())
            chunkSyncQueue.remove();

        attemptedToSend.set(true);
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
        JSONObject packet;
        switch (stage)
        {
            case RECONNECT:
            case DISCONNECT:
                packet = client.newVoiceClose(guildId);
                break;
            default:
            case CONNECT:
                packet = client.newVoiceOpen(audioManager, channelId, guild.getIdLong());
        }
        LOG.debug("Sending voice request {}", packet);
        needRateLimit.set(!client.send(packet.toString(), false));
        if (!needRateLimit.get())
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
        attemptedToSend.set(true);
    }

    private void handleNormalRequest()
    {
        String message = ratelimitQueue.peek();
        if (message != null)
        {
            LOG.debug("Sending normal message {}", message);
            needRateLimit.set(!client.send(message, false));
            if (!needRateLimit.get())
                ratelimitQueue.remove();
            attemptedToSend.set(true);
        }
    }
}
