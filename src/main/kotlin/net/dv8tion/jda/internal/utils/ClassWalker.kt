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

import javax.annotation.Nonnull

class ClassWalker
    private constructor(
        private val clazz: Class<*>,
        private val end: Class<*>,
    ) : Iterable<Class<*>> {
        private constructor(clazz: Class<*>) : this(clazz, Any::class.java)

        @Nonnull
        override fun iterator(): Iterator<Class<*>> =
            // removeFirst() throws NoSuchElementException once the work deque is drained.
            @Suppress("IteratorNotThrowingNoSuchElementException")
            object : Iterator<Class<*>> {
                private val done: MutableSet<Class<*>> = HashSet()
                private val work: ArrayDeque<Class<*>> = ArrayDeque()

                init {
                    work.addLast(clazz)
                    done.add(end)
                }

                override fun hasNext(): Boolean = !work.isEmpty()

                // removeFirst() throws NoSuchElementException once the work deque is drained.
                @Suppress("IteratorNotThrowingNoSuchElementException")
                override fun next(): Class<*> {
                    val current = work.removeFirst()
                    done.add(current)
                    for (parent in current.interfaces) {
                        if (!done.contains(parent)) {
                            work.addLast(parent)
                        }
                    }

                    val parent = current.superclass
                    if (parent != null && !done.contains(parent)) {
                        work.addLast(parent)
                    }
                    return current
                }
            }

        companion object {
            @JvmStatic
            fun range(
                start: Class<*>,
                end: Class<*>,
            ): ClassWalker = ClassWalker(start, end)

            @JvmStatic
            fun walk(start: Class<*>): ClassWalker = ClassWalker(start)
        }
    }
