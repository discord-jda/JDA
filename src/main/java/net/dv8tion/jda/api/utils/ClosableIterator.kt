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

/**
 * Iterator holding a resource that must be free'd by the consumer.
 * <br></br>Close is an idempotent function and can be performed multiple times without effects beyond first invocation.
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
 * The element type
 *
 * @since 4.0.0
</T> */
interface ClosableIterator<T> : MutableIterator<T>, AutoCloseable {
    override fun close()
}
