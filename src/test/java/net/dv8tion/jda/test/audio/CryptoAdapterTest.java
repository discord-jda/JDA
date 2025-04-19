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

import net.dv8tion.jda.internal.audio.AudioEncryption;
import net.dv8tion.jda.internal.audio.AudioPacket;
import net.dv8tion.jda.internal.audio.CryptoAdapter;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoAdapterTest
{
    private static final String TEST_PAYLOAD = "text";
    private static final char TEST_SEQ = 'a';
    private static final int TEST_TIMESTAMP = 1234;
    private static final int TEST_SSRC = 5678;
    private static final int TEST_EXTENSION = 0xBEDE;

    @Test
    void minimalAES()
    {
        AudioPacket original = getMinimalPacket();
        byte[] key = getKey();

        CryptoAdapter adapter = CryptoAdapter.getAdapter(AudioEncryption.AEAD_AES256_GCM_RTPSIZE, key);
        doRoundTripAndAssertPayload(adapter, original);
    }

    @Test
    void minimalXChaCha20()
    {
        AudioPacket original = getMinimalPacket();
        byte[] key = getKey();

        CryptoAdapter adapter = CryptoAdapter.getAdapter(AudioEncryption.AEAD_XCHACHA20_POLY1305_RTPSIZE, key);
        doRoundTripAndAssertPayload(adapter, original);
    }

    @Test
    void extendedAES()
    {
        AudioPacket original = getPacketWithExtension();
        byte[] key = getKey();

        CryptoAdapter adapter = CryptoAdapter.getAdapter(AudioEncryption.AEAD_AES256_GCM_RTPSIZE, key);
        doRoundTripAndAssertPayload(adapter, original);
    }

    @Test
    void extendedXChaCha20()
    {
        AudioPacket original = getPacketWithExtension();
        byte[] key = getKey();

        CryptoAdapter adapter = CryptoAdapter.getAdapter(AudioEncryption.AEAD_XCHACHA20_POLY1305_RTPSIZE, key);
        doRoundTripAndAssertPayload(adapter, original);
    }

    private void doRoundTripAndAssertPayload(CryptoAdapter adapter, AudioPacket original)
    {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer = original.asEncryptedPacket(adapter, buffer);

        AudioPacket decrypted = AudioPacket.decryptAudioPacket(adapter, new DatagramPacket(buffer.array(), buffer.position(), buffer.limit()));

        byte[] payload = new byte[4];
        decrypted.getEncodedAudio().get(payload);

        assertThat(new String(payload, StandardCharsets.UTF_8))
            .isEqualTo(TEST_PAYLOAD);

        assertThat(decrypted.getSequence()).isEqualTo(TEST_SEQ);
        assertThat(decrypted.getTimestamp()).isEqualTo(TEST_TIMESTAMP);
        assertThat(decrypted.getSSRC()).isEqualTo(TEST_SSRC);
    }

    private static AudioPacket getMinimalPacket()
    {
        ByteBuffer rawPacket = ByteBuffer.allocate(12 + 4);

        rawPacket.put(AudioPacket.RTP_VERSION_PAD_EXTEND);
        rawPacket.put(AudioPacket.RTP_PAYLOAD_TYPE);
        rawPacket.putChar(TEST_SEQ);
        rawPacket.putInt(TEST_TIMESTAMP);
        rawPacket.putInt(TEST_SSRC);
        rawPacket.put(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));

        return new AudioPacket(rawPacket.array());
    }

    private static AudioPacket getPacketWithExtension()
    {
        ByteBuffer rawPacket = ByteBuffer.allocate(16 + 4);

        rawPacket.put((byte) (AudioPacket.RTP_VERSION_PAD_EXTEND | 0x10));
        rawPacket.put(AudioPacket.RTP_PAYLOAD_TYPE);
        rawPacket.putChar(TEST_SEQ);
        rawPacket.putInt(TEST_TIMESTAMP);
        rawPacket.putInt(TEST_SSRC);
        rawPacket.putInt(TEST_EXTENSION);
        rawPacket.put(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));

        return new AudioPacket(rawPacket.array());
    }

    private static byte[] getKey()
    {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        return key;
    }
}
