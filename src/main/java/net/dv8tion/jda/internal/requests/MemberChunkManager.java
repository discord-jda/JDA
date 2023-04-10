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

package net.dv8tion.jda.internal.requests;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

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

    public ChunkRequest chunkGuild(GuildImpl guild, boolean presence, BiConsumer<Boolean, List<Member>> handler)
    {
        init();
        DataObject request = DataObject.empty()
                .put("guild_id", guild.getId())
                .put("presences", presence)
                .put("limit", 0)
                .put("query", "");

        ChunkRequest chunkRequest = new ChunkRequest(handler, guild, request);
        makeRequest(chunkRequest);
        return chunkRequest;
    }

    public ChunkRequest chunkGuild(GuildImpl guild, String query, int limit, BiConsumer<Boolean, List<Member>> handler)
    {
        init();
        DataObject request = DataObject.empty()
                .put("guild_id", guild.getId())
                .put("limit", Math.min(100, Math.max(1, limit)))
                .put("query", query);

        ChunkRequest chunkRequest = new ChunkRequest(handler, guild, request);
        makeRequest(chunkRequest);
        return chunkRequest;
    }

    public ChunkRequest chunkGuild(GuildImpl guild, boolean presence, long[] userIds, BiConsumer<Boolean, List<Member>> handler)
    {
        init();
        DataObject request = DataObject.empty()
                .put("guild_id", guild.getId())
                .put("presences", presence)
                .put("user_ids", userIds);

        ChunkRequest chunkRequest = new ChunkRequest(handler, guild, request);
        makeRequest(chunkRequest);
        return chunkRequest;
    }

    public boolean handleChunk(long guildId, DataObject response)
    {
        return MiscUtil.locked(lock, () -> {
            String nonce = response.getString("nonce", null);
            if (nonce == null || nonce.isEmpty())
                return false;
            long key = Long.parseLong(nonce);
            ChunkRequest request = requests.get(key);
            if (request == null)
                return false;

            boolean lastChunk = isLastChunk(response);
            request.handleChunk(lastChunk, response);
            if (lastChunk || request.isCancelled())
            {
                requests.remove(key);
                request.complete(null);
            }
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

    public class ChunkRequest extends CompletableFuture<Void>
    {
        private final BiConsumer<Boolean, List<Member>> handler;
        private final GuildImpl guild;
        private final DataObject request;
        private final long nonce;
        private long startTime;
        private long timeout = MAX_CHUNK_AGE;

        public ChunkRequest(BiConsumer<Boolean, List<Member>> handler, GuildImpl guild, DataObject request)
        {
            this.handler = handler;
            this.guild = guild;
            this.nonce = ThreadLocalRandom.current().nextLong() & ~1;
            this.request = request.put("nonce", getNonce());
        }

        public ChunkRequest setTimeout(long timeout)
        {
            this.timeout = timeout;
            return this;
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

        public boolean isExpired()
        {
            return getAge() > timeout;
        }

        public DataObject getRequest()
        {
            startTime = System.currentTimeMillis();
            return request;
        }

        private List<Member> toMembers(DataObject chunk)
        {
            EntityBuilder builder = guild.getJDA().getEntityBuilder();
            DataArray memberArray = chunk.getArray("members");
            TLongObjectMap<DataObject> presences = chunk.optArray("presences").map(it ->
                Helpers.convertToMap(o -> o.getObject("user").getUnsignedLong("id"), it)
            ).orElseGet(TLongObjectHashMap::new);
            List<Member> collect = new ArrayList<>(memberArray.length());
            for (int i = 0; i < memberArray.length(); i++)
            {
                DataObject json = memberArray.getObject(i);
                long userId = json.getObject("user").getUnsignedLong("id");
                DataObject presence = presences.get(userId);
                MemberImpl member = builder.createMember(guild, json, null, presence);
                builder.updateMemberCache(member);
                collect.add(member);
            }
            return collect;
        }

        public void handleChunk(boolean last, DataObject chunk)
        {
            try
            {
                if (!isDone())
                    handler.accept(last, toMembers(chunk));
            }
            catch (Throwable ex)
            {
                completeExceptionally(ex);
                if (ex instanceof Error)
                    throw (Error) ex;
            }
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
                    if (request.isExpired())
                        request.completeExceptionally(new TimeoutException());
                    return true;
                });
                requests.valueCollection().removeIf(ChunkRequest::isDone);
            });
        }
    }
}
