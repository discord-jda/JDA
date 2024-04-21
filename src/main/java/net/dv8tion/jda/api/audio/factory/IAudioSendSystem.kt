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
package net.dv8tion.jda.api.audio.factory

import java.util.concurrent.ConcurrentMap

/**
 * Interface that acts as a UDP audio packet sending loop.
 * <br></br>This interface is provided so that developers can provide their own implementation for different management
 * of thread pools, process usage, and even for forwarding to native binaries implemented in other languages like C
 * to avoid problems with JVM GC StopTheWorld events.
 * <br></br>JDA, by default, uses [DefaultSendSystem][net.dv8tion.jda.api.audio.factory.DefaultSendSystem] for its
 * UDP audio packet sending loop.
 */
interface IAudioSendSystem {
    /**
     * This represents the start of the loop, similar to [Thread.start], and after a call to this method JDA
     * assumes that the instance will be sending UDP audio packets in a loop.
     *
     *
     * **Note:** The packet sending loop should NOT be started on the current thread. I.E: This method should not
     * block forever, in the same way that [Thread.start] does not. Just like in Thread, the running action of
     * this system should be implemented asynchronously.
     */
    fun start()

    /**
     * This represents the destruction of this instance and should be used to perform all necessary cleanup and shutdown
     * operations needed to free resources.
     *
     *
     * **Note:** This method can be called at any time after instance creation ([.start] may not yet have been called),
     * and it is possible that this method could be called more than once due to edge-case shutdown conditions.
     */
    fun shutdown()

    /**
     * Called with the internal JDA [MDC][org.slf4j.MDC] context map.
     * <br></br>This is guaranteed to be called before [.start].
     *
     * @param contextMap
     * The JDA internal MDC context map, or `null` if disabled
     */
    fun setContextMap(contextMap: ConcurrentMap<String?, String?>?) {}
}
