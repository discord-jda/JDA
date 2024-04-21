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
package net.dv8tion.jda.api.requests.restaction.order

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.ChannelType.Companion.fromSortBucket
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Implementation of [OrderAction]
 * to modify the order of [Channels][GuildChannel] for a [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>To apply the changes you must finish the [RestAction][net.dv8tion.jda.api.requests.RestAction].
 *
 *
 * Before you can use any of the `move` methods
 * you must use either [selectPosition(GuildChannel)][.selectPosition] or [.selectPosition]!
 *
 * @since 3.0
 *
 * @see net.dv8tion.jda.api.entities.Guild
 *
 * @see net.dv8tion.jda.api.entities.Guild.modifyTextChannelPositions
 * @see net.dv8tion.jda.api.entities.Guild.modifyVoiceChannelPositions
 * @see net.dv8tion.jda.api.entities.Guild.modifyCategoryPositions
 * @see CategoryOrderAction
 */
interface ChannelOrderAction : OrderAction<GuildChannel?, ChannelOrderAction?> {
    @get:Nonnull
    val guild: Guild?

    /**
     * The sorting bucket for this order action.
     * <br></br>Multiple different [ChannelTypes][ChannelType] can
     * share a common sorting bucket.
     *
     * @return The sorting bucket
     */
    val sortBucket: Int

    @get:Nonnull
    val channelTypes: EnumSet<ChannelType?>?
        /**
         * The [ChannelTypes][ChannelType] for the [sorting bucket][.getSortBucket].
         *
         * @return The channel types
         *
         * @see ChannelType.fromSortBucket
         */
        get() = fromSortBucket(sortBucket)

    /**
     * Set the parent category for the currently selected channel.
     *
     * @param  category
     * The new parent category, or null to not have any category
     * @param  syncPermissions
     * Whether to sync the permissions of the channel to the new category
     *
     * @throws IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws IllegalArgumentException
     * If the provided category is not in the same guild as the channel
     *
     * @return The current ChannelOrderAction
     */
    @Nonnull
    @CheckReturnValue
    fun setCategory(category: Category?, syncPermissions: Boolean): ChannelOrderAction?

    /**
     * Set the parent category for the currently selected channel.
     *
     *
     * By default, this will not sync the permissions with the new category.
     * You can use [.setCategory] to sync permissions.
     *
     * @param  category
     * The new parent category, or null to not have any category
     *
     * @throws IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws IllegalArgumentException
     * If the provided category is not in the same guild as the channel
     *
     * @return The current ChannelOrderAction
     */
    @Nonnull
    @CheckReturnValue
    fun setCategory(category: Category?): ChannelOrderAction? {
        return setCategory(category, false)
    }
}
