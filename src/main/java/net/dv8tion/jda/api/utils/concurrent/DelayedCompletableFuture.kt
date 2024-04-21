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
package net.dv8tion.jda.api.utils.concurrent

import java.util.concurrent.*
import java.util.function.Function
import javax.annotation.Nonnull

/**
 * Specialized [CompletableFuture] used in combination with a scheduler.
 *
 * @param <T>
 * The result type
 *
 * @since  4.0.0
 *
 * @see CompletableFuture
 *
 * @see Delayed
</T> */
class DelayedCompletableFuture<T> private constructor() : CompletableFuture<T>(), ScheduledFuture<T> {
    private var future: ScheduledFuture<*>? = null

    /**
     * Initializes the backing scheduled task for this promise.
     *
     *
     * The provided future will be cancelled when [.cancel] is invoked
     * and is used as provider for [.getDelay].
     *
     * @param  future
     * The future that should be cancelled when this task is cancelled
     *
     * @throws IllegalStateException
     * If this was already initialized
     */
    private fun initProxy(future: ScheduledFuture<*>) {
        if (this.future == null) this.future = future else throw IllegalStateException("Cannot initialize twice")
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (future != null && !future!!.isDone) future!!.cancel(mayInterruptIfRunning)
        return super.cancel(mayInterruptIfRunning)
    }

    override fun getDelay(@Nonnull unit: TimeUnit): Long {
        return future!!.getDelay(unit)
    }

    override fun compareTo(@Nonnull o: Delayed): Int {
        return future!!.compareTo(o)
    }

    companion object {
        /**
         * Creates a new DelayedCompletableFuture scheduled on the supplied executor.
         *
         * @param  executor
         * The [ScheduledExecutorService] to use for scheduling
         * @param  delay
         * The delay of the task
         * @param  unit
         * Conversion [TimeUnit] for the delay
         * @param  mapping
         * Conversion function which calls [.complete] of the future it receives
         * @param  <E>
         * The result type of the scheduled task
         *
         * @return DelayedCompletableFuture for the specified runnable
        </E> */
        @Nonnull
        fun <E> make(
            @Nonnull executor: ScheduledExecutorService,
            delay: Long,
            @Nonnull unit: TimeUnit?,
            @Nonnull mapping: Function<in DelayedCompletableFuture<E>?, out Runnable?>
        ): DelayedCompletableFuture<E> {
            val handle = DelayedCompletableFuture<E>()
            val future = executor.schedule(mapping.apply(handle), delay, unit)
            handle.initProxy(future)
            return handle
        }
    }
}
