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

/**
 * Interface used to send messages to the voice gateway to facilitate a {@link DaveSession}.
 *
 * @see DaveSessionFactory
 * @see DaveSession
 */
public interface DaveProtocolCallbacks {
    /**
     * Send {@code MLS_KEY_PACKAGE} (opcode {@code 26}).
     *
     * @param mlsKeyPackage
     *        The key package messages as binary representation
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_key_package-26" target="_blank">dave_mls_key_package (26)</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9420.html#section-10" target="_blank">RFC 9420 - 10. Key Packages</a>
     */
    void sendMLSKeyPackage(@Nonnull ByteBuffer mlsKeyPackage);

    /**
     * Send {@code DAVE_PROTOCOL_READY_FOR_TRANSITION} (opcode {@code 23}).
     *
     * <p>This is used to signal to the rest of the MLS group that a transition to a new protocol version is prepared.
     * Usually in response to {@link DaveSession#onDaveProtocolPrepareTransition(int, int)},
     * {@link DaveSession#onMLSPrepareCommitTransition(int, ByteBuffer)}, or
     * {@link DaveSession#onMLSWelcome(int, ByteBuffer)}.
     *
     * @param transitionId
     *        The transition id that is prepared
     *
     * @see <a href="https://daveprotocol.com/#dave_protocol_ready_for_transition-23" target="_blank">dave_protocol_ready_for_transition (23)</a>
     */
    void sendDaveProtocolReadyForTransition(int transitionId);

    /**
     * Send {@code MLS_COMMIT_WELCOME} (opcode {@code 28}).
     *
     * @param commitWelcomeMessage
     *        The binary representation for Commit and Welcome message
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_commit_welcome-28" target="_blank">dave_mls_commit_welcome (28)</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9420.html#name-commit" target="_blank">RFC 9420 - 12.4. Commit</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9420.html#section-12.4.3.1" target="_blank">RFC 9420 - 12.4.3.1. Joining via Welcome Message</a>
     */
    void sendMLSCommitWelcome(@Nonnull ByteBuffer commitWelcomeMessage);

    /**
     * Send {@code MLS_INVALID_COMMIT_WELCOME} (opcode {@code 31}).
     *
     * <p>This message asks the voice gateway to remove and re-add a member
     * to an MLS group so the member can recover from receiving an unprocessable Commit or Welcome.
     *
     * @param transitionId
     *        The transition id in which the invalid Commit or Welcome was received
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_invalid_commit_welcome-31" target="_blank">dave_mls_invalid_commit_welcome (31)</a>
     */
    void sendMLSInvalidCommitWelcome(int transitionId);
}
