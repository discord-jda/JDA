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

import dev.freya02.discord.zstd.api.DiscordZstdException;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.internal.requests.gateway.decoder.JsonDecoder;
import net.dv8tion.jda.internal.requests.gateway.messages.GatewayStreamMessageReader;
import net.dv8tion.jda.internal.utils.compress.DecompressionException;
import net.dv8tion.jda.internal.utils.compress.StreamDecompressor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.zip.ZipException;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class GatewayStreamMessageReaderTest {
    @SuppressWarnings("resource")
    @EnumSource(Compression.class)
    @ParameterizedTest
    public void testDecompressionErrorThrowsDecompressionException(Compression compression) {
        if (compression == Compression.NONE) {
            return;
        }

        // Each compression type has its own exception that the code has to check for
        Supplier<Exception> exceptionSupplier =
                switch (compression) {
                    case ZLIB -> () -> new ZipException("Expected");
                    case ZSTD -> () -> new DiscordZstdException("Expected");
                    default -> throw new AssertionError("Unhandled compression: " + compression);
                };

        var decompressor = Mockito.mock(StreamDecompressor.class);
        var stream = new InputStream() {
            @Override
            public int read() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(@Nonnull byte[] b, int off, int len) throws IOException {
                throw new IOException(exceptionSupplier.get());
            }
        };
        doReturn(stream).when(decompressor).createInputStream(any());

        var messageReader = new GatewayStreamMessageReader(new JsonDecoder(), decompressor);

        assertThatExceptionOfType(DecompressionException.class)
                .describedAs("DecompressionException should have been thrown in response to an exception of the underlying decompression library")
                .isThrownBy(() -> messageReader.read(new byte[0]));
    }
}
