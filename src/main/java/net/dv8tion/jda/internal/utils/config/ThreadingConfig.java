/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import java.util.concurrent.*;
import java.util.function.Supplier;

public class ThreadingConfig
{
    private ScheduledExecutorService rateLimitPool;
    private ScheduledExecutorService gatewayPool;
    private ExecutorService callbackPool = ForkJoinPool.commonPool();

    private boolean shutdownRateLimitPool = true;
    private boolean shutdownGatewayPool = true;
    private boolean shutdownCallbackPool = false;

    public void setRateLimitPool(ScheduledExecutorService executor, boolean shutdown)
    {
        this.rateLimitPool = executor;
        this.shutdownRateLimitPool = shutdown;
    }

    public void setGatewayPool(ScheduledExecutorService executor, boolean shutdown)
    {
        this.gatewayPool = executor;
        this.shutdownGatewayPool = shutdown;
    }

    public void setCallbackPool(ExecutorService executor, boolean shutdown)
    {
        this.callbackPool = executor == null ? ForkJoinPool.commonPool() : executor;
        this.shutdownCallbackPool = shutdown;
    }

    public void init(Supplier<String> identifier)
    {
        if (this.rateLimitPool == null)
            this.rateLimitPool = newScheduler(5, identifier, "RateLimit");
        if (this.gatewayPool == null)
            this.gatewayPool = newScheduler(1, identifier, "Gateway");
    }

    public void shutdown()
    {
        if (shutdownCallbackPool)
            callbackPool.shutdown();
        if (shutdownGatewayPool)
            gatewayPool.shutdown();
        if (shutdownRateLimitPool)
        {
            if (rateLimitPool instanceof ScheduledThreadPoolExecutor)
            {
                ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) rateLimitPool;
                executor.setKeepAliveTime(5L, TimeUnit.SECONDS);
                executor.allowCoreThreadTimeOut(true);
            }
            else
            {
                rateLimitPool.shutdown();
            }
        }
    }

    public void shutdownNow()
    {
        if (shutdownCallbackPool)
            callbackPool.shutdownNow();
        if (shutdownGatewayPool)
            gatewayPool.shutdownNow();
        if (shutdownRateLimitPool)
            rateLimitPool.shutdownNow();
    }

    public ScheduledExecutorService getRateLimitPool()
    {
        return rateLimitPool;
    }

    public ScheduledExecutorService getGatewayPool()
    {
        return gatewayPool;
    }

    public ExecutorService getCallbackPool()
    {
        return callbackPool;
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

    public static ScheduledThreadPoolExecutor newScheduler(int coreSize, Supplier<String> identifier, String baseName)
    {
        return new ScheduledThreadPoolExecutor(coreSize, new CountingThreadFactory(identifier, baseName));
    }
}
