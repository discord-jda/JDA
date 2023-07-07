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

import com.neovisionaries.ws.client.*;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.events.session.*;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.CloseCode;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.audio.ConnectionRequest;
import net.dv8tion.jda.internal.audio.ConnectionStage;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.handle.*;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.managers.PresenceImpl;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.ShutdownReason;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.compress.Decompressor;
import net.dv8tion.jda.internal.utils.compress.ZlibDecompressor;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

public class WebSocketClient extends WebSocketAdapter implements WebSocketListener
{
    public static final ThreadLocal<Boolean> WS_THREAD = ThreadLocal.withInitial(() -> false);
    public static final Logger LOG = JDALogger.getLog(WebSocketClient.class);
    public static final int IDENTIFY_DELAY = 5;
    public static final int ZLIB_SUFFIX = 0x0000FFFF;

    protected static final String INVALIDATE_REASON = "INVALIDATE_SESSION";
    protected static final long IDENTIFY_BACKOFF = TimeUnit.SECONDS.toMillis(SessionController.IDENTIFY_DELAY); // same as 1000 * IDENTIFY_DELAY

    protected final JDAImpl api;
    protected final JDA.ShardInfo shardInfo;
    protected final Map<String, SocketHandler> handlers = new HashMap<>();
    protected final Compression compression;
    protected final int gatewayIntents;
    protected final MemberChunkManager chunkManager;
    protected final GatewayEncoding encoding;

    public WebSocket socket;
    protected volatile String sessionId = null;
    protected final Object readLock = new Object();
    protected Decompressor decompressor;
    protected String resumeUrl = null;

    protected final ReentrantLock queueLock = new ReentrantLock();
    protected final ScheduledExecutorService executor;
    protected WebSocketSendingThread ratelimitThread;
    protected volatile Future<?> keepAliveThread;

    protected final ReentrantLock reconnectLock = new ReentrantLock();
    protected final Condition reconnectCondvar = reconnectLock.newCondition();

    protected boolean initiating;

    protected int missedHeartbeats = 0;
    protected int reconnectTimeoutS = 2;
    protected long heartbeatStartTime;
    protected long identifyTime = 0;

    protected final TLongObjectMap<ConnectionRequest> queuedAudioConnections = MiscUtil.newLongMap();
    protected final Queue<DataObject> chunkSyncQueue = new ConcurrentLinkedQueue<>();
    protected final Queue<DataObject> ratelimitQueue = new ConcurrentLinkedQueue<>();

    protected volatile long ratelimitResetTime;
    protected final AtomicInteger messagesSent = new AtomicInteger(0);

    protected volatile boolean shutdown = false;
    protected boolean shouldReconnect;
    protected boolean handleIdentifyRateLimit = false;
    protected boolean connected = false;

    protected volatile boolean printedRateLimitMessage = false;
    protected volatile boolean sentAuthInfo = false;
    protected boolean firstInit = true;
    protected boolean processingReady = true;

    protected volatile ConnectNode connectNode;

    public WebSocketClient(JDAImpl api, Compression compression, int gatewayIntents, GatewayEncoding encoding)
    {
        this.api = api;
        this.executor = api.getGatewayPool();
        this.shardInfo = api.getShardInfo();
        this.compression = compression;
        this.gatewayIntents = gatewayIntents;
        this.chunkManager = new MemberChunkManager(this);
        this.encoding = encoding;
        this.shouldReconnect = api.isAutoReconnect();
        this.connectNode = new StartingNode();
        setupHandlers();
        try
        {
            api.getSessionController().appendSession(connectNode);
        }
        catch (RuntimeException | Error e)
        {
            LOG.error("Failed to append new session to session controller queue. Shutting down!", e);
            this.api.setStatus(JDA.Status.SHUTDOWN);
            this.api.handleEvent(
                new ShutdownEvent(api, OffsetDateTime.now(), 1006));
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw (Error) e;
        }
    }

    public JDA getJDA()
    {
        return api;
    }

    public void setAutoReconnect(boolean reconnect)
    {
        this.shouldReconnect = reconnect;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public int getGatewayIntents()
    {
        return gatewayIntents;
    }

    public MemberChunkManager getChunkManager()
    {
        return chunkManager;
    }

    public void ready()
    {
        if (initiating)
        {
            initiating = false;
            processingReady = false;
            if (firstInit)
            {
                firstInit = false;
                if (api.getGuilds().size() >= 2000) //Show large warning when connected to >=2000 guilds
                {
                    JDAImpl.LOG.warn(" __      __ _    ___  _  _  ___  _  _   ___  _ ");
                    JDAImpl.LOG.warn(" \\ \\    / //_\\  | _ \\| \\| ||_ _|| \\| | / __|| |");
                    JDAImpl.LOG.warn("  \\ \\/\\/ // _ \\ |   /| .` | | | | .` || (_ ||_|");
                    JDAImpl.LOG.warn("   \\_/\\_//_/ \\_\\|_|_\\|_|\\_||___||_|\\_| \\___|(_)");
                    JDAImpl.LOG.warn("You're running a session with over 2000 connected");
                    JDAImpl.LOG.warn("guilds. You should shard the connection in order");
                    JDAImpl.LOG.warn("to split the load or things like resuming");
                    JDAImpl.LOG.warn("connection might not work as expected.");
                    JDAImpl.LOG.warn("For more info see https://git.io/vrFWP");
                }
                JDAImpl.LOG.info("Finished Loading!");
                api.handleEvent(new ReadyEvent(api));
            }
            else
            {
                updateAudioManagerReferences();
                JDAImpl.LOG.info("Finished (Re)Loading!");
                api.handleEvent(new SessionRecreateEvent(api));
            }
        }
        else
        {
            JDAImpl.LOG.debug("Successfully resumed Session!");
            api.handleEvent(new SessionResumeEvent(api));
        }
        api.setStatus(JDA.Status.CONNECTED);
    }

    public boolean isReady()
    {
        return !initiating;
    }

    public boolean isSession()
    {
        return sessionId != null;
    }

    public void handle(List<DataObject> events)
    {
        events.forEach(this::onDispatch);
    }

    public void send(DataObject message)
    {
        locked("Interrupted while trying to add request to queue", () -> ratelimitQueue.add(message));
    }

    public void cancelChunkRequest(String nonce)
    {
        locked("Interrupted while trying to cancel chunk request",
            () -> chunkSyncQueue.removeIf(it -> it.getString("nonce", "").equals(nonce)));
    }

    public void sendChunkRequest(DataObject request)
    {
        locked("Interrupted while trying to add chunk request", () -> chunkSyncQueue.add(request));
    }

    protected boolean send(DataObject message, boolean skipQueue)
    {
        if (!connected)
            return false;

        long now = System.currentTimeMillis();

        if (this.ratelimitResetTime <= now)
        {
            this.messagesSent.set(0);
            this.ratelimitResetTime = now + 60000;//60 seconds
            this.printedRateLimitMessage = false;
        }

        //Allows 115 messages to be sent before limiting.
        if (this.messagesSent.get() <= 115 || (skipQueue && this.messagesSent.get() <= 119))   //technically we could go to 120, but we aren't going to chance it
        {
            LOG.trace("<- {}", message);
            if (encoding == GatewayEncoding.ETF)
                socket.sendBinary(message.toETF());
            else
                socket.sendText(message.toString());
            this.messagesSent.getAndIncrement();
            return true;
        }
        else
        {
            if (!printedRateLimitMessage)
            {
                LOG.warn("Hit the WebSocket RateLimit! This can be caused by too many presence or voice status updates (connect/disconnect/mute/deaf). " +
                         "Regular: {} Voice: {} Chunking: {}", ratelimitQueue.size(), queuedAudioConnections.size(), chunkSyncQueue.size());
                printedRateLimitMessage = true;
            }
            return false;
        }
    }

    protected void setupSendingThread()
    {
        ratelimitThread = new WebSocketSendingThread(this);
        ratelimitThread.start();
    }

    private void prepareClose()
    {
        try
        {
            if (socket != null)
            {
                Socket rawSocket = this.socket.getSocket();
                if (rawSocket != null) // attempt to set a 10 second timeout for the close frame
                    rawSocket.setSoTimeout(10000); // this has no affect if the socket is already stuck in a read call
            }
        }
        catch (SocketException ignored) {}
    }

    public void close()
    {
        prepareClose();
        if (socket != null)
            socket.sendClose(1000);
    }

    public void close(int code)
    {
        prepareClose();
        if (socket != null)
            socket.sendClose(code);
    }

    public void close(int code, String reason)
    {
        prepareClose();
        if (socket != null)
            socket.sendClose(code, reason);
    }

    public void shutdown()
    {
        boolean callOnShutdown = MiscUtil.locked(reconnectLock, () -> {
            if (shutdown)
                return false;
            shutdown = true;
            shouldReconnect = false;
            if (connectNode != null)
                api.getSessionController().removeSession(connectNode);
            boolean wasConnected = connected;
            close(1000, "Shutting down");
            reconnectCondvar.signalAll(); // signal reconnect attempts to stop
            return !wasConnected;
        });

        if (callOnShutdown)
            onShutdown(1000);
    }


    /*
        ### Start Internal methods ###
     */

    protected void onShutdown(int rawCloseCode)
    {
        api.shutdownInternals(new ShutdownEvent(api, OffsetDateTime.now(), rawCloseCode));
    }

    protected synchronized void connect()
    {
        if (api.getStatus() != JDA.Status.ATTEMPTING_TO_RECONNECT)
            api.setStatus(JDA.Status.CONNECTING_TO_WEBSOCKET);
        if (shutdown)
            throw new RejectedExecutionException("JDA is shutdown!");
        initiating = true;

        try
        {
            String gatewayUrl = resumeUrl != null ? resumeUrl : api.getGatewayUrl();
            gatewayUrl = IOUtil.addQuery(gatewayUrl,
                "encoding", encoding.name().toLowerCase(),
                "v", JDAInfo.DISCORD_GATEWAY_VERSION
            );
            if (compression != Compression.NONE)
            {
                gatewayUrl = IOUtil.addQuery(gatewayUrl, "compress", compression.getKey());
                switch (compression)
                {
                    case ZLIB:
                        if (decompressor == null || decompressor.getType() != Compression.ZLIB)
                            decompressor = new ZlibDecompressor(api.getMaxBufferSize());
                        break;
                    default:
                        throw new IllegalStateException("Unknown compression");
                }
            }

            WebSocketFactory socketFactory = new WebSocketFactory(api.getWebSocketFactory());
            IOUtil.setServerName(socketFactory, gatewayUrl);
            if (socketFactory.getSocketTimeout() > 0)
                socketFactory.setSocketTimeout(Math.max(1000, socketFactory.getSocketTimeout()));
            else
                socketFactory.setSocketTimeout(10000);

            socket = socketFactory.createSocket(gatewayUrl);
            socket.setDirectTextMessage(true);
            socket.addHeader("Accept-Encoding", "gzip")
                  .addListener(this)
                  .connect();
        }
        catch (IOException | WebSocketException | IllegalArgumentException e)
        {
            resumeUrl = null;
            api.resetGatewayUrl();
            //Completely fail here. We couldn't make the connection.
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
        api.setContext();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        prepareClose(); // set 10s timeout in-case discord never sends us a HELLO payload
        api.setStatus(JDA.Status.IDENTIFYING_SESSION);
        if (sessionId == null)
        {
            LOG.info("Connected to WebSocket");
            // Log which intents are used on debug level since most people won't know how to use the binary output anyway
            LOG.debug("Connected with gateway intents: {}", Integer.toBinaryString(gatewayIntents));
        }
        else
        {
            // no need to log for resume here
            LOG.debug("Connected to WebSocket");
        }
        connected = true;
        //reconnectTimeoutS = 2; We will reset this when the session was started successfully (ready/resume)
        messagesSent.set(0);
        ratelimitResetTime = System.currentTimeMillis() + 60000;
        if (sessionId == null)
            sendIdentify();
        else
            sendResume();
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        sentAuthInfo = false;
        connected = false;
        // Use a new thread to avoid issues with sleep interruption
        if (Thread.currentThread().isInterrupted())
        {
            Thread thread = new Thread(() ->
                    handleDisconnect(websocket, serverCloseFrame, clientCloseFrame, closedByServer));
            thread.setName(api.getIdentifierString() + " MainWS-ReconnectThread");
            thread.start();
        }
        else
        {
            handleDisconnect(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        }
    }

    private void handleDisconnect(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        api.setStatus(JDA.Status.DISCONNECTED);
        CloseCode closeCode = null;
        int rawCloseCode = 1005;
        //When we get 1000 from remote close we will try to resume
        // as apparently discord doesn't understand what "graceful disconnect" means
        boolean isInvalidate = false;

        if (keepAliveThread != null)
        {
            keepAliveThread.cancel(false);
            keepAliveThread = null;
        }
        if (closedByServer && serverCloseFrame != null)
        {
            rawCloseCode = serverCloseFrame.getCloseCode();
            String rawCloseReason = serverCloseFrame.getCloseReason();
            closeCode = CloseCode.from(rawCloseCode);
            if (closeCode == CloseCode.RATE_LIMITED)
                LOG.error("WebSocket connection closed due to ratelimit! Sent more than 120 websocket messages in under 60 seconds!");
            else if (closeCode == CloseCode.UNKNOWN_ERROR)
                LOG.error("WebSocket connection closed due to server error! {}: {}", rawCloseCode, rawCloseReason);
            else if (closeCode != null)
                LOG.debug("WebSocket connection closed with code {}", closeCode);
            else if (rawCloseReason != null)
                LOG.warn("WebSocket connection closed with code {}: {}", rawCloseCode, rawCloseReason);
            else
                LOG.warn("WebSocket connection closed with unknown meaning for close-code {}", rawCloseCode);
        }
        else if (clientCloseFrame != null)
        {
            rawCloseCode = clientCloseFrame.getCloseCode();
            if (rawCloseCode == 1000 && INVALIDATE_REASON.equals(clientCloseFrame.getCloseReason()))
            {
                //When we close with 1000 we properly dropped our session due to invalidation
                // in that case we can be sure that resume will not work and instead we invalidate and reconnect here
                isInvalidate = true;
            }
        }

        // null is considered -reconnectable- as we do not know the close-code meaning
        boolean closeCodeIsReconnect = closeCode == null || closeCode.isReconnect();
        if (!shouldReconnect || !closeCodeIsReconnect || executor.isShutdown()) //we should not reconnect
        {
            if (ratelimitThread != null)
            {
                ratelimitThread.shutdown();
                ratelimitThread = null;
            }

            if (!closeCodeIsReconnect)
            {
                //it is possible that a token can be invalidated due to too many reconnect attempts
                //or that a bot reached a new shard minimum and cannot connect with the current settings
                //if that is the case we have to drop our connection and inform the user with a fatal error message
                LOG.error("WebSocket connection was closed and cannot be recovered due to identification issues\n{}", closeCode);

                // Forward the close reason to any hooks to awaitStatus / awaitReady
                // Since people cannot read logs, we have to explicitly forward this error.
                switch (closeCode)
                {
                case SHARDING_REQUIRED:
                case INVALID_SHARD:
                    api.shutdownReason = ShutdownReason.INVALID_SHARDS;
                    break;
                case DISALLOWED_INTENTS:
                    api.shutdownReason = ShutdownReason.DISALLOWED_INTENTS;
                    break;
                case GRACEFUL_CLOSE:
                    break;
                default:
                    api.shutdownReason = new ShutdownReason("Connection closed with code " + closeCode);
                }
            }

            if (decompressor != null)
                decompressor.shutdown();

            onShutdown(rawCloseCode);
        }
        else
        {
            //reset our decompression tools
            synchronized (readLock)
            {
                if (decompressor != null)
                    decompressor.reset();
            }
            if (isInvalidate)
                invalidate(); // 1000 means our session is dropped so we cannot resume
            api.handleEvent(new SessionDisconnectEvent(api, serverCloseFrame, clientCloseFrame, closedByServer, OffsetDateTime.now()));
            try
            {
                handleReconnect(rawCloseCode);
            }
            catch (InterruptedException e)
            {
                LOG.error("Failed to resume due to interrupted thread", e);
                invalidate();
                queueReconnect();
            }
        }
    }

    private void handleReconnect(int code) throws InterruptedException
    {
        if (sessionId == null)
        {
            if (handleIdentifyRateLimit)
            {
                long backoff = calculateIdentifyBackoff();
                if (backoff > 0)
                {
                    // it seems that most of the time this is already sub-0 when we reach this point
                    LOG.error("Encountered IDENTIFY Rate Limit! Waiting {} milliseconds before trying again!", backoff);
                    Thread.sleep(backoff);
                }
                else
                {
                    LOG.error("Encountered IDENTIFY Rate Limit!");
                }
            }
            LOG.warn("Got disconnected from WebSocket (Code {}). Appending to reconnect queue", code);
            queueReconnect();
        }
        else // if resume is possible
        {
            LOG.debug("Got disconnected from WebSocket (Code: {}). Attempting to resume session", code);
            reconnect();
        }
    }

    protected long calculateIdentifyBackoff()
    {
        long currentTime = System.currentTimeMillis();
        // calculate remaining backoff time since identify
        return currentTime - (identifyTime + IDENTIFY_BACKOFF);
    }

    protected void queueReconnect()
    {
        try
        {
            this.api.setStatus(JDA.Status.RECONNECT_QUEUED);
            this.connectNode = new ReconnectNode();
            this.api.getSessionController().appendSession(connectNode);
        }
        catch (IllegalStateException ex)
        {
            LOG.error("Reconnect queue rejected session. Shutting down...");
            this.api.setStatus(JDA.Status.SHUTDOWN);
            this.api.handleEvent(new ShutdownEvent(api, OffsetDateTime.now(), 1006));
        }
    }

    protected void reconnect() throws InterruptedException
    {
        reconnect(false);
    }

    /**
     * This method is used to start the reconnect of the JDA instance.
     * It is public for access from SessionReconnectQueue extensions.
     *
     * @param  callFromQueue
     *         whether this was in SessionReconnectQueue and got polled
     */
    public void reconnect(boolean callFromQueue) throws InterruptedException
    {
        Set<MDC.MDCCloseable> contextEntries = null;
        Map<String, String> previousContext = null;
        {
            ConcurrentMap<String, String> contextMap = api.getContextMap();
            if (callFromQueue && contextMap != null)
            {
                previousContext = MDC.getCopyOfContextMap();
                contextEntries = contextMap.entrySet().stream()
                          .map((entry) -> MDC.putCloseable(entry.getKey(), entry.getValue()))
                          .collect(Collectors.toSet());
            }
        }

        String message = "";
        if (callFromQueue)
            message = String.format("Queue is attempting to reconnect a shard...%s ", shardInfo != null ? " Shard: " + shardInfo.getShardString() : "");
        if (sessionId != null)
            reconnectTimeoutS = 0;
        LOG.debug("{}Attempting to reconnect in {}s", message, reconnectTimeoutS);
        boolean isShutdown = MiscUtil.locked(reconnectLock, () -> {
            while (shouldReconnect)
            {
                api.setStatus(JDA.Status.WAITING_TO_RECONNECT);

                int delay = reconnectTimeoutS;
                // Exponential backoff, reset on session creation (ready/resume)
                reconnectTimeoutS = reconnectTimeoutS == 0 ? 2 : Math.min(reconnectTimeoutS << 1, api.getMaxReconnectDelay());

                try
                {
                    // On shutdown, this condvar is notified and we stop reconnecting
                    reconnectCondvar.await(delay, TimeUnit.SECONDS);
                    if (!shouldReconnect)
                        break;

                    handleIdentifyRateLimit = false;
                    api.setStatus(JDA.Status.ATTEMPTING_TO_RECONNECT);
                    LOG.debug("Attempting to reconnect!");
                    connect();
                    break;
                }
                catch (RejectedExecutionException | InterruptedException ex)
                {
                    // JDA has already been shutdown so we can stop here
                    return true;
                }
                catch (RuntimeException ex)
                {
                    LOG.debug("Reconnect failed with exception", ex);
                    LOG.warn("Reconnect failed! Next attempt in {}s", reconnectTimeoutS);
                }
            }
            return !shouldReconnect;
        });

        if (isShutdown)
        {
            LOG.debug("Reconnect cancelled due to shutdown.");
            shutdown();
        }

        if (contextEntries != null)
            contextEntries.forEach(MDC.MDCCloseable::close);
        if (previousContext != null)
            previousContext.forEach(MDC::put);
    }

    protected void setupKeepAlive(int timeout)
    {
        try
        {
            Socket rawSocket = this.socket.getSocket();
            if (rawSocket != null)
                rawSocket.setSoTimeout(timeout + 10000); // setup a timeout when we miss heartbeats
        }
        catch (SocketException ex)
        {
            LOG.warn("Failed to setup timeout for socket", ex);
        }

        keepAliveThread = executor.scheduleAtFixedRate(() ->
        {
            api.setContext();
            if (connected)
                sendKeepAlive();
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    protected void sendKeepAlive()
    {
        DataObject keepAlivePacket =
                DataObject.empty()
                    .put("op", WebSocketCode.HEARTBEAT)
                    .put("d", api.getResponseTotal()
                );

        if (missedHeartbeats >= 2)
        {
            missedHeartbeats = 0;
            LOG.warn("Missed 2 heartbeats! Trying to reconnect...");
            prepareClose();
            socket.disconnect(4900, "ZOMBIE CONNECTION");
        }
        else
        {
            missedHeartbeats += 1;
            send(keepAlivePacket, true);
            heartbeatStartTime = System.currentTimeMillis();
        }
    }

    protected void sendIdentify()
    {
        LOG.debug("Sending Identify-packet...");
        PresenceImpl presenceObj = (PresenceImpl) api.getPresence();
        DataObject connectionProperties = DataObject.empty()
            .put("os", System.getProperty("os.name"))
            .put("browser", "JDA")
            .put("device", "JDA");
        DataObject payload = DataObject.empty()
            .put("presence", presenceObj.getFullPresence())
            .put("token", getToken())
            .put("properties", connectionProperties)
            .put("large_threshold", api.getLargeThreshold())
            .put("intents", gatewayIntents);

        DataObject identify = DataObject.empty()
                .put("op", WebSocketCode.IDENTIFY)
                .put("d", payload);
        if (shardInfo != null)
        {
            payload
                .put("shard", DataArray.empty()
                    .add(shardInfo.getShardId())
                    .add(shardInfo.getShardTotal()));
        }
        send(identify, true);
        handleIdentifyRateLimit = true;
        identifyTime = System.currentTimeMillis();
        sentAuthInfo = true;
        api.setStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
    }

    protected void sendResume()
    {
        LOG.debug("Sending Resume-packet...");
        DataObject resume = DataObject.empty()
            .put("op", WebSocketCode.RESUME)
            .put("d", DataObject.empty()
                .put("session_id", sessionId)
                .put("token", getToken())
                .put("seq", api.getResponseTotal()));
        send(resume, true);
        //sentAuthInfo = true; set on RESUMED response as this could fail
        api.setStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
    }

    protected void invalidate()
    {
        resumeUrl = null;
        sessionId = null;
        sentAuthInfo = false;

        locked("Interrupted while trying to invalidate chunk/sync queue", chunkSyncQueue::clear);

        api.getTextChannelsView().clear();
        api.getVoiceChannelsView().clear();
        api.getCategoriesView().clear();
        api.getNewsChannelView().clear();
        api.getPrivateChannelsView().clear();
        api.getStageChannelView().clear();
        api.getThreadChannelsView().clear();
        api.getForumChannelsView().clear();

        api.getGuildsView().clear();
        api.getUsersView().clear();

        api.getEventCache().clear();
        api.getGuildSetupController().clearCache();
        chunkManager.clear();

        api.handleEvent(new SessionInvalidateEvent(api));
    }

    protected void updateAudioManagerReferences()
    {
        AbstractCacheView<AudioManager> managerView = api.getAudioManagersView();
        try (UnlockHook hook = managerView.writeLock())
        {
            final TLongObjectMap<AudioManager> managerMap = managerView.getMap();
            if (managerMap.size() > 0)
                LOG.trace("Updating AudioManager references");

            for (TLongObjectIterator<AudioManager> it = managerMap.iterator(); it.hasNext(); )
            {
                it.advance();
                final long guildId = it.key();
                final AudioManagerImpl mng = (AudioManagerImpl) it.value();

                GuildImpl guild = (GuildImpl) api.getGuildById(guildId);
                if (guild == null)
                {
                    //We no longer have access to the guild that this audio manager was for. Set the value to null.
                    queuedAudioConnections.remove(guildId);
                    mng.closeAudioConnection(ConnectionStatus.DISCONNECTED_REMOVED_DURING_RECONNECT);
                    it.remove();
                }
            }
        }
    }

    protected String getToken()
    {
        // all bot tokens are prefixed with "Bot "
        return api.getToken().substring("Bot ".length());
    }

    protected List<DataObject> convertPresencesReplace(long responseTotal, DataArray array)
    {
        // Needs special handling due to content of "d" being an array
        List<DataObject> output = new LinkedList<>();
        for (int i = 0; i < array.length(); i++)
        {
            DataObject presence = array.getObject(i);
            final DataObject obj = DataObject.empty();
            obj.put("comment", "This was constructed from a PRESENCES_REPLACE payload")
               .put("op", WebSocketCode.DISPATCH)
               .put("s", responseTotal)
               .put("d", presence)
               .put("t", "PRESENCE_UPDATE");
            output.add(obj);
        }
        return output;
    }

    protected void handleEvent(DataObject content)
    {
        try
        {
            onEvent(content);
        }
        catch (Exception ex)
        {
            LOG.error("Encountered exception on lifecycle level\nJSON: {}", content, ex);
            api.handleEvent(new ExceptionEvent(api, ex, true));
        }
    }

    protected void onEvent(DataObject content)
    {
        WS_THREAD.set(true);
        int opCode = content.getInt("op");

        if (!content.isNull("s"))
        {
            api.setResponseTotal(content.getInt("s"));
        }

        switch (opCode)
        {
            case WebSocketCode.DISPATCH:
                onDispatch(content);
                break;
            case WebSocketCode.HEARTBEAT:
                LOG.debug("Got Keep-Alive request (OP 1). Sending response...");
                sendKeepAlive();
                break;
            case WebSocketCode.RECONNECT:
                LOG.debug("Got Reconnect request (OP 7). Closing connection now...");
                close(4900, "OP 7: RECONNECT");
                break;
            case WebSocketCode.INVALIDATE_SESSION:
                LOG.debug("Got Invalidate request (OP 9). Invalidating...");
                handleIdentifyRateLimit = handleIdentifyRateLimit && System.currentTimeMillis() - identifyTime < IDENTIFY_BACKOFF;

                sentAuthInfo = false;
                final boolean isResume = content.getBoolean("d");
                // When d: true we can wait a bit and then try to resume again
                //sending 4000 to not drop session
                int closeCode = isResume ? 4900 : 1000;
                if (isResume)
                    LOG.debug("Session can be recovered... Closing and sending new RESUME request");
                else
                    invalidate();

                close(closeCode, INVALIDATE_REASON);
                break;
            case WebSocketCode.HELLO:
                LOG.debug("Got HELLO packet (OP 10). Initializing keep-alive.");
                final DataObject data = content.getObject("d");
                setupKeepAlive(data.getInt("heartbeat_interval"));
                break;
            case WebSocketCode.HEARTBEAT_ACK:
                LOG.trace("Got Heartbeat Ack (OP 11).");
                missedHeartbeats = 0;
                api.setGatewayPing(System.currentTimeMillis() - heartbeatStartTime);
                break;
            default:
                LOG.debug("Got unknown op-code: {} with content: {}", opCode, content);
        }
    }

    protected void onDispatch(DataObject raw)
    {
        String type = raw.getString("t");
        long responseTotal = api.getResponseTotal();

        if (!raw.isType("d", DataType.OBJECT))
        {
            // Needs special handling due to content of "d" being an array
            if (type.equals("PRESENCES_REPLACE"))
            {
                final DataArray payload = raw.getArray("d");
                final List<DataObject> converted = convertPresencesReplace(responseTotal, payload);
                final SocketHandler handler = getHandler("PRESENCE_UPDATE");
                LOG.trace("{} -> {}", type, payload);
                for (DataObject o : converted)
                {
                    handler.handle(responseTotal, o);
                    // Send raw event after cache has been updated - including comment
                    if (api.isRawEvents())
                        api.handleEvent(new RawGatewayEvent(api, responseTotal, o));
                }
            }
            else
            {
                LOG.debug("Received event with unhandled body type JSON: {}", raw);
            }
            return;
        }

        DataObject content = raw.getObject("d");
        LOG.trace("{} -> {}", type, content);

        JDAImpl jda = (JDAImpl) getJDA();
        try
        {
            switch (type)
            {
                //INIT types
                case "READY":
                    reconnectTimeoutS = 2;
                    api.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
                    processingReady = true;
                    handleIdentifyRateLimit = false;
                    // first handle the ready payload before applying the session id
                    // this prevents a possible race condition with the cache of the guild setup controller
                    // otherwise the audio connection requests that are currently pending might be removed in the process
                    handlers.get("READY").handle(responseTotal, raw);
                    sessionId = content.getString("session_id");
                    resumeUrl = content.getString("resume_gateway_url", null);
                    break;
                case "RESUMED":
                    reconnectTimeoutS = 2;
                    sentAuthInfo = true;
                    if (!processingReady)
                    {
                        initiating = false;
                        ready();
                    }
                    else
                    {
                        LOG.debug("Resumed while still processing initial ready");
                        jda.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
                    }
                    break;
                default:
                    long guildId = content.getLong("guild_id", 0L);
                    if (api.isUnavailable(guildId) && !type.equals("GUILD_CREATE") && !type.equals("GUILD_DELETE"))
                    {
                        LOG.debug("Ignoring {} for unavailable guild with id {}. JSON: {}", type, guildId, content);
                        break;
                    }
                    SocketHandler handler = handlers.get(type);
                    if (handler != null)
                        handler.handle(responseTotal, raw);
                    else
                        LOG.debug("Unrecognized event:\n{}", raw);
            }
            // Send raw event after cache has been updated
            if (api.isRawEvents())
                api.handleEvent(new RawGatewayEvent(api, responseTotal, raw));
        }
        catch (ParsingException ex)
        {
            LOG.warn("Got an unexpected Json-parse error. Please redirect the following message to the devs:\n\tJDA {}\n\t{}\n\t{} -> {}",
                JDAInfo.VERSION, ex.getMessage(), type, content, ex);
        }
        catch (Exception ex)
        {
            LOG.error("Got an unexpected error. Please redirect the following message to the devs:\n\tJDA {}\n\t{} -> {}",
                JDAInfo.VERSION, type, content, ex);
        }

        if (responseTotal % EventCache.TIMEOUT_AMOUNT == 0)
            jda.getEventCache().timeout(responseTotal);
    }

    @Override
    public void onTextMessage(WebSocket websocket, byte[] data)
    {
        handleEvent(DataObject.fromJson(data));
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws DataFormatException
    {
        DataObject message;
        // Only acquire lock for decompression and unlock for event handling
        synchronized (readLock)
        {
            message = handleBinary(binary);
        }
        if (message != null)
            handleEvent(message);
    }

    protected DataObject handleBinary(byte[] binary) throws DataFormatException
    {
        if (decompressor == null)
        {
            if (encoding == GatewayEncoding.ETF)
                return DataObject.fromETF(binary);
            throw new IllegalStateException("Cannot decompress binary message due to unknown compression algorithm: " + compression);
        }
        // Scoping allows us to print the json that possibly failed parsing
        byte[] data;
        try
        {
            data = decompressor.decompress(binary);
            if (data == null)
                return null;
        }
        catch (DataFormatException e)
        {
            close(4900, "MALFORMED_PACKAGE");
            throw e;
        }

        try
        {
            if (encoding == GatewayEncoding.ETF)
                return DataObject.fromETF(data);
            else
                return DataObject.fromJson(data);
        }
        catch (ParsingException e)
        {
            String jsonString = "malformed";
            try
            {
                jsonString = new String(data, StandardCharsets.UTF_8);
            }
            catch (Exception ignored) {}
            // Print the string that could not be parsed and re-throw the exception
            LOG.error("Failed to parse json: {}", jsonString);
            throw e;
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception
    {
        if (cause.getCause() instanceof SocketTimeoutException)
        {
            LOG.debug("Socket timed out");
        }
        else if (cause.getCause() instanceof IOException)
        {
            LOG.debug("Encountered I/O error", cause);
        }
        else
        {
            LOG.error("There was an error in the WebSocket connection", cause);
            api.handleEvent(new ExceptionEvent(api, cause, true));
        }
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
        String identifier = api.getIdentifierString();
        switch (threadType)
        {
            case CONNECT_THREAD:
                thread.setName(identifier + " MainWS-ConnectThread");
                break;
            case FINISH_THREAD:
                thread.setName(identifier + " MainWS-FinishThread");
                break;
            case READING_THREAD:
                thread.setName(identifier + " MainWS-ReadThread");
                break;
            case WRITING_THREAD:
                thread.setName(identifier + " MainWS-WriteThread");
                break;
            default:
                thread.setName(identifier + " MainWS-" + threadType);
        }
    }

    protected void locked(String comment, Runnable task)
    {
        try
        {
            MiscUtil.locked(queueLock, task);
        }
        catch (Exception e)
        {
            LOG.error(comment, e);
        }
    }

    protected <T> T locked(String comment, Supplier<T> task)
    {
        try
        {
            return MiscUtil.locked(queueLock, task);
        }
        catch (Exception e)
        {
            LOG.error(comment, e);
            return null;
        }
    }

    public void queueAudioReconnect(AudioChannel channel)
    {
        locked("There was an error queueing the audio reconnect", () ->
        {
            final long guildId = channel.getGuild().getIdLong();
            ConnectionRequest request = queuedAudioConnections.get(guildId);

            if (request == null)
            {
                // If no request, then just reconnect
                request = new ConnectionRequest(channel, ConnectionStage.RECONNECT);
                queuedAudioConnections.put(guildId, request);
            }
            else
            {
                // If there is a request we change it to reconnect, no matter what it is
                request.setStage(ConnectionStage.RECONNECT);
            }
            // in all cases, update to this channel
            request.setChannel(channel);
        });
    }

    public void queueAudioConnect(AudioChannel channel)
    {
        locked("There was an error queueing the audio connect", () ->
        {
            final long guildId = channel.getGuild().getIdLong();
            ConnectionRequest request = queuedAudioConnections.get(guildId);

            if (request == null)
            {
                // starting a whole new connection
                request = new ConnectionRequest(channel, ConnectionStage.CONNECT);
                queuedAudioConnections.put(guildId, request);
            }
            else if (request.getStage() == ConnectionStage.DISCONNECT)
            {
                // if planned to disconnect, we want to reconnect
                request.setStage(ConnectionStage.RECONNECT);
            }

            // in all cases, update to this channel
            request.setChannel(channel);
        });
    }

    public void queueAudioDisconnect(Guild guild)
    {
        locked("There was an error queueing the audio disconnect", () ->
        {
            final long guildId = guild.getIdLong();
            ConnectionRequest request = queuedAudioConnections.get(guildId);

            if (request == null)
            {
                // If we do not have a request
                queuedAudioConnections.put(guildId, new ConnectionRequest(guild));
            }
            else
            {
                // If we have a request, change to DISCONNECT
                request.setStage(ConnectionStage.DISCONNECT);
            }
        });
    }

    public ConnectionRequest removeAudioConnection(long guildId)
    {
        //This will only be used by GuildDeleteHandler to ensure that
        // no further voice state updates are sent for this Guild
        //TODO: users may still queue new requests via the old AudioManager, how could we prevent this?
        return locked("There was an error cleaning up audio connections for deleted guild", () -> queuedAudioConnections.remove(guildId));
    }

    public ConnectionRequest updateAudioConnection(long guildId, AudioChannel connectedChannel)
    {
        return locked("There was an error updating the audio connection", () -> updateAudioConnection0(guildId, connectedChannel));
    }

    public ConnectionRequest updateAudioConnection0(long guildId, AudioChannel connectedChannel)
    {
        //Called by VoiceStateUpdateHandler when we receive a response from discord
        // about our request to CONNECT or DISCONNECT.
        // "stage" should never be RECONNECT here thus we don't check for that case
        ConnectionRequest request = queuedAudioConnections.get(guildId);

        if (request == null)
            return null;
        ConnectionStage requestStage = request.getStage();
        if (connectedChannel == null)
        {
            //If we got an update that DISCONNECT happened
            // -> If it was on RECONNECT we now switch to CONNECT
            // -> If it was on DISCONNECT we can now remove it
            // -> Otherwise we ignore it
            switch (requestStage)
            {
                case DISCONNECT:
                    return queuedAudioConnections.remove(guildId);
                case RECONNECT:
                    request.setStage(ConnectionStage.CONNECT);
                    request.setNextAttemptEpoch(System.currentTimeMillis());
                default:
                    return null;
            }
        }
        else if (requestStage == ConnectionStage.CONNECT)
        {
            //If the removeRequest was related to a channel that isn't the currently queued
            // request, then don't remove it.
            if (request.getChannelId() == connectedChannel.getIdLong())
                return queuedAudioConnections.remove(guildId);
        }
        //If the channel is not the one we are looking for!
        return null;
    }

    private SoftReference<ByteArrayOutputStream> newDecompressBuffer()
    {
        return new SoftReference<>(new ByteArrayOutputStream(1024));
    }

    protected ConnectionRequest getNextAudioConnectRequest()
    {
        //Don't try to setup audio connections before JDA has finished loading.
        if (sessionId == null)
            return null;

        long now = System.currentTimeMillis();
        AtomicReference<ConnectionRequest> request = new AtomicReference<>();
        queuedAudioConnections.retainEntries((guildId, audioRequest) -> // we use this because it locks the mutex
        {
            if (audioRequest.getNextAttemptEpoch() < now)
            {
                // Check if the guild is ready
                Guild guild = api.getGuildById(guildId);
                if (guild == null)
                {
                    // Not yet ready, check if the guild is known to this shard
                    GuildSetupController controller = api.getGuildSetupController();
                    if (!controller.isKnown(guildId))
                    {
                        // The guild is not tracked anymore -> we can't connect the audio channel
                        LOG.debug("Removing audio connection request because the guild has been removed. {}", audioRequest);
                        return false;
                    }
                    return true;
                }

                ConnectionListener listener = guild.getAudioManager().getConnectionListener();
                if (audioRequest.getStage() != ConnectionStage.DISCONNECT)
                {
                    // Check if we can connect to the target channel
                    AudioChannel channel = (AudioChannel) guild.getGuildChannelById(audioRequest.getChannelId());
                    if (channel == null)
                    {
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                        return false;
                    }

                    if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT))
                    {
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_LOST_PERMISSION);
                        return false;
                    }
                }
                // This will take the first result
                request.compareAndSet(null, audioRequest);
            }
            return true;
        });

        return request.get();
    }

    public Map<String, SocketHandler> getHandlers()
    {
        return handlers;
    }

    @SuppressWarnings("unchecked")
    public <T extends SocketHandler> T getHandler(String type)
    {
        try
        {
            return (T) handlers.get(type);
        }
        catch (ClassCastException e)
        {
            throw new IllegalStateException(e);
        }
    }

    protected void setupHandlers()
    {
        final SocketHandler.NOPHandler nopHandler =            new SocketHandler.NOPHandler(api);
        handlers.put("APPLICATION_COMMAND_PERMISSIONS_UPDATE", new ApplicationCommandPermissionsUpdateHandler(api));
        handlers.put("AUTO_MODERATION_RULE_CREATE",            new AutoModRuleHandler(api, "CREATE"));
        handlers.put("AUTO_MODERATION_RULE_UPDATE",            new AutoModRuleHandler(api, "UPDATE"));
        handlers.put("AUTO_MODERATION_RULE_DELETE",            new AutoModRuleHandler(api, "DELETE"));
        handlers.put("AUTO_MODERATION_ACTION_EXECUTION",       new AutoModExecutionHandler(api));
        handlers.put("CHANNEL_CREATE",                         new ChannelCreateHandler(api));
        handlers.put("CHANNEL_DELETE",                         new ChannelDeleteHandler(api));
        handlers.put("CHANNEL_UPDATE",                         new ChannelUpdateHandler(api));
        handlers.put("GUILD_AUDIT_LOG_ENTRY_CREATE",           new GuildAuditLogEntryCreateHandler(api));
        handlers.put("GUILD_BAN_ADD",                          new GuildBanHandler(api, true));
        handlers.put("GUILD_BAN_REMOVE",                       new GuildBanHandler(api, false));
        handlers.put("GUILD_CREATE",                           new GuildCreateHandler(api));
        handlers.put("GUILD_DELETE",                           new GuildDeleteHandler(api));
        handlers.put("GUILD_EMOJIS_UPDATE",                    new GuildEmojisUpdateHandler(api));
        handlers.put("GUILD_SCHEDULED_EVENT_CREATE",           new ScheduledEventCreateHandler(api));
        handlers.put("GUILD_SCHEDULED_EVENT_UPDATE",           new ScheduledEventUpdateHandler(api));
        handlers.put("GUILD_SCHEDULED_EVENT_DELETE",           new ScheduledEventDeleteHandler(api));
        handlers.put("GUILD_SCHEDULED_EVENT_USER_ADD",         new ScheduledEventUserHandler(api, true));
        handlers.put("GUILD_SCHEDULED_EVENT_USER_REMOVE",      new ScheduledEventUserHandler(api, false));
        handlers.put("GUILD_MEMBER_ADD",                       new GuildMemberAddHandler(api));
        handlers.put("GUILD_MEMBER_REMOVE",                    new GuildMemberRemoveHandler(api));
        handlers.put("GUILD_MEMBER_UPDATE",                    new GuildMemberUpdateHandler(api));
        handlers.put("GUILD_MEMBERS_CHUNK",                    new GuildMembersChunkHandler(api));
        handlers.put("GUILD_ROLE_CREATE",                      new GuildRoleCreateHandler(api));
        handlers.put("GUILD_ROLE_DELETE",                      new GuildRoleDeleteHandler(api));
        handlers.put("GUILD_ROLE_UPDATE",                      new GuildRoleUpdateHandler(api));
        handlers.put("GUILD_SYNC",                             new GuildSyncHandler(api));
        handlers.put("GUILD_STICKERS_UPDATE",                  new GuildStickersUpdateHandler(api));
        handlers.put("GUILD_UPDATE",                           new GuildUpdateHandler(api));
        handlers.put("INTERACTION_CREATE",                     new InteractionCreateHandler(api));
        handlers.put("INVITE_CREATE",                          new InviteCreateHandler(api));
        handlers.put("INVITE_DELETE",                          new InviteDeleteHandler(api));
        handlers.put("MESSAGE_CREATE",                         new MessageCreateHandler(api));
        handlers.put("MESSAGE_DELETE",                         new MessageDeleteHandler(api));
        handlers.put("MESSAGE_DELETE_BULK",                    new MessageBulkDeleteHandler(api));
        handlers.put("MESSAGE_REACTION_ADD",                   new MessageReactionHandler(api, true));
        handlers.put("MESSAGE_REACTION_REMOVE",                new MessageReactionHandler(api, false));
        handlers.put("MESSAGE_REACTION_REMOVE_ALL",            new MessageReactionBulkRemoveHandler(api));
        handlers.put("MESSAGE_REACTION_REMOVE_EMOJI",          new MessageReactionClearEmojiHandler(api));
        handlers.put("MESSAGE_UPDATE",                         new MessageUpdateHandler(api));
        handlers.put("PRESENCE_UPDATE",                        new PresenceUpdateHandler(api));
        handlers.put("READY",                                  new ReadyHandler(api));
        handlers.put("STAGE_INSTANCE_CREATE",                  new StageInstanceCreateHandler(api));
        handlers.put("STAGE_INSTANCE_DELETE",                  new StageInstanceDeleteHandler(api));
        handlers.put("STAGE_INSTANCE_UPDATE",                  new StageInstanceUpdateHandler(api));
        handlers.put("THREAD_CREATE",                          new ThreadCreateHandler(api));
        handlers.put("THREAD_DELETE",                          new ThreadDeleteHandler(api));
        handlers.put("THREAD_LIST_SYNC",                       new ThreadListSyncHandler(api));
        handlers.put("THREAD_MEMBERS_UPDATE",                  new ThreadMembersUpdateHandler(api));
        handlers.put("THREAD_MEMBER_UPDATE",                   new ThreadMemberUpdateHandler(api));
        handlers.put("THREAD_UPDATE",                          new ThreadUpdateHandler(api));
        handlers.put("TYPING_START",                           new TypingStartHandler(api));
        handlers.put("USER_UPDATE",                            new UserUpdateHandler(api));
        handlers.put("VOICE_SERVER_UPDATE",                    new VoiceServerUpdateHandler(api));
        handlers.put("VOICE_STATE_UPDATE",                     new VoiceStateUpdateHandler(api));

        // Unused events
        handlers.put("CHANNEL_PINS_ACK",          nopHandler);
        handlers.put("CHANNEL_PINS_UPDATE",       nopHandler);
        handlers.put("GUILD_INTEGRATIONS_UPDATE", nopHandler);
        handlers.put("PRESENCES_REPLACE",         nopHandler);
        handlers.put("WEBHOOKS_UPDATE",           nopHandler);
    }

    protected abstract class ConnectNode implements SessionController.SessionConnectNode
    {
        @Nonnull
        @Override
        public JDA getJDA()
        {
            return api;
        }

        @Nonnull
        @Override
        public JDA.ShardInfo getShardInfo()
        {
            return api.getShardInfo();
        }
    }

    protected class StartingNode extends ConnectNode
    {
        @Override
        public boolean isReconnect()
        {
            return false;
        }

        @Override
        public void run(boolean isLast) throws InterruptedException
        {
            if (shutdown)
                return;
            setupSendingThread();
            connect();
            if (isLast)
                return;
            try
            {
                api.awaitStatus(JDA.Status.LOADING_SUBSYSTEMS, JDA.Status.RECONNECT_QUEUED);
            }
            catch (IllegalStateException ex)
            {
                close();
                LOG.debug("Shutdown while trying to connect");
            }
        }

        @Override
        public int hashCode()
        {
            return Objects.hash("C", getJDA());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof StartingNode))
                return false;
            StartingNode node = (StartingNode) obj;
            return node.getJDA().equals(getJDA());
        }
    }

    protected class ReconnectNode extends ConnectNode
    {
        @Override
        public boolean isReconnect()
        {
            return true;
        }

        @Override
        public void run(boolean isLast) throws InterruptedException
        {
            if (shutdown)
                return;
            reconnect(true);
            if (isLast)
                return;
            try
            {
                api.awaitStatus(JDA.Status.LOADING_SUBSYSTEMS, JDA.Status.RECONNECT_QUEUED);
            }
            catch (IllegalStateException ex)
            {
                close();
                LOG.debug("Shutdown while trying to reconnect");
            }
        }

        @Override
        public int hashCode()
        {
            return Objects.hash("R", getJDA());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof ReconnectNode))
                return false;
            ReconnectNode node = (ReconnectNode) obj;
            return node.getJDA().equals(getJDA());
        }
    }
}
