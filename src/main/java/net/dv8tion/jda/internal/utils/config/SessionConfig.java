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
    private final SessionController sessionController;
    private final OkHttpClient httpClient;
    private final WebSocketFactory webSocketFactory;
    private final VoiceDispatchInterceptor interceptor;
    private boolean autoReconnect;
    private boolean retryOnTimeout;
    private boolean bulkDeleteSplittingEnabled;
    private boolean audioEnabled;
    private int maxReconnectDelay;

    public SessionConfig(
        @Nullable SessionController sessionController, @Nullable OkHttpClient httpClient,
        @Nullable WebSocketFactory webSocketFactory, @Nullable VoiceDispatchInterceptor interceptor,
        boolean audioEnabled, boolean retryOnTimeout, boolean autoReconnect,
        boolean bulkDeleteSplittingEnabled, int maxReconnectDelay)
    {
        this.sessionController = sessionController == null ? new SessionControllerAdapter() : sessionController;
        this.httpClient = httpClient == null ? new OkHttpClient() : httpClient;
        this.webSocketFactory = webSocketFactory == null ? new WebSocketFactory() : webSocketFactory;
        this.interceptor = interceptor;
        this.audioEnabled = audioEnabled;
        this.autoReconnect = autoReconnect;
        this.retryOnTimeout = retryOnTimeout;
        this.bulkDeleteSplittingEnabled = bulkDeleteSplittingEnabled;
        this.maxReconnectDelay = maxReconnectDelay;
    }

    public void setAutoReconnect(boolean autoReconnect)
    {
        this.autoReconnect = autoReconnect;
    }

    @Nonnull
    public SessionController getSessionController()
    {
        return sessionController;
    }

    @Nonnull
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
        return autoReconnect;
    }

    public boolean isRetryOnTimeout()
    {
        return retryOnTimeout;
    }

    public boolean isBulkDeleteSplittingEnabled()
    {
        return bulkDeleteSplittingEnabled;
    }

    public boolean isAudioEnabled()
    {
        return audioEnabled;
    }

    public int getMaxReconnectDelay()
    {
        return maxReconnectDelay;
    }

    @Nonnull
    public static SessionConfig getDefault()
    {
        return new SessionConfig(null, null, null, null, true, true, true, true, 900);
    }
}
