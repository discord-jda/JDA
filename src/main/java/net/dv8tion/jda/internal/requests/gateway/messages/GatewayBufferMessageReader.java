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

package net.dv8tion.jda.internal.requests.gateway.messages;

import net.dv8tion.jda.api.exceptions.DecompressionException;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.gateway.decoder.Decoder;
import net.dv8tion.jda.internal.utils.compress.BufferDecompressor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GatewayBufferMessageReader implements GatewayMessageReader {
    private final Decoder decoder;
    private final BufferDecompressor decompressor;

    public GatewayBufferMessageReader(Decoder decoder, BufferDecompressor decompressor) {
        this.decoder = decoder;
        this.decompressor = decompressor;
    }

    @Nonnull
    @Override
    public Compression getCompression() {
        return decompressor.getType();
    }

    @Nullable
    @Override
    public DataObject read(@Nonnull byte[] data) throws DecompressionException {
        byte[] decompressed = decompressor.decompress(data);
        if (decompressed == null) {
            return null;
        }
        return decoder.decode(decompressed);
    }

    @Override
    public void reset() {
        decompressor.reset();
    }

    @Override
    public void close() {
        decompressor.shutdown();
    }
}
