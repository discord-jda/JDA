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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.internal.utils.JDALogger
import java.util.concurrent.locks.Lock
import javax.annotation.Nonnull

/**
 * Simple implementation of a [ClosableIterator] that uses a lock.
 * <br></br>Close is an idempotent function and can be performed multiple times without effects beyond first invocation.
 * <br></br>This is deployed by [CacheView.lockedIterator] to allow read-only access
 * to the underlying structure without the need to clone it, unlike the normal iterator.
 *
 *
 * This closes automatically when [.hasNext] returns `false` but
 * its recommended to only be used within a `try-with-resources` block for safety.
 *
 *
 * **Example**<br></br>
 * This can handle any exceptions thrown while iterating and ensures the lock is released correctly.
 * <pre>`try (ClosableIterator<T> it = cacheView.lockedIterator()) {
 * while (it.hasNext()) {
 * consume(it.next());
 * }
 * }
`</pre> *
 *
 * @param <T>
 * The element type for this iterator
 *
 * @since  4.0.0
</T> */
class LockIterator<T>(@param:Nonnull private val it: Iterator<T>, private var lock: Lock?) : ClosableIterator<T> {
    override fun close() {
        if (lock != null) lock!!.unlock()
        lock = null
    }

    override fun hasNext(): Boolean {
        if (lock == null) return false
        val hasNext = it.hasNext()
        if (!hasNext) close()
        return hasNext
    }

    @Nonnull
    override fun next(): T {
        if (lock == null) throw NoSuchElementException()
        return it.next()
    }

    @Deprecated("") //Deprecated in Java 9 because the finalization system is being changed/removed
    protected fun finalize() {
        if (lock != null) {
            log.error("Finalizing without closing, performing force close on lock")
            close()
        }
    }

    companion object {
        private val log = JDALogger.getLog(ClosableIterator::class.java)
    }
}
