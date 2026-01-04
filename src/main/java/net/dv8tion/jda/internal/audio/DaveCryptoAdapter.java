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
import net.dv8tion.jda.internal.utils.ResizingByteBuffer;

import java.nio.ByteBuffer;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class DaveCryptoAdapter implements CryptoAdapter {
    protected final CryptoAdapter transportCryptoAdapter;
    protected final DaveSession daveSession;
    protected final int ssrc;

    protected ResizingByteBuffer encryptBuffer = new ResizingByteBuffer(ByteBuffer.allocateDirect(512));
    protected ResizingByteBuffer decryptBuffer = null;

    public DaveCryptoAdapter(CryptoAdapter transportCryptoAdapter, DaveSession daveSession, int ssrc) {
        this.transportCryptoAdapter = transportCryptoAdapter;
        this.daveSession = daveSession;
        this.ssrc = ssrc;
    }

    @Override
    public AudioEncryption getMode() {
        return transportCryptoAdapter.getMode();
    }

    @Override
    public void encrypt(ResizingByteBuffer output, ByteBuffer audio) {
        int maxSize = daveSession.getMaxEncryptedFrameSize(DaveSession.MediaType.AUDIO, audio.remaining());

        output.buffer().mark();
        encryptBuffer.prepareWrite(maxSize);

        if (daveSession.encrypt(DaveSession.MediaType.AUDIO, ssrc, audio, encryptBuffer.buffer())) {
            transportCryptoAdapter.encrypt(output, encryptBuffer.buffer());
        } else {
            throw new IllegalStateException("Failed to encrypt audio");
        }
    }

    @Override
    public boolean decrypt(short extensionLength, long userId, ByteBuffer packet, ResizingByteBuffer decrypted) {
        if (decryptBuffer == null) {
            decryptBuffer = new ResizingByteBuffer(ByteBuffer.allocateDirect(1024));
        }

        boolean success = transportCryptoAdapter.decrypt(extensionLength, userId, packet, decryptBuffer);
        if (!success) {
            return false;
        }

        handleRTPHeaderExtension(decryptBuffer.buffer(), extensionLength);

        int outputSize = daveSession.getMaxDecryptedFrameSize(
                DaveSession.MediaType.AUDIO, userId, decryptBuffer.buffer().remaining());

        decrypted.prepareWrite(outputSize);
        return daveSession.decrypt(DaveSession.MediaType.AUDIO, userId, decryptBuffer.buffer(), decrypted.buffer());
    }

    private void handleRTPHeaderExtension(ByteBuffer decrypted, short extensionLength) {
        if (extensionLength == 0) {
            return;
        }

        int length = ((int) extensionLength) & 0xFFFF;
        int position = decrypted.position();
        int offset = position + 4 * length;
        decrypted.position(Math.min(offset, decrypted.limit() - 1));
    }
}
