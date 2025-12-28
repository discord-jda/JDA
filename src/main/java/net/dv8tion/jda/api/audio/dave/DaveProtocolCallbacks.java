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

public interface DaveProtocolCallbacks {
    // Opcode MLS_KEY_PACKAGE (26)
    void sendMLSKeyPackage(@Nonnull ByteBuffer mlsKeyPackage);

    // Opcode DAVE_PROTOCOL_READY_FOR_TRANSITION (23)
    void sendDaveProtocolReadyForTransition(int transitionId);

    // Opcode MLS_COMMIT_WELCOME (28)
    void sendMLSCommitWelcome(@Nonnull ByteBuffer commitWelcomeMessage);

    // Opcode MLS_INVALID_COMMIT_WELCOME (31)
    void sendMLSInvalidCommitWelcome(int transitionId);
}
