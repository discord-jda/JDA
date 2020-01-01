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
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class ZlibDecompressor implements Decompressor
{
    private static final int Z_SYNC_FLUSH = 0x0000FFFF;

    private final int maxBufferSize;
    private final Inflater inflater = new Inflater();
    private ByteBuffer flushBuffer = null;
    private SoftReference<ByteArrayOutputStream> decompressBuffer = null;

    public ZlibDecompressor(int maxBufferSize)
    {
        this.maxBufferSize = maxBufferSize;
    }

    private SoftReference<ByteArrayOutputStream> newDecompressBuffer()
    {
        return new SoftReference<>(new ByteArrayOutputStream(Math.min(1024, maxBufferSize)));
    }

    private ByteArrayOutputStream getDecompressBuffer()
    {
        // If no buffer has been allocated yet we do that here (lazy init)
        if (decompressBuffer == null)
            decompressBuffer = newDecompressBuffer();
        // Check if the buffer has been collected by the GC or not
        ByteArrayOutputStream buffer = decompressBuffer.get();
        if (buffer == null) // create a ne buffer because the GC got it
            decompressBuffer = newDecompressBuffer();
        return buffer;
    }

    private boolean isFlush(byte[] data)
    {
        if (data.length < 4)
            return false;
        int suffix = IOUtil.getIntBigEndian(data, data.length - 4);
        return suffix == Z_SYNC_FLUSH;
    }

    private void buffer(byte[] data)
    {
        if (flushBuffer == null)
            flushBuffer = ByteBuffer.allocate(data.length * 2);

        //Ensure the capacity can hold the new data, ByteBuffer doesn't grow automatically
        if (flushBuffer.capacity() < data.length + flushBuffer.position())
        {
            //Flip to make it a read buffer
            flushBuffer.flip();
            //Reallocate for the new capacity
            flushBuffer = IOUtil.reallocate(flushBuffer, (flushBuffer.capacity() + data.length) * 2);
        }

        flushBuffer.put(data);
    }

    private Object lazy(byte[] data)
    {
        return JDALogger.getLazyString(() -> Arrays.toString(data));
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
    public String decompress(byte[] data) throws DataFormatException
    {
        //Handle split messages
        if (!isFlush(data))
        {
            //There is no flush suffix so this is not the end of the message
            LOG.debug("Received incomplete data, writing to buffer. Length: {}", data.length);
            buffer(data);
            return null; // signal failure to decompress
        }
        else if (flushBuffer != null)
        {
            //This has a flush suffix and we have an incomplete package buffered
            //concatenate the package with the new data and decompress it below
            LOG.debug("Received final part of incomplete data");
            buffer(data);
            byte[] arr = flushBuffer.array();
            data = new byte[flushBuffer.position()];
            System.arraycopy(arr, 0, data, 0, data.length);
            flushBuffer = null;
        }
        LOG.trace("Decompressing data {}", lazy(data));
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
            if (buffer.size() > maxBufferSize)
                decompressBuffer = newDecompressBuffer();
            else
                buffer.reset();
        }
    }
}
