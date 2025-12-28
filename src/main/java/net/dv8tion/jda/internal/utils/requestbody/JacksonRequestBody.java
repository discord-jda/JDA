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

import com.fasterxml.jackson.databind.ObjectWriter;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.SerializationUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JacksonRequestBody extends RequestBody {
    private final Object data;

    public JacksonRequestBody(Object data) {
        this.data = data;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return Requester.MEDIA_TYPE_JSON;
    }

    @Override
    public void writeTo(@Nonnull BufferedSink bufferedSink) throws IOException {
        ObjectWriter writer = SerializationUtil.getObjectWriter(false);
        writer.writeValue(bufferedSink.outputStream(), data);
    }
}
