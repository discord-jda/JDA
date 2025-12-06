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

package net.dv8tion.jda.api.utils;

import javax.annotation.Nonnull;

/**
 * Compression algorithms that can be used with JDA.
 *
 * @see net.dv8tion.jda.api.JDABuilder#setCompression(Compression) JDABuilder.setCompression(Compression)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#setCompression(Compression) DefaultShardManagerBuilder.setCompression(Compression)
 * @see #ZLIB
 * @see #ZSTD
 */
public enum Compression {
    /** Don't use any compression */
    NONE(""),
    /**
     * Use ZLIB transport compression.
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
     */
    ZLIB("zlib-stream"),
    /**
     * Use Zstandard transport compression,
     * this requires you to include a dependency,
     * see <a href="https://github.com/freya022/discord-zstd-java" target="_blank">discord-zstd-java</a>.
     *
     * <h4>Buffer sizes</h4>
     * This defines the size, in bytes, of the intermediate buffer used for decompression,
     * larger buffer means less decompression loops at the cost of memory.
     *
     * <ul>
     *     <li>The default is {@value dev.freya02.discord.zstd.api.DiscordZstdDecompressor#DEFAULT_BUFFER_SIZE}</li>
     *     <li>
     *         A value "recommended" by Zstd is set with {@link dev.freya02.discord.zstd.api.DiscordZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE DiscordZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE};
     *         However it is not recommended for normal use cases, see the docs for more details.
     *     </li>
     *     <li>The minimum is {@value dev.freya02.discord.zstd.api.DiscordZstdDecompressor#MIN_BUFFER_SIZE}</li>
     * </ul>
     */
    ZSTD("zstd-stream"),
    ;

    private final String key;

    Compression(String key) {
        this.key = key;
    }

    /**
     * The key used for the gateway query to enable this compression
     *
     * @return The query key
     */
    @Nonnull
    public String getKey() {
        return key;
    }
}
