/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.audio;

import com.iwebpp.crypto.TweetNaclFast;

import java.net.DatagramPacket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents the contents of a audio packet that was either received from Discord or
 * will be sent to discord.
 */
public class AudioPacket
{
    public static final int RTP_HEADER_BYTE_LENGTH = 12;

    /**
     * Bit index 0 and 1 represent the RTP Protocol version used. Discord uses the latest RTP protocol version, 2.<br>
     * Bit index 2 represents whether or not we pad. Opus uses an internal padding system, so RTP padding is not used.<br>
     * Bit index 3 represents if we use extensions. Discord does not use RTP extensions.<br>
     * Bit index 4 to 7 represent the CC or CSRC count. CSRC is Combined SSRC. Discord doesn't combine audio streams,
     *      so the Combined count will always be 0 (binary: 0000).<br>
     * This byte should always be the same, no matter the library implementation.
     */
    public static final byte RTP_VERSION_PAD_EXTEND = (byte) 0x80;  //Binary: 1000 0000

    /**
     * This is Discord's RTP Profile Payload type.<br>
     * I've yet to find actual documentation on what the bits inside this value represent.<br>
     * As far as I can tell, this byte will always be the same, no matter the library implementation.<br>
     */
    public static final byte RTP_PAYLOAD_TYPE = (byte) 0x78;        //Binary: 0100 1000

    public static final int SEQ_INDEX =                     2;
    public static final int TIMESTAMP_INDEX =               4;
    public static final int SSRC_INDEX =                    8;

    private final char seq;
    private final int timestamp;
    private final int ssrc;
    private final byte[] encodedAudio;
    private final byte[] rawPacket;

    public AudioPacket(DatagramPacket packet)
    {
        this(Arrays.copyOf(packet.getData(), packet.getLength()));
    }

    public AudioPacket(byte[] rawPacket)
    {
        this.rawPacket = rawPacket;

        ByteBuffer buffer = ByteBuffer.wrap(rawPacket);
        this.seq = buffer.getChar(SEQ_INDEX);
        this.timestamp = buffer.getInt(TIMESTAMP_INDEX);
        this.ssrc = buffer.getInt(SSRC_INDEX);

        final byte versionPad = buffer.get(0);
        final byte[] data = buffer.array();
        if ((versionPad & 0b0001_0000) != 0
            && data[RTP_HEADER_BYTE_LENGTH] == (byte) 0xBE && data[RTP_HEADER_BYTE_LENGTH + 1] == (byte) 0xDE)
        {
            final short headerLength = (short) (data[RTP_HEADER_BYTE_LENGTH + 2] << 8 | data[RTP_HEADER_BYTE_LENGTH + 3]);
            int i = RTP_HEADER_BYTE_LENGTH + 4;
            for (; i < headerLength + RTP_HEADER_BYTE_LENGTH + 4; i++)
            {
                byte len = (byte) ((data[i] & 0x0F) + 1);
                i += len;
            }
            while (data[i] == 0)
                i++;
            this.encodedAudio = new byte[data.length - i];
            System.arraycopy(data, i, encodedAudio, 0, encodedAudio.length);
        }
        else
        {
            this.encodedAudio = new byte[buffer.array().length - RTP_HEADER_BYTE_LENGTH];
            System.arraycopy(buffer.array(), RTP_HEADER_BYTE_LENGTH, this.encodedAudio, 0, this.encodedAudio.length);
        }
    }

    public AudioPacket(char seq, int timestamp, int ssrc, byte[] encodedAudio)
    {
        this.seq = seq;
        this.ssrc = ssrc;
        this.timestamp = timestamp;
        this.encodedAudio = encodedAudio;
        this.rawPacket = generateRawPacket(seq, timestamp, ssrc, encodedAudio);
    }

    @SuppressWarnings("unused")
    public byte[] getHeader()
    {
        //The first 12 bytes of the rawPacket are the RTP Discord Nonce.
        return Arrays.copyOf(rawPacket, RTP_HEADER_BYTE_LENGTH);
    }

    public byte[] getNoncePadded()
    {
        byte[] nonce = new byte[TweetNaclFast.SecretBox.nonceLength];
        //The first 12 bytes are the rawPacket are the RTP Discord Nonce.
        System.arraycopy(rawPacket, 0, nonce, 0, RTP_HEADER_BYTE_LENGTH);
        return nonce;
    }

    public byte[] getRawPacket()
    {
        return rawPacket;
    }

    public byte[] getEncodedAudio()
    {
        return encodedAudio;
    }

    public byte[] getEncodedAudio(int nonceLength)
    {
        if (nonceLength == 0)
            return encodedAudio;
        return Arrays.copyOf(encodedAudio, encodedAudio.length - nonceLength);
    }

    public char getSequence()
    {
        return seq;
    }

    public int getSSRC()
    {
        return ssrc;
    }

    public int getTimestamp()
    {
        return timestamp;
    }

    public ByteBuffer asEncryptedPacket(ByteBuffer buffer, byte[] secretKey, byte[] nonce, int nlen)
    {
        //Xsalsa20's Nonce is 24 bytes long, however RTP (and consequently Discord)'s nonce is a different length
        // so we need to create a 24 byte array, and copy the nonce into it.
        // we will leave the extra bytes as nulls. (Java sets non-populated bytes as 0).
        byte[] extendedNonce = nonce;
        if (nonce == null)
            extendedNonce = getNoncePadded();

        //Create our SecretBox encoder with the secretKey provided by Discord.
        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
        byte[] encryptedAudio = boxer.box(encodedAudio, extendedNonce);
        ((Buffer) buffer).clear();
        int capacity = RTP_HEADER_BYTE_LENGTH + encryptedAudio.length + nlen;
        if (capacity > buffer.remaining())
            buffer = ByteBuffer.allocate(capacity);
        populateBuffer(seq, timestamp, ssrc, encryptedAudio, buffer);
        if (nonce != null)
            buffer.put(nonce, 0, nlen);

        return buffer;
    }

    public static AudioPacket decryptAudioPacket(AudioEncryption encryption, DatagramPacket packet, byte[] secretKey)
    {
        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
        AudioPacket encryptedPacket = new AudioPacket(packet);

        byte[] extendedNonce;
        byte[] rawPacket = encryptedPacket.getRawPacket();
        switch (encryption)
        {
            case XSALSA20_POLY1305:
                extendedNonce = encryptedPacket.getNoncePadded();
                break;
            case XSALSA20_POLY1305_SUFFIX:
                extendedNonce = new byte[TweetNaclFast.SecretBox.nonceLength];
                System.arraycopy(rawPacket, rawPacket.length - extendedNonce.length, extendedNonce, 0, extendedNonce.length);
                break;
            case XSALSA20_POLY1305_LITE:
                extendedNonce = new byte[TweetNaclFast.SecretBox.nonceLength];
                System.arraycopy(rawPacket, rawPacket.length - 4, extendedNonce, 0, 4);
                break;
            default:
                AudioConnection.LOG.debug("Failed to decrypt audio packet, unsupported encryption mode!");
                return null;
        }

        byte[] encodedAudio;
        switch (encryption)
        {
            case XSALSA20_POLY1305:
                encodedAudio = encryptedPacket.getEncodedAudio();
                break;
            case XSALSA20_POLY1305_LITE:
                encodedAudio = encryptedPacket.getEncodedAudio(4);
                break;
            case XSALSA20_POLY1305_SUFFIX:
                encodedAudio = encryptedPacket.getEncodedAudio(TweetNaclFast.SecretBox.nonceLength);
                break;
            default:
                AudioConnection.LOG.debug("Failed to decrypt audio packet, unsupported encryption mode!");
                return null;
        }

        final byte[] decryptedAudio = boxer.open(encodedAudio, extendedNonce);
        if (decryptedAudio == null)
        {
            AudioConnection.LOG.debug("Failed to decrypt audio packet");
            return null;
        }
        final byte[] decryptedRawPacket = new byte[RTP_HEADER_BYTE_LENGTH + decryptedAudio.length];

        //first 12 bytes of rawPacket are the RTP header
        //the rest is the audio data we just decrypted
        System.arraycopy(encryptedPacket.rawPacket, 0, decryptedRawPacket, 0, RTP_HEADER_BYTE_LENGTH);
        System.arraycopy(decryptedAudio, 0, decryptedRawPacket, RTP_HEADER_BYTE_LENGTH, decryptedAudio.length);

        return new AudioPacket(decryptedRawPacket);
    }

    private static byte[] generateRawPacket(char seq, int timestamp, int ssrc, byte[] data)
    {
        ByteBuffer buffer = ByteBuffer.allocate(RTP_HEADER_BYTE_LENGTH + data.length);
        populateBuffer(seq, timestamp, ssrc, data, buffer);
        return buffer.array();
    }

    private static void populateBuffer(char seq, int timestamp, int ssrc, byte[] data, ByteBuffer buffer)
    {
        buffer.put(RTP_VERSION_PAD_EXTEND);
        buffer.put(RTP_PAYLOAD_TYPE);
        buffer.putChar(seq);
        buffer.putInt(timestamp);
        buffer.putInt(ssrc);
        buffer.put(data);
    }
}
