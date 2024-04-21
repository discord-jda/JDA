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

import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * The query input for an [auto-complete interaction][net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback].
 *
 *
 * The [value][.getValue] of such a query may not be a valid instance of the expected [type][.getType].
 * Discord does not do any validation for auto-complete queries. However, you are required to reply with the correct type.
 */
class AutoCompleteQuery(@Nonnull option: OptionMapping) {
    /**
     * The name of the input field, usually an option name in [CommandAutoCompleteInteraction].
     *
     * @return The option name
     */
    @get:Nonnull
    val name: String

    /**
     * The query value that the user is currently typing.
     *
     *
     * This is not validated and may not be a valid value for an actual command.
     * For instance, a user may input invalid numbers for [OptionType.NUMBER].
     *
     * @return The current auto-completable query value
     */
    @get:Nonnull
    val value: String

    /**
     * The expected option type for this query.
     *
     * @return The option type expected from this auto-complete response
     */
    @JvmField
    @get:Nonnull
    val type: OptionType

    init {
        name = option.name
        value = option.asString
        type = option.type
    }

    override fun hashCode(): Int {
        return Objects.hash(name, value, type)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is AutoCompleteQuery) return false
        val query = obj
        return type == query.type && name == query.name && value == query.value
    }

    override fun toString(): String {
        return EntityString(this)
            .setType(type)
            .addMetadata("name", name)
            .addMetadata("value", value)
            .toString()
    }
}
