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

package net.dv8tion.jda.internal.utils.requestbody;

import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MediaType;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BufferedRequestBody extends TypedBody<BufferedRequestBody>
{
    private final Source source;
    private byte[] data;

    public BufferedRequestBody(Source source, MediaType type)
    {
        super(type);
        this.source = source;
    }

    @Nonnull
    public BufferedRequestBody withType(@Nonnull MediaType type)
    {
        if (type.equals(this.type))
            return this;
        synchronized (source)
        {
            BufferedRequestBody copy = new BufferedRequestBody(source, type);
            copy.data = data;
            return copy;
        }
    }

    @Override
    public void writeTo(@Nonnull BufferedSink sink) throws IOException
    {
        synchronized (source)
        {
            if (data != null)
            {
                sink.write(data);
                return;
            }

            try (BufferedSource s = Okio.buffer(source))
            {
                data = s.readByteArray();
                sink.write(data);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize()
    {
        IOUtil.silentClose(source);
    }
}
