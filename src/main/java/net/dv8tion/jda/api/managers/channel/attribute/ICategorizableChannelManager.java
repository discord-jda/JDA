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

package net.dv8tion.jda.api.managers.channel.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICategorizableChannelManager<T extends ICategorizableChannel, M extends ICategorizableChannelManager<T, M>>
        extends ChannelManager<T, M>, IPermissionContainerManager<T, M>
{
    /**
     * Sets the <b><u>{@link Category Parent Category}</u></b>
     * of the selected {@link GuildChannel GuildChannel}.
     *
     *
     * @param  category
     *         The new parent for the selected {@link GuildChannel GuildChannel}
     *
     * @throws IllegalStateException
     *         If the target is a category itself
     * @throws IllegalArgumentException
     *         If the provided category is not from the same Guild
     *
     * @return ChannelManager for chaining convenience
     *
     * @since  3.4.0
     */
    @Nonnull
    @CheckReturnValue
    M setParent(@Nullable Category category);

    /**
     * Syncs all {@link PermissionOverride PermissionOverrides} of this GuildChannel with
     * its parent ({@link Category Category}).
     *
     * <p>After this operation, all {@link PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the parent.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This behaves as if calling {@link #sync(IPermissionContainer)} with this GuildChannel's {@link ICategorizableChannel#getParentCategory()} Parent}.
     *
     * @throws  IllegalStateException
     *          If this GuildChannel has no parent
     * @throws  net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel or {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://discord.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Discord Documentation - Permission Syncing</a>
     */
    @Nonnull
    @CheckReturnValue
    default M sync()
    {
        if (!(getChannel() instanceof ICategorizableChannel))
            throw new IllegalStateException("sync() requires that the channel be categorizable as it syncs the channel to the parent category.");

        ICategorizableChannel categorizableChannel = (ICategorizableChannel) getChannel();
        if (categorizableChannel.getParentCategory() == null)
            throw new IllegalStateException("sync() requires a parent category");
        return sync(categorizableChannel.getParentCategory());
    }

    /**
     * Syncs all {@link PermissionOverride PermissionOverrides} of this GuildChannel with
     * the given ({@link GuildChannel GuildChannel}).
     *
     * <p>After this operation, all {@link PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the syncSource.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This will only work for Channels of the same {@link Guild Guild}!.
     *
     * @param   syncSource
     *          The GuildChannel from where all PermissionOverrides should be copied from
     *
     * @throws  IllegalArgumentException
     *          If the given snySource is {@code null}, this GuildChannel or from a different Guild.
     * @throws  net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel or {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://discord.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Discord Documentation - Permission Syncing</a>
     */
    @Nonnull
    @CheckReturnValue
    M sync(@Nonnull IPermissionContainer syncSource);
}
