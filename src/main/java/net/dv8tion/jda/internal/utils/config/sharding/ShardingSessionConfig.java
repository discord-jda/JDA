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

package net.dv8tion.jda.internal.utils.config.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.internal.utils.config.SessionConfig;
import okhttp3.OkHttpClient;

public class ShardingSessionConfig extends SessionConfig
{
    private final OkHttpClient.Builder builder;
    private final IAudioSendFactory audioSendFactory;

    public ShardingSessionConfig(
            SessionController sessionController,
            OkHttpClient httpClient, OkHttpClient.Builder httpClientBuilder,
            WebSocketFactory webSocketFactory, IAudioSendFactory audioSendFactory,
            boolean audioEnabled, boolean retryOnTimeout,  boolean autoReconnect,
            boolean bulkDeleteSplittingEnabled, int maxReconnectDelay)
    {
        super(sessionController, httpClient, webSocketFactory,
            audioEnabled, retryOnTimeout, autoReconnect,
            bulkDeleteSplittingEnabled, maxReconnectDelay);
        if (httpClient == null)
            this.builder = httpClientBuilder == null ? new OkHttpClient.Builder() : httpClientBuilder;
        else
            this.builder = null;
        this.audioSendFactory = audioSendFactory;
    }

    public OkHttpClient.Builder getHttpBuilder()
    {
        return builder;
    }

    public IAudioSendFactory getAudioSendFactory()
    {
        return audioSendFactory;
    }

    public static ShardingSessionConfig getDefault()
    {
        return new ShardingSessionConfig(null, null, null, null, null, true, true, true, true, 900);
    }
}
