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

import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Supplier;

public class ThreadingConfig
{
    private final Object audioLock = new Object();

    private ScheduledExecutorService rateLimitPool;
    private ScheduledExecutorService gatewayPool;
    private ExecutorService callbackPool;
    private ExecutorService eventPool;
    private ScheduledExecutorService audioPool;

    private boolean shutdownRateLimitPool;
    private boolean shutdownGatewayPool;
    private boolean shutdownCallbackPool;
    private boolean shutdownEventPool;
    private boolean shutdownAudioPool;

    public ThreadingConfig()
    {
        this.callbackPool = ForkJoinPool.commonPool();

        this.shutdownRateLimitPool = true;
        this.shutdownGatewayPool = true;
        this.shutdownCallbackPool = false;
        this.shutdownAudioPool = true;
    }

    public void setRateLimitPool(@Nullable ScheduledExecutorService executor, boolean shutdown)
    {
        this.rateLimitPool = executor;
        this.shutdownRateLimitPool = shutdown;
    }

    public void setGatewayPool(@Nullable ScheduledExecutorService executor, boolean shutdown)
    {
        this.gatewayPool = executor;
        this.shutdownGatewayPool = shutdown;
    }

    public void setCallbackPool(@Nullable ExecutorService executor, boolean shutdown)
    {
        this.callbackPool = executor == null ? ForkJoinPool.commonPool() : executor;
        this.shutdownCallbackPool = shutdown;
    }

    public void setEventPool(@Nullable ExecutorService executor, boolean shutdown)
    {
        this.eventPool = executor;
        this.shutdownEventPool = shutdown;
    }

    public void setAudioPool(@Nullable ScheduledExecutorService executor, boolean shutdown)
    {
        this.audioPool = executor;
        this.shutdownAudioPool = shutdown;
    }

    public void init(@Nonnull Supplier<String> identifier)
    {
        if (this.rateLimitPool == null)
            this.rateLimitPool = newScheduler(5, identifier, "RateLimit", false);
        if (this.gatewayPool == null)
            this.gatewayPool = newScheduler(1, identifier, "Gateway");
    }

    public void shutdown()
    {
        if (shutdownCallbackPool)
            callbackPool.shutdown();
        if (shutdownGatewayPool)
            gatewayPool.shutdown();
        if (shutdownEventPool && eventPool != null)
            eventPool.shutdown();
        if (shutdownAudioPool && audioPool != null)
            audioPool.shutdown();
    }

    public void shutdownRequester()
    {
        if (shutdownRateLimitPool)
            rateLimitPool.shutdown();
    }

    public void shutdownNow()
    {
        if (shutdownCallbackPool)
            callbackPool.shutdownNow();
        if (shutdownGatewayPool)
            gatewayPool.shutdownNow();
        if (shutdownRateLimitPool)
            rateLimitPool.shutdownNow();
        if (shutdownEventPool && eventPool != null)
            eventPool.shutdownNow();
        if (shutdownAudioPool && audioPool != null)
            audioPool.shutdownNow();
    }

    @Nonnull
    public ScheduledExecutorService getRateLimitPool()
    {
        return rateLimitPool;
    }

    @Nonnull
    public ScheduledExecutorService getGatewayPool()
    {
        return gatewayPool;
    }

    @Nonnull
    public ExecutorService getCallbackPool()
    {
        return callbackPool;
    }

    @Nullable
    public ExecutorService getEventPool()
    {
        return eventPool;
    }

    @Nullable
    public ScheduledExecutorService getAudioPool(@Nonnull Supplier<String> identifier)
    {
        ScheduledExecutorService pool = audioPool;
        if (pool == null)
        {
            synchronized (audioLock)
            {
                pool = audioPool;
                if (pool == null)
                    pool = audioPool = ThreadingConfig.newScheduler(1, identifier, "AudioLifeCycle");
            }
        }
        return pool;
    }

    public boolean isShutdownRateLimitPool()
    {
        return shutdownRateLimitPool;
    }

    public boolean isShutdownGatewayPool()
    {
        return shutdownGatewayPool;
    }

    public boolean isShutdownCallbackPool()
    {
        return shutdownCallbackPool;
    }

    public boolean isShutdownEventPool()
    {
        return shutdownEventPool;
    }

    public boolean isShutdownAudioPool()
    {
        return shutdownAudioPool;
    }

    @Nonnull
    public static ScheduledThreadPoolExecutor newScheduler(int coreSize, Supplier<String> identifier, String baseName)
    {
        return newScheduler(coreSize, identifier, baseName, true);
    }

    @Nonnull
    public static ScheduledThreadPoolExecutor newScheduler(int coreSize, Supplier<String> identifier, String baseName, boolean daemon)
    {
        return new ScheduledThreadPoolExecutor(coreSize, new CountingThreadFactory(identifier, baseName, daemon));
    }

    @Nonnull
    public static ThreadingConfig getDefault()
    {
        return new ThreadingConfig();
    }
}
