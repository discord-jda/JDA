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

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public interface DaveSession {
    int getMaxProtocolVersion();

    int getMaxEncryptedFrameSize(@Nonnull MediaType type, int frameSize);

    int getMaxDecryptedFrameSize(@Nonnull MediaType type, long userId, int frameSize);

    void assignSsrcToCodec(@Nonnull Codec codec, int ssrc);

    void encryptOpus(int ssrc, @Nonnull ByteBuffer audio, @Nonnull ByteBuffer encrypted);

    void decryptOpus(long userId, @Nonnull ByteBuffer encrypted, @Nonnull ByteBuffer decrypted);

    void addUser(long userId);

    void removeUser(long userId);

    void initialize();

    void destroy();

    // Protocol communication

    // Opcode SELECT_PROTOCOL_ACK (4)
    void onSelectProtocolAck(int protocolVersion);

    // Opcode DAVE_PROTOCOL_PREPARE_TRANSITION (21)
    void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion);

    // Opcode DAVE_PROTOCOL_EXECUTE_TRANSITION (22)
    void onDaveProtocolExecuteTransition(int transitionId);

    // Opcode DAVE_PROTOCOL_PREPARE_EPOCH (24)
    void onDaveProtocolPrepareEpoch(@Nonnull String epoch, int protocolVersion);

    // Opcode MLS_EXTERNAL_SENDER_PACKAGE (25)
    void onDaveProtocolMLSExternalSenderPackage(@Nonnull ByteBuffer externalSenderPackage);

    // Opcode MLS_PROPOSALS (27)
    void onMLSProposals(@Nonnull ByteBuffer proposals);

    // Opcode MLS_PREPARE_COMMIT_TRANSITION (29)
    void onMLSPrepareCommitTransition(int transitionId, @Nonnull ByteBuffer commit);

    // Opcode MLS_WELCOME (30)
    void onMLSWelcome(int transitionId, @Nonnull ByteBuffer welcome);

    enum MediaType {
        AUDIO,
    }

    enum Codec {
        OPUS,
    }
}
