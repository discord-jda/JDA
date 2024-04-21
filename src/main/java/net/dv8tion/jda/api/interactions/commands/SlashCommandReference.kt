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
package net.dv8tion.jda.api.interactions.commands

import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Represents a slash command mention, such as `</ban soft:1021082477038678126>`
 */
class SlashCommandReference(
    @get:Nonnull
    @param:Nonnull override val name: String,
    /**
     * Returns the subcommand group of the slash command
     *
     * @return the subcommand group of the slash command
     */
    val subcommandGroup: String?,
    /**
     * Returns the subcommand of the slash command
     *
     * @return the subcommand of the slash command
     */
    val subcommandName: String?, override val idLong: Long
) : ICommandReference {

    @get:Nonnull
    override val fullCommandName: String
        get() {
            val joiner = StringJoiner(" ")
            joiner.add(name)
            if (subcommandGroup != null) joiner.add(subcommandGroup)
            if (subcommandName != null) joiner.add(subcommandName)
            return joiner.toString()
        }

    override fun toString(): String {
        return EntityString(this)
            .setName(fullCommandName)
            .toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is SlashCommandReference) return false
        val that = o
        return idLong == that.idLong && name == that.name && subcommandName == that.subcommandName && subcommandGroup == that.subcommandGroup
    }

    override fun hashCode(): Int {
        return Objects.hash(idLong, name, subcommandName, subcommandGroup)
    }
}
