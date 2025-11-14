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

package net.dv8tion.jda.internal.audio;

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
public class AudioPacket {
    public static final int RTP_HEADER_BYTE_LENGTH = 12;

    /**
     * Bit index 0 and 1 represent the RTP Protocol version used. Discord uses the latest RTP protocol version, 2.<br>
     * Bit index 2 represents whether or not we pad. Opus uses an internal padding system, so RTP padding is not used.<br>
     * Bit index 3 represents if we use extensions.<br>
     * Bit index 4 to 7 represent the CC or CSRC count. CSRC is Combined SSRC.
     */
    public static final byte RTP_VERSION_PAD_EXTEND = (byte) 0x80; // Binary: 1000 0000

    /**
     * This is Discord's RTP Profile Payload type.<br>
     * I've yet to find actual documentation on what the bits inside this value represent.
     */
    public static final byte RTP_PAYLOAD_TYPE = (byte) 0x78; // Binary: 0100 1000

    private final byte type;
    private final char seq;
    private final int timestamp;
    private final int ssrc;
    private final int extension;
    private final boolean hasExtension;
    private final int[] csrc;
    private final int headerLength;
    private final byte[] rawPacket;
    private final ByteBuffer encodedAudio;

    public AudioPacket(DatagramPacket packet) {
        this(Arrays.copyOf(packet.getData(), packet.getLength()));
    }

    public AudioPacket(byte[] rawPacket) {
        this.rawPacket = rawPacket;

        ByteBuffer buffer = ByteBuffer.wrap(rawPacket);

        // Parsing header as described by https://datatracker.ietf.org/doc/html/rfc3550#section-5.1

        byte first = buffer.get();
        // extension, 1 if extension is present
        this.hasExtension = (first & 0b0001_0000) != 0;
        // CSRC count, 0 to 15
        int cc = first & 0x0f;

        this.type = buffer.get();
        this.seq = buffer.getChar();
        this.timestamp = buffer.getInt();
        this.ssrc = buffer.getInt();

        this.csrc = new int[cc];
        for (int i = 0; i < cc; i++) {
            this.csrc[i] = buffer.getInt();
        }

        // Extract extension length as described by
        // https://datatracker.ietf.org/doc/html/rfc3550#section-5.3.1
        if (this.hasExtension) {
            buffer.position(buffer.position() + 2);
            this.extension = buffer.getShort();
        } else {
            this.extension = 0;
        }

        this.headerLength = buffer.position();
        this.encodedAudio = buffer;
    }

    public AudioPacket(ByteBuffer buffer, char seq, int timestamp, int ssrc, ByteBuffer encodedAudio) {
        this.seq = seq;
        this.ssrc = ssrc;
        this.timestamp = timestamp;
        this.csrc = new int[0];
        this.extension = 0;
        this.hasExtension = false;
        this.headerLength = RTP_HEADER_BYTE_LENGTH;
        this.type = RTP_PAYLOAD_TYPE;
        this.rawPacket = generateRawPacket(buffer, seq, timestamp, ssrc, encodedAudio);
        this.encodedAudio = encodedAudio;
    }

    public byte[] getHeader() {
        return Arrays.copyOf(rawPacket, headerLength);
    }

    public ByteBuffer getEncodedAudio() {
        return encodedAudio;
    }

    public char getSequence() {
        return seq;
    }

    public int getSSRC() {
        return ssrc;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public ByteBuffer asEncryptedPacket(CryptoAdapter crypto, ByteBuffer buffer) {
        ((Buffer) buffer).clear();
        writeHeader(seq, timestamp, ssrc, buffer);
        buffer = crypto.encrypt(buffer, encodedAudio);
        ((Buffer) buffer).flip();
        return buffer;
    }

    public static AudioPacket decryptAudioPacket(CryptoAdapter crypto, DatagramPacket packet) {
        AudioPacket encryptedPacket = new AudioPacket(packet);
        if (encryptedPacket.type != RTP_PAYLOAD_TYPE) {
            return null;
        }

        byte[] decryptedPayload = crypto.decrypt(encryptedPacket.encodedAudio);
        int offset = 4 * encryptedPacket.extension;

        return new AudioPacket(
                null,
                encryptedPacket.seq,
                encryptedPacket.timestamp,
                encryptedPacket.ssrc,
                ByteBuffer.wrap(decryptedPayload, offset, decryptedPayload.length - offset)
                        .slice());
    }

    private static byte[] generateRawPacket(ByteBuffer buffer, char seq, int timestamp, int ssrc, ByteBuffer data) {
        if (buffer == null) {
            buffer = ByteBuffer.allocate(RTP_HEADER_BYTE_LENGTH + data.remaining());
        }
        populateBuffer(seq, timestamp, ssrc, data, buffer);
        return buffer.array();
    }

    private static void writeHeader(char seq, int timestamp, int ssrc, ByteBuffer buffer) {
        buffer.put(RTP_VERSION_PAD_EXTEND);
        buffer.put(RTP_PAYLOAD_TYPE);
        buffer.putChar(seq);
        buffer.putInt(timestamp);
        buffer.putInt(ssrc);
    }

    private static void populateBuffer(char seq, int timestamp, int ssrc, ByteBuffer data, ByteBuffer buffer) {
        writeHeader(seq, timestamp, ssrc, buffer);
        buffer.put(data);
        ((Buffer) data).flip();
    }
}
