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

package net.dv8tion.jda.test.requests.gateway.messages;

import net.dv8tion.jda.api.exceptions.DecompressionException;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import net.dv8tion.jda.internal.requests.gateway.decoder.JsonDecoder;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayStreamedMessageReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class GatewayStreamedMessageReaderTest {
    @SuppressWarnings("resource")
    @Test
    public void testDecompressionErrorThrowsDecompressionException() {
        var decompressor = Mockito.mock(GatewayDecompressor.Transport.Streamed.class);
        var stream = new InputStream() {
            @Override
            public int read() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(@Nonnull byte[] b, int off, int len) throws IOException {
                throw new DecompressionException("Expected");
            }
        };
        doReturn(stream).when(decompressor).createInputStream(any());

        var messageReader = new GatewayStreamedMessageReader(new JsonDecoder(), decompressor);

        assertThatExceptionOfType(DecompressionException.class).isThrownBy(() -> messageReader.read(new byte[0]));
    }
}
