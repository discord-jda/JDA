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

package net.dv8tion.jda.core.audio.factory;

import javax.annotation.CheckForNull;
import java.util.concurrent.ConcurrentMap;

/**
 * Interface that acts as a UDP audio packet sending loop.
 * <br>This interface is provided so that developers can provide their own implementation for different management
 * of thread pools, process usage, and even for forwarding to native binaries implemented in other languages like C
 * to avoid problems with JVM GC StopTheWorld events.
 * <br>JDA, by default, uses {@link net.dv8tion.jda.core.audio.factory.DefaultSendSystem DefaultSendSystem} for its
 * UDP audio packet sending loop.
 */
public interface IAudioSendSystem
{
    /**
     * This represents the start of the loop, similar to {@link Thread#start()}, and after a call to this method JDA
     * assumes that the instance will be sending UDP audio packets in a loop.
     * <p>
     * <b>Note:</b> The packet sending loop should NOT be started on the current thread. I.E: This method should not
     * block forever, in the same way that {@link Thread#start()} does not. Just like in Thread, the running action of
     * this system should be implemented asynchronously.
     */
    void start();

    /**
     * This represents the destruction of this instance and should be used to perform all necessary cleanup and shutdown
     * operations needed to free resources.
     * <p>
     * <b>Note:</b> This method can be called at any time after instance creation ({@link #start()} may not yet have been called),
     * and it is possible that this method could be called more than once due to edge-case shutdown conditions.
     */
    void shutdown();

    /**
     * Called with the internal JDA {@link org.slf4j.MDC MDC} context map.
     * <br>This is guaranteed to be called before {@link #start()}.
     *
     * @param contextMap
     *        The JDA internal MDC context map, or {@code null} if disabled
     */
    default void setContextMap(@CheckForNull ConcurrentMap<String, String> contextMap) {}
}
