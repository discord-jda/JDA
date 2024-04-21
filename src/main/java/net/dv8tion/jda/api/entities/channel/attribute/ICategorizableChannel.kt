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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.Nonnull

/**
 * Represents a channel that can be a member of a [Category].
 * Channels represented by this interface can have a parent [Category].
 *
 * @see Category
 *
 * @see net.dv8tion.jda.api.entities.Guild.getCategories
 */
interface ICategorizableChannel : GuildChannel, IPermissionContainer, IPositionableChannel {
    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
    val positionInCategory: Int
        /**
         * Computes the relative position of this channel in the [parent category][.getParentCategory].
         * <br></br>This is effectively the same as `getParentCategory().getChannels().indexOf(channel)`.
         *
         * @return The relative position in the parent category, or `-1` if no parent is set
         */
        get() {
            val parent = parentCategory
            return parent?.channels?.indexOf(this) ?: -1
        }

    /**
     * Get the snowflake of the [Category] that contains this channel.
     *
     *
     * This will return `0` if this channel doesn't have a parent category.
     *
     * @return The Discord ID snowflake of the parent channel as a long.
     */
    @JvmField
    val parentCategoryIdLong: Long
    val parentCategoryId: String?
        /**
         * Get the snowflake of the [Category] that contains this channel.
         *
         *
         * This will return `null` if this channel doesn't have a parent category.
         *
         * @return Possibly-null String representation of the Discord ID snowflake of the parent channel.
         */
        get() {
            val parentID = parentCategoryIdLong
            return if (parentID == 0L) null else java.lang.Long.toUnsignedString(parentID)
        }
    val parentCategory: Category?
        /**
         * Parent [Category] of this
         * GuildChannel. Channels don't need to have a parent Category.
         * <br></br>Note that a [Category] will
         * always return `null` for this method as nested categories are not supported.
         *
         * @return Possibly-null [Category] for this GuildChannel
         */
        get() = getGuild().getCategoryById(parentCategoryIdLong)

    /**
     * Whether or not this GuildChannel's [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] match
     * those of [its parent category][.getParentCategory]. If the channel doesn't have a parent category, this will return true.
     *
     *
     * This requires [CacheFlag.MEMBER_OVERRIDES][net.dv8tion.jda.api.utils.cache.CacheFlag.MEMBER_OVERRIDES] to be enabled.
     * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables this CacheFlag by default.
     *
     * @return True, if this channel is synced with its parent category
     *
     * @since  4.2.1
     */
    val isSynced: Boolean
}
