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
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

@SuppressWarnings("WeakerAccess")
public class GuildSetupController
{
    protected static final Logger log = JDALogger.getLog(GuildSetupController.class);
    private final WeakReference<JDAImpl> api;
    private final TLongObjectMap<GuildSetupNode> setupNodes = new TLongObjectHashMap<>();
    private final TLongSet chunkingGuilds = new TLongHashSet();
    private final TLongSet syncingGuilds;
    private int incompleteCount = 0;
    private int syncingCount = 0;

    public GuildSetupController(JDAImpl api)
    {
        this.api = new WeakReference<>(api);
        if (isClient())
            syncingGuilds = new TLongHashSet();
        else
            syncingGuilds = null;
    }

    JDAImpl getJDA()
    {
        JDAImpl tmp = api.get();
        if (tmp == null)
            throw new IllegalStateException();
        return tmp;
    }

    boolean isClient()
    {
        return getJDA().getAccountType() == AccountType.CLIENT;
    }

    void addGuildForChunking(long id, boolean join)
    {
        log.trace("Adding guild for chunking ID: {}", id);
        if (join)
        {
            if (incompleteCount <= 0)
            {
                // this happens during runtime -> chunk right away
                sendChunkRequest(new JSONArray().put(id));
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
        if (join)
        {
            if (incompleteCount <= 0)
            {
                // this happens during runtime -> sync right away
                sendSyncRequest(new JSONArray().put(id));
                return;
            }
            syncingCount++;
        }
        syncingGuilds.add(id);
        trySyncing();
    }

    void remove(long id)
    {
        setupNodes.remove(id);
    }

    // Called by:

    // - ReadyHandler
    // - GuildSetupNode
    public void ready(long id)
    {
        setupNodes.remove(id);
        if (--incompleteCount < 1)
            getJDA().getClient().ready();
        else
            tryChunking();
    }

    // - ReadyHandler
    public void setIncompleteCount(int count)
    {
        log.debug("Setting incomplete count to {}", count);
        this.incompleteCount = count;
        this.syncingCount = count;
    }

    // - ReadyHandler
    public void onReady(long id, JSONObject obj)
    {
        log.trace("Adding id to setup cache {}", id);
        GuildSetupNode node = new GuildSetupNode(id, this, false);
        setupNodes.put(id, node);
        node.handleReady(obj);
    }

    // - ReadyHandler (for client accounts)
    // - GuildCreateHandler
    public void onCreate(long id, JSONObject obj)
    {
        boolean available = obj.isNull("unavailable") || !obj.getBoolean("unavailable");
        log.trace("Received guild create for id: {} available: {}", id, available);
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
        {
            // this is a join event
            node = new GuildSetupNode(id, this, true);
            setupNodes.put(id, node);
            // do not increment incomplete counter, it is only relevant to init guilds
        }
        node.handleCreate(obj);
    }

    // - GuildDeleteHandler
    public boolean onDelete(long id, JSONObject obj)
    {
        boolean available = obj.isNull("unavailable") || !obj.getBoolean("unavailable");
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
        log.debug("Received guild delete for id: {} available: {}", id, available);
        if (!available)
        {
            node.reset();
        }
        else
        {
            node.cleanup(); // clear EventCache
            // this was actually deleted
            if (node.join)
                remove(id);
            else
                ready(id);
        }
        return true;
    }

    // - GuildMemberChunkHandler
    public void onMemberChunk(long id, JSONArray chunk)
    {
        log.debug("Received member chunk for guild id: {} size: {}", id, chunk.length());
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
        {
            log.warn("Received member chunk for a guild that is not currently chunking! ID: {}", id);
            return;
        }
        node.handleMemberChunk(chunk);
    }

    // - GuildMemberAddHandler
    public boolean onAddMember(long id, JSONObject member)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
        log.debug("Received GUILD_MEMBER_ADD during setup, adding member to guild. GuildID: {}", id);
        node.handleAddMember(member);
        return true;
    }

    // - GuildMemberRemoveHandler
    public boolean onRemoveMember(long id, JSONObject member)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
        log.debug("Received GUILD_MEMBER_REMOVE during setup, removing member from guild. GuildID: {}", id);
        node.handleRemoveMember(member);
        return true;
    }

    public void onSync(long id, JSONObject obj)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node != null)
            node.handleSync(obj);
    }

    // Anywhere \\

    public boolean isLocked(long id)
    {
        return setupNodes.containsKey(id);
    }

    public void cacheEvent(long guildId, JSONObject event)
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
        incompleteCount = 0;
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

    // Chunking

    private void sendChunkRequest(JSONArray arr)
    {
        log.debug("Sending chunking requests for {} guilds", arr.length());

        getJDA().getClient().chunkOrSyncRequest(
            new JSONObject()
                .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
                .put("d", new JSONObject()
                    .put("guild_id", arr)
                    .put("query", "")
                    .put("limit", 0)));
    }

    private void tryChunking()
    {
        if (chunkingGuilds.size() >= 50)
        {
            // request chunks
            final JSONArray subset = new JSONArray();
            for (final TLongIterator it = chunkingGuilds.iterator(); subset.length() < 50; )
            {
                subset.put(it.next());
                it.remove();
            }
            sendChunkRequest(subset);
        }
        if (chunkingGuilds.size() == incompleteCount)
        {
            // request last chunks
            final JSONArray array = new JSONArray();
            chunkingGuilds.forEach((guild) -> {
                array.put(guild);
                return true;
            });
            chunkingGuilds.clear();
            sendChunkRequest(array);
        }
    }

    // Syncing

    private void sendSyncRequest(JSONArray arr)
    {
        log.debug("Sending syncing requests for {} guilds", arr.length());

        getJDA().getClient().chunkOrSyncRequest(
            new JSONObject()
                .put("op", WebSocketCode.GUILD_SYNC)
                .put("d", arr));
    }

    private void trySyncing()
    {
        if (syncingGuilds.size() >= 50)
        {
            // request chunks
            final JSONArray subset = new JSONArray();
            for (final TLongIterator it = syncingGuilds.iterator(); subset.length() < 50; )
            {
                subset.put(it.next());
                it.remove();
            }
            sendSyncRequest(subset);
            syncingCount -= subset.length();
        }
        if (syncingGuilds.size() == syncingCount)
        {
            final JSONArray array = new JSONArray();
            syncingGuilds.forEach((guild) -> {
                array.put(guild);
                return true;
            });
            sendSyncRequest(array);
            syncingCount -= array.length();
        }
    }
}
