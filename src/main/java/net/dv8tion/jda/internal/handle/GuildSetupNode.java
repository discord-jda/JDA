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

package net.dv8tion.jda.internal.handle;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class GuildSetupNode
{
    private final long id;
    private final GuildSetupController controller;
    private final List<DataObject> cachedEvents = new LinkedList<>();
    private TLongObjectMap<DataObject> members;
    private TLongSet removedMembers;
    private DataObject partialGuild;
    private int expectedMemberCount = 1;
    boolean requestedChunk;

    final Type type;
    boolean firedUnavailableJoin = false;
    boolean markedUnavailable = false;
    GuildSetupController.Status status = GuildSetupController.Status.INIT;

    GuildSetupNode(long id, GuildSetupController controller, Type type)
    {
        this.id = id;
        this.controller = controller;
        this.type = type;
    }

    public long getIdLong()
    {
        return id;
    }

    public String getId()
    {
        return Long.toUnsignedString(id);
    }

    public GuildSetupController.Status getStatus()
    {
        return status;
    }

    @Nullable
    public DataObject getGuildPayload()
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

    public Type getType()
    {
        return type;
    }

    public boolean isJoin()
    {
        return type == Type.JOIN;
    }

    public boolean isMarkedUnavailable()
    {
        return markedUnavailable;
    }

    public boolean requestedChunks()
    {
        return requestedChunk;
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
                "requestedChunk="      + requestedChunk + ", " +
                "type="                + type + ", " +
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
        return controller;
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
        if (members != null)
            members.clear();
        if (removedMembers != null)
            removedMembers.clear();
        cachedEvents.clear();
    }

    void handleReady(DataObject obj) {}

    void handleCreate(DataObject obj)
    {
        if (partialGuild == null)
        {
            partialGuild = obj;
        }
        else
        {
            for (String key : obj.keys())
            {
                partialGuild.put(key, obj.opt(key).orElse(null));
            }
        }
        boolean unavailable = partialGuild.getBoolean("unavailable");
        boolean wasMarkedUnavailable = this.markedUnavailable;
        this.markedUnavailable = unavailable;
        if (unavailable)
        {
            if (!firedUnavailableJoin && isJoin())
            {
                firedUnavailableJoin = true;
                JDAImpl api = getController().getJDA();
                api.handleEvent(new UnavailableGuildJoinedEvent(api, api.getResponseTotal(), id));
            }
            return;
        }

        ensureMembers();
    }

    void handleSync(DataObject obj)
    {
        if (partialGuild == null)
        {
            //In this case we received a GUILD_DELETE with unavailable = true while syncing
            // however we have to wait for the GUILD_CREATE with unavailable = false before
            // requesting new chunks
            GuildSetupController.log.debug("Dropping sync update due to unavailable guild");
            return;
        }
        for (String key : obj.keys())
        {
            partialGuild.put(key, obj.opt(key).orElse(null));
        }

        ensureMembers();
    }

    boolean handleMemberChunk(boolean last, DataArray arr)
    {
        if (partialGuild == null)
        {
            //In this case we received a GUILD_DELETE with unavailable = true while chunking
            // however we have to wait for the GUILD_CREATE with unavailable = false before
            // requesting new chunks
            GuildSetupController.log.debug("Dropping member chunk due to unavailable guild");
            return true;
        }
        for (int index = 0; index < arr.length(); index++)
        {
            DataObject obj = arr.getObject(index);
            long id = obj.getObject("user").getLong("id");
            members.put(id, obj);
        }

        if (last || members.size() >= expectedMemberCount || !getController().getJDA().chunkGuild(id))
        {
            completeSetup();
            return false;
        }
        return true;
    }

    void handleAddMember(DataObject member)
    {
        if (members == null || removedMembers == null)
            return;
        expectedMemberCount++;
        long userId = member.getObject("user").getLong("id");
        members.put(userId, member);
        removedMembers.remove(userId);
    }

    void handleRemoveMember(DataObject member)
    {
        if (members == null || removedMembers == null)
            return;
        expectedMemberCount--;
        long userId = member.getObject("user").getLong("id");
        members.remove(userId);
        removedMembers.add(userId);
        EventCache eventCache = getController().getJDA().getEventCache();
        if (!getController().containsMember(userId, this)) // if no other setup node contains this userId we clear it here
            eventCache.clear(EventCache.Type.USER, userId);
    }

    void cacheEvent(DataObject event)
    {
        GuildSetupController.log.trace("Caching {} event during init. GuildId: {}", event.getString("t"), id);
        cachedEvents.add(event);
        //Check if more than 2000 events cached - suspicious
        // Print warning every 1000 events
        int cacheSize = cachedEvents.size();
        if (cacheSize >= 2000 && cacheSize % 1000 == 0)
        {
            GuildSetupController controller = getController();
            GuildSetupController.log.warn(
                "Accumulating suspicious amounts of cached events during guild setup, " +
                "something might be wrong. Cached: {} Members: {}/{} Status: {} GuildId: {} Incomplete: {}/{}",
                cacheSize, getCurrentMemberCount(), getExpectedMemberCount(),
                status, id, controller.getChunkingCount(), controller.getIncompleteCount());

            if (status == GuildSetupController.Status.CHUNKING)
            {
                GuildSetupController.log.debug("Forcing new chunk request for guild: {}", id);
                controller.sendChunkRequest(id);
            }
        }
    }

    void cleanup()
    {
        updateStatus(GuildSetupController.Status.REMOVED);
        EventCache eventCache = getController().getJDA().getEventCache();
        eventCache.clear(EventCache.Type.GUILD, id);
        if (partialGuild == null)
            return;

        Optional<DataArray> channels = partialGuild.optArray("channels");
        Optional<DataArray> roles = partialGuild.optArray("roles");
        channels.ifPresent((arr) -> {
            for (int i = 0; i < arr.length(); i++)
            {
                DataObject json = arr.getObject(i);
                long id = json.getLong("id");
                eventCache.clear(EventCache.Type.CHANNEL, id);
            }
        });

        roles.ifPresent((arr) -> {
            for (int i = 0; i < arr.length(); i++)
            {
                DataObject json = arr.getObject(i);
                long id = json.getLong("id");
                eventCache.clear(EventCache.Type.ROLE, id);
            }
        });

        if (members != null)
        {
            for (TLongObjectIterator<DataObject> it = members.iterator(); it.hasNext();)
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
        GuildImpl guild = api.getEntityBuilder().createGuild(id, partialGuild, members, expectedMemberCount);
        updateAudioManagerReference(guild);
        switch (type)
        {
        case AVAILABLE:
            api.handleEvent(new GuildAvailableEvent(api, api.getResponseTotal(), guild));
            getController().remove(id);
            break;
        case JOIN:
            api.handleEvent(new GuildJoinEvent(api, api.getResponseTotal(), guild));
            if (requestedChunk)
                getController().ready(id);
            else
                getController().remove(id);
            break;
        default:
            api.handleEvent(new GuildReadyEvent(api, api.getResponseTotal(), guild));
            getController().ready(id);
            break;
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
        DataArray memberArray = partialGuild.getArray("members");
        if (!getController().getJDA().chunkGuild(id))
        {
            handleMemberChunk(true, memberArray);
        }
        else if (memberArray.length() < expectedMemberCount && !requestedChunk)
        {
            updateStatus(GuildSetupController.Status.CHUNKING);
            getController().addGuildForChunking(id, isJoin());
            requestedChunk = true;
        }
        else if (handleMemberChunk(false, memberArray) && !requestedChunk)
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
            getController().addGuildForChunking(id, isJoin());
            requestedChunk = true;
        }
    }

    private void updateAudioManagerReference(GuildImpl guild)
    {
        JDAImpl api = getController().getJDA();
        AbstractCacheView<AudioManager> managerView = api.getAudioManagersView();
        try (UnlockHook hook = managerView.writeLock())
        {
            TLongObjectMap<AudioManager> audioManagerMap = managerView.getMap();
            AudioManagerImpl mng = (AudioManagerImpl) audioManagerMap.get(id);
            if (mng == null)
                return;
            ConnectionListener listener = mng.getConnectionListener();
            final AudioManagerImpl newMng = new AudioManagerImpl(guild);
            newMng.setSelfMuted(mng.isSelfMuted());
            newMng.setSelfDeafened(mng.isSelfDeafened());
            newMng.setQueueTimeout(mng.getConnectTimeout());
            newMng.setSendingHandler(mng.getSendingHandler());
            newMng.setReceivingHandler(mng.getReceivingHandler());
            newMng.setConnectionListener(listener);
            newMng.setAutoReconnect(mng.isAutoReconnect());

            if (mng.isConnected())
            {
                final long channelId = mng.getConnectedChannel().getIdLong();

                final VoiceChannel channel = api.getVoiceChannelById(channelId);
                if (channel != null)
                {
                    if (mng.isConnected())
                        mng.closeAudioConnection(ConnectionStatus.ERROR_CANNOT_RESUME);
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

    public enum Type
    {
        INIT, JOIN, AVAILABLE
    }
}
