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

import net.dv8tion.jda.api.audio.dave.DaveSession;

import java.nio.ByteBuffer;

public class DaveCryptoAdapter implements CryptoAdapter {
    protected final CryptoAdapter delegate;
    protected final DaveSession daveSession;
    protected final int ssrc;

    protected ByteBuffer encryptBuffer = ByteBuffer.allocate(1024);

    public DaveCryptoAdapter(CryptoAdapter delegate, DaveSession daveSession, int ssrc) {
        this.delegate = delegate;
        this.daveSession = daveSession;
        this.ssrc = ssrc;
    }

    @Override
    public AudioEncryption getMode() {
        return delegate.getMode();
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer output, ByteBuffer audio) {
        encryptBuffer.clear();
        output.flip();
        encryptBuffer.put(output);
        output.limit(output.capacity());

        encryptBuffer = delegate.encrypt(encryptBuffer, audio);
        encryptBuffer.position(output.position());

        int maxSize = daveSession.getMaxEncryptedFrameSize(DaveSession.MediaType.AUDIO, encryptBuffer.remaining());
        if (maxSize > output.remaining()) {
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(output.position() + (int) (1.25 * maxSize));
            output.flip();
            newBuffer.put(output);
            output = newBuffer;
        }

        daveSession.encryptOpus(ssrc, encryptBuffer, output);
        return output;
    }

    @Override
    public byte[] decrypt(long userId, ByteBuffer packet) {
        int headerLength = packet.position();
        int audioFrameSize = packet.remaining();
        int decryptedSize = daveSession.getMaxDecryptedFrameSize(DaveSession.MediaType.AUDIO, userId, audioFrameSize);

        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(headerLength + decryptedSize);
        packet.flip();
        outputBuffer.put(packet);

        packet.position(headerLength);
        packet.limit(headerLength + audioFrameSize);

        daveSession.decryptOpus(userId, packet, outputBuffer);
        outputBuffer.position(headerLength);

        return delegate.decrypt(userId, outputBuffer);
    }
}
