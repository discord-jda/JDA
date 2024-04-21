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

/**
 * Indicates that the [global name][User.getGlobalName] of the current user changed.
 *
 *
 * Can be used to retrieve the old global name.
 *
 *
 * Identifier: `global_name`
 */
class SelfUpdateGlobalNameEvent(api: JDA, responseNumber: Long, oldName: String?) :
    GenericSelfUpdateEvent<String?>(api, responseNumber, oldName, api.getSelfUser().globalName, IDENTIFIER) {
    val oldGlobalName: String?
        /**
         * The old global name
         *
         * @return The old global name
         */
        get() = oldValue
    val newGlobalName: String?
        /**
         * The new global name
         *
         * @return The new global name
         */
        get() = newValue

    override fun toString(): String {
        return "SelfUpdateGlobalName($oldValue->$newValue)"
    }

    companion object {
        const val IDENTIFIER = "global_name"
    }
}
