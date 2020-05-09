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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class MemberChunkManager
{
    private static final long MAX_CHUNK_AGE = 10 * 1000; // 10 seconds
    private final WebSocketClient client;
    private final ReentrantLock lock = new ReentrantLock();
    private final TLongObjectMap<ChunkRequest> requests = new TLongObjectHashMap<>();
    private Future<?> timeoutHandle;

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

    private void init()
    {
        MiscUtil.locked(lock, () -> {
            if (timeoutHandle == null)
                timeoutHandle = client.getJDA().getGatewayPool().scheduleAtFixedRate(new TimeoutHandler(), 5, 5, TimeUnit.SECONDS);
        });
    }

    public void shutdown()
    {
        if (timeoutHandle != null)
            timeoutHandle.cancel(false);
    }

    public CompletableFuture<DataObject> chunkGuild(long guildId, String query, int limit)
    {
        init();
        DataObject request = DataObject.empty()
                .put("guild_id", guildId)
                .put("limit", Math.min(100, Math.max(1, limit)))
                .put("query", query);

        ChunkRequest chunkRequest = new ChunkRequest(request);
        makeRequest(chunkRequest);
        return chunkRequest;
    }

    public CompletableFuture<DataObject> chunkGuild(long guildId, long[] userIds)
    {
        init();
        DataObject request = DataObject.empty()
                .put("guild_id", guildId)
                .put("user_ids", userIds);

        ChunkRequest chunkRequest = new ChunkRequest(request);
        makeRequest(chunkRequest);
        return chunkRequest;
    }

    public boolean handleChunk(long guildId, DataObject response)
    {
        return MiscUtil.locked(lock, () -> {
            String nonce = response.getString("nonce", null);
            if (nonce == null || nonce.isEmpty())
                return false;
            ChunkRequest request = requests.remove(Long.parseLong(nonce));
            if (request == null)
                return false;

            request.complete(response);
            return true;
        });
    }

    public void cancelRequest(ChunkRequest request)
    {
        MiscUtil.locked(lock, () -> {
            requests.remove(request.nonce);
        });
    }

    private void makeRequest(ChunkRequest request)
    {
        MiscUtil.locked(lock, () -> {
            requests.put(request.nonce, request);
            sendChunkRequest(request.getRequest());
        });
    }

    private void sendChunkRequest(DataObject request)
    {
        client.sendChunkRequest(request);
    }

    private class ChunkRequest extends CompletableFuture<DataObject>
    {
        private final DataObject request;
        private final long nonce;
        private long startTime;

        public ChunkRequest(DataObject request)
        {
            this.nonce = System.nanoTime() & ~1;
            this.request = request.put("nonce", getNonce());
        }

        public boolean isNonce(String nonce)
        {
            return this.nonce == Long.parseLong(nonce);
        }

        public String getNonce()
        {
            return String.valueOf(nonce);
        }

        public long getAge()
        {
            return startTime <= 0 ? 0 : System.currentTimeMillis() - startTime;
        }

        public DataObject getRequest()
        {
            startTime = System.currentTimeMillis();
            return request;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            client.cancelChunkRequest(getNonce());
            cancelRequest(this);
            return super.cancel(mayInterruptIfRunning);
        }
    }

    private class TimeoutHandler implements Runnable
    {
        @Override
        public void run()
        {
            MiscUtil.locked(lock, () ->
            {
                requests.forEachValue(request -> {
                    if (request.getAge() > MAX_CHUNK_AGE)
                        request.completeExceptionally(new TimeoutException());
                    return true;
                });
                requests.valueCollection().removeIf(ChunkRequest::isDone);
            });
        }
    }
}
