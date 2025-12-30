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

    protected ByteBuffer encryptBuffer = ByteBuffer.allocateDirect(512);

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
        int maxSize = daveSession.getMaxEncryptedFrameSize(DaveSession.MediaType.AUDIO, audio.remaining());

        if (maxSize > encryptBuffer.capacity()) {
            encryptBuffer = ByteBuffer.allocateDirect((int) (1.25 * maxSize));
        }

        encryptBuffer.clear();
        daveSession.encryptOpus(ssrc, audio, encryptBuffer);

        return delegate.encrypt(output, encryptBuffer);
    }

    @Override
    public byte[] decrypt(long userId, ByteBuffer packet) {
        byte[] decrypted = delegate.decrypt(userId, packet);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(decrypted.length);
        inputBuffer.put(decrypted);
        inputBuffer.flip();

        int outputSize =
                daveSession.getMaxDecryptedFrameSize(DaveSession.MediaType.AUDIO, userId, inputBuffer.remaining());
        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(outputSize);
        daveSession.decryptOpus(userId, inputBuffer, outputBuffer);

        byte[] output = new byte[outputBuffer.remaining()];
        outputBuffer.get(output);
        return output;
    }
}
