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

package net.dv8tion.jda.core.handle;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildReadyEvent;
import net.dv8tion.jda.core.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.cache.UpstreamReference;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GuildSetupNode
{
    private final long id;
    private final UpstreamReference<GuildSetupController> controller;
    private final List<JSONObject> cachedEvents = new LinkedList<>();
    private TLongObjectMap<JSONObject> members;
    private TLongSet removedMembers;
    private JSONObject partialGuild;
    private int expectedMemberCount = 1;
    private boolean requestedSync;
    boolean requestedChunk;

    final boolean join;
    final boolean sync;
    boolean firedUnavailableJoin = false;
    boolean markedUnavailable = false;
    GuildSetupController.Status status = GuildSetupController.Status.INIT;

    GuildSetupNode(long id, GuildSetupController controller, boolean join)
    {
        this.id = id;
        this.controller = new UpstreamReference<>(controller);
        this.join = join;
        this.sync = controller.isClient();
    }

    public long getIdLong()
    {
        return id;
    }

    public String getId()
    {
        return Long.toUnsignedString(id);
    }

    @Nullable
    public JSONObject getGuildPayload()
    {
        return partialGuild;
    }

    public int getExpectedMemberCount()
    {
        return expectedMemberCount;
    }

    public int getCurrentMemberCount()
    {
        TLongHashSet knownMembers = new TLongHashSet(members.keySet());
        knownMembers.removeAll(removedMembers);
        return knownMembers.size();
    }

    public boolean isJoin()
    {
        return join;
    }

    public boolean isMarkedUnavailable()
    {
        return markedUnavailable;
    }

    public boolean requestedChunks()
    {
        return requestedChunk;
    }

    public boolean requestedSync()
    {
        return requestedSync;
    }

    public boolean containsMember(long userId)
    {
        if (members == null || members.isEmpty())
            return false;
        return members.containsKey(userId);
    }

    @Override
    public String toString()
    {
        return "GuildSetupNode[" + id + "|" + status + ']' +
            '{' +
                "expectedMemberCount=" + expectedMemberCount + ", " +
                "requestedSync="       + requestedSync + ", " +
                "requestedChunk="      + requestedChunk + ", " +
                "join="                + join + ", " +
                "sync="                + sync + ", " +
                "markedUnavailable="   + markedUnavailable +
            '}';
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof GuildSetupNode))
            return false;
        GuildSetupNode node = (GuildSetupNode) obj;
        return node.id == id;
    }

    private GuildSetupController getController()
    {
        return controller.get();
    }

    void updateStatus(GuildSetupController.Status status)
    {
        if (status == this.status)
            return;
        try
        {
            getController().listener.onStatusChange(id, this.status, status);
        }
        catch (Exception ex)
        {
            GuildSetupController.log.error("Uncaught exception in status listener", ex);
        }
        this.status = status;
    }

    void reset()
    {
        updateStatus(GuildSetupController.Status.UNAVAILABLE);
        expectedMemberCount = 1;
        partialGuild = null;
        requestedChunk = false;
        requestedSync = false;
        if (members != null)
            members.clear();
        if (removedMembers != null)
            removedMembers.clear();
        cachedEvents.clear();
    }

    void handleReady(JSONObject obj)
    {
        if (!sync)
            return;
        partialGuild = obj;
        markedUnavailable = Helpers.optBoolean(partialGuild, "unavailable");
        if (markedUnavailable)
        {
            updateStatus(GuildSetupController.Status.UNAVAILABLE);
        }
        else
        {
            getController().addGuildForSyncing(id, join);
            requestedSync = true;
        }
    }

    void handleCreate(JSONObject obj)
    {
        if (partialGuild == null)
        {
            partialGuild = obj;
        }
        else
        {
            for (Iterator<String> it = obj.keys(); it.hasNext();)
            {
                String key = it.next();
                partialGuild.put(key, obj.opt(key));
            }
        }
        boolean unavailable = Helpers.optBoolean(partialGuild, "unavailable");
        boolean wasMarkedUnavailable = this.markedUnavailable;
        this.markedUnavailable = unavailable;
        if (unavailable)
        {
            if (!firedUnavailableJoin && join)
            {
                firedUnavailableJoin = true;
                JDAImpl api = getController().getJDA();
                api.getEventManager().handle(new UnavailableGuildJoinedEvent(api, api.getResponseTotal(), id));
            }
            return;
        }
        if (wasMarkedUnavailable && sync && !requestedSync)
        {
            // We are using a client-account and joined a guild
            //  in that case we need to sync before doing anything
            updateStatus(GuildSetupController.Status.SYNCING);
            getController().addGuildForSyncing(id, join);
            requestedSync = true;
            return;
        }

        ensureMembers();
    }

    void handleSync(JSONObject obj)
    {
        if (partialGuild == null)
        {
            //In this case we received a GUILD_DELETE with unavailable = true while syncing
            // however we have to wait for the GUILD_CREATE with unavailable = false before
            // requesting new chunks
            GuildSetupController.log.debug("Dropping sync update due to unavailable guild");
            return;
        }
        for (Iterator<String> it = obj.keys(); it.hasNext();)
        {
            String key = it.next();
            partialGuild.put(key, obj.opt(key));
        }

        ensureMembers();
    }

    boolean handleMemberChunk(JSONArray arr)
    {
        if (partialGuild == null)
        {
            //In this case we received a GUILD_DELETE with unavailable = true while chunking
            // however we have to wait for the GUILD_CREATE with unavailable = false before
            // requesting new chunks
            GuildSetupController.log.debug("Dropping member chunk due to unavailable guild");
            return true;
        }
        for (Object o : arr)
        {
            JSONObject obj = (JSONObject) o;
            long id = obj.getJSONObject("user").getLong("id");
            members.put(id, obj);
        }

        if (members.size() >= expectedMemberCount)
        {
            completeSetup();
            return false;
        }
        return true;
    }

    void handleAddMember(JSONObject member)
    {
        if (members == null || removedMembers == null)
            return;
        expectedMemberCount++;
        long userId = member.getJSONObject("user").getLong("id");
        members.put(userId, member);
        removedMembers.remove(userId);
    }

    void handleRemoveMember(JSONObject member)
    {
        if (members == null || removedMembers == null)
            return;
        expectedMemberCount--;
        long userId = member.getJSONObject("user").getLong("id");
        members.remove(userId);
        removedMembers.add(userId);
        EventCache eventCache = getController().getJDA().getEventCache();
        if (!getController().containsMember(userId, this)) // if no other setup node contains this userId we clear it here
            eventCache.clear(EventCache.Type.USER, userId);
    }

    void cacheEvent(JSONObject event)
    {
        GuildSetupController.log.trace("Caching {} event during init. GuildId: {}", event.getString("t"), id);
        cachedEvents.add(event);
        //Check if more than 2000 events cached - suspicious
        // Print warning every 1000 events
        int cacheSize = cachedEvents.size();
        if (cacheSize >= 2000 && cacheSize % 1000 == 0)
        {
            GuildSetupController.log.warn(
                "Accumulating suspicious amounts of cached events during guild setup, " +
                "something might be wrong. Cached: {} GuildId: {}", cacheSize, id);
        }
    }

    void cleanup()
    {
        updateStatus(GuildSetupController.Status.REMOVED);
        EventCache eventCache = getController().getJDA().getEventCache();
        eventCache.clear(EventCache.Type.GUILD, id);
        if (partialGuild == null)
            return;

        JSONArray channels = partialGuild.optJSONArray("channels");
        JSONArray roles = partialGuild.optJSONArray("roles");
        if (channels != null)
        {
            for (Object o : channels)
            {
                JSONObject json = (JSONObject) o;
                long id = json.getLong("id");
                eventCache.clear(EventCache.Type.CHANNEL, id);
            }
        }

        if (roles != null)
        {
            for (Object o : roles)
            {
                JSONObject json = (JSONObject) o;
                long id = json.getLong("id");
                eventCache.clear(EventCache.Type.ROLE, id);
            }
        }

        if (members != null)
        {
            for (TLongObjectIterator<JSONObject> it = members.iterator(); it.hasNext();)
            {
                it.advance();
                long userId = it.key();
                if (!getController().containsMember(userId, this)) // if no other setup node contains this userId we clear it here
                    eventCache.clear(EventCache.Type.USER, userId);
            }
        }
    }

    private void completeSetup()
    {
        updateStatus(GuildSetupController.Status.BUILDING);
        JDAImpl api = getController().getJDA();
        for (TLongIterator it = removedMembers.iterator(); it.hasNext(); )
            members.remove(it.next());
        removedMembers.clear();
        GuildImpl guild = api.getEntityBuilder().createGuild(id, partialGuild, members);
        updateAudioManagerReference(guild);
        if (join)
        {
            api.getEventManager().handle(new GuildJoinEvent(api, api.getResponseTotal(), guild));
            if (requestedChunk)
                getController().ready(id);
            else
                getController().remove(id);
        }
        else
        {
            api.getEventManager().handle(new GuildReadyEvent(api, api.getResponseTotal(), guild));
            getController().ready(id);
        }
        updateStatus(GuildSetupController.Status.READY);
        GuildSetupController.log.debug("Finished setup for guild {} firing cached events {}", id, cachedEvents.size());
        api.getClient().handle(cachedEvents);
        api.getEventCache().playbackCache(EventCache.Type.GUILD, id);
    }

    private void ensureMembers()
    {
        expectedMemberCount = partialGuild.getInt("member_count");
        members = new TLongObjectHashMap<>(expectedMemberCount);
        removedMembers = new TLongHashSet();
        JSONArray memberArray = partialGuild.getJSONArray("members");
        if (memberArray.length() < expectedMemberCount && !requestedChunk)
        {
            updateStatus(GuildSetupController.Status.CHUNKING);
            getController().addGuildForChunking(id, join);
            requestedChunk = true;
        }
        else if (handleMemberChunk(memberArray) && !requestedChunk)
        {
            // Discord sent us enough members to satisfy the member_count
            //  but we found duplicates and still didn't reach enough to satisfy the count
            //  in this case we try to do chunking instead
            // This is caused by lazy guilds and intended behavior according to jake
            GuildSetupController.log.trace(
                "Received suspicious members with a guild payload. Attempting to chunk. " +
                "member_count: {} members: {} actual_members: {} guild_id: {}",
                expectedMemberCount, memberArray.length(), members.size(), id);
            members.clear();
            updateStatus(GuildSetupController.Status.CHUNKING);
            getController().addGuildForChunking(id, join);
            requestedChunk = true;
        }
    }

    private void updateAudioManagerReference(GuildImpl guild)
    {
        JDAImpl api = getController().getJDA();
        TLongObjectMap<AudioManager> audioManagerMap = api.getAudioManagerMap();
        synchronized (audioManagerMap)
        {
            AudioManagerImpl mng = (AudioManagerImpl) audioManagerMap.get(id);
            if (mng == null)
                return;
            ConnectionListener listener = mng.getConnectionListener();
            final AudioManagerImpl newMng = new AudioManagerImpl(guild);
            newMng.setSelfMuted(mng.isSelfMuted());
            newMng.setSelfDeafened(mng.isSelfDeafened());
            newMng.setQueueTimeout(mng.getConnectTimeout());
            newMng.setSendingHandler(mng.getSendingHandler());
            newMng.setReceivingHandler(mng.getReceiveHandler());
            newMng.setConnectionListener(listener);
            newMng.setAutoReconnect(mng.isAutoReconnect());

            if (mng.isConnected() || mng.isAttemptingToConnect())
            {
                final long channelId = mng.isConnected()
                                       ? mng.getConnectedChannel().getIdLong()
                                       : mng.getQueuedAudioConnection().getIdLong();

                final VoiceChannel channel = api.getVoiceChannelById(channelId);
                if (channel != null)
                {
                    if (mng.isConnected())
                        mng.closeAudioConnection(ConnectionStatus.ERROR_CANNOT_RESUME);
                    //closing old connection in order to reconnect later
                    newMng.setQueuedAudioConnection(channel);
                }
                else
                {
                    //The voice channel is not cached. It was probably deleted.
                    api.getClient().removeAudioConnection(id);
                    if (listener != null)
                        listener.onStatusChange(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                }
            }
            audioManagerMap.put(id, newMng);
        }
    }
}
