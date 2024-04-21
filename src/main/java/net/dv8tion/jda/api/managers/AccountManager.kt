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

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.api.entities.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for the logged in account.
 *
 *
 * **Example**
 * <pre>`manager.setAvatar(null).queue();
 * manager.reset(AccountManager.AVATAR)
 * .setAvatar(icon)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.JDA.getSelfUser
 * @see net.dv8tion.jda.api.entities.SelfUser.getManager
 */
interface AccountManager : Manager<AccountManager?> {
    @get:Nonnull
    val selfUser: SelfUser?

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(AccountManager.NAME | AccountManager.AVATAR);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.AVATAR]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    override fun reset(fields: Long): AccountManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(AccountManager.NAME, AccountManager.AVATAR);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.AVATAR]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    override fun reset(vararg fields: Long): AccountManager?

    /**
     * Sets the username for the currently logged in account
     *
     * @param  name
     * The new username
     *
     * @throws IllegalArgumentException
     * If the provided name is:
     *
     *  * Equal to `null`
     *  * Less than `2` or more than `32` characters in length
     *
     *
     * @return AccountManager for chaining convenience
     *
     */
    @Nonnull
    @CheckReturnValue
    @ForRemoval
    @Deprecated("Bot usernames are set through the application name now.")
    fun setName(@Nonnull name: String?): AccountManager?

    /**
     * Sets the avatar for the currently logged in account
     *
     * @param  avatar
     * An [Icon][net.dv8tion.jda.api.entities.Icon] instance representing
     * the new Avatar for the current account, `null` to reset the avatar to the default avatar.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAvatar(avatar: Icon?): AccountManager?

    /**
     * Sets the banner for the currently logged in account
     *
     * @param  banner
     * An [Icon][net.dv8tion.jda.api.entities.Icon] instance representing
     * the new banner for the current account, `null` to reset the banner to the default banner.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setBanner(banner: Icon?): AccountManager?

    companion object {
        /**
         * Used to reset the name field
         *
         */
        @JvmField
        @ForRemoval
        @Deprecated("Bot usernames are set through the application name now.")
        val NAME: Long = 1

        /** Used to reset the avatar field  */
        const val AVATAR = (1 shl 1).toLong()

        /** Used to reset the banner field  */
        const val BANNER = (1 shl 2).toLong()
    }
}
