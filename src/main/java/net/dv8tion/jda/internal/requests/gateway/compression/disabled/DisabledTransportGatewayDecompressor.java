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

package net.dv8tion.jda.internal.requests.gateway.compression.disabled;

import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DisabledTransportGatewayDecompressor
        implements GatewayDecompressor.Transport.Buffered, GatewayDecompressor.Transport.Streamed {
    public static final DisabledTransportGatewayDecompressor INSTANCE = new DisabledTransportGatewayDecompressor();

    private DisabledTransportGatewayDecompressor() {}

    @Nullable
    @Override
    public String getQueryParameter() {
        return null;
    }

    @Override
    public void reset() {}

    @Override
    public void shutdown() {}

    @Nullable
    @Override
    public byte[] decompress(@Nonnull byte[] data) {
        return data;
    }

    @Nullable
    @Override
    public InputStream createInputStream(@Nonnull byte[] data) {
        return new ByteArrayInputStream(data);
    }
}
