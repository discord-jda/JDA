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

import com.google.crypto.tink.aead.internal.InsecureNonceXChaCha20Poly1305;
import com.iwebpp.crypto.TweetNaclFast;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.EnumSet;

public interface CryptoAdapter
{
    String AES_GCM_NO_PADDING = "AES_256/GCM/NOPADDING";

    AudioEncryption getMode();

    boolean encryptHeader();

    ByteBuffer encrypt(ByteBuffer output, ByteBuffer audio);

    byte[] decrypt(byte[] data, int offset, int length,
                   byte[] nonce);

    static AudioEncryption negotiate(EnumSet<AudioEncryption> supportedModes)
    {
        for (AudioEncryption mode : AudioEncryption.values())
        {
            if (supportedModes.contains(mode) && isModeSupported(mode))
                return mode;
        }

        return null;
    }

    static boolean isModeSupported(AudioEncryption mode)
    {
        switch (mode)
        {
        case AEAD_AES256_GCM_RTPSIZE:
            return Security.getAlgorithms("Cipher").contains(AES_GCM_NO_PADDING);
        case AEAD_XCHACHA20_POLY1305_RTPSIZE:
        case XSALSA20_POLY1305_SUFFIX:
        case XSALSA20_POLY1305:
            return true;
        default:
            return false;
        }
    }

    static CryptoAdapter getAdapter(AudioEncryption mode, byte[] secretKey)
    {
        switch (mode)
        {
        case AEAD_AES256_GCM_RTPSIZE:
            return new CryptoAdapter.AES_GCM_Adapter(secretKey);
        case AEAD_XCHACHA20_POLY1305_RTPSIZE:
            return new ChaCha20Poly1305Adapter(secretKey);
        case XSALSA20_POLY1305_SUFFIX:
        case XSALSA20_POLY1305:
            return new CryptoAdapter.SecretBoxAdapter(mode, secretKey);
        default:
            throw new IllegalStateException("Unsupported encryption mode: " + mode);
        }
    }

    class SecretBoxAdapter implements CryptoAdapter
    {
        private final AudioEncryption mode;
        private final TweetNaclFast.SecretBox boxer;
        private final byte[] secretKey;

        public SecretBoxAdapter(AudioEncryption mode, byte[] secretKey)
        {
            this.mode = mode;
            this.boxer = new TweetNaclFast.SecretBox(secretKey);
            this.secretKey = secretKey;
        }

        @Override
        public AudioEncryption getMode()
        {
            return mode;
        }

        @Override
        public boolean encryptHeader()
        {
            return false;
        }

        @Override
        public ByteBuffer encrypt(ByteBuffer header, ByteBuffer audio)
        {
            //FIXME nonce handling for each mode
            return ByteBuffer.wrap(boxer.box(audio.array(), audio.arrayOffset(), audio.remaining(), null));
        }

        @Override
        public byte[] decrypt(byte[] data, int offset, int length, byte[] nonce)
        {
            TweetNaclFast.SecretBox opener = new TweetNaclFast.SecretBox(secretKey);
            return opener.open(data, offset, length, nonce);
        }
    }

    class AES_GCM_Adapter implements CryptoAdapter
    {
        private static final SecureRandom random = new SecureRandom();
        private final byte[] secretKey;
        private int encryptCounter;

        public AES_GCM_Adapter(byte[] secretKey)
        {
            this.secretKey = secretKey;
            this.encryptCounter = Math.abs(random.nextInt()) % 513 + 1;
        }

        @Override
        public AudioEncryption getMode()
        {
            return AudioEncryption.AEAD_AES256_GCM_RTPSIZE;
        }

        @Override
        public boolean encryptHeader()
        {
            return true;
        }

        @Override
        public ByteBuffer encrypt(ByteBuffer output, ByteBuffer audio)
        {
            int minimumOutputSize = audio.remaining() + 20; // TAG (16) + NONCE (4)

            if (output.remaining() < minimumOutputSize)
            {
                ByteBuffer newBuffer = ByteBuffer.allocate(output.capacity() + minimumOutputSize);
                output.flip();
                newBuffer.put(output);
                output = newBuffer;
            }

            byte[] iv = new byte[12];
            IOUtil.setIntBigEndian(iv, 0, encryptCounter);

            try
            {
                Cipher cipher = getCipher(iv);
                cipher.updateAAD(output.array(), 0, output.position());
                cipher.doFinal(audio, output);
                output.putInt(encryptCounter++);
                return output;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] decrypt(byte[] data, int offset, int length, byte[] nonce)
        {
            // TODO
            return new byte[0];
        }

        private Cipher getCipher(byte[] iv)
        {
            try
            {
                Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey, "AES"), new GCMParameterSpec(128, iv));
                return cipher;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    class ChaCha20Poly1305Adapter implements CryptoAdapter
    {
        private static final SecureRandom random = new SecureRandom();
        private final byte[] secretKey;
        private int encryptCounter;

        public ChaCha20Poly1305Adapter(byte[] secretKey)
        {
            this.secretKey = secretKey;
            this.encryptCounter = Math.abs(random.nextInt()) % 513 + 1;
        }

        @Override
        public AudioEncryption getMode()
        {
            return AudioEncryption.AEAD_XCHACHA20_POLY1305_RTPSIZE;
        }

        @Override
        public boolean encryptHeader()
        {
            return true;
        }

        @Override
        public ByteBuffer encrypt(ByteBuffer output, ByteBuffer audio)
        {
            int minimumOutputSize = audio.remaining() + 20; // TAG (16) + NONCE (4)

            if (output.remaining() < minimumOutputSize)
            {
                ByteBuffer newBuffer = ByteBuffer.allocate(output.capacity() + minimumOutputSize);
                output.flip();
                newBuffer.put(output);
                output = newBuffer;
            }

            byte[] iv = new byte[24];
            IOUtil.setIntBigEndian(iv, 0, encryptCounter);

            try
            {
                InsecureNonceXChaCha20Poly1305 xChaCha20Poly1305 = new InsecureNonceXChaCha20Poly1305(secretKey);

                byte[] input = Arrays.copyOfRange(audio.array(), audio.arrayOffset() + audio.position(), audio.arrayOffset() + audio.limit());
                byte[] additionalData = Arrays.copyOfRange(output.array(), output.arrayOffset(), output.arrayOffset() + output.position());

                byte[] encrypted = xChaCha20Poly1305.encrypt(
                    iv,
                    input,
                    additionalData
                );

                output.put(encrypted);
                output.putInt(encryptCounter++);
                return output;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] decrypt(byte[] data, int offset, int length, byte[] nonce)
        {
            // TODO
            return new byte[0];
        }
    }
}
