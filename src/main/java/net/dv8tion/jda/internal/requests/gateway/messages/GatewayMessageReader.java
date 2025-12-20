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

package net.dv8tion.jda.internal.requests.gateway.messages;

import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.compress.DecompressionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Bulk + JSON = OK
 * Bulk + ETF = OK
 * Streaming + JSON = OK
 * Streaming + ETF = NO (adapt ExTermDecoder to use input streams)
 */
public interface GatewayMessageReader {
    @Nonnull
    Compression getCompression();

    @Nullable
    DataObject read(@Nonnull byte[] data) throws DecompressionException;

    void reset();

    void close();
}
