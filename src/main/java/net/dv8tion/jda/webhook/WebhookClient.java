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
import net.dv8tion.jda.core.exceptions.HttpException;
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
import java.util.concurrent.*;

/**
 * WebhookClient representing an executable {@link net.dv8tion.jda.core.entities.Webhook Webhook}
 * <br>This client allows to send messages to a Discord Webhook without reliance on a JDA instance/Webhook entity.
 *
 * <p>Instances of this class can be retrieved using {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilders}
 */
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
    protected boolean isShutdown;

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

    /**
     * The snowflake id of the target Webhook
     *
     * @return id of the target Webhook
     */
    public long getIdLong()
    {
        return id;
    }

    /**
     * The snowflake id of the target Webhook
     *
     * @return id of the target Webhook
     */
    public String getId()
    {
        return Long.toUnsignedString(id);
    }

    /**
     * The URL of this WebhookClient
     *
     * @return The URL of this client
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage}
     * to this webhook.
     *
     * <p>Hint: Use {@link net.dv8tion.jda.webhook.WebhookMessageBuilder WebhookMessageBuilder} to
     * create a {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage} instance!
     *
     * @param  message
     *         The message to send
     *
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(WebhookMessage message)
    {
        Checks.notNull(message, "WebhookMessage");
        return execute(message.getBody());
    }

    /**
     * Sends the provided {@link java.io.File File} to this webhook.
     *
     * @param  file
     *         The file to send
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}, does not exist or is not readable
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(File file)
    {
        return send(new WebhookMessageBuilder().setFile(file).build());
    }

    /**
     * Sends the provided {@link java.io.File File} to this webhook.
     *
     * @param  file
     *         The file to send
     * @param  fileName
     *         The name that should be used for this file
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}, does not exist or is not readable
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(File file, String fileName)
    {
        return send(new WebhookMessageBuilder().setFile(file, fileName).build());
    }

    /**
     * Sends the provided {@code byte[]} data to this webhook.
     *
     * @param  data
     *         The file data to send
     * @param  fileName
     *         The name that should be used for this file
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null} or exceeds the limit of 8MB
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(byte[] data, String fileName)
    {
        return send(new WebhookMessageBuilder().setFile(data, fileName).build());
    }

    /**
     * Sends the provided {@link java.io.InputStream InputStream} data to this webhook.
     *
     * @param  data
     *         The file data to send
     * @param  fileName
     *         The name that should be used for this file
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(InputStream data, String fileName)
    {
        return send(new WebhookMessageBuilder().setFile(data, fileName).build());
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message}
     * to this webhook.
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.Message Message} instance!
     *
     * @param  message
     *         The message to send
     *
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(Message message)
    {
        return send(WebhookMessage.from(message));
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to this webhook.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.EmbedBuilder EmbedBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} instance!
     *
     * @param  embeds
     *         The embeds to send
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(MessageEmbed... embeds)
    {
        return send(WebhookMessage.of(embeds));
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to this webhook.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.EmbedBuilder EmbedBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} instance!
     *
     * @param  embeds
     *         The embeds to send
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(Collection<MessageEmbed> embeds)
    {
        return send(WebhookMessage.of(embeds));
    }

    /**
     * Sends the provided text message to this webhook.
     *
     * @param  content
     *         The text message to send
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided message is {@code null}, blank or exceeds 2000 characters in length
     * @throws java.util.concurrent.RejectedExecutionException
     *         If this client was closed
     * @throws net.dv8tion.jda.core.exceptions.HttpException
     *         If the HTTP request fails
     *
     * @return {@link java.util.concurrent.Future Future} representing the execution task,
     *         this will be completed if the message was sent without ratelimits.
     */
    public Future<?> send(String content)
    {
        Checks.notBlank(content, "Content");
        Checks.check(content.length() <= 2000, "Content may not exceed 2000 characters!");
        return execute(newBody(new JSONObject().put("content", content).toString()));
    }

    @Override
    public void close()
    {
        isShutdown = true;
        pool.shutdown();
    }

    @Override
    protected void finalize() throws Throwable
    {
        if (!isShutdown)
            LOG.warn("Detected unclosed WebhookClient! Did you forget to close it?");
    }

    protected void checkShutdown()
    {
        if (isShutdown)
            throw new RejectedExecutionException("Cannot send to closed client!");
    }

    protected static RequestBody newBody(String object)
    {
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, object);
    }

    protected Future<?> execute(RequestBody body)
    {
        checkShutdown();
        if (isQueued || bucket.isRateLimit())
            return queueRequest(body);

        Request request = newRequest(body);
        try (Response response = client.newCall(request).execute())
        {
            bucket.update(response);
            if (response.code() == Bucket.RATE_LIMIT_CODE)
                return queueRequest(body);
            if (!response.isSuccessful())
                throw failure(response);
            return CompletableFuture.completedFuture(null);
        }
        catch (IOException e)
        {
            LOG.log(e);
            throw new IllegalStateException(e);//FIXME: return new FailedFuture(e);
        }
    }

    protected static HttpException failure(Response response) throws IOException
    {
        final InputStream stream = Requester.getBody(response);
        final String responseBody = new String(IOUtil.readFully(stream));
        return new HttpException("Request returned failure " + response.code() + ": " + responseBody);
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
                .build();
    }


    protected void drainQueue()
    {
        while (!queue.isEmpty())
        {
            final Request request = newRequest(queue.peek().getLeft());
            try (Response response = client.newCall(request).execute())
            {
                bucket.update(response);
                if (response.code() == Bucket.RATE_LIMIT_CODE)
                {
                    pool.schedule(this::drainQueue, bucket.retryAfter(), TimeUnit.MILLISECONDS);
                    return;
                }
                else if (!response.isSuccessful())
                {
                    final HttpException exception = failure(response);
                    LOG.log(exception);
                    queue.poll().getRight().completeExceptionally(exception);
                    continue;
                }
                queue.poll().getRight().complete(null);
            }
            catch (IOException e)
            {
                LOG.log(e);
                queue.poll().getRight().completeExceptionally(e);
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
            }
            else if (!response.isSuccessful())
            {
                LOG.debug("Failed to update buckets due to unsuccessful response with code: " + response.code() + " and body: ");
                LOG.debug(new String(IOUtil.readFully(Requester.getBody(response))));
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
