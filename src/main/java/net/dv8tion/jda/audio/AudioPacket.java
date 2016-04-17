/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.audio;

import com.iwebpp.crypto.TweetNaclFast;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents the contents of a audio packet that was either received from Discord or
 * will be sent to discord.
 */
public class AudioPacket
{
    public static final int RTP_HEADER_BYTE_LENGTH = 12;
    public static final int XSALSA20_NONCE_LENGTH = 24;

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

    public static final int RTP_VERSION_PAD_EXTEND_INDEX =  0;
    public static final int RTP_PAYLOAD_INDEX =             1;
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

        byte[] audio = new byte[buffer.array().length - RTP_HEADER_BYTE_LENGTH];
        System.arraycopy(buffer.array(), RTP_HEADER_BYTE_LENGTH, audio, 0, audio.length);
        this.encodedAudio = audio;
    }

    public AudioPacket(char seq, int timestamp, int ssrc, byte[] encodedAudio)
    {
        this.seq = seq;
        this.ssrc = ssrc;
        this.timestamp = timestamp;
        this.encodedAudio = encodedAudio;

        ByteBuffer buffer = ByteBuffer.allocate(RTP_HEADER_BYTE_LENGTH + encodedAudio.length);
        buffer.put(RTP_VERSION_PAD_EXTEND_INDEX, RTP_VERSION_PAD_EXTEND);   //0
        buffer.put(RTP_PAYLOAD_INDEX, RTP_PAYLOAD_TYPE);                    //1
        buffer.putChar(SEQ_INDEX, seq);                                     //2 - 3
        buffer.putInt(TIMESTAMP_INDEX, timestamp);                          //4 - 7
        buffer.putInt(SSRC_INDEX, ssrc);                                    //8 - 11
        System.arraycopy(encodedAudio, 0, buffer.array(), RTP_HEADER_BYTE_LENGTH, encodedAudio.length);//12 - n
        this.rawPacket = buffer.array();

    }

    public byte[] getNonce()
    {
        //The first 12 bytes are the rawPacket are the RTP Discord Nonce.
        return Arrays.copyOf(rawPacket, RTP_HEADER_BYTE_LENGTH);
    }

    public byte[] getRawPacket()
    {
        return Arrays.copyOf(rawPacket, rawPacket.length);
    }

    public byte[] getEncodedAudio()
    {
        return Arrays.copyOf(encodedAudio, encodedAudio.length);
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

    public DatagramPacket asUdpPacket(InetSocketAddress address)
    {
        //We use getRawPacket() instead of the rawPacket variable so that we get a copy of the array instead of the
        //actual array. We want AudioPacket to be immutable.
        return new DatagramPacket(getRawPacket(), rawPacket.length, address);
    }

    public DatagramPacket asEncryptedUdpPacket(InetSocketAddress address, byte[] secretKey)
    {
        //Xsalsa20's Nonce is 24 bytes long, however RTP (and consequently Discord)'s nonce is
        // only 12 bytes long, so we need to create a 24 byte array, and copy the 12 byte nonce into it.
        // we will leave the extra bytes as nulls. (Java sets non-populated bytes as 0).
        byte[] extendedNonce = new byte[XSALSA20_NONCE_LENGTH];

        //Copy the RTP nonce into our Xsalsa20 nonce array.
        // Note, it doesn't fill the Xsalsa20 nonce array completely.
        System.arraycopy(getNonce(), 0, extendedNonce, 0, RTP_HEADER_BYTE_LENGTH);

        //Create our SecretBox encoder with the secretKey provided by Discord.
        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
        byte[] encryptedAudio = boxer.box(encodedAudio, extendedNonce);

        //Create a new temp audio packet using the encrypted audio so that we don't
        // need to write extra code to create the rawPacket with the encryptedAudio.
        //Use the temp packet to create a UdpPacket.
        return new AudioPacket(seq, timestamp, ssrc, encryptedAudio).asUdpPacket(address);
    }

    public static AudioPacket createEchoPacket(DatagramPacket packet, int ssrc)
    {
        ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOf(packet.getData(), packet.getLength()));
        buffer.put(RTP_VERSION_PAD_EXTEND_INDEX, RTP_VERSION_PAD_EXTEND);
        buffer.put(RTP_PAYLOAD_INDEX, RTP_PAYLOAD_TYPE);
        buffer.putInt(SSRC_INDEX, ssrc);
        return new AudioPacket(buffer.array());
    }

    public static AudioPacket decryptAudioPacket(DatagramPacket packet, byte[] secretKey)
    {
        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
        AudioPacket encryptedPacket = new AudioPacket(packet);

        byte[] extendedNonce = new byte[XSALSA20_NONCE_LENGTH];
        System.arraycopy(encryptedPacket.getNonce(), 0, extendedNonce, 0, RTP_HEADER_BYTE_LENGTH);

        byte[] decryptedAudio = boxer.open(encryptedPacket.getEncodedAudio(), extendedNonce);
        byte[] decryptedRawPacket = new byte[RTP_HEADER_BYTE_LENGTH + decryptedAudio.length];

        System.arraycopy(encryptedPacket.getNonce(), 0, decryptedRawPacket, 0, RTP_HEADER_BYTE_LENGTH);
        System.arraycopy(decryptedAudio, 0, decryptedRawPacket, RTP_HEADER_BYTE_LENGTH, decryptedAudio.length);

        return new AudioPacket(decryptedRawPacket);
    }
}
