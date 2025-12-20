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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.requests.GatewayConfig;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.internal.requests.gateway.decoder.Decoder;
import net.dv8tion.jda.internal.requests.gateway.decoder.ETFDecoder;
import net.dv8tion.jda.internal.requests.gateway.decoder.JsonDecoder;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayMessageReader;

import java.util.function.Function;

import javax.annotation.Nonnull;

public class GatewayConfigImpl implements GatewayConfig {
    private final Function<Decoder, GatewayMessageReader> messageReaderFunction;
    private final GatewayEncoding encoding;

    @Nonnull
    public static GatewayConfigImpl fromLegacy(Compression compression, int maxBufferSize) {
        GatewayConfig.Builder builder = new GatewayConfig.Builder();
        switch (compression) {
            case NONE:
                builder.disableCompression();
                break;
            case ZLIB:
                builder.useBulkZlibDecompression(maxBufferSize);
                break;
            case ZSTD:
                builder.useBulkZstdDecompression(maxBufferSize);
                break;
        }
        return (GatewayConfigImpl) builder.build();
    }

    public GatewayConfigImpl(
            @Nonnull Function<Decoder, GatewayMessageReader> messageReaderFunction, @Nonnull GatewayEncoding encoding) {
        this.messageReaderFunction = messageReaderFunction;
        this.encoding = encoding;
    }

    @Nonnull
    public GatewayMessageReader createMessageReader() {
        Decoder decoder;
        switch (encoding) {
            case JSON:
                decoder = new JsonDecoder();
                break;
            case ETF:
                decoder = new ETFDecoder();
                break;
            default:
                throw new IllegalArgumentException("Invalid GatewayEncoding " + encoding);
        }

        return messageReaderFunction.apply(decoder);
    }

    @Nonnull
    public GatewayEncoding getEncoding() {
        return encoding;
    }
}
