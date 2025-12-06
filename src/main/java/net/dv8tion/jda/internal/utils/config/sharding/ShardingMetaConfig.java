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

package net.dv8tion.jda.internal.utils.config.sharding;

import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.compress.DecompressorFactory;
import net.dv8tion.jda.internal.utils.config.MetaConfig;
import net.dv8tion.jda.internal.utils.config.flags.ConfigFlag;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShardingMetaConfig extends MetaConfig {
    private static final ShardingMetaConfig defaultConfig = new ShardingMetaConfig(
            i -> 2048, null, null, ConfigFlag.getDefault(), i -> Compression.ZLIB, GatewayEncoding.JSON);
    private final GatewayEncoding encoding;
    private final IntFunction<? extends ConcurrentMap<String, String>> contextProvider;
    private final IntUnaryOperator bufferSizeHintProvider;
    private final IntFunction<Compression> compressionProvider;

    public ShardingMetaConfig(
            IntUnaryOperator bufferSizeHintProvider,
            @Nullable IntFunction<? extends ConcurrentMap<String, String>> contextProvider,
            @Nullable EnumSet<CacheFlag> cacheFlags,
            EnumSet<ConfigFlag> flags,
            IntFunction<Compression> compressionProvider,
            GatewayEncoding encoding) {
        super(null, cacheFlags, flags);
        this.bufferSizeHintProvider = bufferSizeHintProvider;
        this.compressionProvider = compressionProvider;
        this.contextProvider = contextProvider;
        this.encoding = encoding;
    }

    @Nullable
    public ConcurrentMap<String, String> getContextMap(int shardId) {
        return contextProvider == null ? null : contextProvider.apply(shardId);
    }

    @Nonnull
    public DecompressorFactory getDecompressorFactory(int shardId) {
        Compression compression = compressionProvider.apply(shardId);
        Checks.notNull(compression, "Compression");
        int bufferSizeHint = bufferSizeHintProvider.applyAsInt(shardId);

        return DecompressorFactory.of(compression, bufferSizeHint);
    }

    public GatewayEncoding getEncoding() {
        return encoding;
    }

    @Nullable
    public IntFunction<? extends ConcurrentMap<String, String>> getContextProvider() {
        return contextProvider;
    }

    @Nonnull
    public static ShardingMetaConfig getDefault() {
        return defaultConfig;
    }
}
