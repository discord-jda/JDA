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

package net.dv8tion.jda.internal.requests.gateway.compression.zlib;

import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class AbstractZlibTransportGatewayDecompressor implements GatewayDecompressor.Transport {
    protected static final Logger LOG = JDALogger.getLog(GatewayDecompressor.class);

    private static final int Z_SYNC_FLUSH = 0x0000FFFF;

    protected final Inflater inflater = new Inflater();
    protected ByteBuffer flushBuffer = null;

    @Nullable
    protected byte[] bufferOrGetCompleteData(@Nonnull byte[] data) {
        // Handle split messages
        if (!isFlush(data)) {
            // There is no flush suffix so this is not the end of the message
            LOG.debug("Received incomplete data, writing to buffer. Length: {}", data.length);
            buffer(data);
            return null;
        } else if (flushBuffer != null) {
            // This has a flush suffix and we have an incomplete package buffered
            // concatenate the package with the new data and decompress it below
            LOG.debug("Received final part of incomplete data");
            buffer(data);
            byte[] arr = flushBuffer.array();
            data = new byte[flushBuffer.position()];
            System.arraycopy(arr, 0, data, 0, data.length);
            flushBuffer = null;
        }
        return data;
    }

    private boolean isFlush(byte[] data) {
        if (data.length < 4) {
            return false;
        }
        int suffix = IOUtil.getIntBigEndian(data, data.length - 4);
        return suffix == Z_SYNC_FLUSH;
    }

    private void buffer(byte[] data) {
        if (flushBuffer == null) {
            flushBuffer = ByteBuffer.allocate(data.length * 2);
        }

        // Ensure the capacity can hold the new data, ByteBuffer doesn't grow automatically
        if (flushBuffer.capacity() < data.length + flushBuffer.position()) {
            // Flip to make it a read buffer
            flushBuffer.flip();
            // Reallocate for the new capacity
            flushBuffer = IOUtil.reallocate(flushBuffer, (flushBuffer.capacity() + data.length) * 2);
        }

        flushBuffer.put(data);
    }

    @Nullable
    @Override
    public String getQueryParameter() {
        return "zlib-stream";
    }

    @Override
    public void reset() {
        inflater.reset();
    }

    @Override
    public void shutdown() {
        inflater.end();
    }

    protected Object lazy(byte[] data) {
        return JDALogger.getLazyString(() -> Arrays.toString(data));
    }
}
