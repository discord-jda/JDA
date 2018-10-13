/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import com.neovisionaries.ws.client.*;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.handle.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.audio.ConnectionRequest;
import net.dv8tion.jda.core.audio.ConnectionStage;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.handle.*;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.SessionController;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class WebSocketClient extends WebSocketAdapter implements WebSocketListener
{
    public static final Logger LOG = JDALogger.getLog(WebSocketClient.class);
    public static final int DISCORD_GATEWAY_VERSION = 6;
    public static final int IDENTIFY_DELAY = 5;
    public static final int ZLIB_SUFFIX = 0x0000FFFF;

    protected static final String INVALIDATE_REASON = "INVALIDATE_SESSION";
    protected static final long IDENTIFY_BACKOFF = TimeUnit.SECONDS.toMillis(SessionController.IDENTIFY_DELAY); // same as 1000 * IDENTIFY_DELAY

    protected final JDAImpl api;
    protected final JDA.ShardInfo shardInfo;
    protected final Map<String, SocketHandler> handlers = new HashMap<>();
    protected final Set<String> cfRays = ConcurrentHashMap.newKeySet();
    protected final Set<String> traces = ConcurrentHashMap.newKeySet();
    protected final boolean compression;

    public WebSocket socket;
    protected String sessionId = null;
    protected final Object readLock = new Object();
    protected Inflater zlibContext = new Inflater();
    protected ByteArrayOutputStream readBuffer;
    //this is a SoftReference in order to allow this resource to be freed to prevent resources starvation
    protected SoftReference<ByteArrayOutputStream> decompressBuffer;

    protected final ReentrantLock queueLock = new ReentrantLock();
    protected final ScheduledExecutorService executor;
    protected WebSocketSendingThread ratelimitThread;
    protected volatile Future<?> keepAliveThread;

    protected boolean initiating;

    protected int reconnectTimeoutS = 2;
    protected long heartbeatStartTime;
    protected long identifyTime = 0;

    protected final TLongObjectMap<ConnectionRequest> queuedAudioConnections = MiscUtil.newLongMap();
    protected final Queue<String> chunkSyncQueue = new ConcurrentLinkedQueue<>();
    protected final Queue<String> ratelimitQueue = new ConcurrentLinkedQueue<>();

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

    public WebSocketClient(JDAImpl api, boolean compression)
    {
        this.api = api;
        this.executor = api.getGatewayPool();
        this.shardInfo = api.getShardInfo();
        this.compression = compression;
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
            this.api.getEventManager().handle(
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

    public Set<String> getCfRays()
    {
        return cfRays;
    }

    public Set<String> getTraces()
    {
        return traces;
    }

    protected void updateTraces(JSONArray arr, String type, int opCode)
    {
        WebSocketClient.LOG.debug("Received a _trace for {} (OP: {}) with {}", type, opCode, arr);
        traces.clear();
        for (Object o : arr)
            traces.add(String.valueOf(o));
    }

    protected void allocateBuffer(byte[] binary) throws IOException
    {
        this.readBuffer = new ByteArrayOutputStream(binary.length * 2);
        this.readBuffer.write(binary);
    }

    protected void extendBuffer(byte[] binary) throws IOException
    {
        if (this.readBuffer != null)
            this.readBuffer.write(binary);
    }

    public void setAutoReconnect(boolean reconnect)
    {
        this.shouldReconnect = reconnect;
    }

    public boolean isConnected()
    {
        return connected;
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
                api.getEventManager().handle(new ReadyEvent(api, api.getResponseTotal()));
            }
            else
            {
                updateAudioManagerReferences();
                JDAImpl.LOG.info("Finished (Re)Loading!");
                api.getEventManager().handle(new ReconnectedEvent(api, api.getResponseTotal()));
            }
        }
        else
        {
            JDAImpl.LOG.info("Successfully resumed Session!");
            api.getEventManager().handle(new ResumedEvent(api, api.getResponseTotal()));
        }
        api.setStatus(JDA.Status.CONNECTED);
    }

    public boolean isReady()
    {
        return !initiating;
    }

    public void handle(List<JSONObject> events)
    {
        events.forEach(this::onDispatch);
    }

    public void send(String message)
    {
        locked("Interrupted while trying to add request to queue", () -> ratelimitQueue.add(message));
    }

    public void chunkOrSyncRequest(JSONObject request)
    {
        locked("Interrupted while trying to add chunk request", () -> chunkSyncQueue.add(request.toString()));
    }

    protected boolean send(String message, boolean skipQueue)
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
            socket.sendText(message);
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

    public void close()
    {
        if (socket != null)
            socket.sendClose(1000);
    }

    public void close(int code)
    {
        if (socket != null)
            socket.sendClose(code);
    }

    public void close(int code, String reason)
    {
        if (socket != null)
            socket.sendClose(code, reason);
    }

    public synchronized void shutdown()
    {
        shutdown = true;
        shouldReconnect = false;
        if (connectNode != null)
            api.getSessionController().removeSession(connectNode);
        close(1000, "Shutting down");
    }

    /*
        ### Start Internal methods ###
     */

    protected synchronized void connect()
    {
        if (api.getStatus() != JDA.Status.ATTEMPTING_TO_RECONNECT)
            api.setStatus(JDA.Status.CONNECTING_TO_WEBSOCKET);
        if (shutdown)
            throw new RejectedExecutionException("JDA is shutdown!");
        initiating = true;

        String url = api.getGatewayUrl() + "?encoding=json&v=" + DISCORD_GATEWAY_VERSION;
        if (compression)
        {
            url += "&compress=zlib-stream";
            decompressBuffer = newDecompressBuffer();
        }

        try
        {
            socket = api.getWebSocketFactory()
                    .createSocket(url)
                    .addHeader("Accept-Encoding", "gzip")
                    .addListener(this);
            socket.connect();
        }
        catch (IOException | WebSocketException e)
        {
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
        api.setStatus(JDA.Status.IDENTIFYING_SESSION);
        if (sessionId == null) //no need to log for resume here
            LOG.info("Connected to WebSocket");
        else
            LOG.debug("Connected to WebSocket");
        if (headers.containsKey("cf-ray"))
        {
            List<String> values = headers.get("cf-ray");
            if (!values.isEmpty())
            {
                String ray = values.get(0);
                cfRays.add(ray);
                LOG.trace("Received new CF-RAY: {}", ray);
            }
        }
        connected = true;
        reconnectTimeoutS = 2;
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
        api.setStatus(JDA.Status.DISCONNECTED);

        CloseCode closeCode = null;
        int rawCloseCode = 1000;
        //When we get 1000 from remote close we will try to resume
        // as apparently discord doesn't understand what "graceful disconnect" means
        boolean isInvalidate = false;

        if (keepAliveThread != null)
        {
            keepAliveThread.cancel(false);
            keepAliveThread = null;
        }
        if (serverCloseFrame != null)
        {
            rawCloseCode = serverCloseFrame.getCloseCode();
            closeCode = CloseCode.from(rawCloseCode);
            if (closeCode == CloseCode.RATE_LIMITED)
                LOG.error("WebSocket connection closed due to ratelimit! Sent more than 120 websocket messages in under 60 seconds!");
            else if (closeCode != null)
                LOG.debug("WebSocket connection closed with code {}", closeCode);
            else
                LOG.warn("WebSocket connection closed with unknown meaning for close-code {}", rawCloseCode);
        }
        if (clientCloseFrame != null
            && clientCloseFrame.getCloseCode() == 1000
            && Objects.equals(clientCloseFrame.getCloseReason(), INVALIDATE_REASON))
        {
            //When we close with 1000 we properly dropped our session due to invalidation
            // in that case we can be sure that resume will not work and instead we invalidate and reconnect here
            isInvalidate = true;
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
            }

            api.shutdownInternals();
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), rawCloseCode));
        }
        else
        {
            //reset our zlib decompression tools
            synchronized (readLock)
            {
                zlibContext = new Inflater();
                if (decompressBuffer != null)
                    decompressBuffer.clear();
                readBuffer = null;
            }
            if (isInvalidate)
                invalidate(); // 1000 means our session is dropped so we cannot resume
            api.getEventManager().handle(new DisconnectEvent(api, serverCloseFrame, clientCloseFrame, closedByServer, OffsetDateTime.now()));
            try
            {
                handleReconnect();
            }
            catch (InterruptedException e)
            {
                LOG.error("Failed to resume due to interrupted thread", e);
                invalidate();
                queueReconnect();
            }
        }
    }

    private void handleReconnect() throws InterruptedException
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
            LOG.warn("Got disconnected from WebSocket. Appending to reconnect queue");
            queueReconnect();
        }
        else // if resume is possible
        {
            LOG.warn("Got disconnected from WebSocket. Attempting to resume session");
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
            this.api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), 1006));
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
        if (shutdown)
        {
            api.setStatus(JDA.Status.SHUTDOWN);
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), 1000));
            return;
        }
        String message = "";
        if (callFromQueue)
            message = String.format("Queue is attempting to reconnect a shard...%s ", shardInfo != null ? " Shard: " + shardInfo.getShardString() : "");
        LOG.debug("{}Attempting to reconnect in {}s", message, reconnectTimeoutS);
        while (shouldReconnect)
        {
            api.setStatus(JDA.Status.WAITING_TO_RECONNECT);
            int delay = reconnectTimeoutS;
            Thread.sleep(delay * 1000);
            handleIdentifyRateLimit = false;
            api.setStatus(JDA.Status.ATTEMPTING_TO_RECONNECT);
            LOG.debug("Attempting to reconnect!");
            try
            {
                connect();
                break;
            }
            catch (RejectedExecutionException ex)
            {
                // JDA has already been shutdown so we can stop here
                api.setStatus(JDA.Status.SHUTDOWN);
                api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), 1000));
                return;
            }
            catch (RuntimeException ex)
            {
                reconnectTimeoutS = Math.min(reconnectTimeoutS << 1, api.getMaxReconnectDelay());
                LOG.warn("Reconnect failed! Next attempt in {}s", reconnectTimeoutS);
            }
        }
        if (contextEntries != null)
            contextEntries.forEach(MDC.MDCCloseable::close);
        if (previousContext != null)
            previousContext.forEach(MDC::put);
    }

    protected void setupKeepAlive(long timeout)
    {
        keepAliveThread = executor.scheduleAtFixedRate(() ->
        {
            api.setContext();
            if (connected)
                sendKeepAlive();
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    protected void sendKeepAlive()
    {
        String keepAlivePacket =
                new JSONObject()
                    .put("op", WebSocketCode.HEARTBEAT)
                    .put("d", api.getResponseTotal()
                ).toString();

        send(keepAlivePacket, true);
        heartbeatStartTime = System.currentTimeMillis();
    }

    protected void sendIdentify()
    {
        LOG.debug("Sending Identify-packet...");
        PresenceImpl presenceObj = (PresenceImpl) api.getPresence();
        JSONObject connectionProperties = new JSONObject()
            .put("$os", System.getProperty("os.name"))
            .put("$browser", "JDA")
            .put("$device", "JDA")
            .put("$referring_domain", "")
            .put("$referrer", "");
        JSONObject payload = new JSONObject()
            .put("presence", presenceObj.getFullPresence())
            .put("token", getToken())
            .put("properties", connectionProperties)
            .put("v", DISCORD_GATEWAY_VERSION)
            .put("large_threshold", 250);
            //Used to make the READY event be given
            // as compressed binary data when over a certain size. TY @ShadowLordAlpha
            //.put("compress", true);
        JSONObject identify = new JSONObject()
                .put("op", WebSocketCode.IDENTIFY)
                .put("d", payload);
        if (shardInfo != null)
        {
            payload
                .put("shard", new JSONArray()
                    .put(shardInfo.getShardId())
                    .put(shardInfo.getShardTotal()));
        }
        send(identify.toString(), true);
        handleIdentifyRateLimit = true;
        identifyTime = System.currentTimeMillis();
        sentAuthInfo = true;
        api.setStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
    }

    protected void sendResume()
    {
        LOG.debug("Sending Resume-packet...");
        JSONObject resume = new JSONObject()
            .put("op", WebSocketCode.RESUME)
            .put("d", new JSONObject()
                .put("session_id", sessionId)
                .put("token", getToken())
                .put("seq", api.getResponseTotal()));
        send(resume.toString(), true);
        //sentAuthInfo = true; set on RESUMED response as this could fail
        api.setStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
    }

    protected void invalidate()
    {
        sessionId = null;
        sentAuthInfo = false;

        locked("Interrupted while trying to invalidate chunk/sync queue", chunkSyncQueue::clear);

        api.getTextChannelMap().clear();
        api.getVoiceChannelMap().clear();
        api.getCategoryMap().clear();
        api.getGuildMap().clear();
        api.getUserMap().clear();
        api.getPrivateChannelMap().clear();
        api.getFakeUserMap().clear();
        api.getFakePrivateChannelMap().clear();
        api.getEventCache().clear();
        api.getGuildSetupController().clearCache();

        if (api.getAccountType() == AccountType.CLIENT)
        {
            JDAClientImpl client = api.asClient();

            client.getRelationshipMap().clear();
            client.getGroupMap().clear();
            client.getCallUserMap().clear();
        }
    }

    protected void updateAudioManagerReferences()
    {
        final TLongObjectMap<AudioManager> managerMap = api.getAudioManagerMap();
        if (managerMap.size() > 0)
            LOG.trace("Updating AudioManager references");

        synchronized (managerMap)
        {
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
        if (api.getAccountType() == AccountType.BOT)
            return api.getToken().substring("Bot ".length());
        return api.getToken();
    }

    protected List<JSONObject> convertPresencesReplace(long responseTotal, JSONArray array)
    {
        // Needs special handling due to content of "d" being an array
        List<JSONObject> output = new LinkedList<>();
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject presence = array.getJSONObject(i);
            final JSONObject obj = new JSONObject();
            obj.put("jda-field", "This was constructed from a PRESENCES_REPLACE payload")
               .put("op", WebSocketCode.DISPATCH)
               .put("s", responseTotal)
               .put("d", presence)
               .put("t", "PRESENCE_UPDATE");
            output.add(obj);
        }
        return output;
    }

    protected void handleEvent(JSONObject content)
    {
        try
        {
            onEvent(content);
        }
        catch (Exception ex)
        {
            LOG.error("Encountered exception on lifecycle level\nJSON: {}", content, ex);
            api.getEventManager().handle(new ExceptionEvent(api, ex, true));
        }
    }

    protected void onEvent(JSONObject content)
    {
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
                close(4000, "OP 7: RECONNECT");
                break;
            case WebSocketCode.INVALIDATE_SESSION:
                LOG.debug("Got Invalidate request (OP 9). Invalidating...");
                handleIdentifyRateLimit = handleIdentifyRateLimit && System.currentTimeMillis() - identifyTime < IDENTIFY_BACKOFF;

                sentAuthInfo = false;
                final boolean isResume = content.getBoolean("d");
                // When d: true we can wait a bit and then try to resume again
                //sending 4000 to not drop session
                int closeCode = isResume ? 4000 : 1000;
                if (isResume)
                    LOG.debug("Session can be recovered... Closing and sending new RESUME request");
                else
                    invalidate();

                close(closeCode, INVALIDATE_REASON);
                break;
            case WebSocketCode.HELLO:
                LOG.debug("Got HELLO packet (OP 10). Initializing keep-alive.");
                final JSONObject data = content.getJSONObject("d");
                setupKeepAlive(data.getLong("heartbeat_interval"));
                if (!data.isNull("_trace"))
                    updateTraces(data.getJSONArray("_trace"), "HELLO", WebSocketCode.HELLO);
                break;
            case WebSocketCode.HEARTBEAT_ACK:
                LOG.trace("Got Heartbeat Ack (OP 11).");
                api.setPing(System.currentTimeMillis() - heartbeatStartTime);
                break;
            default:
                LOG.debug("Got unknown op-code: {} with content: {}", opCode, content);
        }
    }

    protected void onDispatch(JSONObject raw)
    {
        String type = raw.getString("t");
        long responseTotal = api.getResponseTotal();

        if (!(raw.opt("d") instanceof JSONObject))
        {
            // Needs special handling due to content of "d" being an array
            if (type.equals("PRESENCES_REPLACE"))
            {
                final JSONArray payload = raw.getJSONArray("d");
                final List<JSONObject> converted = convertPresencesReplace(responseTotal, payload);
                final PresenceUpdateHandler handler = getHandler("PRESENCE_UPDATE");
                LOG.trace("{} -> {}", type, payload);
                for (JSONObject o : converted)
                    handler.handle(responseTotal, o);
            }
            else
            {
                LOG.debug("Received event with unhandled body type JSON: {}", raw);
            }
            return;
        }

        JSONObject content = raw.getJSONObject("d");
        LOG.trace("{} -> {}", type, content);

        JDAImpl jda = (JDAImpl) getJDA();
        try
        {
            switch (type)
            {
                //INIT types
                case "READY":
                    api.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
                    processingReady = true;
                    handleIdentifyRateLimit = false;
                    sessionId = content.getString("session_id");
                    if (!content.isNull("_trace"))
                        updateTraces(content.getJSONArray("_trace"), "READY", WebSocketCode.DISPATCH);
                    handlers.get("READY").handle(responseTotal, raw);
                    break;
                case "RESUMED":
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
                    if (!content.isNull("_trace"))
                        updateTraces(content.getJSONArray("_trace"), "RESUMED", WebSocketCode.DISPATCH);
                    break;
                default:
                    SocketHandler handler = handlers.get(type);
                    if (handler != null)
                        handler.handle(responseTotal, raw);
                    else
                        LOG.debug("Unrecognized event:\n{}", raw);
            }
        }
        catch (JSONException ex)
        {
            LOG.warn("Got an unexpected Json-parse error. Please redirect following message to the devs:\n\t{}\n\t{} -> {}",
                ex.getMessage(), type, content, ex);
        }
        catch (Exception ex)
        {
            LOG.error("Got an unexpected error. Please redirect following message to the devs:\n\t{} -> {}", type, content, ex);
        }

        if (responseTotal % EventCache.TIMEOUT_AMOUNT == 0)
            jda.getEventCache().timeout(responseTotal);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        handleEvent(new JSONObject(message));
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws IOException, DataFormatException
    {
        JSONObject json;
        synchronized (readLock)
        {
            if (!onBufferMessage(binary))
                return;
            json = handleBinary(binary);
        }
        handleEvent(json);
    }

    protected boolean onBufferMessage(byte[] binary) throws IOException
    {
        if (binary.length >= 4 && getInt(binary, binary.length - 4) == ZLIB_SUFFIX)
        {
            extendBuffer(binary);
            return true;
        }

        if (readBuffer != null)
            extendBuffer(binary);
        else
            allocateBuffer(binary);

        return false;
    }

    protected JSONObject handleBinary(byte[] binary) throws DataFormatException, UnsupportedEncodingException
    {
        //Thanks to ShadowLordAlpha and Shredder121 for code and debugging.
        //Get the compressed message and inflate it
        //We use the same buffer here to optimize gc use
        ByteArrayOutputStream decompressBuffer = getDecompressBuffer();
        try (InflaterOutputStream decompressor = new InflaterOutputStream(decompressBuffer, zlibContext))
        {
            if (readBuffer != null)
                readBuffer.writeTo(decompressor);
            else
                decompressor.write(binary);
        }
        catch (IOException e)
        {
            decompressBuffer.reset();
            throw (DataFormatException) new DataFormatException("Malformed").initCause(e);
        }
        finally { readBuffer = null; }

        String jsonString = decompressBuffer.toString("UTF-8");
        decompressBuffer.reset();
        return new JSONObject(jsonString);
    }

    protected ByteArrayOutputStream getDecompressBuffer()
    {
        if (decompressBuffer == null)
            decompressBuffer = newDecompressBuffer();
        ByteArrayOutputStream buffer = decompressBuffer.get();
        if (buffer == null)
            decompressBuffer = new SoftReference<>(buffer = new ByteArrayOutputStream(1024));
        return buffer;
    }

    protected static int getInt(byte[] sink, int offset)
    {
        return sink[offset + 3] & 0xFF
            | (sink[offset + 2] & 0xFF) << 8
            | (sink[offset + 1] & 0xFF) << 16
            | (sink[offset    ] & 0xFF) << 24;
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception
    {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause)
    {
        LOG.error("There was an error in the WebSocket connection", cause);
        api.getEventManager().handle(new ExceptionEvent(api, cause, true));
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

    protected void maybeUnlock()
    {
        if (queueLock.isHeldByCurrentThread())
            queueLock.unlock();
    }

    protected void locked(String comment, Runnable task)
    {
        try
        {
            queueLock.lockInterruptibly();
            task.run();
        }
        catch (InterruptedException e)
        {
            LOG.error(comment, e);
        }
        finally
        {
            maybeUnlock();
        }
    }

    protected <T> T locked(String comment, Supplier<T> task)
    {
        try
        {
            queueLock.lockInterruptibly();
            return task.get();
        }
        catch (InterruptedException e)
        {
            LOG.error(comment, e);
            return null;
        }
        finally
        {
            maybeUnlock();
        }
    }

    public void queueAudioReconnect(VoiceChannel channel)
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

    public void queueAudioConnect(VoiceChannel channel)
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

    public ConnectionRequest updateAudioConnection(long guildId, VoiceChannel connectedChannel)
    {
        return locked("There was an error updating the audio connection", () -> updateAudioConnection0(guildId, connectedChannel));
    }

    public ConnectionRequest updateAudioConnection0(long guildId, VoiceChannel connectedChannel)
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
        if (!isReady())
            return null;

        long now = System.currentTimeMillis();
        TLongObjectIterator<ConnectionRequest> it =  queuedAudioConnections.iterator();
        while (it.hasNext())
        {
            it.advance();
            ConnectionRequest audioRequest = it.value();
            if (audioRequest.getNextAttemptEpoch() < now)
            {
                Guild guild = api.getGuildById(audioRequest.getGuildIdLong());
                if (guild == null)
                {
                    it.remove();
                    //if (listener != null)
                    //    listener.onStatusChange(ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD);
                    //already handled by event handling
                    continue;
                }

                ConnectionListener listener = guild.getAudioManager().getConnectionListener();
                if (audioRequest.getStage() != ConnectionStage.DISCONNECT)
                {
                    VoiceChannel channel = guild.getVoiceChannelById(audioRequest.getChannelId());
                    if (channel == null)
                    {
                        it.remove();
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                        continue;
                    }

                    if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT))
                    {
                        it.remove();
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_LOST_PERMISSION);
                        continue;
                    }
                }

                return audioRequest;
            }
        }

        return null;
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
        final SocketHandler.NOPHandler nopHandler = new SocketHandler.NOPHandler(api);
        handlers.put("CHANNEL_CREATE",              new ChannelCreateHandler(api));
        handlers.put("CHANNEL_DELETE",              new ChannelDeleteHandler(api));
        handlers.put("CHANNEL_UPDATE",              new ChannelUpdateHandler(api));
        handlers.put("GUILD_BAN_ADD",               new GuildBanHandler(api, true));
        handlers.put("GUILD_BAN_REMOVE",            new GuildBanHandler(api, false));
        handlers.put("GUILD_CREATE",                new GuildCreateHandler(api));
        handlers.put("GUILD_DELETE",                new GuildDeleteHandler(api));
        handlers.put("GUILD_EMOJIS_UPDATE",         new GuildEmojisUpdateHandler(api));
        handlers.put("GUILD_MEMBER_ADD",            new GuildMemberAddHandler(api));
        handlers.put("GUILD_MEMBER_REMOVE",         new GuildMemberRemoveHandler(api));
        handlers.put("GUILD_MEMBER_UPDATE",         new GuildMemberUpdateHandler(api));
        handlers.put("GUILD_MEMBERS_CHUNK",         new GuildMembersChunkHandler(api));
        handlers.put("GUILD_ROLE_CREATE",           new GuildRoleCreateHandler(api));
        handlers.put("GUILD_ROLE_DELETE",           new GuildRoleDeleteHandler(api));
        handlers.put("GUILD_ROLE_UPDATE",           new GuildRoleUpdateHandler(api));
        handlers.put("GUILD_SYNC",                  new GuildSyncHandler(api));
        handlers.put("GUILD_UPDATE",                new GuildUpdateHandler(api));
        handlers.put("MESSAGE_CREATE",              new MessageCreateHandler(api));
        handlers.put("MESSAGE_DELETE",              new MessageDeleteHandler(api));
        handlers.put("MESSAGE_DELETE_BULK",         new MessageBulkDeleteHandler(api));
        handlers.put("MESSAGE_REACTION_ADD",        new MessageReactionHandler(api, true));
        handlers.put("MESSAGE_REACTION_REMOVE",     new MessageReactionHandler(api, false));
        handlers.put("MESSAGE_REACTION_REMOVE_ALL", new MessageReactionBulkRemoveHandler(api));
        handlers.put("MESSAGE_UPDATE",              new MessageUpdateHandler(api));
        handlers.put("PRESENCE_UPDATE",             new PresenceUpdateHandler(api));
        handlers.put("READY",                       new ReadyHandler(api));
        handlers.put("TYPING_START",                new TypingStartHandler(api));
        handlers.put("USER_UPDATE",                 new UserUpdateHandler(api));
        handlers.put("VOICE_SERVER_UPDATE",         new VoiceServerUpdateHandler(api));
        handlers.put("VOICE_STATE_UPDATE",          new VoiceStateUpdateHandler(api));

        // Unused events
        handlers.put("CHANNEL_PINS_ACK",          nopHandler);
        handlers.put("CHANNEL_PINS_UPDATE",       nopHandler);
        handlers.put("GUILD_INTEGRATIONS_UPDATE", nopHandler);
        handlers.put("PRESENCES_REPLACE",         nopHandler);
        handlers.put("WEBHOOKS_UPDATE",           nopHandler);

        if (api.getAccountType() == AccountType.CLIENT)
        {
            handlers.put("CALL_CREATE",              new CallCreateHandler(api));
            handlers.put("CALL_DELETE",              new CallDeleteHandler(api));
            handlers.put("CALL_UPDATE",              new CallUpdateHandler(api));
            handlers.put("CHANNEL_RECIPIENT_ADD",    new ChannelRecipientAddHandler(api));
            handlers.put("CHANNEL_RECIPIENT_REMOVE", new ChannelRecipientRemoveHandler(api));
            handlers.put("RELATIONSHIP_ADD",         new RelationshipAddHandler(api));
            handlers.put("RELATIONSHIP_REMOVE",      new RelationshipRemoveHandler(api));

            // Unused client events
            handlers.put("MESSAGE_ACK", nopHandler);
        }
    }

    protected abstract class ConnectNode implements SessionController.SessionConnectNode
    {
        @Override
        public JDA getJDA()
        {
            return api;
        }

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
                api.awaitStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
            }
            catch (IllegalStateException ex)
            {
                close();
                LOG.debug("Shutdown while trying to connect");
            }
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
                api.awaitStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
            }
            catch (IllegalStateException ex)
            {
                close();
                LOG.debug("Shutdown while trying to reconnect");
            }
        }
    }
}
