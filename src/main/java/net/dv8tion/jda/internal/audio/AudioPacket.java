/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.audio;

import com.iwebpp.crypto.TweetNaclFast;
import net.dv8tion.jda.internal.utils.IOUtil;

import java.net.DatagramPacket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents the contents of a audio packet that was either received from Discord or
 * will be sent to discord.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3550" target="_blank">RFC 3350 - RTP: A Transport Protocol for Real-Time Applications</a>
 */
public class AudioPacket
{
    public static final int RTP_HEADER_BYTE_LENGTH = 12;

    /**
     * Bit index 0 and 1 represent the RTP Protocol version used. Discord uses the latest RTP protocol version, 2.<br>
     * Bit index 2 represents whether or not we pad. Opus uses an internal padding system, so RTP padding is not used.<br>
     * Bit index 3 represents if we use extensions.<br>
     * Bit index 4 to 7 represent the CC or CSRC count. CSRC is Combined SSRC.
     */
    public static final byte RTP_VERSION_PAD_EXTEND = (byte) 0x80;  //Binary: 1000 0000

    /**
     * This is Discord's RTP Profile Payload type.<br>
     * I've yet to find actual documentation on what the bits inside this value represent.
     */
    public static final byte RTP_PAYLOAD_TYPE = (byte) 0x78;        //Binary: 0100 1000

    /**
     * This defines the extension type used by discord for presumably video?
     */
    public static final short RTP_DISCORD_EXTENSION = (short) 0xBEDE;

    public static final int PT_INDEX =                      1;
    public static final int SEQ_INDEX =                     2;
    public static final int TIMESTAMP_INDEX =               4;
    public static final int SSRC_INDEX =                    8;

    private final byte type;
    private final char seq;
    private final int timestamp;
    private final int ssrc;
    private final byte[] rawPacket;
    private final ByteBuffer encodedAudio;

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
        this.type = buffer.get(PT_INDEX);

        final byte profile = buffer.get(0);
        final byte[] data = buffer.array();
        final boolean hasExtension = (profile & 0x10) != 0; // extension bit is at 000X
        final byte cc = (byte) (profile & 0x0f);            // CSRC count - we ignore this for now
        final int csrcLength = cc * 4;                      // defines count of 4-byte words
        // it seems as if extensions only exist without a csrc list being present
        final short extension = hasExtension ? IOUtil.getShortBigEndian(data, RTP_HEADER_BYTE_LENGTH + csrcLength) : 0;

        int offset = RTP_HEADER_BYTE_LENGTH + csrcLength;
        if (hasExtension && extension == RTP_DISCORD_EXTENSION)
            offset = getPayloadOffset(data, csrcLength);

        this.encodedAudio = ByteBuffer.allocate(data.length - offset);
        this.encodedAudio.put(data, offset, encodedAudio.capacity());
        ((Buffer) this.encodedAudio).flip();
    }

    public AudioPacket(ByteBuffer buffer, char seq, int timestamp, int ssrc, ByteBuffer encodedAudio)
    {
        this.seq = seq;
        this.ssrc = ssrc;
        this.timestamp = timestamp;
        this.encodedAudio = encodedAudio;
        this.type = RTP_PAYLOAD_TYPE;
        this.rawPacket = generateRawPacket(buffer, seq, timestamp, ssrc, encodedAudio);
    }

    private int getPayloadOffset(byte[] data, int csrcLength)
    {
        // headerLength defines number of 4-byte words in the extension
        final short headerLength = IOUtil.getShortBigEndian(data, RTP_HEADER_BYTE_LENGTH + 2 + csrcLength);
        int i = RTP_HEADER_BYTE_LENGTH // RTP header = 12 bytes
                + 4                    // header which defines a profile and length each 2-bytes = 4 bytes
                + csrcLength           // length of CSRC list (this seems to be always 0 when an extension exists)
                + headerLength * 4;    // number of 4-byte words in extension = len * 4 bytes

        // strip excess 0 bytes
        while (data[i] == 0)
            i++;
        return i;
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

    public ByteBuffer getEncodedAudio()
    {
        return encodedAudio;
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

    protected ByteBuffer asEncryptedPacket(TweetNaclFast.SecretBox boxer, ByteBuffer buffer, byte[] nonce, int nlen)
    {
        //Xsalsa20's Nonce is 24 bytes long, however RTP (and consequently Discord)'s nonce is a different length
        // so we need to create a 24 byte array, and copy the nonce into it.
        // we will leave the extra bytes as nulls. (Java sets non-populated bytes as 0).
        byte[] extendedNonce = nonce;
        if (nlen == 0) // this means the header is the nonce!
            extendedNonce = getNoncePadded();

        //Create our SecretBox encoder with the secretKey provided by Discord.
        byte[] array = encodedAudio.array();
        int offset = encodedAudio.arrayOffset() + encodedAudio.position();
        int length = encodedAudio.remaining();
        byte[] encryptedAudio = boxer.box(array, offset, length, extendedNonce);

        ((Buffer) buffer).clear();
        int capacity = RTP_HEADER_BYTE_LENGTH + encryptedAudio.length + nlen;
        if (capacity > buffer.remaining())
            buffer = ByteBuffer.allocate(capacity);
        populateBuffer(seq, timestamp, ssrc, ByteBuffer.wrap(encryptedAudio), buffer);
        if (nlen > 0) // this means we append the nonce to the payload
            buffer.put(nonce, 0, nlen);

        ((Buffer) buffer).flip();
        return buffer;
    }

    protected static AudioPacket decryptAudioPacket(AudioEncryption encryption, DatagramPacket packet, byte[] secretKey)
    {
        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
        AudioPacket encryptedPacket = new AudioPacket(packet);
        if (encryptedPacket.type != RTP_PAYLOAD_TYPE)
            return null;

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

        ByteBuffer encodedAudio = encryptedPacket.encodedAudio;
        int length = encodedAudio.remaining();
        int offset = encodedAudio.arrayOffset() + encodedAudio.position();
        switch (encryption)
        {
            case XSALSA20_POLY1305:
//                length = encodedAudio.remaining();
                break;
            case XSALSA20_POLY1305_LITE:
                length -= 4;
                break;
            case XSALSA20_POLY1305_SUFFIX:
                length -= TweetNaclFast.SecretBox.nonceLength;
                break;
            default:
                AudioConnection.LOG.debug("Failed to decrypt audio packet, unsupported encryption mode!");
                return null;
        }

        final byte[] decryptedAudio = boxer.open(encodedAudio.array(), offset, length, extendedNonce);
        if (decryptedAudio == null)
        {
            AudioConnection.LOG.trace("Failed to decrypt audio packet");
            return null;
        }
        final byte[] decryptedRawPacket = new byte[RTP_HEADER_BYTE_LENGTH + decryptedAudio.length];

        //first 12 bytes of rawPacket are the RTP header
        //the rest is the audio data we just decrypted
        System.arraycopy(encryptedPacket.rawPacket, 0, decryptedRawPacket, 0, RTP_HEADER_BYTE_LENGTH);
        System.arraycopy(decryptedAudio, 0, decryptedRawPacket, RTP_HEADER_BYTE_LENGTH, decryptedAudio.length);

        return new AudioPacket(decryptedRawPacket);
    }

    private static byte[] generateRawPacket(ByteBuffer buffer, char seq, int timestamp, int ssrc, ByteBuffer data)
    {
        if (buffer == null)
            buffer = ByteBuffer.allocate(RTP_HEADER_BYTE_LENGTH + data.remaining());
        populateBuffer(seq, timestamp, ssrc, data, buffer);
        return buffer.array();
    }

    private static void populateBuffer(char seq, int timestamp, int ssrc, ByteBuffer data, ByteBuffer buffer)
    {
        buffer.put(RTP_VERSION_PAD_EXTEND);
        buffer.put(RTP_PAYLOAD_TYPE);
        buffer.putChar(seq);
        buffer.putInt(timestamp);
        buffer.putInt(ssrc);
        buffer.put(data);
        ((Buffer) data).flip();
    }
}
