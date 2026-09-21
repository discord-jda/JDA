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

package net.dv8tion.jda.internal.requests.gateway.decoder;

import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

public class JsonDecoder implements Decoder {
    private static final Logger LOG = JDALogger.getLog(JsonDecoder.class);

    @Nonnull
    @Override
    public DataObject decode(@Nonnull byte[] data) {
        try {
            return DataObject.fromJson(data);
        } catch (ParsingException e) {
            String jsonString = "malformed";
            try {
                jsonString = new String(data, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
            // Print the string that could not be parsed and re-throw the exception
            LOG.error("Failed to parse json: {}", jsonString);
            throw e;
        }
    }

    @Nonnull
    @Override
    public DataObject decode(@Nonnull InputStream stream) {
        // No exception handling as we can't get read the stream twice to get the parsed string
        return DataObject.fromJson(stream);
    }
}
