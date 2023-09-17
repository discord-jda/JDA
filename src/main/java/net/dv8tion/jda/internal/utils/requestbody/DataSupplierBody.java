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

import okhttp3.MediaType;
import okio.BufferedSink;
import okio.Source;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.Supplier;

public class DataSupplierBody extends TypedBody<DataSupplierBody>
{
    private final Supplier<? extends Source> streamSupply;

    public DataSupplierBody(MediaType type, Supplier<? extends Source> streamSupply)
    {
        super(type);
        this.streamSupply = streamSupply;
    }

    @Nonnull
    @Override
    public DataSupplierBody withType(@Nonnull MediaType newType)
    {
        if (this.type.equals(newType))
            return this;
        return new DataSupplierBody(newType, streamSupply);
    }

    @Override
    public void writeTo(@Nonnull BufferedSink bufferedSink) throws IOException
    {
        synchronized (streamSupply)
        {
            try (Source stream = streamSupply.get())
            {
                bufferedSink.writeAll(stream);
            }
        }
    }
}
