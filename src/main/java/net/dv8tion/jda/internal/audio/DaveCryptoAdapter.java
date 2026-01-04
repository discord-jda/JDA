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
import net.dv8tion.jda.internal.utils.IOUtil;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

public class DaveCryptoAdapter implements CryptoAdapter {
    protected final CryptoAdapter transportCryptoAdapter;
    protected final DaveSession daveSession;
    protected final int ssrc;

    protected ByteBuffer encryptBuffer = ByteBuffer.allocateDirect(512);

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
    public ByteBuffer encrypt(ByteBuffer output, ByteBuffer audio) {
        int maxSize = daveSession.getMaxEncryptedFrameSize(DaveSession.MediaType.AUDIO, audio.remaining());

        if (encryptBuffer.capacity() < maxSize) {
            encryptBuffer = IOUtil.allocateLike(encryptBuffer, (int) (1.25 * maxSize));
        }

        output.mark();
        encryptBuffer.clear();

        if (daveSession.encrypt(DaveSession.MediaType.AUDIO, ssrc, audio, encryptBuffer)) {
            return transportCryptoAdapter.encrypt(output, encryptBuffer);
        } else {
            output.reset();
            return output;
        }
    }

    @Nullable
    @Override
    public ByteBuffer decrypt(short extensionLength, long userId, ByteBuffer packet, ByteBuffer decrypted) {
        decrypted = transportCryptoAdapter.decrypt(extensionLength, userId, packet, decrypted);
        if (decrypted == null) {
            return null;
        }

        handleRTPHeaderExtension(decrypted, extensionLength);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(decrypted.remaining());
        inputBuffer.put(decrypted);
        inputBuffer.flip();

        int outputSize =
                daveSession.getMaxDecryptedFrameSize(DaveSession.MediaType.AUDIO, userId, inputBuffer.remaining());

        if (decrypted.capacity() < outputSize) {
            decrypted = IOUtil.allocateLike(decrypted, outputSize);
        } else {
            decrypted.clear();
        }

        boolean success = daveSession.decrypt(DaveSession.MediaType.AUDIO, userId, inputBuffer, decrypted);

        if (success) {
            return decrypted;
        } else {
            return null;
        }
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
