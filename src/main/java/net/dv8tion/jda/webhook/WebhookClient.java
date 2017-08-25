/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.IOUtil;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.tuple.ImmutablePair;
import net.dv8tion.jda.core.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.*;

public class WebhookClient implements Closeable
{
    public static final String WEBHOOK_URL = "https://discordapp.com/api/v6/webhooks/%s/%s";
    public static final SimpleLog LOG = SimpleLog.getLog("WebhookClient");

    protected final String url;
    protected final long id;
    protected final OkHttpClient client;
    protected final ScheduledExecutorService pool;
    protected final Bucket bucket;
    protected final BlockingQueue<Pair<RequestBody, CompletableFuture<?>>> queue;
    protected volatile boolean isQueued;

    protected WebhookClient(final long id, final String token, final OkHttpClient client, final ScheduledExecutorService pool)
    {
        this.client = client;
        this.id = id;
        this.url = String.format(WEBHOOK_URL, Long.toUnsignedString(id), token);
        this.pool = pool;
        this.bucket = new Bucket();
        this.queue = new LinkedBlockingQueue<>();
        this.isQueued = false;
    }

    public long getIdLong()
    {
        return id;
    }

    public String getId()
    {
        return Long.toUnsignedString(id);
    }

    public String getUrl()
    {
        return url;
    }

    public Future<?> send(WebhookMessage message)
    {
        Checks.notNull(message, "WebhookMessage");
        return execute(message.getBody());
    }

    public Future<?> send(File file)
    {
        Checks.notNull(file, "File");
        return send(file, file.getName());
    }

    public Future<?> send(File file, String fileName)
    {
        return send(new WebhookMessageBuilder().setFile(file, fileName).build());
    }

    public Future<?> send(byte[] data, String fileName)
    {
        Checks.notNull(data, "Data");
        return send(new ByteArrayInputStream(data), fileName);
    }

    public Future<?> send(InputStream data, String fileName)
    {
        return send(new WebhookMessageBuilder().setFile(data, fileName).build());
    }

    public Future<?> send(Message message)
    {
        return send(WebhookMessage.from(message));
    }

    public Future<?> send(MessageEmbed embed)
    {
        return send(Collections.singleton(embed));
    }

    public Future<?> send(MessageEmbed... embeds)
    {
        return send(WebhookMessage.of(embeds));
    }

    public Future<?> send(Collection<MessageEmbed> embeds)
    {
        return send(WebhookMessage.of(embeds));
    }

    public Future<?> send(String content)
    {
        return execute(newBody(new JSONObject().put("content", content).toString()));
    }

    @Override
    public void close()
    {
        pool.shutdown();
    }

    protected static RequestBody newBody(String object)
    {
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, object);
    }

    protected Future<?> execute(RequestBody body)
    {
        if (isQueued || bucket.isRateLimit())
            return queueRequest(body);

        Request request = newRequest(body);
        try (Response response = client.newCall(request).execute())
        {
            bucket.update(response);
            if (response.code() == Bucket.RATE_LIMIT_CODE)
                return queueRequest(body);
            return CompletableFuture.completedFuture(null);
        }
        catch (IOException e)
        {
            LOG.log(e);
            return null;//FIXME: return new FailedFuture(e);
        }
    }

    protected Future<?> queueRequest(RequestBody body)
    {
        final boolean wasQueued = isQueued;
        isQueued = true;
        CompletableFuture<?> callback = new CompletableFuture<>();
        queue.add(ImmutablePair.of(body, callback));
        if (!wasQueued)
            pool.schedule(this::drainQueue, bucket.retryAfter(), TimeUnit.MILLISECONDS);
        return callback;
    }

    protected Request newRequest(RequestBody body)
    {
        return new Request.Builder()
                .url(url)
                .method("POST", body)
                .header("accept-encoding", "gzip")
                .header("content-type", "application/json")
                .build();
    }


    protected void drainQueue()
    {
        while (!queue.isEmpty())
        {
            Request request = newRequest(queue.peek().getLeft());
            try (Response response = client.newCall(request).execute())
            {
                bucket.update(response);
                if (response.code() == Bucket.RATE_LIMIT_CODE)
                {
                    pool.schedule(this::drainQueue, bucket.retryAfter(), TimeUnit.MILLISECONDS);
                    return;
                }
                queue.poll().getRight().complete(null);
            }
            catch (IOException e)
            {
                LOG.log(e);
            }
        }
        isQueued = false;
    }

    protected static final class Bucket
    {
        public static final int RATE_LIMIT_CODE = 429;
        public long resetTime;
        public int remainingUses = Integer.MAX_VALUE;
        public int limit = Integer.MAX_VALUE;

        public boolean isRateLimit()
        {
            if (remainingUses < 1 && retryAfter() <= 0)
                remainingUses = limit;
            return remainingUses < 1;
        }

        public long retryAfter()
        {
            return resetTime - System.currentTimeMillis();
        }

        private void handleRatelimit(Response response, long current) throws IOException
        {
            final String retryAfter = response.header("Retry-After");
            long delay;
            if (retryAfter == null)
            {
                final JSONObject body = new JSONObject(new JSONTokener(Requester.getBody(response)));
                delay = body.getLong("retry_after");
            }
            else
            {
                delay = Long.parseLong(retryAfter);
            }
            resetTime = current + delay;
        }

        private void update0(Response response) throws IOException
        {
            final long current = System.currentTimeMillis();
            if (response.code() == RATE_LIMIT_CODE)
            {
                handleRatelimit(response, current);
                return;
            }
            if (!response.isSuccessful())
            {
                LOG.fatal("Received unsuccessful response with code: " + response.code() + " and body: ");
                LOG.fatal(new String(IOUtil.readFully(Requester.getBody(response))));
                return;
            }
            final long reset = Long.parseLong(response.header("X-RateLimit-Reset")); //not millis
            remainingUses = Integer.parseInt(response.header("X-RateLimit-Remaining"));
            limit = Integer.parseInt(response.header("X-RateLimit-Limit"));
            final String date = response.header("Date");

            if (date != null)
            {
                OffsetDateTime tDate = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
                final long delay = tDate.toInstant().until(Instant.ofEpochSecond(reset), ChronoUnit.MILLIS);
                resetTime = current + delay;
            }
        }

        public void update(Response response)
        {
            try
            {
                update0(response);
            }
            catch (Exception ex)
            {
                LOG.log(ex);
            }
        }
    }
}
