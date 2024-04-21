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
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [GuildWelcomeScreen].
 *
 *
 * **Example**
 * <pre>`manager.setEnabled(false)
 * .setDescription(null)
 * .setWelcomeChannels()
 * .queue();
 * manager.setEnabled(true)
 * .setDescription("Bot desc")
 * .setWelcomeChannels(Arrays.asList(
 * GuildWelcomeScreen.Channel.of(rulesChannel, "Read the rules first"),
 * GuildWelcomeScreen.Channel.of(generalChannel, "Go have a chat", Emoji.fromUnicode("U+1F4AC"))
 * ))
 * .queue();
`</pre> *
 *
 * @see Guild.modifyWelcomeScreen
 */
interface GuildWelcomeScreenManager : Manager<GuildWelcomeScreenManager?> {
    @get:Nonnull
    val guild: Guild?

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(GuildWelcomeScreenManager.DESCRIPTION | GuildWelcomeScreenManager.CHANNELS);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.ENABLED]
     *  * [.DESCRIPTION]
     *  * [.CHANNELS]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): GuildWelcomeScreenManager?

    /**
     * Resets the specified fields.
     * <br></br>Example: `manager.reset(GuildWelcomeScreenManager.DESCRIPTION, GuildWelcomeScreenManager.CHANNELS);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.ENABLED]
     *  * [.DESCRIPTION]
     *  * [.CHANNELS]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): GuildWelcomeScreenManager?

    /**
     * Sets the enabled state of the welcome screen.
     *
     * @param  enabled
     * `True` if the welcome screen should be enabled
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setEnabled(enabled: Boolean): GuildWelcomeScreenManager?

    /**
     * Sets the description of the welcome screen.
     *
     *
     * The description must not be longer than {@value GuildWelcomeScreen#MAX_DESCRIPTION_LENGTH}
     *
     * @param  description
     * The new description of the welcome screen, or `null` to remove the description
     *
     * @throws IllegalArgumentException
     * If the description longer than {@value GuildWelcomeScreen#MAX_DESCRIPTION_LENGTH}
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDescription(description: String?): GuildWelcomeScreenManager?

    @get:Nonnull
    val welcomeChannels: List<GuildWelcomeScreen.Channel?>?

    /**
     * Removes all welcome channels.
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clearWelcomeChannels(): GuildWelcomeScreenManager?

    /**
     * Sets the welcome channels of the welcome screen.
     *
     *
     * The order of the [Collection] defines in what order the channels appear on Discord.
     *
     * @param  channels
     * The new welcome channels to use, can be an empty list to remove all welcome channels.
     *
     * @throws IllegalArgumentException
     *
     *  * If `channels` is `null`
     *  * If more than {@value GuildWelcomeScreen#MAX_WELCOME_CHANNELS} welcome channels are set
     *
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     *
     * @see .setWelcomeChannels
     */
    @Nonnull
    @CheckReturnValue
    fun setWelcomeChannels(@Nonnull channels: Collection<GuildWelcomeScreen.Channel?>?): GuildWelcomeScreenManager?

    /**
     * Sets the welcome channels of the welcome screen.
     *
     *
     * The order of the parameters defines in what order the channels appear on Discord.
     *
     * @param  channels
     * The new welcome channels to use, you can provide nothing in order to remove all welcome channels.
     *
     * @throws IllegalArgumentException
     *
     *  * If `channels` is `null`
     *  * If more than {@value GuildWelcomeScreen#MAX_WELCOME_CHANNELS} welcome channels are set
     *
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     *
     * @see .setWelcomeChannels
     */
    @Nonnull
    @CheckReturnValue
    fun setWelcomeChannels(@Nonnull vararg channels: GuildWelcomeScreen.Channel?): GuildWelcomeScreenManager? {
        Checks.notNull(channels, "Welcome channels")
        return setWelcomeChannels(Arrays.asList(*channels))
    }

    companion object {
        /** Used to reset the enabled field  */
        const val ENABLED: Long = 1

        /** Used to reset the description field  */
        const val DESCRIPTION = (1 shl 1).toLong()

        /** Used to reset the channels field  */
        const val CHANNELS = (1 shl 2).toLong()
    }
}
