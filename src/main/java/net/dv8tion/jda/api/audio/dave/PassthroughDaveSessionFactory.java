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

package net.dv8tion.jda.api.audio.dave;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public class PassthroughDaveSessionFactory implements DaveSessionFactory {
    private static final Logger LOG = JDALogger.getLog(DaveSessionFactory.class);
    private static boolean isWarningPrinted = false;

    private static void printWarning() {
        if (isWarningPrinted) {
            return;
        }

        isWarningPrinted = true;
        LOG.warn(
                "Using passthrough dave session. Please migrate to an implementation of libdave! "
                        + "Your audio connections will stop working on 01.03.2026",
                new IllegalStateException());
    }

    @Nonnull
    @Override
    public DaveSession createDaveSession(@Nonnull DaveProtocolCallbacks callbacks, long userId, long channelId) {
        printWarning();
        return new PassthroughDaveSession();
    }

    private static class PassthroughDaveSession implements DaveSession {

        @Override
        public int getMaxProtocolVersion() {
            return 0;
        }

        @Override
        public int getMaxEncryptedFrameSize(@Nonnull MediaType type, int frameSize) {
            return frameSize;
        }

        @Override
        public int getMaxDecryptedFrameSize(@Nonnull MediaType type, long userId, int frameSize) {
            return frameSize;
        }

        @Override
        public void assignSsrcToCodec(@Nonnull Codec codec, int ssrc) {}

        @Override
        public void encrypt(int ssrc, @Nonnull ByteBuffer audio, @Nonnull ByteBuffer encrypted) {
            // passthrough
            encrypted.put(audio);
            encrypted.flip();
        }

        @Override
        public void decrypt(long userId, @Nonnull ByteBuffer encrypted, @Nonnull ByteBuffer decrypted) {
            // passthrough
            decrypted.put(encrypted);
            decrypted.flip();
        }

        @Override
        public void addUser(long userId) {
            // passthrough
        }

        @Override
        public void removeUser(long userId) {
            // passthrough
        }

        @Override
        public void initialize() {}

        @Override
        public void destroy() {}

        @Override
        public void onSelectProtocolAck(int protocolVersion) {}

        @Override
        public void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion) {}

        @Override
        public void onDaveProtocolExecuteTransition(int transitionId) {}

        @Override
        public void onDaveProtocolPrepareEpoch(@Nonnull String epoch, int protocolVersion) {}

        @Override
        public void onDaveProtocolMLSExternalSenderPackage(@Nonnull ByteBuffer externalSenderPackage) {}

        @Override
        public void onMLSProposals(@Nonnull ByteBuffer proposals) {}

        @Override
        public void onMLSPrepareCommitTransition(int transitionId, @Nonnull ByteBuffer commit) {}

        @Override
        public void onMLSWelcome(int transitionId, @Nonnull ByteBuffer welcome) {}
    }
}
