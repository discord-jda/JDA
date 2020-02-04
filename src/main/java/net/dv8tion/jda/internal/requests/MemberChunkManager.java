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
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class MemberChunkManager
{
    private final WebSocketClient client;
    private final ReentrantLock lock = new ReentrantLock();
    private final TLongObjectMap<Queue<ChunkRequest>> requests = new TLongObjectHashMap<>();
    private final TLongSet requestedChunk = new TLongHashSet();

    public MemberChunkManager(WebSocketClient client)
    {
        this.client = client;
    }

    public void clear()
    {
        MiscUtil.locked(lock, () -> {
            requests.clear();
            requestedChunk.clear();
        });
    }

    public CompletableFuture<DataObject> chunkGuild(long guildId, String query, int limit)
    {
        DataObject request = DataObject.empty()
                .put("guild_id", guildId)
                .put("limit", Math.min(100, Math.max(1, limit)))
                .put("query", query);

        CompletableFuture<DataObject> responseHandler = new CompletableFuture<>();
        ChunkRequest chunkRequest = new ChunkRequest(request, responseHandler);
        makeRequest(guildId, request, chunkRequest);
        return responseHandler;
    }

    public boolean handleChunk(long guildId, DataObject response)
    {
        //TODO: We currently have no way to detect "no matches found" so the system can lock up here
        boolean[] handled = {false};
        MiscUtil.locked(lock, () -> {
            if (!requestedChunk.remove(guildId))
                return;
            handled[0] = true;
            Queue<ChunkRequest> queue = requests.get(guildId);
            ChunkRequest chunkRequest = queue.remove();
            chunkRequest.responseHandler.complete(response);

            if (!queue.isEmpty())
            {
                sendChunkRequest(queue.peek().request);
                requestedChunk.add(guildId);
            }
            else
            {
                requests.remove(guildId);
            }
        });
        return handled[0];
    }

    private void makeRequest(long guildId, DataObject request, ChunkRequest chunkRequest)
    {
        MiscUtil.locked(lock, () -> {
            Queue<ChunkRequest> queue = requests.get(guildId);
            if (queue == null)
                requests.put(guildId, queue = new LinkedList<>());

            queue.add(chunkRequest);
            if (requestedChunk.add(guildId))
                sendChunkRequest(request);
        });
    }

    private void sendChunkRequest(DataObject request)
    {
        client.chunkOrSyncRequest(DataObject.empty()
            .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
            .put("d", request));
    }

    private class ChunkRequest
    {
        private final DataObject request;
        private final CompletableFuture<DataObject> responseHandler;

        public ChunkRequest(DataObject request, CompletableFuture<DataObject> responseHandler)
        {
            this.request = request;
            this.responseHandler = responseHandler;
        }
    }
}
