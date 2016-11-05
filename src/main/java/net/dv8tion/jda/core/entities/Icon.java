/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.core.utils.DataUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.util.Args;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class Icon
{
    protected final String encoding;

    protected Icon(String base64Encoding)
    {
        this.encoding = "data:image/jpeg;base64," + base64Encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public static Icon from(File file) throws IOException
    {
        Args.notNull(file, "Provided File");
        Args.check(file.exists(), "Provided file does not exist!");

        return from(DataUtil.readFully(file));
    }

    public static Icon from(InputStream stream) throws IOException
    {
        Args.notNull(stream, "InputStream");

        return from(DataUtil.readFully(stream));
    }

    public static Icon from(byte[] data)
    {
        Args.notNull(data, "Provided byte[]");

        String encoding = StringUtils.newStringUtf8(Base64.getEncoder().encode(data));
        return new Icon(encoding);
    }
}
