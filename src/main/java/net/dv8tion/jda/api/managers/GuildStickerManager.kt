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
 * Manager providing functionality to update one or more fields for [GuildSticker].
 *
 *
 * **Example**
 * <pre>`manager.setName("catDance")
 * .setDescription("Cat dancing")
 * .queue();
 * manager.reset(GuildStickerManager.NAME | GuildStickerManager.TAGS)
 * .setName("dogDance")
 * .setTags("dancing", "dog")
 * .queue();
`</pre> *
 *
 * @see GuildSticker.getManager
 * @see Guild.editSticker
 */
interface GuildStickerManager : Manager<GuildStickerManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(GuildStickerManager.NAME | GuildStickerManager.TAGS);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.DESCRIPTION]
     *  * [.TAGS]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): GuildStickerManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(GuildStickerManager.NAME, GuildStickerManager.TAGS);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.DESCRIPTION]
     *  * [.TAGS]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): GuildStickerManager?

    /**
     * The [Guild] this Manager's [GuildSticker] is in.
     *
     *
     * This is null if [GuildSticker.getManager] is used on a sticker with an uncached guild.
     *
     * @return The [Guild], or null if not present.
     *
     * @see .getGuildId
     */
    val guild: Guild?

    /**
     * The ID of the guild this sticker belongs to.
     *
     * @return The guild id
     */
    val guildIdLong: Long

    @get:Nonnull
    val guildId: String?
        /**
         * The ID of the guild this sticker belongs to.
         *
         * @return The guild id
         */
        get() = java.lang.Long.toUnsignedString(guildIdLong)

    /**
     * Sets the **<u>name</u>** of the sticker.
     *
     *
     * A sticker name **must** be between 2-30 characters long!
     *
     *
     * **Example**: `catDance` or `dogWave`
     *
     * @param  name
     * The new name for the sticker (2-30 characters)
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 2-30 characters long
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): GuildStickerManager?

    /**
     * Sets the **<u>description</u>** of the sticker.
     *
     *
     * A sticker description **must** be between 2-100 characters long!
     *
     * @param  description
     * The new description for the sticker (2-100 characters)
     *
     * @throws IllegalArgumentException
     * If the provided description is `null` or not between 2-100 characters long
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDescription(@Nonnull description: String?): GuildStickerManager?

    /**
     * Sets the **<u>tags</u>** of the sticker.
     * <br></br>These are used for auto-complete when sending a message in the client, and for the sticker picker menu.
     *
     *
     * The combined list of sticker tags **must** at most be 200 characters long!
     *
     *
     * **Example**: `catDance` or `dogWave`
     *
     * @param  tags
     * The new tags for the sticker (up to 200 characters)
     *
     * @throws IllegalArgumentException
     *
     *  * If `tags` is `null`
     *  * If `tags` is empty
     *  * If `tags` contains `null` or empty strings
     *  * If the concatenated tags are more than 200 characters long (including commas between tags)
     *
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setTags(@Nonnull tags: Collection<String?>?): GuildStickerManager?

    /**
     * Sets the **<u>tags</u>** of the sticker.
     * <br></br>These are used for auto-complete when sending a message in the client, and for the sticker picker menu.
     *
     *
     * The combined list of sticker tags **must** at most be 200 characters long!
     *
     *
     * **Example**: `catDance` or `dogWave`
     *
     * @param  tags
     * The new tags for the sticker (up to 200 characters)
     *
     * @throws IllegalArgumentException
     *
     *  * If `tags` is `null`
     *  * If `tags` is empty
     *  * If `tags` contains `null` or empty strings
     *  * If the concatenated tags are more than 200 characters long (including commas between tags)
     *
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setTags(@Nonnull vararg tags: String?): GuildStickerManager? {
        Checks.noneNull(tags, "Tags")
        return setTags(Arrays.asList(*tags))
    }

    companion object {
        /** Used to reset name field  */
        const val NAME: Long = 1

        /** Used to reset description field  */
        const val DESCRIPTION = (1 shl 1).toLong()

        /** Used to reset tags field  */
        const val TAGS = (1 shl 2).toLong()
    }
}
