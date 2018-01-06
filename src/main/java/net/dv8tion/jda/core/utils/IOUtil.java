/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.utils;

import java.io.*;

public class IOUtil
{

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
}
