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

package net.dv8tion.jda.internal.utils;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.internal.utils.requestbody.BufferedRequestBody;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okio.Okio;
import org.slf4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class IOUtil
{
    private static final Logger log = JDALogger.getLog(IOUtil.class);

    public static void silentClose(AutoCloseable closeable)
    {
        try
        {
            closeable.close();
        }
        catch (Exception ignored) {}
    }

    public static void silentClose(Closeable closeable)
    {
        try
        {
            closeable.close();
        }
        catch (IOException ignored) {}
    }

    public static String addQuery(String base, Object... params)
    {
        try
        {
            StringBuilder builder = new StringBuilder(base);
            // Start a new query or append to existing one
            if (new URI(base).getQuery() == null)
                builder.append('?');
            else
                builder.append('&');

            for (int i = 0; i < params.length; i += 2)
            {
                builder.append(params[i])
                    .append('=')
                    .append(URLEncoder.encode(params[i + 1].toString(), "UTF-8"))
                    .append('&');
            }

            // Remove trailing &
            builder.setLength(builder.length() - 1);

            return builder.toString();
        }
        catch (URISyntaxException | UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getHost(String uri)
    {
        return URI.create(uri).getHost();
    }

    public static void setServerName(WebSocketFactory factory, String url)
    {
        String host = getHost(url);
        // null if the host is undefined, unlikely but we should handle it
        if (host != null)
            factory.setServerName(host);
    }

    public static OkHttpClient.Builder newHttpClientBuilder()
    {
        Dispatcher dispatcher = new Dispatcher();
        // Allow 25 parallel requests to the same host (usually discord.com)
        dispatcher.setMaxRequestsPerHost(25);
        // Allow 5 idle threads with 10 seconds timeout for each
        ConnectionPool connectionPool = new ConnectionPool(5, 10, TimeUnit.SECONDS);
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .dispatcher(dispatcher);
    }

    /**
     * Used as an alternate to Java's nio Files.readAllBytes.
     *
     * <p>This customized version for File is provide (instead of just using {@link #readFully(java.io.InputStream)} with a FileInputStream)
     * because with a File we can determine the total size of the array and do not need to have a buffer.
     * This results in a memory footprint that is half the size of {@link #readFully(java.io.InputStream)}
     *
     * <p>Code provided from <a href="http://stackoverflow.com/a/6276139">Stackoverflow</a>
     *
     * @param  file
     *         The file from which we should retrieve the bytes from
     *
     * @throws java.io.IOException
     *         Thrown if there is a problem while reading the file.
     *
     * @return A byte[] containing all of the file's data
     */
    public static byte[] readFully(File file) throws IOException
    {
        Checks.notNull(file, "File");
        Checks.check(file.exists(), "Provided file does not exist!");

        try (InputStream is = new FileInputStream(file))
        {
            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE)
            {
                throw new IOException("Cannot read the file into memory completely due to it being too large!");
                // File is too large
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
            {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length)
            {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();
            return bytes;
        }
    }

    /**
     * Provided as a simple way to fully read an InputStream into a byte[].
     *
     * <p>This method will block until the InputStream has been fully read, so if you provide an InputStream that is
     * non-finite, you're gonna have a bad time.
     *
     * @param  stream
     *         The Stream to be read.
     *
     * @throws IOException
     *         If the first byte cannot be read for any reason other than the end of the file,
     *         if the input stream has been closed, or if some other I/O error occurs.
     *
     * @return A byte[] containing all of the data provided by the InputStream
     */
    public static byte[] readFully(InputStream stream) throws IOException
    {
        Checks.notNull(stream, "InputStream");

        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream())
        {
            int readAmount = 0;
            while ((readAmount = stream.read(buffer)) != -1)
            {
                bos.write(buffer, 0, readAmount);
            }
            return bos.toByteArray();
        }
    }

    /**
     * Creates a new request body that transmits the provided {@link java.io.InputStream InputStream}.
     *
     * @param  contentType
     *         The {@link okhttp3.MediaType MediaType} of the data
     * @param  stream
     *         The {@link java.io.InputStream InputStream} to be transmitted
     *
     * @return RequestBody capable of transmitting the provided InputStream of data
     */
    public static BufferedRequestBody createRequestBody(final MediaType contentType, final InputStream stream)
    {
        return new BufferedRequestBody(Okio.source(stream), contentType);
    }

    public static short getShortBigEndian(byte[] arr, int offset)
    {
        return (short) ((arr[offset    ] & 0xff) << 8
                       | arr[offset + 1] & 0xff);
    }

    public static short getShortLittleEndian(byte[] arr, int offset)
    {
        // Same as big endian but reversed order of bytes (java uses big endian)
        return (short) ((arr[offset    ] & 0xff)
                      | (arr[offset + 1] & 0xff) << 8);
    }

    public static int getIntBigEndian(byte[] arr, int offset)
    {
        return arr[offset + 3] & 0xFF
            | (arr[offset + 2] & 0xFF) << 8
            | (arr[offset + 1] & 0xFF) << 16
            | (arr[offset    ] & 0xFF) << 24;
    }

    public static void setIntBigEndian(byte[] arr, int offset, int it)
    {
        arr[offset    ] = (byte) ((it >>> 24) & 0xFF);
        arr[offset + 1] = (byte) ((it >>> 16) & 0xFF);
        arr[offset + 2] = (byte) ((it >>> 8)  & 0xFF);
        arr[offset + 3] = (byte) ( it         & 0xFF);
    }

    public static ByteBuffer reallocate(ByteBuffer original, int length)
    {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(original);
        return buffer;
    }

    /**
     * Retrieves an {@link InputStream InputStream} for the provided {@link okhttp3.Response Response}.
     * <br>When the header for {@code content-encoding} is set with {@code gzip} this will wrap the body
     * in a {@link java.util.zip.GZIPInputStream GZIPInputStream} which decodes the data.
     *
     * <p>This is used to make usage of encoded responses more user-friendly in various parts of JDA.
     *
     * @param  response
     *         The not-null Response object
     *
     * @return InputStream representing the body of this response
     */
    @SuppressWarnings("ConstantConditions") // methods here don't return null despite the annotations on them, read the docs
    public static InputStream getBody(okhttp3.Response response) throws IOException
    {
        String encoding = response.header("content-encoding", "");
        InputStream data = new BufferedInputStream(response.body().byteStream());
        data.mark(256);
        try
        {
            if (encoding.equalsIgnoreCase("gzip"))
                return new GZIPInputStream(data);
            else if (encoding.equalsIgnoreCase("deflate"))
                return new InflaterInputStream(data, new Inflater(true));
        }
        catch (ZipException | EOFException ex)
        {
            data.reset(); // reset to get full content
            log.error("Failed to read gzip content for response. Headers: {}\nContent: '{}'",
                response.headers(), JDALogger.getLazyString(() -> new String(readFully(data))), ex);
            return null;
        }
        return data;
    }
}
