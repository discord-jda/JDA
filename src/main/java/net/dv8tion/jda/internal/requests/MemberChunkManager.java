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
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class MemberChunkManager
{
    private final WebSocketClient client;
    private final ReentrantLock lock = new ReentrantLock();
    private final TLongObjectMap<Queue<ChunkRequest>> requests = new TLongObjectHashMap<>();

    public MemberChunkManager(WebSocketClient client)
    {
        this.client = client;
    }

    public static boolean isLastChunk(DataObject chunk)
    {
        return chunk.getInt("chunk_index") + 1 == chunk.getInt("chunk_count");
    }

    public void clear()
    {
        MiscUtil.locked(lock, requests::clear);
    }

    public CompletableFuture<DataObject> chunkGuild(long guildId, String query, int limit)
    {
        DataObject request = DataObject.empty()
                .put("guild_id", guildId)
                .put("limit", Math.min(100, Math.max(1, limit)))
                .put("query", query);

        ChunkRequest chunkRequest = new ChunkRequest(guildId, request);
        makeRequest(guildId, chunkRequest);
        return chunkRequest;
    }

    public boolean handleChunk(long guildId, DataObject response)
    {
        return MiscUtil.locked(lock, () -> {
            long nonce = response.getLong("nonce", 0L);
            if (nonce == 0L)
                return false;
            Queue<ChunkRequest> queue = requests.get(guildId);
            if (queue == null || queue.isEmpty())
                return false;

            Optional<ChunkRequest> request = queue.stream().filter(r -> r.isNonce(nonce)).findFirst();
            if (request.isPresent())
            {
                ChunkRequest node = request.get();
                queue.remove(node);
                node.complete(response);
                processQueue(guildId, queue);
                return true;
            }
            else
            {
                processQueue(guildId, queue);
                return false;
            }
        });
    }

    public void cancelRequest(ChunkRequest request)
    {
        MiscUtil.locked(lock, () -> {
            Queue<ChunkRequest> queue = requests.get(request.guildId);
            if (queue == null || queue.isEmpty())
                return;

            boolean removed = queue.removeIf(request::equals);
        });
    }

    private void processQueue(long guildId, Queue<ChunkRequest> queue)
    {
        while (queue != null && !queue.isEmpty())
        {
            ChunkRequest element = queue.peek();
            if (element.isCancelled())
            {
                queue.remove();
                continue;
            }

            sendChunkRequest(element.getRequest());
            return;
        }

        requests.remove(guildId);
    }

    private void makeRequest(long guildId, ChunkRequest request)
    {
        MiscUtil.locked(lock, () -> {
            Queue<ChunkRequest> queue = requests.get(guildId);
            if (queue == null)
                requests.put(guildId, queue = new LinkedList<>());

            queue.add(request);
            sendChunkRequest(request.getRequest());
        });
    }

    private void sendChunkRequest(DataObject request)
    {
        client.chunkOrSyncRequest(DataObject.empty()
            .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
            .put("d", request));
    }

    private class ChunkRequest extends CompletableFuture<DataObject>
    {
        private final long guildId;
        private final DataObject request;
        private long nonce;

        public ChunkRequest(long guildId, DataObject request)
        {
            this.guildId = guildId;
            this.request = request;
        }

        public boolean isNonce(long nonce)
        {
            return this.nonce == nonce;
        }

        public long getNonce()
        {
            return this.nonce = System.currentTimeMillis() & ~1;
        }

        public DataObject getRequest()
        {
            return request.put("nonce", getNonce());
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            cancelRequest(this);
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
