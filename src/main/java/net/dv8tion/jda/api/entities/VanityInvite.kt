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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Meta data for the vanity invite of a guild
 *
 * @since  4.2.1
 */
class VanityInvite(
    /**
     * The invite code used for the invite url.
     *
     * @return The code
     */
    @get:Nonnull
    @param:Nonnull val code: String,
    /**
     * How many times this invite has been used.
     * <br></br>This is reset after the invite is changed or removed.
     *
     * @return The invite uses
     */
    val uses: Int
) {

    @get:Nonnull
    val url: String
        /**
         * The invite url.
         *
         * @return The invite url
         */
        get() = "https://discord.gg/" + this.code

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj !is VanityInvite) return false
        val other = obj
        return uses == other.uses && code == other.code
    }

    override fun hashCode(): Int {
        return Objects.hash(code, uses)
    }

    override fun toString(): String {
        return EntityString(this)
            .addMetadata("code", code)
            .toString()
    }
}
