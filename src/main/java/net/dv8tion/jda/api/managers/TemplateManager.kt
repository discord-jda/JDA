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

import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [Template][net.dv8tion.jda.api.entities.templates.Template].
 *
 *
 * **Example**
 * <pre>`manager.setName("backup")
 * .setDescription("backup for our server")
 * .queue();
 * manager.reset(TemplateManager.DESCRIPTION | TemplateManager.NAME)
 * .setName("server template")
 * .setDescription(null)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.entities.templates.Template.getManager
 */
interface TemplateManager : Manager<TemplateManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(TemplateManager.NAME | TemplateManager.DESCRIPTION);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.DESCRIPTION]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): TemplateManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(TemplateManager.NAME, TemplateManager.DESCRIPTION);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.DESCRIPTION]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): TemplateManager?

    /**
     * Sets the name of this [Template].
     *
     * @param  name
     * The new name for this [Template]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 1-100 characters long
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): TemplateManager?

    /**
     * Sets the description of this [Template].
     *
     * @param  description
     * The new description for this [Template]
     *
     * @throws IllegalArgumentException
     * If the provided description is not between 0-120 characters long
     *
     * @return TemplateManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDescription(description: String?): TemplateManager?

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the description field  */
        const val DESCRIPTION = (1 shl 1).toLong()
    }
}
