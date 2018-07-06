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
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;

public class GuildSetupController
{
    protected static final Logger log = JDALogger.getLog(GuildSetupController.class);
    private final WeakReference<JDAImpl> api;
    private final TLongObjectMap<GuildSetupNode> setupNodes = new TLongObjectHashMap<>();
    private final TLongSet chunkingGuilds = new TLongHashSet();
    private final TLongSet syncingGuilds = new TLongHashSet(); //TODO: guild-sync
    private int incompleteCount = 0;

    public GuildSetupController(JDAImpl api)
    {
        this.api = new WeakReference<>(api);
    }

    JDAImpl getJDA()
    {
        JDAImpl tmp = api.get();
        if (tmp == null)
            throw new IllegalStateException();
        return tmp;
    }

    void addGuildForChunking(long id)
    {
        log.debug("Adding guild for chunking ID: {}", id);
        chunkingGuilds.add(id);
        //TODO: guild-sync
        tryChunking();
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
    }

    // - ReadyHandler
    public void onReady(long id, JSONObject obj)
    {
        log.debug("Adding id to setup cache {}", id);
        GuildSetupNode node = new GuildSetupNode(id, this, false);
        setupNodes.put(id, node);
        node.handleReady(obj);
    }

    // - ReadyHandler (for client accounts)
    // - GuildCreateHandler
    public void onCreate(long id, JSONObject obj)
    {
        boolean available = obj.isNull("unavailable") || !obj.getBoolean("unavailable");
        log.debug("Received guild create for id: {} available: {}", id, available);
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
        log.debug("Received guild delete for id: {} available: {}", id, available);
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return false;
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
    // - GuildMemberRemoveHandler
    public void updateMemberChunk(long id, int change)
    {
        GuildSetupNode node = setupNodes.get(id);
        if (node == null)
            return;
        log.debug("Updating member chunk count for id {} with {}", id, change);
        node.updateMemberChunkCount(change);
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

    // Chunking

    private void sendChunkRequest(JSONArray arr)
    {
        log.debug("Sending chunking requests for {} guilds", arr.length());

        getJDA().getClient().send(
            new JSONObject()
                .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
                .put("d", new JSONObject()
                    .put("guild_id", arr)
                    .put("query", "")
                    .put("limit", 0)).toString());
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
}
