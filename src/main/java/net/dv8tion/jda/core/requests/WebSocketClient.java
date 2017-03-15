/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.handle.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.handle.*;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class WebSocketClient extends WebSocketAdapter implements WebSocketListener
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDASocket");
    public static final int DISCORD_GATEWAY_VERSION = 6;

    protected final JDAImpl api;
    protected final JDA.ShardInfo shardInfo;
    protected final HttpHost proxy;
    protected final HashMap<String, SocketHandler> handlers = new HashMap<>();

    protected WebSocket socket;
    protected String gatewayUrl = null;

    protected String sessionId = null;

    protected volatile Thread keepAliveThread;
    protected boolean connected;

    protected volatile boolean chunkingAndSyncing = false;
    protected boolean initiating;             //cache all events?
    protected final List<JSONObject> cachedEvents = new LinkedList<>();

    protected boolean shouldReconnect = true;
    protected int reconnectTimeoutS = 2;
    protected long heartbeatStartTime;

    //GuildId, <TimeOfNextAttempt, AudioConnection>
    protected final HashMap<String, MutablePair<Long, VoiceChannel>> queuedAudioConnections = new HashMap<>();

    protected final LinkedList<String> ratelimitQueue = new LinkedList<>();
    protected volatile Thread ratelimitThread = null;
    protected volatile long ratelimitResetTime;
    protected volatile int messagesSent;
    protected volatile boolean printedRateLimitMessage = false;

    protected boolean firstInit = true;

    public WebSocketClient(JDAImpl api)
    {
        this.api = api;
        this.shardInfo = api.getShardInfo();
        this.proxy = api.getGlobalProxy();
        this.shouldReconnect = api.isAutoReconnect();
        setupHandlers();
        setupSendingThread();
        connect();
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
            if (firstInit)
            {
                firstInit = false;
                JDAImpl.LOG.info("Finished Loading!");
                if (api.getGuilds().size() >= 2500) //Show large warning when connected to >2500 guilds
                {
                    JDAImpl.LOG.warn(" __      __ _    ___  _  _  ___  _  _   ___  _ ");
                    JDAImpl.LOG.warn(" \\ \\    / //_\\  | _ \\| \\| ||_ _|| \\| | / __|| |");
                    JDAImpl.LOG.warn("  \\ \\/\\/ // _ \\ |   /| .` | | | | .` || (_ ||_|");
                    JDAImpl.LOG.warn("   \\_/\\_//_/ \\_\\|_|_\\|_|\\_||___||_|\\_| \\___|(_)");
                    JDAImpl.LOG.warn("You're running a session with over 2500 connected");
                    JDAImpl.LOG.warn("guilds. You should shard the connection in order");
                    JDAImpl.LOG.warn("to split the load or things like resuming");
                    JDAImpl.LOG.warn("connection might not work as expected.");
                    JDAImpl.LOG.warn("For more info see https://git.io/vrFWP");
                }
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
        LOG.debug("Resending " + cachedEvents.size() + " cached events...");
        handle(cachedEvents);
        LOG.debug("Sending of cached events finished.");
        cachedEvents.clear();
    }

    public boolean isReady()
    {
        return !initiating;
    }

    public void handle(List<JSONObject> events)
    {
        events.forEach(this::handleEvent);
    }

    public void send(String message)
    {
        ratelimitQueue.addLast(message);
    }

    private boolean send(String message, boolean skipQueue)
    {
        if (!connected)
            return false;

        long now = System.currentTimeMillis();

        if (this.ratelimitResetTime <= now)
        {
            this.messagesSent = 0;
            this.ratelimitResetTime = now + 60000;//60 seconds
            this.printedRateLimitMessage = false;
        }

        //Allows 115 messages to be sent before limiting.
        if (this.messagesSent <= 115 || (skipQueue && this.messagesSent <= 119))   //technically we could go to 120, but we aren't going to chance it
        {
            LOG.trace("<- " + message);
            socket.sendText(message);
            this.messagesSent++;
            return true;
        }
        else
        {
            if (!printedRateLimitMessage)
            {
                LOG.warn("Hit the WebSocket RateLimit! If you see this message a lot then you might need to talk to DV8FromTheWorld.");
                printedRateLimitMessage = true;
            }
            return false;
        }
    }

    private void setupSendingThread()
    {
        ratelimitThread = new Thread(api.getIdentifierString() + " MainWS-Sending Thread")
        {

            @Override
            public void run()
            {
                boolean needRatelimit;
                boolean attemptedToSend;
                while (!this.isInterrupted())
                {
                    try
                    {
                        attemptedToSend = false;
                        needRatelimit = false;

                        MutablePair<Long, VoiceChannel> audioRequest = getNextAudioConnectRequest();

                        if (audioRequest != null)
                        {
                            VoiceChannel channel = audioRequest.getRight();
                            AudioManager audioManager = channel.getGuild().getAudioManager();
                            JSONObject audioConnectPacket = new JSONObject()
                                    .put("op", 4)
                                    .put("d", new JSONObject()
                                            .put("guild_id", channel.getGuild().getId())
                                            .put("channel_id", channel.getId())
                                            .put("self_mute", audioManager.isSelfMuted())
                                            .put("self_deaf", audioManager.isSelfDeafened())
                                    );
                            needRatelimit = !send(audioConnectPacket.toString(), false);
                            if (!needRatelimit)
                            {
                                //If we didn't get RateLimited, Next allowed connect request will be 2 seconds from now
                                audioRequest.setLeft(System.currentTimeMillis() + 2000);

                                //If the connection is already established, then the packet just sent
                                // was a move channel packet, thus, it won't trigger the removal from
                                // queuedAudioConnections in VoiceServerUpdateHandler because we won't receive
                                // that event just for a move, so we remove it here after successfully sending.
                                if (audioManager.isConnected())
                                {
                                    queuedAudioConnections.remove(channel.getGuild().getId());
                                }
                            }
                            attemptedToSend = true;
                        }
                        else
                        {
                            String message = ratelimitQueue.peekFirst();
                            if (message != null)
                            {
                                needRatelimit = !send(message, false);
                                if (!needRatelimit)
                                {
                                    ratelimitQueue.removeFirst();
                                }
                                attemptedToSend = true;
                            }
                        }

                        if (needRatelimit || !attemptedToSend)
                        {
                            Thread.sleep(1000);
                        }
                    }
                    catch (InterruptedException ignored)
                    {
                        LOG.debug("Main WS send thread interrupted. Most likely JDA is disconnecting the websocket.");
                        break;
                    }
                }
            }
        };
        ratelimitThread.start();
    }

    public void close()
    {
        socket.sendClose(1000);
    }

    /*
        ### Start Internal methods ###
     */

    protected void connect()
    {
        if (api.getStatus() != JDA.Status.ATTEMPTING_TO_RECONNECT)
            api.setStatus(JDA.Status.CONNECTING_TO_WEBSOCKET);
        initiating = true;

        try
        {
            if (gatewayUrl == null)
            {
                gatewayUrl = getGateway();
                if (gatewayUrl == null)
                {
                    throw new RuntimeException("Could not fetch WS-Gateway!");
                }
            }
            socket = api.getWebSocketFactory()
                    .createSocket(gatewayUrl)
                    .addHeader("Accept-Encoding", "gzip")
                    .addListener(this);
            socket.connect();
        }
        catch (IOException | WebSocketException e)
        {
            //Completely fail here. We couldn't make the connection.
            throw new RuntimeException(e);
        }
    }

    protected String getGateway()
    {
        try
        {
            RestAction<String> gateway = new RestAction<String>(api, Route.Misc.GATEWAY.compile(),null)
            {
                @Override
                protected void handleResponse(Response response, Request request)
                {
                    try
                    {
                        if (response.isOk())
                            request.onSuccess(response.getObject().getString("url"));
                        else
                            request.onFailure(new Exception("Failed to get gateway url"));
                    }
                    catch (Exception e)
                    {
                        request.onFailure(e);
                    }
                }
            };

            return gateway.complete(false) + "?encoding=json&v=" + DISCORD_GATEWAY_VERSION;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        api.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
        LOG.info("Connected to WebSocket");
        connected = true;
        reconnectTimeoutS = 2;
        messagesSent = 0;
        ratelimitResetTime = System.currentTimeMillis() + 60000;
        if (sessionId == null)
        {
            sendIdentify();
        }
        else
        {
            sendResume();
        }
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        connected = false;
        api.setStatus(JDA.Status.DISCONNECTED);

        CloseCode closeCode = null;
        int rawCloseCode = 1000;

        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        if (serverCloseFrame != null)
        {
            rawCloseCode = serverCloseFrame.getCloseCode();
            closeCode = CloseCode.from(rawCloseCode);
            if (closeCode == CloseCode.RATE_LIMITED)
                LOG.fatal("WebSocket connection closed due to ratelimit! Sent more than 120 websocket messages in under 60 seconds!");
            else if (closeCode != null)
                LOG.debug("WebSocket connection closed with code " + closeCode);
            else
                LOG.warn("WebSocket connection closed with unknown meaning for close-code " + rawCloseCode);
        }

        // null is considered -reconnectable- as we do not know the close-code meaning
        boolean closeCodeIsReconnect = closeCode == null || closeCode.isReconnect();
        if (!shouldReconnect || !closeCodeIsReconnect) //we should not reconnect
        {
            if (ratelimitThread != null)
                ratelimitThread.interrupt();

            if (!closeCodeIsReconnect)
            {
                //it is possible that a token can be invalidated due to too many reconnect attempts
                //or that a bot reached a new shard minimum and cannot connect with the current settings
                //if that is the case we have to drop our connection and inform the user with a fatal error message
                LOG.fatal("WebSocket connection was closed and cannot be recovered due to identification issues");
                LOG.fatal(closeCode);
            }

            api.setStatus(JDA.Status.SHUTDOWN);
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), rawCloseCode));
        }
        else
        {
            api.getEventManager().handle(new DisconnectEvent(api, serverCloseFrame, clientCloseFrame, closedByServer, OffsetDateTime.now()));
            reconnect();
        }
    }

    protected void reconnect()
    {
        LOG.warn("Got disconnected from WebSocket (Internet?!)... Attempting to reconnect in " + reconnectTimeoutS + "s");
        while(shouldReconnect)
        {
            try
            {
                api.setStatus(JDA.Status.WAITING_TO_RECONNECT);
                Thread.sleep(reconnectTimeoutS * 1000);
                api.setStatus(JDA.Status.ATTEMPTING_TO_RECONNECT);
            }
            catch(InterruptedException ignored) {}
            LOG.warn("Attempting to reconnect!");
            try
            {
                connect();
                break;
            }
            catch (RuntimeException ex)
            {
                reconnectTimeoutS = Math.min(reconnectTimeoutS << 1, 900);      //*2, cap at 15min max
                LOG.warn("Reconnect failed! Next attempt in " + reconnectTimeoutS + "s");
            }
        }
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        JSONObject content = new JSONObject(message);
        int opCode = content.getInt("op");

        if (content.has("s") && !content.isNull("s"))
        {
            api.setResponseTotal(content.getInt("s"));
        }

        switch (opCode)
        {
            case 0:
                handleEvent(content);
                break;
            case 1:
                LOG.debug("Got Keep-Alive request (OP 1). Sending response...");
                sendKeepAlive();
                break;
            case 7:
                LOG.debug("Got Reconnect request (OP 7). Closing connection now...");
                close();
                break;
            case 9:
                LOG.debug("Got Invalidate request (OP 9). Invalidating...");
                invalidate();
                sendIdentify();
                break;
            case 10:
                LOG.debug("Got HELLO packet (OP 10). Initializing keep-alive.");
                setupKeepAlive(content.getJSONObject("d").getLong("heartbeat_interval"));
                break;
            case 11:
                LOG.trace("Got Heartbeat Ack (OP 11).");
                api.setPing(System.currentTimeMillis() - heartbeatStartTime);
                break;
            default:
                LOG.debug("Got unknown op-code: " + opCode + " with content: " + message);
        }
    }

    protected void setupKeepAlive(long timeout)
    {
        keepAliveThread = new Thread(() ->
        {
            while (connected)
            {
                try
                {
                    sendKeepAlive();

                    //Sleep for heartbeat interval
                    Thread.sleep(timeout);
                }
                catch (InterruptedException ex)
                {
                    //connection got cut... terminating keepAliveThread
                    break;
                }
            }
        });
        keepAliveThread.setName(api.getIdentifierString() + " MainWS-KeepAlive Thread");
        keepAliveThread.setPriority(Thread.MAX_PRIORITY);
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    protected void sendKeepAlive()
    {
        String keepAlivePacket =
                new JSONObject()
                    .put("op", 1)
                    .put("d", api.getResponseTotal()
                ).toString();

        if (!send(keepAlivePacket, true))
            ratelimitQueue.addLast(keepAlivePacket);
        heartbeatStartTime = System.currentTimeMillis();
    }

    protected void sendIdentify()
    {
        LOG.debug("Sending Identify-packet...");
        PresenceImpl presenceObj = (PresenceImpl) api.getPresence();
        JSONObject identify = new JSONObject()
                .put("op", 2)
                .put("d", new JSONObject()
                        .put("presence", presenceObj.getFullPresence())
                        .put("token", api.getToken())
                        .put("properties", new JSONObject()
                                .put("$os", System.getProperty("os.name"))
                                .put("$browser", "JDA")
                                .put("$device", "JDA")
                                .put("$referring_domain", "")
                                .put("$referrer", "")
                        )
                        .put("v", DISCORD_GATEWAY_VERSION)
                        .put("large_threshold", 250)
                        .put("compress", true));    //Used to make the READY event be given as compressed binary data when over a certain size. TY @ShadowLordAlpha
        if (shardInfo != null)
        {
            identify.getJSONObject("d")
                    .put("shard", new JSONArray()
                        .put(shardInfo.getShardId())
                        .put(shardInfo.getShardTotal()));
        }
        send(identify.toString(), true);
    }

    protected void sendResume()
    {
        LOG.debug("Sending Resume-packet...");
        JSONObject resume = new JSONObject()
                .put("op", 6)
                .put("d", new JSONObject()
                        .put("session_id", sessionId)
                        .put("token", api.getToken())
                        .put("seq", api.getResponseTotal())
                );
        send(resume.toString(), true);
    }

    protected void invalidate()
    {
        sessionId = null;
        chunkingAndSyncing = false;

        api.getTextChannelMap().clear();
        api.getVoiceChannelMap().clear();
        api.getGuildMap().clear();
        api.getUserMap().clear();
        api.getPrivateChannelMap().clear();
        api.getFakeUserMap().clear();
        api.getFakePrivateChannelMap().clear();
        EntityBuilder.get(api).clearCache();
        EventCache.get(api).clear();
        GuildLock.get(api).clear();
        this.<ReadyHandler>getHandler("READY").clearCache();
        this.<GuildMembersChunkHandler>getHandler("GUILD_MEMBERS_CHUNK").clearCache();

        if (api.getAccountType() == AccountType.CLIENT)
        {
            JDAClientImpl client = (JDAClientImpl) api.asClient();

            client.getRelationshipMap().clear();
            client.getGroupMap().clear();
            client.getCallUserMap().clear();
        }
    }

    protected void updateAudioManagerReferences()
    {
        if (api.getAudioManagerMap().size() > 0)
            LOG.trace("Updating AudioManager references");

        api.getAudioManagerMap().entrySet().forEach(entry ->
        {
            String guildId = entry.getKey();
            AudioManager mng = entry.getValue();
            ConnectionListener listener = mng.getConnectionListener();

            Guild guild = api.getGuildById(guildId);
            if (guild == null)
            {
                //We no longer have access to the guild that this audio manager was for. Set the value to null.
                entry.setValue(null);
                queuedAudioConnections.remove(guildId);
                if (listener != null)
                    listener.onStatusChange(ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD);
            }
            else
            {
                AudioManagerImpl newMng = new AudioManagerImpl(guild);
                newMng.setSelfMuted(mng.isSelfMuted());
                newMng.setSelfDeafened(mng.isSelfDeafened());
                newMng.setQueueTimeout(mng.getConnectTimeout());
                newMng.setSendingHandler(mng.getSendingHandler());
                newMng.setReceivingHandler(mng.getReceiveHandler());
                newMng.setConnectionListener(mng.getConnectionListener());
                newMng.setAutoReconnect(mng.isAutoReconnect());

                if (mng.isConnected() || mng.isAttemptingToConnect())
                {
                    String channelId = mng.isConnected() ? mng.getConnectedChannel().getId() : mng.getQueuedAudioConnection().getId();
                    VoiceChannel channel = api.getVoiceChannelById(channelId);
                    if (channel != null)
                    {
                        if (mng.isConnected())
                            newMng.setConnectedChannel(channel);
                        else
                            newMng.setQueuedAudioConnection(channel);
                    }
                    else
                    {
                        //The voice channel is not cached. It was probably deleted.
                        queuedAudioConnections.remove(guildId);
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                    }
                }
            }
        });

        //Removes all null AudioManagers set null by the above guild-missing check.
        api.getAudioManagerMap().values().removeIf(Objects::isNull);
    }

    protected void handleEvent(JSONObject raw)
    {
        String type = raw.getString("t");
        long responseTotal = api.getResponseTotal();

        if (type.equals("GUILD_MEMBER_ADD"))
            ((GuildMembersChunkHandler) getHandler("GUILD_MEMBERS_CHUNK")).modifyExpectedGuildMember(raw.getJSONObject("d").getString("guild_id"), 1);
        if (type.equals("GUILD_MEMBER_REMOVE"))
            ((GuildMembersChunkHandler) getHandler("GUILD_MEMBERS_CHUNK")).modifyExpectedGuildMember(raw.getJSONObject("d").getString("guild_id"), -1);

        //If initiating, only allows READY, RESUMED, GUILD_MEMBERS_CHUNK, GUILD_SYNC, and GUILD_CREATE through.
        // If we are currently chunking, we don't allow GUILD_CREATE through anymore.
        if (initiating &&  !(type.equals("READY")
                || type.equals("GUILD_MEMBERS_CHUNK")
                || type.equals("RESUMED")
                || type.equals("GUILD_SYNC")
                || (!chunkingAndSyncing && type.equals("GUILD_CREATE"))))
        {
            //If we are currently GuildStreaming, and we get a GUILD_DELETE informing us that a Guild is unavailable
            // convert it to a GUILD_CREATE for handling.
            JSONObject content = raw.getJSONObject("d");
            if (!chunkingAndSyncing && type.equals("GUILD_DELETE") && content.has("unavailable") && content.getBoolean("unavailable"))
            {
                type = "GUILD_CREATE";
                raw.put("t", "GUILD_CREATE")
                        .put("jda-field","This event was originally a GUILD_DELETE but was converted to GUILD_CREATE for WS init Guild streaming");
            }
            else
            {
                LOG.debug("Caching " + type + " event during init!");
                cachedEvents.add(raw);
                return;
            }
        }
//
//        // Needs special handling due to content of "d" being an array
//        if(type.equals("PRESENCE_REPLACE"))
//        {
//            JSONArray presences = raw.getJSONArray("d");
//            LOG.trace(String.format("%s -> %s", type, presences.toString()));
//            PresenceUpdateHandler handler = new PresenceUpdateHandler(api, responseTotal);
//            for (int i = 0; i < presences.length(); i++)
//            {
//                JSONObject presence = presences.getJSONObject(i);
//                handler.handle(presence);
//            }
//            return;
//        }

        JSONObject content = raw.getJSONObject("d");
        LOG.trace(String.format("%s -> %s", type, content.toString()));

        try
        {
            switch (type)
            {
                //INIT types
                case "READY":
                    LOG.debug(String.format("%s -> %s", type, content.toString()));
                    sessionId = content.getString("session_id");
                    handlers.get("READY").handle(responseTotal, raw);
                    break;
                case "RESUMED":
                    initiating = false;
                    ready();
                    break;
                default:
                    SocketHandler handler = handlers.get(type);
                    if (handler != null)
                        handler.handle(responseTotal, raw);
                    else
                        LOG.debug("Unrecognized event:\n" + raw);
            }
        }
        catch (JSONException ex)
        {
            LOG.warn("Got an unexpected Json-parse error. Please redirect following message to the devs:\n\t"
                    + ex.getMessage() + "\n\t" + type + " -> " + content);
        }
        catch (Exception ex)
        {
            LOG.log(ex);
        }
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws UnsupportedEncodingException, DataFormatException
    {
        //Thanks to ShadowLordAlpha for code and debugging.
        //Get the compressed message and inflate it
        StringBuilder builder = new StringBuilder();
        Inflater decompresser = new Inflater();
        decompresser.setInput(binary, 0, binary.length);
        byte[] result = new byte[128];
        while(!decompresser.finished())
        {
            int resultLength = decompresser.inflate(result);
            builder.append(new String(result, 0, resultLength, "UTF-8"));
        }
        decompresser.end();

        // send the inflated message to the TextMessage method
        onTextMessage(websocket, builder.toString());
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception
    {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause)
    {
//        LOG.log(cause);
    }

    public void setChunkingAndSyncing(boolean active)
    {
        chunkingAndSyncing = active;
    }

    public void queueAudioConnect(VoiceChannel channel)
    {
        queuedAudioConnections.put(channel.getGuild().getId(), new MutablePair<>(System.currentTimeMillis(), channel));
    }

    public HashMap<String, MutablePair<Long, VoiceChannel>> getQueuedAudioConnectionMap()
    {
        return queuedAudioConnections;
    }

    protected MutablePair<Long, VoiceChannel> getNextAudioConnectRequest()
    {
        //Don't try to setup audio connections before JDA has finished loading.
        if (!isReady())
            return null;

        synchronized (queuedAudioConnections)
        {
            long now = System.currentTimeMillis();
            Iterator<MutablePair<Long, VoiceChannel>> it =  queuedAudioConnections.values().iterator();
            while (it.hasNext())
            {
                MutablePair<Long, VoiceChannel> audioRequest = it.next();
                if (audioRequest.getLeft() < now)
                {
                    VoiceChannel channel = audioRequest.getRight();
                    Guild guild = channel.getGuild();
                    ConnectionListener listener = guild.getAudioManager().getConnectionListener();

                    Guild connGuild = api.getGuildById(guild.getId());
                    if (connGuild == null)
                    {
                        it.remove();
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD);
                        continue;
                    }

                    VoiceChannel connChannel = connGuild.getVoiceChannelById(channel.getId());
                    if (connChannel == null)
                    {
                        it.remove();
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                        continue;
                    }

                    if (!connGuild.getSelfMember().hasPermission(connChannel, Permission.VOICE_CONNECT))
                    {
                        it.remove();
                        if (listener != null)
                            listener.onStatusChange(ConnectionStatus.DISCONNECTED_LOST_PERMISSION);
                        continue;
                    }

                    return audioRequest;
                }
            }
        }

        return null;
    }

    public HashMap<String, SocketHandler> getHandlers()
    {
        return handlers;
    }

    public <T> T getHandler(String type)
    {
        return (T) handlers.get(type);
    }

    private void setupHandlers()
    {
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

        if (api.getAccountType() == AccountType.CLIENT)
        {
            handlers.put("CALL_CREATE",              new CallCreateHandler(api));
            handlers.put("CALL_DELETE",              new CallDeleteHandler(api));
            handlers.put("CALL_UPDATE",              new CallUpdateHandler(api));
            handlers.put("CHANNEL_RECIPIENT_ADD",    new ChannelRecipientAddHandler(api));
            handlers.put("CHANNEL_RECIPIENT_REMOVE", new ChannelRecipientRemoveHandler(api));
            handlers.put("RELATIONSHIP_ADD",         new RelationshipAddHandler(api));
            handlers.put("RELATIONSHIP_REMOVE",      new RelationshipRemoveHandler(api));

            handlers.put("MESSAGE_ACK", new SocketHandler(api)
            {
                @Override
                protected String handleInternally(JSONObject content)
                {
                    return null;
                }
            });
        }
    }

}

