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
package net.dv8tion.jda.api.interactions.components.selections

import net.dv8tion.jda.internal.utils.Helpers
import javax.annotation.Nonnull

/**
 * Component Interaction for a [StringSelectMenu].
 *
 * @see StringSelectInteractionEvent
 */
interface StringSelectInteraction : SelectMenuInteraction<String?, StringSelectMenu?> {
    @get:Nonnull
    abstract override val values: List<T>

    @get:Nonnull
    val selectedOptions: List<SelectOption?>?
        /**
         * This resolves the selected [values][.getValues] to the representative [SelectOption] instances.
         * <br></br>It is recommended to check [.getValues] directly instead of using the options.
         *
         * @return Immutable [List] of the selected options
         */
        get() {
            val menu = getComponent()
            val values: List<String?> = values
            return menu.getOptions()
                .stream()
                .filter { it: SelectOption? -> values.contains(it.getValue()) }
                .collect(Helpers.toUnmodifiableList())
        }
}
