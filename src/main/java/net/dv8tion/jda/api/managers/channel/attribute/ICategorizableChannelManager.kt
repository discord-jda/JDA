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
package net.dv8tion.jda.api.managers.channel.attribute

import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager abstraction to set the [Parent Category][Category] of a [categorizable channel][ICategorizableChannel].
 *
 * @param <T> The channel type
 * @param <M> The manager type
</M></T> */
interface ICategorizableChannelManager<T : ICategorizableChannel?, M : ICategorizableChannelManager<T, M>?> :
    ChannelManager<T, M>, IPermissionContainerManager<T, M>, IPositionableChannelManager<T, M> {
    /**
     * Sets the **<u>[Parent Category][Category]</u>**
     * of the selected [GuildChannel].
     *
     * @param  category
     * The new parent for the selected [GuildChannel]
     *
     * @throws IllegalStateException
     * If the target is a category itself
     * @throws IllegalArgumentException
     * If the provided category is not from the same Guild
     *
     * @return ChannelManager for chaining convenience
     *
     * @since  3.4.0
     */
    @Nonnull
    @CheckReturnValue
    fun setParent(category: Category?): M

    /**
     * Syncs all [PermissionOverrides][PermissionOverride] of this GuildChannel with
     * its parent ([Category]).
     *
     *
     * After this operation, all [PermissionOverrides][PermissionOverride]
     * will be exactly the same as the ones from the parent.
     * <br></br>**That means that all current PermissionOverrides are lost!**
     *
     *
     * This behaves as if calling [.sync] with this GuildChannel's [ICategorizableChannel.getParentCategory] Parent}.
     *
     * @throws  IllegalStateException
     * If this GuildChannel has no parent
     * @throws  net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel or [IPermissionHolder.canSync] is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see [Discord Documentation - Permission Syncing](https://discord.com/developers/docs/topics/permissions.permission-syncing)
     */
    @Nonnull
    @CheckReturnValue
    fun sync(): M {
        check(channel is ICategorizableChannel) { "sync() requires that the channel be categorizable as it syncs the channel to the parent category." }
        val categorizableChannel = channel as ICategorizableChannel
        checkNotNull(categorizableChannel.parentCategory) { "sync() requires a parent category" }
        return sync(categorizableChannel.parentCategory)
    }

    /**
     * Syncs all [PermissionOverrides][PermissionOverride] of this GuildChannel with
     * the given ([GuildChannel]).
     *
     *
     * After this operation, all [PermissionOverrides][PermissionOverride]
     * will be exactly the same as the ones from the syncSource.
     * <br></br>**That means that all current PermissionOverrides are lost!**
     *
     *
     * This will only work for Channels of the same [Guild]!.
     *
     * @param   syncSource
     * The GuildChannel from where all PermissionOverrides should be copied from
     *
     * @throws  IllegalArgumentException
     * If the given snySource is `null`, this GuildChannel or from a different Guild.
     * @throws  net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel or [IPermissionHolder.canSync] is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see [Discord Documentation - Permission Syncing](https://discord.com/developers/docs/topics/permissions.permission-syncing)
     */
    @Nonnull
    @CheckReturnValue
    fun sync(@Nonnull syncSource: IPermissionContainer?): M
}
