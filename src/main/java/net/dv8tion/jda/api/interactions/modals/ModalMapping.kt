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
package net.dv8tion.jda.api.interactions.modals

import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.Component.Type.Companion.fromKey
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * ID/Value pair for a [ModalInteractionEvent][net.dv8tion.jda.api.events.interaction.ModalInteractionEvent].
 *
 * @see ModalInteractionEvent.getValue
 * @see ModalInteractionEvent.getValues
 */
class ModalMapping(`object`: DataObject) {
    /**
     * The custom id of this component
     *
     * @return The custom id of this component
     */
    @get:Nonnull
    val id: String

    /**
     * The String representation of this component.
     *
     *
     * For TextInputs, this returns what the User typed in it.
     *
     * @return The String representation of this component.
     */
    @get:Nonnull
    val asString: String

    /**
     * The [Type][Component.Type] of this component
     *
     * @return Type of this component
     */
    @get:Nonnull
    val type: Component.Type

    init {
        id = `object`.getString("custom_id")
        asString = `object`.getString("value")
        type = fromKey(`object`.getInt("type"))
    }

    override fun toString(): String {
        return EntityString(this)
            .setType(type)
            .addMetadata("value", asString)
            .toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ModalMapping) return false
        val that = o
        return type == that.type && id == that.id && asString == that.asString
    }

    override fun hashCode(): Int {
        return Objects.hash(id, asString, type)
    }
}
