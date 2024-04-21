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
package net.dv8tion.jda.api.events.self

import net.dv8tion.jda.api.JDA
import javax.annotation.Nonnull

/**
 * Indicates that the mfa level of the current user changed.
 * <br></br>This is relevant for elevated permissions (guild moderating/managing).
 *
 *
 * Can be used to retrieve the old mfa level.
 *
 *
 * Identifier: `mfa_enabled`
 */
class SelfUpdateMFAEvent(@Nonnull api: JDA, responseNumber: Long, wasMfaEnabled: Boolean) :
    GenericSelfUpdateEvent<Boolean?>(api, responseNumber, wasMfaEnabled, !wasMfaEnabled, IDENTIFIER) {
    /**
     * Whether MFA was previously enabled or not
     *
     * @return True, if the account had MFA enabled prior to this event
     */
    fun wasMfaEnabled(): Boolean {
        return oldValue
    }

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "mfa_enabled"
    }
}
