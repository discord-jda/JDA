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
package net.dv8tion.jda.api.managers.channel

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.managers.Manager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [GuildChannel].
 *
 *
 * **Example**
 * <pre>`manager.setName("github-log")
 * .setTopic("logs for github commits")
 * .setNSFW(false)
 * .queue();
 * manager.reset(ChannelManager.PARENT | ChannelManager.NAME)
 * .setName("nsfw-commits")
 * .queue();
 * manager.setTopic("Java is to Javascript as wall is to wallet")
 * .queue();
`</pre> *
 *
 * @see GuildChannel.getManager
 */
interface ChannelManager<T : GuildChannel?, M : ChannelManager<T, M>?> : Manager<M> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(ChannelManager.NAME | ChannelManager.PARENT);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.PARENT]
     *  * [.TOPIC]
     *  * [.POSITION]
     *  * [.NSFW]
     *  * [.SLOWMODE]
     *  * [.USERLIMIT]
     *  * [.BITRATE]
     *  * [.PERMISSION]
     *  * [.TYPE]
     *  * [.REGION]
     *  * [.AUTO_ARCHIVE_DURATION]
     *  * [.ARCHIVED]
     *  * [.LOCKED]
     *  * [.INVITEABLE]
     *  * [.AVAILABLE_TAGS]
     *  * [.APPLIED_TAGS]
     *  * [.PINNED]
     *  * [.REQUIRE_TAG]
     *  * [.DEFAULT_REACTION]
     *  * [.DEFAULT_LAYOUT]
     *  * [.DEFAULT_SORT_ORDER]
     *  * [.HIDE_MEDIA_DOWNLOAD_OPTIONS]
     *  * [.DEFAULT_THREAD_SLOWMODE]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): M & Any

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(ChannelManager.NAME, ChannelManager.PARENT);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.PARENT]
     *  * [.TOPIC]
     *  * [.POSITION]
     *  * [.NSFW]
     *  * [.USERLIMIT]
     *  * [.BITRATE]
     *  * [.PERMISSION]
     *  * [.TYPE]
     *  * [.REGION]
     *  * [.AUTO_ARCHIVE_DURATION]
     *  * [.ARCHIVED]
     *  * [.LOCKED]
     *  * [.INVITEABLE]
     *  * [.AVAILABLE_TAGS]
     *  * [.APPLIED_TAGS]
     *  * [.PINNED]
     *  * [.REQUIRE_TAG]
     *  * [.DEFAULT_REACTION]
     *  * [.DEFAULT_LAYOUT]
     *  * [.DEFAULT_SORT_ORDER]
     *  * [.HIDE_MEDIA_DOWNLOAD_OPTIONS]
     *  * [.DEFAULT_THREAD_SLOWMODE]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): M & Any

    @get:Nonnull
    val channel: T

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild] this Manager's
         * [GuildChannel] is in.
         * <br></br>This is logically the same as calling `getChannel().getGuild()`
         *
         * @return The parent [Guild]
         */
        get() = channel!!.guild

    /**
     * Sets the **<u>name</u>** of the selected [GuildChannel].
     *
     *
     * A channel name **must not** be `null` nor empty or more than {@value Channel#MAX_NAME_LENGTH} characters long!
     * <br></br>TextChannel names may only be populated with alphanumeric (with underscore and dash).
     *
     *
     * **Example**: `mod-only` or `generic_name`
     * <br></br>Characters will automatically be lowercased by Discord for text channels!
     *
     * @param  name
     * The new name for the selected [GuildChannel]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 1-{@value Channel#MAX_NAME_LENGTH} characters long
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): M

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the parent field  */
        const val PARENT = (1 shl 1).toLong()

        /** Used to reset the topic field  */
        const val TOPIC = (1 shl 2).toLong()

        /** Used to reset the position field  */
        const val POSITION = (1 shl 3).toLong()

        /** Used to reset the nsfw field  */
        const val NSFW = (1 shl 4).toLong()

        /** Used to reset the userlimit field  */
        const val USERLIMIT = (1 shl 5).toLong()

        /** Used to reset the bitrate field  */
        const val BITRATE = (1 shl 6).toLong()

        /** Used to reset the permission field  */
        const val PERMISSION = (1 shl 7).toLong()

        /** Used to reset the rate-limit per user field  */
        const val SLOWMODE = (1 shl 8).toLong()

        /** Used to reset the channel type field  */
        const val TYPE = (1 shl 9).toLong()

        /** Used to reset the region field  */
        const val REGION = (1 shl 10).toLong()

        /** Used to reset the auto-archive-duration field  */
        const val AUTO_ARCHIVE_DURATION = (1 shl 11).toLong()

        /** Used to reset the archived field  */
        const val ARCHIVED = (1 shl 12).toLong()

        /** Used to reset the locked field  */
        const val LOCKED = (1 shl 13).toLong()

        /** Used to reset the invitable field  */
        const val INVITEABLE = (1 shl 14).toLong()

        /** Used to reset the available tags field  */
        const val AVAILABLE_TAGS = (1 shl 15).toLong()

        /** Used to reset the applied tags field  */
        const val APPLIED_TAGS = (1 shl 16).toLong()

        /** Used to reset the pinned state field  */
        const val PINNED = (1 shl 17).toLong()

        /** Used to reset the require tag state field  */
        const val REQUIRE_TAG = (1 shl 18).toLong()

        /** Used to reset the default reaction emoji field  */
        const val DEFAULT_REACTION = (1 shl 19).toLong()

        /** Used to reset the default layout field  */
        const val DEFAULT_LAYOUT = (1 shl 20).toLong()

        /** Used to reset the default sort order field  */
        const val DEFAULT_SORT_ORDER = (1 shl 21).toLong()

        /** Used to reset the hide media download option flag  */
        const val HIDE_MEDIA_DOWNLOAD_OPTIONS = (1 shl 22).toLong()

        /** Used to reset the default thread slowmode of a thread container  */
        const val DEFAULT_THREAD_SLOWMODE = (1 shl 23).toLong()
    }
}
