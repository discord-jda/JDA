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
 * Indicates that the verification state of the current user changed. (client-only)
 *
 *
 * Can be used to retrieve the old verification state.
 *
 *
 * Identifier: `verified`
 */
class SelfUpdateVerifiedEvent(@Nonnull api: JDA, responseNumber: Long, wasVerified: Boolean) :
    GenericSelfUpdateEvent<Boolean?>(api, responseNumber, wasVerified, !wasVerified, IDENTIFIER) {
    /**
     * Whether the account was verified
     *
     * @return True, if this account was previously verified
     */
    fun wasVerified(): Boolean {
        return oldValue
    }

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "verified"
    }
}
