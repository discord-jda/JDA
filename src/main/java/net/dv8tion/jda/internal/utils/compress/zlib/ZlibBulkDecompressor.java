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

package net.dv8tion.jda.internal.utils.compress.zlib;

import net.dv8tion.jda.internal.utils.compress.BulkDecompressor;
import net.dv8tion.jda.internal.utils.compress.DecompressionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.zip.InflaterOutputStream;

public class ZlibBulkDecompressor extends ZlibAbstractDecompressor implements BulkDecompressor {
    protected final int maxBufferSize;
    protected SoftReference<ByteArrayOutputStream> decompressBuffer = null;

    public ZlibBulkDecompressor(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    protected SoftReference<ByteArrayOutputStream> newDecompressBuffer() {
        return new SoftReference<>(new ByteArrayOutputStream(Math.min(1024, maxBufferSize)));
    }

    protected ByteArrayOutputStream getDecompressBuffer() {
        // If no buffer has been allocated yet we do that here (lazy init)
        if (decompressBuffer == null) {
            decompressBuffer = newDecompressBuffer();
        }
        // Check if the buffer has been collected by the GC or not
        ByteArrayOutputStream buffer = decompressBuffer.get();
        if (buffer == null) { // create a ne buffer because the GC got it
            decompressBuffer = new SoftReference<>(buffer = new ByteArrayOutputStream(Math.min(1024, maxBufferSize)));
        }
        return buffer;
    }

    @Override
    public byte[] decompress(byte[] data) throws DecompressionException {
        data = bufferOrGetCompleteData(data);
        // signal we can't decompress yet
        if (data == null) {
            return null;
        }

        LOG.trace("Decompressing data {}", lazy(data));
        // Get the compressed message and inflate it
        // We use the same buffer here to optimize gc use
        ByteArrayOutputStream buffer = getDecompressBuffer();
        try (InflaterOutputStream decompressor = new InflaterOutputStream(buffer, inflater)) {
            // This decompressor writes the received data and inflates it
            decompressor.write(data);
            // Once decompressed we re-interpret the data as a String which can be used for JSON
            // parsing
            return buffer.toByteArray();
        } catch (IOException e) {
            // Some issue appeared during decompression that caused a failure
            throw new DecompressionException(e);
        } finally {
            // When done with decompression we want to reset the buffer so it can be used again
            // later
            if (buffer.size() > maxBufferSize) {
                decompressBuffer = newDecompressBuffer();
            } else {
                buffer.reset();
            }
        }
    }
}
