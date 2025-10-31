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

package net.dv8tion.jda.internal.utils.compress;

import net.dv8tion.jda.api.utils.Compression;

import javax.annotation.Nonnull;

public class NullDecompressorFactory implements DecompressorFactory
{
    public static final DecompressorFactory INSTANCE = new NullDecompressorFactory();

    private NullDecompressorFactory()
    {
    }

    @Override
    public Decompressor create()
    {
        return NullDecompressor.INSTANCE;
    }

    private static class NullDecompressor implements Decompressor
    {

        private static final Decompressor INSTANCE = new NullDecompressor();

        @Override
        public Compression getType()
        {
            return Compression.NONE;
        }

        @Override
        public void reset()
        {

        }

        @Override
        public void shutdown()
        {

        }

        @Nonnull
        @Override
        public byte[] decompress(byte[] data)
        {
            return data;
        }
    }
}
