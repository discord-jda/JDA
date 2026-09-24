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

package net.dv8tion.jda.internal.utils

import net.dv8tion.jda.api.utils.ClosableIterator
import net.dv8tion.jda.api.utils.cache.CacheView
import java.util.NoSuchElementException

class ChainedClosableIterator<T>(
    private val generator: Iterator<CacheView<T>>,
) : ClosableIterator<T> {
    private val items: MutableSet<T> = HashSet()
    private var currentIterator: ClosableIterator<T>? = null

    private var item: T? = null

    fun getItems(): Set<T> = items

    override fun close() {
        currentIterator?.close()
        currentIterator = null
    }

    // Mirrors the original Java control flow, where each branch returns directly.
    @Suppress("ReturnCount")
    override fun hasNext(): Boolean {
        if (item != null) {
            return true
        }
        // get next item from current iterator if exists
        if (currentIterator != null) {
            if (!currentIterator!!.hasNext()) {
                currentIterator!!.close()
                currentIterator = null
            } else {
                if (findNext()) {
                    return true
                }
                currentIterator!!.close()
                currentIterator = null
            }
        }
        // get next iterator in chain
        return processChain()
    }

    private fun processChain(): Boolean {
        while (item == null) {
            var view: CacheView<T>? = null
            while (generator.hasNext()) {
                view = generator.next()
                if (!view.isEmpty) {
                    break
                }
                view = null
            }
            if (view == null) {
                return false
            }

            // find next item in this iterator
            currentIterator = view.lockedIterator()
            if (findNext()) {
                break
            }
        }
        return true
    }

    private fun findNext(): Boolean {
        while (currentIterator!!.hasNext()) {
            val next = currentIterator!!.next()
            if (items.contains(next)) {
                continue
            }
            item = next
            items.add(item!!) // avoid duplicates
            return true
        }
        return false
    }

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        val tmp = item!!
        item = null
        return tmp
    }

    override fun remove(): Unit = throw UnsupportedOperationException("remove")

    @Deprecated("Deprecated in Java 9 because the finalization system is being changed/removed")
    protected fun finalize() {
        if (currentIterator != null) {
            log.error("Finalizing without closing, performing force close on lock")
            close()
        }
    }

    private companion object {
        private val log = JDALogger.getLog(ClosableIterator::class.java)
    }
}
