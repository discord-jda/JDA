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

package net.dv8tion.jda.api.requests.gateway.compression;

import net.dv8tion.jda.api.exceptions.DecompressionException;

import java.io.InputStream;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base interface for compression of gateway messages.
 *
 * @see GatewayDecompressor.Transport
 */
public interface GatewayDecompressor {
    /**
     * Returns the compression key, <a href="https://discord.com/developers/docs/events/gateway#transport-compression" target="_blank">as defined by Discord</a>,
     * which is added as a query parameter for the gateway URL.
     *
     * @return The compression key, or {@code null} when compression is disabled
     */
    @Nullable
    String getQueryParameter();

    /**
     * Shuts down the decompressor and releases resources.
     */
    void shutdown();

    /**
     * Base interface for transport compression of gateway messages.
     *
     * @see GatewayDecompressor.Transport.Buffered
     * @see GatewayDecompressor.Transport.Streamed
     */
    interface Transport extends GatewayDecompressor {
        /**
         * Resets the decompressor so it can be used again with a new set of payloads.
         */
        void reset();

        /**
         * An interface for transport compression of gateway messages,
         * this one is called a "buffer" decompressor as it decompresses everything at once.
         *
         * <p>Implementations do not need to be thread-safe, as there is a single instance per shard.
         *
         * @see net.dv8tion.jda.api.requests.gateway.GatewayConfig.Builder#useBufferedTransportDecompression(Supplier) GatewayConfig.Builder.useBufferedTransportDecompression(Supplier)
         */
        interface Buffered extends GatewayDecompressor.Transport {
            /**
             * Decompresses the provided data,
             * or returns {@code null} if some input is missing and must be completed in a future operation.
             *
             * <p>Errors which prevents the decompressor from functioning with future payloads <b>must</b> throw a {@link DecompressionException}.
             *
             * @param  data
             *         The data to decompress
             *
             * @throws DecompressionException
             *         If an unrecoverable decompression error occurs
             *
             * @return The decompressed data, or {@code null}.
             */
            @Nullable
            byte[] decompress(@Nonnull byte[] data) throws DecompressionException;
        }

        /**
         * An interface for transport compression of gateway messages,
         * this one is called a "stream" decompressor as it decompresses gradually,
         * typically using a recycled buffer.
         *
         * <p>Implementations do not need to be thread-safe, as there is a single instance per shard.
         *
         * @see net.dv8tion.jda.api.requests.gateway.GatewayConfig.Builder#useStreamedTransportDecompression(Supplier) GatewayConfig.Builder.useStreamedTransportDecompression(Supplier)
         */
        interface Streamed extends GatewayDecompressor.Transport {
            /**
             * Returns an {@link InputStream} to progressively decompresses the provided data,
             * or returns {@code null} if some input is missing and must be completed in a future operation.
             *
             * <p>Errors which prevents the decompressor from functioning with future payloads <b>must</b> throw a {@link DecompressionException}.
             *
             * @param  data
             *         The data to decompress
             *
             * @return A stream for the decompressed data, or {@code null}.
             */
            @Nullable
            InputStream createInputStream(@Nonnull byte[] data);
        }
    }
}
