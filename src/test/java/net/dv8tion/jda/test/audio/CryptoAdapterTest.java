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

package net.dv8tion.jda.test.audio;

import net.dv8tion.jda.api.audio.dave.PassthroughDaveSessionFactory;
import net.dv8tion.jda.internal.audio.AudioEncryption;
import net.dv8tion.jda.internal.audio.AudioPacket;
import net.dv8tion.jda.internal.audio.CryptoAdapter;
import net.dv8tion.jda.internal.audio.DaveCryptoAdapter;
import net.dv8tion.jda.internal.utils.ResizingByteBuffer;
import net.dv8tion.jda.test.Constants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoAdapterTest {
    private static final String TEST_PAYLOAD = "text";
    private static final char TEST_SEQ = 'a';
    private static final int TEST_TIMESTAMP = 1234;
    private static final int TEST_SSRC = 5678;
    private static final int TEST_EXTENSION = 0xBEDE;

    @EnumSource
    @ParameterizedTest
    void testMinimalRoundtrip(AudioEncryption encryption) {
        AudioPacket original = getMinimalPacket();
        byte[] key = getKey();

        CryptoAdapter adapter = getAdapter(encryption, key);
        doRoundTripAndAssertPayload(adapter, original);
    }

    @EnumSource
    @ParameterizedTest
    void testRoundtripWithExtension(AudioEncryption encryption) {
        AudioPacket original = getPacketWithExtension();
        byte[] key = getKey();

        CryptoAdapter adapter = getAdapter(encryption, key);
        doRoundTripAndAssertPayload(adapter, original);
    }

    private CryptoAdapter getAdapter(AudioEncryption encryption, byte[] key) {
        return new DaveCryptoAdapter(
                CryptoAdapter.getAdapter(encryption, key),
                new PassthroughDaveSessionFactory()
                        .createDaveSession(null, Constants.BUTLER_USER_ID, Constants.CHANNEL_ID),
                0);
    }

    private void doRoundTripAndAssertPayload(CryptoAdapter adapter, AudioPacket original) {
        ResizingByteBuffer buffer = new ResizingByteBuffer(ByteBuffer.allocateDirect(512));
        ResizingByteBuffer decryptBuffer = new ResizingByteBuffer(ByteBuffer.allocateDirect(512));

        original.asEncryptedPacket(adapter, buffer);

        byte[] rawPacket = new byte[buffer.buffer().remaining()];
        buffer.buffer().get(rawPacket);

        AudioPacket decrypted =
                new AudioPacket(rawPacket).asDecryptAudioPacket(adapter, Constants.MINN_USER_ID, decryptBuffer);
        assertThat(decrypted).isNotNull();

        byte[] payload = new byte[4];
        decrypted.getEncodedAudio().get(payload);

        assertThat(new String(payload, StandardCharsets.UTF_8)).isEqualTo(TEST_PAYLOAD);

        assertThat(decrypted.getSequence()).isEqualTo(TEST_SEQ);
        assertThat(decrypted.getTimestamp()).isEqualTo(TEST_TIMESTAMP);
        assertThat(decrypted.getSSRC()).isEqualTo(TEST_SSRC);
    }

    private static AudioPacket getMinimalPacket() {
        ByteBuffer rawPacket = ByteBuffer.allocate(12 + 4);

        rawPacket.put(AudioPacket.RTP_VERSION_PAD_EXTEND);
        rawPacket.put(AudioPacket.RTP_PAYLOAD_TYPE);
        rawPacket.putChar(TEST_SEQ);
        rawPacket.putInt(TEST_TIMESTAMP);
        rawPacket.putInt(TEST_SSRC);
        rawPacket.put(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));

        return new AudioPacket(rawPacket.array());
    }

    private static AudioPacket getPacketWithExtension() {
        ByteBuffer rawPacket = ByteBuffer.allocate(16 + 4);

        rawPacket.put((byte) (AudioPacket.RTP_VERSION_PAD_EXTEND | 0x10));
        rawPacket.put(AudioPacket.RTP_PAYLOAD_TYPE);
        rawPacket.putChar(TEST_SEQ);
        rawPacket.putInt(TEST_TIMESTAMP);
        rawPacket.putInt(TEST_SSRC);
        rawPacket.putInt(TEST_EXTENSION);
        rawPacket.put(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));

        return new AudioPacket(rawPacket.flip());
    }

    private static byte[] getKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        return key;
    }
}
