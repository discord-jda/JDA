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

package net.dv8tion.jda.api.utils;

import java.util.Iterator;

/**
 * Iterator holding a resource that must be free'd by the consumer.
 * <br>Close is an idempotent function and can be performed multiple times without effects beyond first invocation.
 *
 * <p>This closes automatically when {@link #hasNext()} returns {@code false} but
 * its recommended to only be used within a {@code try-with-resources} block for safety.
 *
 * <h2>Example</h2>
 * This can handle any exceptions thrown while iterating and ensures the lock is released correctly.
 * <pre>{@code
 * try (ClosableIterator<T> it = cacheView.lockedIterator()) {
 *     while (it.hasNext()) {
 *         consume(it.next());
 *     }
 * }
 * }</pre>
 *
 * @param <T>
 *        The element type
 *
 * @since 4.0.0
 */
public interface ClosableIterator<T> extends Iterator<T>, AutoCloseable
{
    @Override
    void close();
}
