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

import java.nio.ByteBuffer
import javax.annotation.Nonnull

class ResizingByteBuffer(
    @Nonnull private var buffer: ByteBuffer,
) {
    @Nonnull
    fun buffer(): ByteBuffer = buffer

    @Nonnull
    fun prepareWrite(capacity: Int): ResizingByteBuffer {
        if (buffer.capacity() < capacity) {
            buffer = IOUtil.allocateLike(buffer, (GROWTH_FACTOR * capacity).toInt())
        } else {
            buffer.clear()
        }

        return this
    }

    @Nonnull
    fun ensureRemaining(capacity: Int): ResizingByteBuffer {
        if (buffer.remaining() < capacity) {
            val newBuffer = IOUtil.allocateLike(buffer, (GROWTH_FACTOR * (buffer.position() + capacity)).toInt())
            buffer.flip()
            newBuffer.put(buffer)
            buffer = newBuffer
        }

        return this
    }

    @Nonnull
    fun replace(
        @Nonnull data: ByteBuffer,
    ): ResizingByteBuffer {
        buffer = IOUtil.replace(buffer, data)
        return this
    }

    @Nonnull
    fun clear(): ResizingByteBuffer {
        buffer.clear()
        return this
    }

    private companion object {
        private const val GROWTH_FACTOR = 1.25
    }
}
