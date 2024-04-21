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
package net.dv8tion.jda.api.interactions

import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Enum for interaction types.
 *
 * @see Interaction.getType
 */
enum class InteractionType(val key: Int) {
    UNKNOWN(-1),
    PING(1),
    COMMAND(2),
    COMPONENT(3),
    COMMAND_AUTOCOMPLETE(4),
    MODAL_SUBMIT(5);

    companion object {
        @JvmStatic
        @Nonnull
        @CheckReturnValue
        fun fromKey(key: Int): InteractionType {
            return when (key) {
                1 -> PING
                2 -> COMMAND
                3 -> COMPONENT
                4 -> COMMAND_AUTOCOMPLETE
                5 -> MODAL_SUBMIT
                else -> UNKNOWN
            }
        }
    }
}
