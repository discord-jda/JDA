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

import net.dv8tion.jda.api.exceptions.DecompressionException;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ZlibTransportGatewayStreamedDecompressor extends AbstractZlibTransportGatewayDecompressor
        implements GatewayDecompressor.Transport.Streamed {
    private final Inflater inflater = new Inflater();

    @Nullable
    @Override
    public InputStream createInputStream(@Nonnull byte[] data) {
        data = bufferOrGetCompleteData(data);
        // signal we can't decompress yet
        if (data == null) {
            return null;
        }

        LOG.trace("Decompressing data {}", lazy(data));
        return new FixedInflaterInputStream(inflater, data);
    }

    private static class FixedInflaterInputStream extends InputStream {
        private final Inflater inflater;

        private boolean closed = false;
        private boolean invalidated = false;

        public FixedInflaterInputStream(Inflater inflater, byte[] data) {
            this.inflater = inflater;
            inflater.setInput(data);
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public int read() throws IOException {
            byte[] buf = new byte[1];
            return read(buf, 0, 1);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throw new IOException("Stream is closed");
            }
            if (invalidated) {
                throw new IllegalStateException("Decompressor is in an errored state and needs to be reset");
            }
            if ((b.length | off | len) < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            if (inflater.needsInput()) {
                return -1;
            }

            try {
                return inflater.inflate(b, off, len);
            } catch (DataFormatException e) {
                invalidated = true;
                String s = e.getMessage();
                throw new DecompressionException(s != null ? s : "Invalid ZLIB data format");
            }
        }
    }
}
