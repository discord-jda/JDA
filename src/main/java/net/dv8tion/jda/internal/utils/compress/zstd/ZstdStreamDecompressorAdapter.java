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

package net.dv8tion.jda.internal.utils.compress.zstd;

import dev.freya02.discord.zstd.api.DiscordZstdContext;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.internal.utils.compress.StreamDecompressor;

import java.io.InputStream;

import javax.annotation.Nullable;

public class ZstdStreamDecompressorAdapter implements StreamDecompressor {
    private final DiscordZstdContext context;

    public ZstdStreamDecompressorAdapter(DiscordZstdContext context) {
        this.context = context;
    }

    @Override
    public Compression getType() {
        return Compression.ZSTD;
    }

    @Override
    public void reset() {
        context.reset();
    }

    @Override
    public void shutdown() {
        context.close();
    }

    @Nullable
    @Override
    public InputStream createInputStream(byte[] data) {
        LOG.trace("Decompressing data {}", lazy(data));
        return context.createInputStream(data);
    }
}
