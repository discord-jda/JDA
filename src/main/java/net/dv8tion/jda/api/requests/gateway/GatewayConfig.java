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

package net.dv8tion.jda.api.requests.gateway;

import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import net.dv8tion.jda.internal.requests.GatewayConfigImpl;
import net.dv8tion.jda.internal.requests.gateway.compression.disabled.DisabledTransportGatewayDecompressor;
import net.dv8tion.jda.internal.requests.gateway.compression.zlib.ZlibTransportGatewayBufferedDecompressor;
import net.dv8tion.jda.internal.requests.gateway.compression.zlib.ZlibTransportGatewayStreamedDecompressor;
import net.dv8tion.jda.internal.requests.gateway.decoder.Decoder;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayBufferedMessageReader;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayMessageReader;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayStreamedMessageReader;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Configuration for gateway connections.
 * <br>This can be used to configure compression, its parameters and the encoding of gateway messages.
 *
 * @see #builder()
 */
public interface GatewayConfig {
    /**
     * Creates a builder for {@link GatewayConfig}.
     *
     * @return A new {@link GatewayConfig.Builder}
     */
    @Nonnull
    static GatewayConfig.Builder builder() {
        return new Builder();
    }

    /**
     * A builder of {@link GatewayConfig}.
     */
    class Builder {
        private Function<Decoder, GatewayMessageReader> messageReaderFunction;
        private boolean isStreamDecompression = false;
        private GatewayEncoding encoding = GatewayEncoding.JSON;

        public Builder() {
            useBufferedZlibTransportDecompression(2048);
        }

        /**
         * Disables compression of gateway messages.
         * This highly increases bandwidth usages and should <b>NOT</b> be used unless you have issues with compression.
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder disableCompression() {
            return useBufferedTransportDecompression(() -> DisabledTransportGatewayDecompressor.INSTANCE);
        }

        /**
         * Enables receiving gateway messages compressed with Zlib,
         * which are decompresses all at once. <b>This is used by default.</b>
         *
         * <h4>Maximum buffer sizes</h4>
         * If the decompression buffer gets extended above this value, in bytes, it will be shunk back after it is finished.
         * <br>This does not define the buffer size itself. Only the threshold at which it will be shrunk.
         *
         * <ul>
         *     <li>The default is {@code 2048}</li>
         *     <li>Setting this to {@link Integer#MAX_VALUE} would imply the buffer will never be resized unless memory starvation is imminent.</li>
         *     <li>Setting this to {@code 0} would imply the buffer would need to be allocated again for every payload (not recommended).</li>
         * </ul>
         *
         * @param  maxBufferSize
         *         The size of the intermediary buffer that has to be reached before it has to be shrunk down
         *
         * @throws IllegalArgumentException
         *         If the provided buffer size is negative
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useBufferedZlibTransportDecompression(int maxBufferSize) {
            Checks.notNegative(maxBufferSize, "Max buffer size");
            return useBufferedTransportDecompression(() -> new ZlibTransportGatewayBufferedDecompressor(maxBufferSize));
        }

        /**
         * Enables receiving gateway messages and decompressing with the provided buffer decompressor.
         *
         * @param  supplier
         *         The supplier of decompressor, called for each shard
         *
         * @throws IllegalArgumentException
         *         If the provided supplier is {@code null}
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useBufferedTransportDecompression(
                @Nonnull Supplier<? extends GatewayDecompressor.Transport.Buffered> supplier) {
            Checks.notNull(supplier, "Supplier");
            this.messageReaderFunction = (decoder) -> new GatewayBufferedMessageReader(decoder, supplier.get());
            this.isStreamDecompression = false;

            return this;
        }

        /**
         * Enables receiving gateway messages and decompressing with Zlib.
         *
         * <p>While this greatly reduces memory allocations,
         * this prevents JDA from logging (incredibly rare) parsing issues possibly
         * caused by undetected decompression issues or a faulty decoder.
         *
         * <p><b>Note:</b> Using this with the {@link GatewayEncoding#ETF ETF} encoding is not supported.
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useStreamedZlibTransportDecompression() {
            return useStreamedTransportDecompression(ZlibTransportGatewayStreamedDecompressor::new);
        }

        /**
         * Enables receiving gateway messages and decompressing with the provided stream decompressor.
         *
         * <p>While this greatly reduces memory allocations,
         * this prevents JDA from logging (incredibly rare) parsing issues possibly
         * caused by undetected decompression issues or a faulty decoder.
         *
         * <p><b>Note:</b> Using this with the {@link GatewayEncoding#ETF ETF} encoding is not supported.
         *
         * @throws IllegalArgumentException
         *         If the provided supplier is {@code null}
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useStreamedTransportDecompression(
                @Nonnull Supplier<? extends GatewayDecompressor.Transport.Streamed> supplier) {
            Checks.notNull(supplier, "Supplier");
            this.messageReaderFunction = (decoder) -> new GatewayStreamedMessageReader(decoder, supplier.get());
            this.isStreamDecompression = true;

            return this;
        }

        /**
         * Encodes gateway messages, both received and sent, with the provided encoding. (default: {@link GatewayEncoding#JSON JSON})
         *
         * <p><b>Note:</b> Using this with {@linkplain #useStreamedTransportDecompression(Supplier) stream decompression} is not supported.
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useEncoding(@Nonnull GatewayEncoding encoding) {
            Checks.notNull(encoding, "Gateway encoding");
            this.encoding = encoding;

            return this;
        }

        /**
         * Builds a {@link GatewayConfig} out of this builder.
         *
         * @throws IllegalArgumentException
         *         If stream decompression is used
         *
         * @return The new {@link GatewayConfig}
         */
        @Nonnull
        public GatewayConfig build() {
            if (isStreamDecompression) {
                Checks.check(
                        encoding != GatewayEncoding.ETF, "Cannot use stream decompression with ETF payload encoding");
            }
            return new GatewayConfigImpl(messageReaderFunction, encoding);
        }
    }
}
