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
 * Implementation for the <a href="https://daveprotocol.com" target="_blank">Discord Audio &amp; Video End-to-End Encryption (DAVE) Protocol</a>.
 *
 * @see <a href="https://daveprotocol.com" target="_blank">Discord Audio &amp; Video Encryption (DAVE)</a>
 * @see <a href="https://github.com/discord/libdave" target="_blank">discord/libdave on GitHub</a>
 */
public interface DaveSession {
    /**
     * The maximum supported version of the DAVE protocol.
     *
     * @return Maximum supported version
     */
    int getMaxProtocolVersion();

    /**
     * Calculate the maximum encrypted size for the provided frame.
     *
     * <p>This is used to ensure the {@link ByteBuffer} provided to {@link #encrypt(MediaType, int, ByteBuffer, ByteBuffer)}
     * has enough space for the encrypted data.
     *
     * @param type
     *        The {@link MediaType} of the frame
     * @param frameSize
     *        The size of the encoded frame
     *
     * @return The maximum number of bytes an encrypted frame will need
     */
    int getMaxEncryptedFrameSize(@Nonnull MediaType type, int frameSize);

    /**
     * Calculate the maximum decrypted size for the provided frame.
     *
     * <p>This is used to ensure the {@link ByteBuffer} provided to {@link #decrypt(MediaType, long, ByteBuffer, ByteBuffer)}
     * has enough space for the decrypted data.
     *
     * @param type
     *        The {@link MediaType} of the frame
     * @param userId
     *        The id for the user that sent this frame
     * @param frameSize
     *        The size of the encrypted frame
     *
     * @return The maximum number of bytes a decrypted frame will need
     */
    int getMaxDecryptedFrameSize(@Nonnull MediaType type, long userId, int frameSize);

    /**
     * Associates a media {@link Codec} with a given SSRC (synchronization source).
     *
     * <p>This mapping is used by the implementation to select the correct codec for
     * incoming or outgoing media identified by the specified {@code ssrc}.
     *
     * @param codec
     *         The codec that should be used for the given SSRC
     * @param ssrc
     *         The SSRC value provided by the voice gateway
     */
    void assignSsrcToCodec(@Nonnull Codec codec, int ssrc);

    /**
     * Encrypts the plaintext frame and writes the encrypted data into {@code encrypted}.
     *
     * <p>Implementations should read the {@linkplain ByteBuffer#remaining() remaining} bytes
     * from {@code data} starting at its current {@linkplain ByteBuffer#position() position},
     * and write the resulting encrypted payload into {@code encrypted} starting at its current position.
     *
     * <p>The caller is responsible for ensuring that {@code encrypted} has sufficient
     * remaining capacity to receive the encrypted frame, which can be determined via
     * {@link #getMaxEncryptedFrameSize(MediaType, int)}.
     *
     * <p>After successfully encrypting the data, the {@code encrypted} buffer should have its
     * {@linkplain ByteBuffer#position() position} at the beginning of the encrypted data and its
     * {@linkplain ByteBuffer#limit() limit} at the end of the encrypted data.
     * (Usually achieved by calling {@link ByteBuffer#flip()})
     *
     * @param mediaType
     *        The media type of the {@code data}
     * @param ssrc
     *        The SSRC (synchronization source) of the sender
     * @param data
     *        The direct buffer containing the data to encrypt
     * @param encrypted
     *        The direct buffer to fill with the encrypted data
     *        (with enough space for {@linkplain #getMaxEncryptedFrameSize(MediaType, int) max encrypted frame size}).
     *
     * @return True, if the encryption was successful
     */
    boolean encrypt(@Nonnull MediaType mediaType, int ssrc, @Nonnull ByteBuffer data, @Nonnull ByteBuffer encrypted);

    /**
     * Decrypts an Opus-encoded audio frame received from the network.
     *
     * <p>Implementations should read the {@linkplain ByteBuffer#remaining() remaining} bytes
     * from {@code encrypted} starting at its current {@linkplain ByteBuffer#position() position},
     * and write the resulting decrypted payload into {@code decrypted} starting at its current position.
     *
     * <p>The caller is responsible for ensuring that {@code decrypted} has sufficient
     * remaining capacity to receive the decrypted frame, which can be determined via
     * {@link #getMaxDecryptedFrameSize(MediaType, long, int)}.
     *
     * <p>After successfully decrypting the data, the {@code decrypted} buffer should have its
     * {@linkplain ByteBuffer#position() position} at the beginning of the decrypted data and its
     * {@linkplain ByteBuffer#limit() limit} at the end of the decrypted data.
     * (Usually achieved by calling {@link ByteBuffer#flip()})
     *
     * @param mediaType
     *        The media type of the {@code encrypted} data
     * @param userId
     *        The id for the user that sent this frame
     * @param encrypted
     *        The direct buffer containing encrypted data
     * @param decrypted
     *        The direct buffer to fill with the decrypted data
     *        (with enough space for {@linkplain #getMaxDecryptedFrameSize(MediaType, long, int) max decrypted frame size})
     *
     * @return True, if the decryption was successful
     */
    boolean decrypt(
            @Nonnull MediaType mediaType, long userId, @Nonnull ByteBuffer encrypted, @Nonnull ByteBuffer decrypted);

    /**
     * Add a new recognized user for the MLS group.
     *
     * @param userId
     *        The user id of the new member
     *
     * @see <a href="https://daveprotocol.com/#clients_connect-11" target="_blank">clients_connect (11)</a>
     */
    void addUser(long userId);

    /**
     * Remove a user from the MLS group.
     *
     * @param userId
     *        The user id to remove
     *
     * @see <a href="https://daveprotocol.com/#client_disconnect-13" target="_blank">client_disconnect (13)</a>
     */
    void removeUser(long userId);

    /**
     * Called when the connection is established.
     *
     * <p>Can be used to track timeouts before the first call to {@link #onSelectProtocolAck(int)}.
     */
    void initialize();

    /**
     * Called when the session is no longer needed and all related resources should be cleaned up.
     */
    void destroy();

    // Protocol communication

    /**
     * Handle {@code SELECT_PROTOCOL_ACK}/{@code SESSION_DESCRIPTION} (opcode {@code 4}).
     *
     * @param protocolVersion
     *        The protocol version to transition to
     *
     * @see <a href="https://daveprotocol.com/#select_protocol_ack-4" target="_blank">select_protocol_ack (4)</a>
     */
    void onSelectProtocolAck(int protocolVersion);

    /**
     * Handle {@code DAVE_PROTOCOL_PREPARE_TRANSITION} (opcode {@code 21}).
     *
     * <p>Sent when a protocol transition should be prepared.
     * Followed by {@link #onDaveProtocolExecuteTransition(int)}
     * once all group members are {@linkplain DaveProtocolCallbacks#sendDaveProtocolReadyForTransition(int) ready for transition}
     *
     * @param transitionId
     *        The transition id, to be referenced later by {@link #onDaveProtocolExecuteTransition(int)}
     * @param protocolVersion
     *        The target protocol version to transition to
     *
     * @see <a href="https://daveprotocol.com/#dave_protocol_prepare_transition-21" target="_blank">dave_protocol_prepare_transition (21)</a>
     */
    void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion);

    /**
     * Handle {@code DAVE_PROTOCOL_EXECUTE_TRANSITION} (opcode {@code 22}).
     *
     * <p>Sent when a transition should be executed.
     * This is usually preceded by {@link #onDaveProtocolPrepareTransition(int, int)}.
     *
     * @param transitionId
     *        The transition id, previously registered by {@link #onDaveProtocolPrepareTransition(int, int)}
     *
     * @see <a href="https://daveprotocol.com/#dave_protocol_execute_transition-22" target="_blank">dave_protocol_execute_transition (22)</a>
     */
    void onDaveProtocolExecuteTransition(int transitionId);

    /**
     * Handle {@code DAVE_PROTOCOL_PREPARE_EPOCH} (opcode {@code 24}).
     *
     * <p>Sent from the server to clients to announce upcoming protocol version changes.
     * When the epoch ID is equal to 1, this message indicates that a new MLS group is to be created for the given protocol version.
     *
     * @param epoch
     *        The epoch used by the session (equal to {@code 1} to indicate a new MLS group should be created)
     * @param protocolVersion
     *        The protocol version for the new MLS group
     *
     * @see <a href="https://daveprotocol.com/#dave_protocol_prepare_epoch-24" target="_blank">dave_protocol_prepare_epoch (24)</a>
     */
    void onDaveProtocolPrepareEpoch(long epoch, int protocolVersion);

    /**
     * Handle {@code MLS_EXTERNAL_SENDER_PACKAGE} (opcode {@code 25}).
     *
     * <p>Includes the basic credential and public key
     * for the voice gateway external sender to be added to the MLS group's extensions.
     *
     * @param externalSenderPackage
     *        Binary representation for the external sender package
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_external_sender_package-25" target="_blank">dave_mls_external_sender_package (25)</a>
     */
    void onDaveProtocolMLSExternalSenderPackage(@Nonnull ByteBuffer externalSenderPackage);

    /**
     * Handle {@code MLS_PROPOSALS} (opcode {@code 27}).
     *
     * <p>Includes a list of proposals to be appended or a list of proposal refs to be revoked.
     *
     * @param proposals
     *        Binary representation for the proposals
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_proposals-27" target="_blank">dave_mls_proposals (27)</a>
     */
    void onMLSProposals(@Nonnull ByteBuffer proposals);

    /**
     * Handle {@code MLS_PREPARE_COMMIT_TRANSITION} (opcode {@code 29}).
     *
     * <p>Includes the MLS commit being broadcast to move to the next epoch.
     *
     * @param transitionId
     *        The transition id for the commit transition
     * @param commit
     *        Binary representation of the commit message
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_announce_commit_transition-29" target="_blank">dave_mls_announce_commit_transition (29)</a>
     */
    void onMLSPrepareCommitTransition(int transitionId, @Nonnull ByteBuffer commit);

    /**
     * Handle {@code MLS_WELCOME} (opcode {@code 30}).
     *
     * <p>Includes the MLS Welcome which adds the pending member to the group
     *
     * @param transitionId
     *        The transition id for the welcome transition
     * @param welcome
     *        Binary representation of the welcome message
     *
     * @see <a href="https://daveprotocol.com/#dave_mls_welcome-30" target="_blank">dave_mls_welcome (30)</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9420.html#section-12.4.3.1">Joining via Welcome Message</a>
     */
    void onMLSWelcome(int transitionId, @Nonnull ByteBuffer welcome);

    /**
     * MediaType used by transmissions
     */
    enum MediaType {
        AUDIO,
    }

    /**
     * Codec used for encoded frames
     */
    enum Codec {
        OPUS,
    }
}
