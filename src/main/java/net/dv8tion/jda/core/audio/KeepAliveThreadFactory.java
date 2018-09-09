/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.audio;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import org.slf4j.MDC;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class KeepAliveThreadFactory implements ThreadFactory
{
    final String identifier;
    final AtomicInteger threadCount = new AtomicInteger(1);
    final ConcurrentMap<String, String> contextMap;

    public KeepAliveThreadFactory(JDAImpl api)
    {
        contextMap = api.getContextMap();
        identifier = api.getIdentifierString() + " Audio-KeepAlive Pool";
    }

    @Override
    public Thread newThread(Runnable r)
    {
        Runnable r2 = () ->
        {
            if (contextMap != null)
                contextMap.forEach(MDC::put);
            r.run();
        };
        final Thread t = new Thread(AudioManagerImpl.AUDIO_THREADS, r2, identifier + " - Thread " + threadCount.getAndIncrement());
        t.setDaemon(true);
        return t;
    }
}
