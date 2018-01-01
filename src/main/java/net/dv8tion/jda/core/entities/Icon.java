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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.utils.IOUtil;
import net.dv8tion.jda.core.utils.Checks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * Icon containing a base64 encoded jpeg/png/gif/gifv image.
 * <br>Used to represent various base64 images in the Discord api.
 * <br>Example: {@link net.dv8tion.jda.core.managers.AccountManager#setAvatar(Icon)}.
 *
 * @since 3.0
 */
public class Icon
{
    protected final String encoding;

    protected Icon(String base64Encoding)
    {
        //Note: the usage of `image/jpeg` does not mean png/gif are not supported!
        this.encoding = "data:image/jpeg;base64," + base64Encoding;
    }

    /**
     * The base64 encoded data for this Icon
     *
     * @return String representation of the encoded data for this icon
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.File File}.
     * <br>We here read the specified File and forward the retrieved byte data to {@link #from(byte[])}.
     *
     * @param  file
     *         An existing, not-null file.
     *
     * @throws IllegalArgumentException
     *         if the provided file is either null or does not exist
     * @throws IOException
     *         if there is a problem while reading the file.
     *
     * @return An Icon instance representing the specified File
     *
     * @see    net.dv8tion.jda.core.utils.IOUtil#readFully(File)
     */
    public static Icon from(File file) throws IOException
    {
        Checks.notNull(file, "Provided File");
        Checks.check(file.exists(), "Provided file does not exist!");

        return from(IOUtil.readFully(file));
    }

    /**
     * Creates an {@link Icon Icon} with the specified {@link java.io.InputStream InputStream}.
     * <br>We here read the specified InputStream and forward the retrieved byte data to {@link #from(byte[])}.
     *
     * @param  stream
     *         A not-null InputStream.
     *
     * @throws IllegalArgumentException
     *         if the provided stream is null
     * @throws IOException
     *         If the first byte cannot be read for any reason other than the end of the file,
     *         if the input stream has been closed, or if some other I/O error occurs.
     *
     * @return An Icon instance representing the specified InputStream
     *
     * @see    net.dv8tion.jda.core.utils.IOUtil#readFully(InputStream)
     */
    public static Icon from(InputStream stream) throws IOException
    {
        Checks.notNull(stream, "InputStream");

        return from(IOUtil.readFully(stream));
    }

    /**
     * Creates an {@link Icon Icon} with the specified image data.
     *
     * @param  data
     *         not-null image data bytes.
     *
     * @throws IllegalArgumentException
     *         if the provided data is null
     *
     * @return An Icon instance representing the specified image data
     */
    public static Icon from(byte[] data)
    {
        Checks.notNull(data, "Provided byte[]");

        try
        {
            return new Icon(new String(Base64.getEncoder().encode(data), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError(e); // thanks JDK 1.4
        }
    }
}
