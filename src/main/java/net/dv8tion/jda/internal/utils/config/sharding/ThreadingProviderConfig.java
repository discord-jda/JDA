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

package net.dv8tion.jda.internal.utils.config.sharding;

import net.dv8tion.jda.api.sharding.ThreadPoolProvider;

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
            ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider,
            ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
            ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
            ThreadFactory threadFactory)
    {
        this.rateLimitPoolProvider = rateLimitPoolProvider;
        this.gatewayPoolProvider = gatewayPoolProvider;
        this.callbackPoolProvider = callbackPoolProvider;
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    public ThreadPoolProvider<? extends ScheduledExecutorService> getRateLimitPoolProvider()
    {
        return rateLimitPoolProvider;
    }

    public ThreadPoolProvider<? extends ScheduledExecutorService> getGatewayPoolProvider()
    {
        return gatewayPoolProvider;
    }

    public ThreadPoolProvider<? extends ExecutorService> getCallbackPoolProvider()
    {
        return callbackPoolProvider;
    }

    public static ThreadingProviderConfig getDefault()
    {
        return new ThreadingProviderConfig(null, null, null, null);
    }
}
