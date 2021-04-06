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

package net.dv8tion.jda.internal.utils.config;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.utils.ConcurrentSessionController;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.internal.utils.config.flags.ConfigFlag;
import okhttp3.OkHttpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class SessionConfig
{
    private final SessionController sessionController;
    private final OkHttpClient httpClient;
    private final WebSocketFactory webSocketFactory;
    private final VoiceDispatchInterceptor interceptor;
    private final int largeThreshold;
    private EnumSet<ConfigFlag> flags;
    private int maxReconnectDelay;

    public SessionConfig(
        @Nullable SessionController sessionController, @Nullable OkHttpClient httpClient,
        @Nullable WebSocketFactory webSocketFactory, @Nullable VoiceDispatchInterceptor interceptor,
        EnumSet<ConfigFlag> flags, int maxReconnectDelay, int largeThreshold)
    {
        this.sessionController = sessionController == null ? new ConcurrentSessionController() : sessionController;
        this.httpClient = httpClient;
        this.webSocketFactory = webSocketFactory == null ? newWebSocketFactory() : webSocketFactory;
        this.interceptor = interceptor;
        this.flags = flags;
        this.maxReconnectDelay = maxReconnectDelay;
        this.largeThreshold = largeThreshold;
    }

    private static WebSocketFactory newWebSocketFactory()
    {
        return new WebSocketFactory().setConnectionTimeout(10000);
    }

    public void setAutoReconnect(boolean autoReconnect)
    {
        if (autoReconnect)
            flags.add(ConfigFlag.AUTO_RECONNECT);
        else
            flags.remove(ConfigFlag.AUTO_RECONNECT);
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
        return flags.contains(ConfigFlag.AUTO_RECONNECT);
    }

    public boolean isRetryOnTimeout()
    {
        return flags.contains(ConfigFlag.RETRY_TIMEOUT);
    }

    public boolean isBulkDeleteSplittingEnabled()
    {
        return flags.contains(ConfigFlag.BULK_DELETE_SPLIT);
    }

    public boolean isRawEvents()
    {
        return flags.contains(ConfigFlag.RAW_EVENTS);
    }

    public boolean isRelativeRateLimit()
    {
        return flags.contains(ConfigFlag.USE_RELATIVE_RATELIMIT);
    }

    public int getMaxReconnectDelay()
    {
        return maxReconnectDelay;
    }

    public int getLargeThreshold()
    {
        return largeThreshold;
    }

    public EnumSet<ConfigFlag> getFlags()
    {
        return flags;
    }

    @Nonnull
    public static SessionConfig getDefault()
    {
        return new SessionConfig(null, new OkHttpClient(), null, null, ConfigFlag.getDefault(), 900, 250);
    }
}
