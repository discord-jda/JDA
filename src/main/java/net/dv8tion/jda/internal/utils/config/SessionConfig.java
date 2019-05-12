/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.utils.config;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;
import okhttp3.OkHttpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SessionConfig
{
    public static final int FLAG_CONTEXT_ENABLED   = 1;
    public static final int FLAG_RETRY_TIMEOUT     = 1 << 1;
    public static final int FLAG_RAW_EVENTS        = 1 << 2;
    public static final int FLAG_BULK_DELETE_SPLIT = 1 << 3;
    public static final int FLAG_SHUTDOWN_HOOK     = 1 << 4;
    public static final int FLAG_MDC_CONTEXT       = 1 << 5;
    public static final int FLAG_AUTO_RECONNECT    = 1 << 6;

    public static final int FLAG_DEFAULTS = FLAG_CONTEXT_ENABLED
                                          | FLAG_AUTO_RECONNECT
                                          | FLAG_BULK_DELETE_SPLIT
                                          | FLAG_SHUTDOWN_HOOK
                                          | FLAG_RETRY_TIMEOUT;

    private final SessionController sessionController;
    private final OkHttpClient httpClient;
    private final WebSocketFactory webSocketFactory;
    private final VoiceDispatchInterceptor interceptor;
    private int flags;
    private int maxReconnectDelay;

    public SessionConfig(
        @Nullable SessionController sessionController, @Nullable OkHttpClient httpClient,
        @Nullable WebSocketFactory webSocketFactory, @Nullable VoiceDispatchInterceptor interceptor,
        int flags, int maxReconnectDelay)
    {
        this.sessionController = sessionController == null ? new SessionControllerAdapter() : sessionController;
        this.httpClient = httpClient;
        this.webSocketFactory = webSocketFactory == null ? new WebSocketFactory() : webSocketFactory;
        this.interceptor = interceptor;
        this.flags = flags;
        this.maxReconnectDelay = maxReconnectDelay;
    }

    public void setAutoReconnect(boolean autoReconnect)
    {
        this.flags |= autoReconnect ? FLAG_AUTO_RECONNECT : ~FLAG_AUTO_RECONNECT;
    }

    @Nonnull
    public SessionController getSessionController()
    {
        return sessionController;
    }

    @Nullable
    public OkHttpClient getHttpClient()
    {
        return httpClient;
    }

    @Nonnull
    public WebSocketFactory getWebSocketFactory()
    {
        return webSocketFactory;
    }

    @Nullable
    public VoiceDispatchInterceptor getVoiceDispatchInterceptor()
    {
        return interceptor;
    }

    public boolean isAutoReconnect()
    {
        return (flags & FLAG_AUTO_RECONNECT) == FLAG_AUTO_RECONNECT;
    }

    public boolean isRetryOnTimeout()
    {
        return (flags & FLAG_RETRY_TIMEOUT) == FLAG_RETRY_TIMEOUT;
    }

    public boolean isBulkDeleteSplittingEnabled()
    {
        return (flags & FLAG_BULK_DELETE_SPLIT) == FLAG_BULK_DELETE_SPLIT;
    }

    public boolean isRawEvents()
    {
        return (flags & FLAG_RAW_EVENTS) == FLAG_RAW_EVENTS;
    }

    public int getMaxReconnectDelay()
    {
        return maxReconnectDelay;
    }

    public int getFlags()
    {
        return flags;
    }

    @Nonnull
    public static SessionConfig getDefault()
    {
        return new SessionConfig(null, null, null, null, FLAG_DEFAULTS, 900);
    }
}
