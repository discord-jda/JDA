/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.internal.utils.JDALogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class ZlibDecompressor implements Decompressor
{
    private final Inflater inflater = new Inflater();
    private SoftReference<ByteArrayOutputStream> decompressBuffer = null;

    private SoftReference<ByteArrayOutputStream> newDecompressBuffer()
    {
        return new SoftReference<>(new ByteArrayOutputStream(1024));
    }

    private ByteArrayOutputStream getDecompressBuffer()
    {
        // If no buffer has been allocated yet we do that here (lazy init)
        if (decompressBuffer == null)
            decompressBuffer = newDecompressBuffer();
        // Check if the buffer has been collected by the GC or not
        ByteArrayOutputStream buffer = decompressBuffer.get();
        if (buffer == null) // create a ne buffer because the GC got it
            decompressBuffer = new SoftReference<>(buffer = new ByteArrayOutputStream(1024));
        return buffer;
    }

    @Override
    public Compression getType()
    {
        return Compression.ZLIB;
    }

    @Override
    public void reset()
    {
        inflater.reset();
    }

    @Override
    public void shutdown()
    {
        reset();
    }

    @Override
    @SuppressWarnings("CharsetObjectCanBeUsed")
    public String decompress(byte[] data) throws DataFormatException
    {
        LOG.trace("Decompressing data {}", JDALogger.getLazyString(() -> Arrays.toString(data)));
        //Get the compressed message and inflate it
        //We use the same buffer here to optimize gc use
        ByteArrayOutputStream buffer = getDecompressBuffer();
        try (InflaterOutputStream decompressor = new InflaterOutputStream(buffer, inflater))
        {
            // This decompressor writes the received data and inflates it
            decompressor.write(data);
            // Once decompressed we re-interpret the data as a String which can be used for JSON parsing
            return buffer.toString("UTF-8");
        }
        catch (IOException e)
        {
            // Some issue appeared during decompression that caused a failure
            throw (DataFormatException) new DataFormatException("Malformed").initCause(e);
        }
        finally
        {
            // When done with decompression we want to reset the buffer so it can be used again later
            buffer.reset();
        }
    }
}
