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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for an [RichCustomEmoji].
 *
 *
 * **Example**
 * <pre>`manager.setName("minn")
 * .setRoles(null)
 * .queue();
 * manager.reset(CustomEmojiManager.NAME | CustomEmojiManager.ROLES)
 * .setName("dv8")
 * .setRoles(roles)
 * .queue();
`</pre> *
 *
 * @see RichCustomEmoji.getManager
 */
interface CustomEmojiManager : Manager<CustomEmojiManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(CustomEmojiManager.NAME | CustomEmojiManager.ROLES);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.ROLES]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return CustomEmojiManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): CustomEmojiManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(CustomEmojiManager.NAME, CustomEmojiManager.ROLES);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.ROLES]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return CustomEmojiManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): CustomEmojiManager?

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] this Manager's
         * [RichCustomEmoji] is in.
         * <br></br>This is logically the same as calling `getEmoji().getGuild()`
         *
         * @return The parent [Guild][net.dv8tion.jda.api.entities.Guild]
         */
        get() = emoji.guild

    @get:Nonnull
    val emoji: RichCustomEmoji

    /**
     * Sets the **<u>name</u>** of the selected [RichCustomEmoji].
     *
     *
     * An emoji name **must** be between 2-32 characters long!
     * <br></br>Emoji names may only be populated with alphanumeric (with underscore and dash).
     *
     *
     * **Example**: `tatDab` or `fmgSUP`
     *
     * @param  name
     * The new name for the selected [RichCustomEmoji]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 2-32 characters long
     *
     * @return CustomEmojiManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): CustomEmojiManager?

    /**
     * Sets the **<u>restriction roles</u>** of the selected [RichCustomEmoji].
     * <br></br>If these are empty the emoji will be available to everyone otherwise only available to the specified roles.
     *
     *
     * An emoji's restriction roles **must not** contain `null`!
     *
     * @param  roles
     * The new set of [Roles][net.dv8tion.jda.api.entities.Role] for the selected [RichCustomEmoji]
     * to be restricted to, or `null` to clear the roles
     *
     * @throws IllegalArgumentException
     * If any of the provided values is `null`
     *
     * @return CustomEmojiManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setRoles(roles: Set<Role?>?): CustomEmojiManager?

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the roles field  */
        const val ROLES = (1 shl 1).toLong()
    }
}
