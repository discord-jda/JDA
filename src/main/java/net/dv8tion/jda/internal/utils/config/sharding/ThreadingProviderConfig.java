/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
    private final ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider;
    private final ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider;
    private final ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider;
    private final ThreadFactory threadFactory;

    public ThreadingProviderConfig(
            @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider,
            @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
            @Nullable ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
            @Nullable ThreadFactory threadFactory)
    {
        this.rateLimitPoolProvider = rateLimitPoolProvider;
        this.gatewayPoolProvider = gatewayPoolProvider;
        this.callbackPoolProvider = callbackPoolProvider;
        this.threadFactory = threadFactory;
    }

    @Nullable
    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> getRateLimitPoolProvider()
    {
        return rateLimitPoolProvider;
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

    @Nonnull
    public static ThreadingProviderConfig getDefault()
    {
        return new ThreadingProviderConfig(null, null, null, null);
    }
}
