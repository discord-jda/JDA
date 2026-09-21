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
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.gateway.decoder.Decoder;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GatewayStreamedMessageReader implements GatewayMessageReader {
    private final Decoder decoder;
    private final GatewayDecompressor.Transport.Streamed decompressor;

    public GatewayStreamedMessageReader(Decoder decoder, GatewayDecompressor.Transport.Streamed decompressor) {
        this.decoder = decoder;
        this.decompressor = decompressor;
    }

    @Nullable
    @Override
    public String getCompressionQueryParameter() {
        return decompressor.getQueryParameter();
    }

    @Nullable
    @Override
    public DataObject read(@Nonnull byte[] data) throws DecompressionException {
        InputStream stream = decompressor.createInputStream(data);
        if (stream == null) {
            return null;
        }

        try {
            return decoder.decode(stream);
        } catch (ParsingException e) {
            // Unwrap and see if it is a DecompressionException, so the WS can be invalidated
            Throwable cause = e.getCause();
            if (cause instanceof DecompressionException) {
                throw ((DecompressionException) cause);
            }

            throw e;
        }
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
