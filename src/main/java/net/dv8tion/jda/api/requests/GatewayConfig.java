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

package net.dv8tion.jda.api.requests;

import dev.freya02.discord.zstd.api.DiscordZstdContext;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.DiscordZstdProvider;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.internal.requests.GatewayConfigImpl;
import net.dv8tion.jda.internal.requests.gateway.decoder.Decoder;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayBufferMessageReader;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayMessageReader;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayStreamMessageReader;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.compress.disabled.DisabledDecompressor;
import net.dv8tion.jda.internal.utils.compress.zlib.ZlibBufferDecompressor;
import net.dv8tion.jda.internal.utils.compress.zlib.ZlibStreamDecompressor;
import net.dv8tion.jda.internal.utils.compress.zstd.ZstdBufferDecompressorAdapter;
import net.dv8tion.jda.internal.utils.compress.zstd.ZstdStreamDecompressorAdapter;

import java.util.function.Function;

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
        private Function<Decoder, GatewayMessageReader> messageReaderFunction =
                (decoder) -> new GatewayBufferMessageReader(decoder, new ZlibBufferDecompressor(2048));
        private boolean isStreamDecompression = false;
        private GatewayEncoding encoding = GatewayEncoding.JSON;

        /**
         * Disables compression of gateway messages.
         * This highly increases bandwidth usages and should <b>NOT</b> be used unless you have issues with compression.
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder disableCompression() {
            this.messageReaderFunction =
                    (decoder) -> new GatewayBufferMessageReader(decoder, DisabledDecompressor.INSTANCE);
            this.isStreamDecompression = false;

            return this;
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
        public Builder useBufferZlibDecompression(int maxBufferSize) {
            Checks.notNegative(maxBufferSize, "Max buffer size");
            this.messageReaderFunction =
                    (decoder) -> new GatewayBufferMessageReader(decoder, new ZlibBufferDecompressor(maxBufferSize));
            this.isStreamDecompression = false;

            return this;
        }

        /**
         * Enabled receiving gateway messages compressed with Zstandard,
         * which are decompressed all at once.
         *
         * <p>This uses way less resources than Zlib, however this typically is noticeable for large bots.
         *
         * <p><b>Note:</b> This requires you to include a dependency,
         * see <a href="https://github.com/freya022/discord-zstd-java" target="_blank">discord-zstd-java</a>.
         *
         * <h4>Buffer sizes</h4>
         * This defines the size, in bytes, of the intermediate buffer used for decompression,
         * larger buffer means less decompression loops at the cost of memory.
         *
         * <ul>
         *     <li>The minimum is {@value dev.freya02.discord.zstd.api.DiscordZstdDecompressor#MIN_BUFFER_SIZE}</li>
         *     <li>We recommend setting a buffer size of {@value dev.freya02.discord.zstd.api.DiscordZstdDecompressor#DEFAULT_BUFFER_SIZE} as this will be enough for most payloads</li>
         *     <li>
         *         A value "recommended" by Zstd is set with {@link dev.freya02.discord.zstd.api.DiscordZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE DiscordZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE};
         *         However it is not recommended for normal use cases, see the docs for more details.
         *     </li>
         * </ul>
         *
         * @param  bufferSizeHint
         *         The buffer size, or a hint for it, to decompress the data
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useBufferZstdDecompression(int bufferSizeHint) {
            DiscordZstdDecompressorFactory underlyingFactory =
                    DiscordZstdProvider.get().createDecompressorFactory(bufferSizeHint);
            this.messageReaderFunction = (decoder) -> new GatewayBufferMessageReader(
                    decoder, new ZstdBufferDecompressorAdapter(underlyingFactory.create()));
            this.isStreamDecompression = false;
            return this;
        }

        /**
         * Enables receiving gateway messages compressed by the provided compression algorithm,
         * which are decompressed progressively.
         *
         * <p>While this greatly reduces memory allocations,
         * this prevents JDA from logging (incredibly rare) parsing issues possibly
         * caused by undetected decompression issues or a faulty decoder.
         *
         * <p>Consult the documentation of the provided {@link Compression} algorithm for more details.
         *
         * <p><b>Note:</b> Using this with the {@link GatewayEncoding#ETF ETF} encoding is not supported.
         *
         * @throws IllegalArgumentException
         *         If the provided compression is {@code null}
         *
         * @return This builder for chaining convenience
         */
        @Nonnull
        public Builder useStreamDecompression(@Nonnull Compression compression) {
            Checks.notNull(compression, "Compression");
            switch (compression) {
                case NONE:
                    messageReaderFunction =
                            (decoder) -> new GatewayStreamMessageReader(decoder, DisabledDecompressor.INSTANCE);
                    break;
                case ZLIB:
                    messageReaderFunction =
                            (decoder) -> new GatewayStreamMessageReader(decoder, new ZlibStreamDecompressor());
                    break;
                case ZSTD:
                    DiscordZstdContext context = DiscordZstdProvider.get().createContext();
                    messageReaderFunction = (decoder) ->
                            new GatewayStreamMessageReader(decoder, new ZstdStreamDecompressorAdapter(context));
                    break;
            }
            this.isStreamDecompression = true;

            return this;
        }

        /**
         * Encodes gateway messages, both received and sent, with the provided encoding. (default: {@link GatewayEncoding#JSON JSON})
         *
         * <p><b>Note:</b> Using this with {@linkplain #useStreamDecompression(Compression) stream decompression} is not supported.
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
