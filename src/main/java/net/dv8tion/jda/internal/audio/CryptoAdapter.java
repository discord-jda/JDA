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

import com.google.crypto.tink.aead.internal.InsecureNonceAesGcmJce;
import com.google.crypto.tink.aead.internal.InsecureNonceXChaCha20Poly1305;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.ResizingByteBuffer;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.EnumSet;

public interface CryptoAdapter {
    String AES_GCM_NO_PADDING = "AES_256/GCM/NOPADDING";

    AudioEncryption getMode();

    void encrypt(ResizingByteBuffer output, ByteBuffer audio);

    boolean decrypt(short extensionLength, long userId, ByteBuffer packet, ResizingByteBuffer decrypted);

    static AudioEncryption negotiate(EnumSet<AudioEncryption> supportedModes) {
        for (AudioEncryption mode : AudioEncryption.values()) {
            if (supportedModes.contains(mode) && isModeSupported(mode)) {
                return mode;
            }
        }

        return null;
    }

    static boolean isModeSupported(AudioEncryption mode) {
        switch (mode) {
            case AEAD_AES256_GCM_RTPSIZE:
                return Security.getAlgorithms("Cipher").contains(AES_GCM_NO_PADDING);
            case AEAD_XCHACHA20_POLY1305_RTPSIZE:
                return true;
            default:
                return false;
        }
    }

    static CryptoAdapter getAdapter(AudioEncryption mode, byte[] secretKey) {
        switch (mode) {
            case AEAD_AES256_GCM_RTPSIZE:
                return new CryptoAdapter.AES_GCM_Adapter(secretKey);
            case AEAD_XCHACHA20_POLY1305_RTPSIZE:
                return new XChaCha20Poly1305Adapter(secretKey);
            default:
                throw new IllegalStateException("Unsupported encryption mode: " + mode);
        }
    }

    abstract class AbstractAaedAdapter implements CryptoAdapter {
        protected static final int nonceBytes = 4;
        protected static final SecureRandom random = new SecureRandom();

        protected final byte[] secretKey;
        protected final byte[] nonceBuffer;
        protected final int tagBytes;
        protected final int paddedNonceBytes;
        protected int encryptCounter;

        protected AbstractAaedAdapter(byte[] secretKey, int tagBytes, int paddedNonceBytes) {
            this.secretKey = secretKey;
            this.tagBytes = tagBytes;
            this.paddedNonceBytes = paddedNonceBytes;
            this.nonceBuffer = new byte[paddedNonceBytes];
            this.encryptCounter = Math.abs(random.nextInt()) % 513 + 1;
        }

        @Override
        public void encrypt(ResizingByteBuffer output, ByteBuffer audio) {
            int minimumOutputSize = audio.remaining() + this.tagBytes + nonceBytes;

            output.ensureRemaining(minimumOutputSize);
            IOUtil.setIntBigEndian(nonceBuffer, 0, encryptCounter);

            try {
                encryptInternally(output.buffer(), audio, nonceBuffer);
                output.buffer().putInt(encryptCounter++);
                output.buffer().flip();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean decrypt(short extensionLength, long userId, ByteBuffer packet, ResizingByteBuffer decrypted) {
            try {
                int headerLength = packet.position();
                packet.position(0);
                byte[] associatedData = new byte[headerLength];
                packet.get(associatedData);
                byte[] cipherText = new byte[packet.remaining() - nonceBytes];
                packet.get(cipherText);
                byte[] nonce = new byte[paddedNonceBytes];
                packet.get(nonce, 0, nonceBytes);
                byte[] output = decryptInternally(cipherText, associatedData, nonce);

                decrypted.replace(ByteBuffer.wrap(output));

                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract void encryptInternally(ByteBuffer output, ByteBuffer audio, byte[] nonce) throws Exception;

        protected abstract byte[] decryptInternally(byte[] cipherText, byte[] associatedData, byte[] nonce)
                throws Exception;

        protected byte[] getAssociatedData(ByteBuffer output) {
            byte[] ad = new byte[output.position()];
            output.position(0);
            output.get(ad);
            return ad;
        }

        protected byte[] getPlaintextCopy(ByteBuffer audio) {
            byte[] plaintext = new byte[audio.remaining()];
            audio.get(plaintext);
            return plaintext;
        }
    }

    class AES_GCM_Adapter extends AbstractAaedAdapter implements CryptoAdapter {
        public AES_GCM_Adapter(byte[] secretKey) {
            super(secretKey, 16, 12);
        }

        @Override
        public AudioEncryption getMode() {
            return AudioEncryption.AEAD_AES256_GCM_RTPSIZE;
        }

        @Override
        protected void encryptInternally(ByteBuffer output, ByteBuffer audio, byte[] nonce) throws Exception {
            InsecureNonceAesGcmJce cipher = getCipher();
            byte[] input = getPlaintextCopy(audio);
            byte[] associatedData = getAssociatedData(output);
            output.put(cipher.encrypt(nonce, input, associatedData));
        }

        @Override
        public byte[] decryptInternally(byte[] cipherText, byte[] associatedData, byte[] nonce) throws Exception {
            InsecureNonceAesGcmJce cipher = getCipher();
            return cipher.decrypt(nonce, cipherText, associatedData);
        }

        private InsecureNonceAesGcmJce getCipher() throws GeneralSecurityException {
            return new InsecureNonceAesGcmJce(secretKey);
        }
    }

    class XChaCha20Poly1305Adapter extends AbstractAaedAdapter implements CryptoAdapter {
        public XChaCha20Poly1305Adapter(byte[] secretKey) {
            super(secretKey, 16, 24);
        }

        @Override
        public AudioEncryption getMode() {
            return AudioEncryption.AEAD_XCHACHA20_POLY1305_RTPSIZE;
        }

        @Override
        public void encryptInternally(ByteBuffer output, ByteBuffer audio, byte[] nonce) throws Exception {
            InsecureNonceXChaCha20Poly1305 cipher = getCipher();
            byte[] input = getPlaintextCopy(audio);
            byte[] associatedData = getAssociatedData(output);
            output.put(cipher.encrypt(nonce, input, associatedData));
        }

        @Override
        public byte[] decryptInternally(byte[] cipherText, byte[] associatedData, byte[] nonce) throws Exception {
            InsecureNonceXChaCha20Poly1305 cipher = getCipher();
            return cipher.decrypt(nonce, cipherText, associatedData);
        }

        private InsecureNonceXChaCha20Poly1305 getCipher() throws GeneralSecurityException {
            return new InsecureNonceXChaCha20Poly1305(secretKey);
        }
    }
}
