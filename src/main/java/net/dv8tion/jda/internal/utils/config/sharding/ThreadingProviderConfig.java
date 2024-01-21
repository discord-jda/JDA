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

package net.dv8tion.jda.internal.utils.config.sharding;

import net.dv8tion.jda.api.sharding.ThreadPoolProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ThreadingProviderConfig
{
    private final ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitSchedulerProvider;
    private final ThreadPoolProvider<? extends ExecutorService> rateLimitElasticProvider;
    private final ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider;
    private final ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider;
    private final ThreadPoolProvider<? extends ExecutorService> eventPoolProvider;
    private final ThreadPoolProvider<? extends ScheduledExecutorService> audioPoolProvider;
    private final ThreadFactory threadFactory;

    public ThreadingProviderConfig(
            @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitSchedulerProvider,
            @Nullable ThreadPoolProvider<? extends ExecutorService> rateLimitElasticProvider,
            @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
            @Nullable ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
            @Nullable ThreadPoolProvider<? extends ExecutorService> eventPoolProvider,
            @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> audioPoolProvider,
            @Nullable ThreadFactory threadFactory)
    {
        this.rateLimitSchedulerProvider = rateLimitSchedulerProvider;
        this.rateLimitElasticProvider = rateLimitElasticProvider;
        this.gatewayPoolProvider = gatewayPoolProvider;
        this.callbackPoolProvider = callbackPoolProvider;
        this.eventPoolProvider = eventPoolProvider;
        this.audioPoolProvider = audioPoolProvider;
        this.threadFactory = threadFactory;
    }

    @Nullable
    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    private void init(ThreadPoolProvider<?> provider, int shardTotal)
    {
        if (provider instanceof ThreadPoolProvider.LazySharedProvider)
            ((ThreadPoolProvider.LazySharedProvider<?>) provider).init(shardTotal);
    }

    private void shutdown(ThreadPoolProvider<?> provider)
    {
        if (provider instanceof ThreadPoolProvider.LazySharedProvider)
            ((ThreadPoolProvider.LazySharedProvider<?>) provider).shutdown();
    }

    public void init(int shardTotal)
    {
        init(rateLimitSchedulerProvider, shardTotal);
        init(rateLimitElasticProvider, shardTotal);
        init(gatewayPoolProvider, shardTotal);
        init(callbackPoolProvider, shardTotal);
        init(eventPoolProvider, shardTotal);
        init(audioPoolProvider, shardTotal);
    }

    public void shutdown()
    {
        shutdown(rateLimitSchedulerProvider);
        shutdown(rateLimitElasticProvider);
        shutdown(gatewayPoolProvider);
        shutdown(callbackPoolProvider);
        shutdown(eventPoolProvider);
        shutdown(audioPoolProvider);
    }

    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> getRateLimitSchedulerProvider()
    {
        return rateLimitSchedulerProvider;
    }

    @Nullable
    public ThreadPoolProvider<? extends ExecutorService> getRateLimitElasticProvider()
    {
        return rateLimitElasticProvider;
    }

    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> getGatewayPoolProvider()
    {
        return gatewayPoolProvider;
    }

    @Nullable
    public ThreadPoolProvider<? extends ExecutorService> getCallbackPoolProvider()
    {
        return callbackPoolProvider;
    }

    @Nullable
    public ThreadPoolProvider<? extends ExecutorService> getEventPoolProvider()
    {
        return eventPoolProvider;
    }

    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> getAudioPoolProvider()
    {
        return audioPoolProvider;
    }

    @Nonnull
    public static ThreadingProviderConfig getDefault()
    {
        return new ThreadingProviderConfig(null, null, null, null, null, null, null);
    }
}
