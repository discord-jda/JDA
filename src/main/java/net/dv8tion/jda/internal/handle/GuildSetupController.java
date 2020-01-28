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

package net.dv8tion.jda.internal.handle;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.requests.WebSocketCode;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class GuildSetupController
{
    protected static final int CHUNK_TIMEOUT = 10000;
    protected static final Logger log = JDALogger.getLog(GuildSetupController.class);

    private final JDAImpl api;
    private final TLongObjectMap<GuildSetupNode> setupNodes = new TLongObjectHashMap<>();
    private final TLongSet chunkingGuilds = new TLongHashSet();
    private final TLongLongMap pendingChunks = new TLongLongHashMap();
    private final TLongSet syncingGuilds;
    private final TLongSet unavailableGuilds = new TLongHashSet();

    private int incompleteCount = 0;
    private int syncingCount = 0;

    private Future<?> timeoutHandle;

    protected StatusListener listener = (id, oldStatus, newStatus) -> log.trace("[{}] Updated status {}->{}", id, oldStatus, newStatus);

    public GuildSetupController(JDAImpl api)
    {
        this.api = api;
        if (isClient())
            syncingGuilds = new TLongHashSet();
        else
            syncingGuilds = null;
    }

    JDAImpl getJDA()
    {
        return api;
    }

    boolean isClient()
    {
        return getJDA().getAccountType() == AccountType.CLIENT;
    }

    void addGuildForChunking(long id, boolean join)
    {
        log.trace("Adding guild for chunking ID: {}", id);
        if (join || incompleteCount <= 0)
        {
            if (incompleteCount <= 0)
            {
                // this happens during runtime -> chunk right away
                sendChunkRequest(id);
                return;
            }
            incompleteCount++;
        }
        chunkingGuilds.add(id);
        tryChunking();
    }

    void addGuildForSyncing(long id, boolean join)
    {
        if (!isClient())
            return;
        log.trace("Adding guild for syncing ID: {}", id);
        if (join || incompleteCount <= 0)
        {
            if (incompleteCount <= 0)
            {
                // this happens during runtime -> sync right away
                sendSyncRequest(DataArray.empty().add(id));
                return;
            }
            syncingCount++;
        }
        syncingGuilds.add(id);
        trySyncing();
    }

    void remove(long id)
    {
        unavailableGuilds.remove(id);
        setupNodes.remove(id);
        chunkingGuilds.remove(id);
        synchronized (pendingChunks) { pendingChunks.remove(id); }
        if (syncingGuilds != null)
            syncingGuilds.remove(id);
    }

    public void ready(long id)
    {
        remove(id);
        WebSocketClient client = getJDA().getClient();
        if (--incompleteCount < 1 && !client.isReady())
            client.ready();
        else
            tryChunking();
    }

    public boolean setIncompleteCount(int count)
    {
        log.debug("Setting incomplete count to {}", count);
        this.incompleteCount = count;
        this.syncingCount = count;
        boolean ready = count == 0;
        if (ready)
            getJDA().getClient().ready();
        else
            startTimeout();
        return !ready;
    }

    public void onReady(long id, DataObject obj)
    {
        log.trace("Adding id to setup cache {}", id);
        GuildSetupNode node = new GuildSetupNode(id, this, GuildSetupNode.Type.INIT);
        setupNodes.put(id, node);
        node.handleReady(obj);
        if (node.markedUnavailable)
        {
            if (node.sync)
            {
                syncingCount--;
                trySyncing();
            }
            incompleteCount--;
            tryChunking();
        }
    }

    public void onCreate(long id, DataObject obj)
    {
        boolean available = obj.isNull("unavailable") || !obj.getBoolean("unavailable");
        log.trace("Received guild create for id: {} available: {}", id, available);

        if (available && unavailableGuilds.contains(id) && !setupNodes.containsKey(id))
        {
            // Guild was unavailable for a moment, its back now so initialize it again!
            unavailableGuilds.remove(id);
            setupNodes.put(id, new GuildSetupNode(id, this, GuildSetupNode.Type.AVAILABLE));
        }

        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
        {
            // this is a join event
            node = new GuildSetupNode(id, this, GuildSetupNode.Type.JOIN);
            setupNodes.put(id, node);
            // do not increment incomplete counter, it is only relevant to init guilds
        }
        else if (node.markedUnavailable && available && incompleteCount > 0)
        {
            //Looks like this guild decided to become available again during startup
            // that means we can now consider it for ReadyEvent status again!
            if (node.sync)
                syncingCount++;
            incompleteCount++;
        }
        node.handleCreate(obj);
    }

    public boolean onDelete(long id, DataObject obj)
    {
        boolean available = obj.isNull("unavailable") || !obj.getBoolean("unavailable");
        if (isUnavailable(id) && available)
        {
            log.debug("Leaving unavailable guild with id {}", id);
            remove(id);
            api.getEventManager().handle(new UnavailableGuildLeaveEvent(api, api.getResponseTotal(), id));
            return true;
        }

        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
        log.debug("Received guild delete for id: {} available: {}", id, available);
        if (!available)
        {
            // The guild is currently unavailable and should be ignored for chunking requests
            if (!node.markedUnavailable)
            {
                node.markedUnavailable = true; // this prevents repeated decrements from duplicate events
                if (node.sync && !node.requestedChunk)
                {
                    // If this node is chunking then it is already synced
                    syncingGuilds.remove(id);
                    syncingCount--;
                    trySyncing();
                }
                if (incompleteCount > 0)
                {
                    // Allow other guilds to start chunking
                    chunkingGuilds.remove(id);
                    synchronized (pendingChunks) { pendingChunks.remove(id); }
                    incompleteCount--;
                    tryChunking();
                }
            }
            node.reset();
        }
        else
        {
            // This guild was deleted
            node.cleanup(); // clear EventCache
            if (node.isJoin() && !node.requestedChunk)
                remove(id);
            else
                ready(id);
            api.getEventManager().handle(new UnavailableGuildLeaveEvent(api, api.getResponseTotal(), id));
        }
        log.debug("Updated incompleteCount to {} and syncCount to {}", incompleteCount, syncingCount);
        return true;
    }

    public void onMemberChunk(long id, DataArray chunk)
    {
        log.debug("Received member chunk for guild id: {} size: {}", id, chunk.length());
        synchronized (pendingChunks)
        {
            pendingChunks.remove(id);
        }
        GuildSetupNode node = setupNodes.get(id);
        if (node != null)
            node.handleMemberChunk(chunk);
    }

    public boolean onAddMember(long id, DataObject member)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
        log.debug("Received GUILD_MEMBER_ADD during setup, adding member to guild. GuildID: {}", id);
        node.handleAddMember(member);
        return true;
    }

    public boolean onRemoveMember(long id, DataObject member)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
        log.debug("Received GUILD_MEMBER_REMOVE during setup, removing member from guild. GuildID: {}", id);
        node.handleRemoveMember(member);
        return true;
    }

    public void onSync(long id, DataObject obj)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node != null)
            node.handleSync(obj);
    }

    public boolean isLocked(long id)
    {
        return setupNodes.containsKey(id);
    }

    public boolean isUnavailable(long id)
    {
        return unavailableGuilds.contains(id);
    }

    public boolean isKnown(long id)
    {
        // Whether we know this guild at all
        return isLocked(id) || isUnavailable(id);
    }

    public void cacheEvent(long guildId, DataObject event)
    {
        GuildSetupNode node = setupNodes.get(guildId);
        if (node != null)
            node.cacheEvent(event);
        else
            log.warn("Attempted to cache event for a guild that is not locked. {}", event, new IllegalStateException());
    }

    public void clearCache()
    {
        setupNodes.clear();
        chunkingGuilds.clear();
        unavailableGuilds.clear();
        incompleteCount = 0;
        close();
        synchronized (pendingChunks)
        {
            pendingChunks.clear();
        }
    }

    public void close()
    {
        if (timeoutHandle != null)
            timeoutHandle.cancel(false);
    }

    public boolean containsMember(long userId, @Nullable GuildSetupNode excludedNode)
    {
        for (TLongObjectIterator<GuildSetupNode> it = setupNodes.iterator(); it.hasNext();)
        {
            it.advance();
            GuildSetupNode node = it.value();
            if (node != excludedNode && node.containsMember(userId))
                return true;
        }
        return false;
    }

    public TLongSet getUnavailableGuilds()
    {
        return unavailableGuilds;
    }

    public Set<GuildSetupNode> getSetupNodes()
    {
        return new HashSet<>(setupNodes.valueCollection());
    }

    public Set<GuildSetupNode> getSetupNodes(Status status)
    {
        return getSetupNodes().stream().filter((node) -> node.status == status).collect(Collectors.toSet());
    }

    public GuildSetupNode getSetupNodeById(long id)
    {
        return setupNodes.get(id);
    }

    public GuildSetupNode getSetupNodeById(String id)
    {
        return getSetupNodeById(MiscUtil.parseSnowflake(id));
    }

    public void setStatusListener(StatusListener listener)
    {
        this.listener = Objects.requireNonNull(listener);
    }

    // Chunking

    int getIncompleteCount()
    {
        return incompleteCount;
    }

    int getChunkingCount()
    {
        return chunkingGuilds.size();
    }

    void sendChunkRequest(Object obj)
    {
        log.debug("Sending chunking requests for {} guilds", obj instanceof DataArray ? ((DataArray) obj).length() : 1);

        long timeout = System.currentTimeMillis() + CHUNK_TIMEOUT;
        synchronized (pendingChunks)
        {
            if (obj instanceof DataArray)
            {
                DataArray arr = (DataArray) obj;
                for (Object o : arr)
                    pendingChunks.put((long) o, timeout);
            }
            else
            {
                pendingChunks.put((long) obj, timeout);
            }
        }

        getJDA().getClient().chunkOrSyncRequest(
            DataObject.empty()
                .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
                .put("d", DataObject.empty()
                    .put("guild_id", obj)
                    .put("query", "")
                    .put("limit", 0)));
    }

    private void tryChunking()
    {
        if (chunkingGuilds.size() >= 50)
        {
            // request chunks
            final DataArray subset = DataArray.empty();
            for (final TLongIterator it = chunkingGuilds.iterator(); subset.length() < 50; )
            {
                subset.add(it.next());
                it.remove();
            }
            sendChunkRequest(subset);
        }
        if (incompleteCount > 0 && chunkingGuilds.size() >= incompleteCount)
        {
            // request last chunks
            final DataArray array = DataArray.empty();
            chunkingGuilds.forEach((guild) -> {
                array.add(guild);
                return true;
            });
            chunkingGuilds.clear();
            sendChunkRequest(array);
        }
    }

    private void startTimeout()
    {
        timeoutHandle = getJDA().getGatewayPool().scheduleAtFixedRate(new ChunkTimeout(), CHUNK_TIMEOUT, CHUNK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    // Syncing

    private void sendSyncRequest(DataArray arr)
    {
        log.debug("Sending syncing requests for {} guilds", arr.length());

        getJDA().getClient().chunkOrSyncRequest(
            DataObject.empty()
                .put("op", WebSocketCode.GUILD_SYNC)
                .put("d", arr));
    }

    private void trySyncing()
    {
        if (syncingGuilds.size() >= 50)
        {
            // request chunks
            final DataArray subset = DataArray.empty();
            for (final TLongIterator it = syncingGuilds.iterator(); subset.length() < 50; )
            {
                subset.add(it.next());
                it.remove();
            }
            sendSyncRequest(subset);
            syncingCount -= subset.length();
        }
        if (syncingCount > 0 && syncingGuilds.size() >= syncingCount)
        {
            final DataArray array = DataArray.empty();
            syncingGuilds.forEach((guild) -> {
                array.add(guild);
                return true;
            });
            syncingGuilds.clear();
            sendSyncRequest(array);
            syncingCount = 0;
        }
    }

    public void onUnavailable(long id)
    {
        unavailableGuilds.add(id);
        log.debug("Guild with id {} is now marked unavailable. Total: {}", id, unavailableGuilds.size());
    }

    public enum Status
    {
        INIT,
        SYNCING,
        CHUNKING,
        BUILDING,
        READY,
        UNAVAILABLE,
        REMOVED
    }

    @FunctionalInterface
    public interface StatusListener
    {
        void onStatusChange(long guildId, Status oldStatus, Status newStatus);
    }

    private class ChunkTimeout implements Runnable
    {
        @Override
        public void run()
        {
            if (pendingChunks.isEmpty())
                return;
            synchronized (pendingChunks)
            {
                TLongLongIterator it = pendingChunks.iterator();
                List<DataArray> requests = new LinkedList<>();
                DataArray arr = DataArray.empty();
                while (it.hasNext())
                {
                    // key=guild_id, value=timeout
                    it.advance();
                    if (System.currentTimeMillis() <= it.value())
                        continue;
                    arr.add(it.key());

                    if (arr.length() == 50)
                    {
                        requests.add(arr);
                        arr = DataArray.empty();
                    }
                }
                if (arr.length() > 0)
                    requests.add(arr);
                requests.forEach(GuildSetupController.this::sendChunkRequest);
            }
        }
    }
}
