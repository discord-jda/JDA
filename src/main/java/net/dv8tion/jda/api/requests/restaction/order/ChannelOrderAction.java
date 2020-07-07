/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.requests.restaction.order;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Implementation of {@link OrderAction OrderAction}
 * to modify the order of {@link net.dv8tion.jda.api.entities.GuildChannel Channels} for a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 * <br>To apply the changes you must finish the {@link net.dv8tion.jda.api.requests.RestAction RestAction}.
 *
 * <p>Before you can use any of the {@code move} methods
 * you must use either {@link #selectPosition(Object) selectPosition(GuildChannel)} or {@link #selectPosition(int)}!
 *
 * @since 3.0
 *
 * @see   net.dv8tion.jda.api.entities.Guild
 * @see   net.dv8tion.jda.api.entities.Guild#modifyTextChannelPositions()
 * @see   net.dv8tion.jda.api.entities.Guild#modifyVoiceChannelPositions()
 * @see   net.dv8tion.jda.api.entities.Guild#modifyCategoryPositions()
 * @see   CategoryOrderAction
 */
public interface ChannelOrderAction extends OrderAction<GuildChannel, ChannelOrderAction>
{
    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} which holds
     * the channels from {@link #getCurrentOrder()}
     *
     * @return The corresponding {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * The sorting bucket for this order action.
     * <br>Multiple different {@link net.dv8tion.jda.api.entities.ChannelType ChannelTypes} can
     * share a common sorting bucket.
     *
     * @return The sorting bucket
     */
    int getSortBucket();

    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelTypes} for the {@link #getSortBucket() sorting bucket}.
     *
     * @return The channel types
     *
     * @see    net.dv8tion.jda.api.entities.ChannelType#fromSortBucket(int)
     */
    @Nonnull
    default EnumSet<ChannelType> getChannelTypes()
    {
        return ChannelType.fromSortBucket(getSortBucket());
    }

    /**
     * Moves the currently selected channel to the provided category.
     * <br>This will automatically sync the permission overrides with the target category.
     * You can use {@link #moveTo(Category, boolean) moveTo(newParent, false)} to disable permission sync.
     *
     * @param  newParent
     *         The target category, or null to remove the parent
     *
     * @throws IllegalStateException
     *         If no channel has been selected yet (See {@link #selectPosition(Object)})
     * @throws IllegalArgumentException
     *         If the provided category is not from the same guild
     *
     * @return The current ChannelOrderAction instance
     */
    @Nonnull
    default ChannelOrderAction moveTo(@Nullable Category newParent)
    {
        return moveTo(newParent, true);
    }

    /**
     * Moves the channel at the specified position to the provided category.
     * <br>This will automatically sync the permission overrides with the target category.
     * You can use {@link #moveTo(Category, boolean) moveTo(newParent, false)} to disable permission sync.
     *
     * @param  position
     *         The position of the channel that is moved
     * @param  newParent
     *         The target category, or null to remove the parent
     *
     * @throws IllegalArgumentException
     *         If the provided category is not from the same guild,
     *         or if the specified position is out-of-bounds
     *
     * @return The current ChannelOrderAction instance
     */
    @Nonnull
    default ChannelOrderAction moveTo(int position, @Nullable Category newParent)
    {
        return moveTo(position, newParent, true);
    }

    /**
     * Moves the specified channel to the provided category.
     * <br>This will automatically sync the permission overrides with the target category.
     * You can use {@link #moveTo(Category, boolean) moveTo(newParent, false)} to disable permission sync.
     *
     * @param  entity
     *         The channel to move
     * @param  newParent
     *         The target category, or null to remove the parent
     *
     * @throws IllegalArgumentException
     *         If the provided category is not from the same guild,
     *         or if the channel to move is null or not tracked by this order action
     *
     * @return The current ChannelOrderAction instance
     */
    @Nonnull
    default ChannelOrderAction moveTo(@Nonnull GuildChannel entity, @Nullable Category newParent)
    {
        return moveTo(entity, newParent, true);
    }

    /**
     * Moves the channel at the specified position to the provided category.
     *
     * @param  position
     *         The position of the channel that is moved
     * @param  newParent
     *         The target category, or null to remove the parent
     * @param  lockPermissions
     *         Whether to sync the permissions overrides with the new parent
     *
     * @throws IllegalArgumentException
     *         If the provided category is not from the same guild,
     *         or if the specified position is out-of-bounds
     *
     * @return The current ChannelOrderAction instance
     */
    @Nonnull
    default ChannelOrderAction moveTo(int position, @Nullable Category newParent, boolean lockPermissions)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(position).moveTo(newParent, lockPermissions);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return this;
    }

    /**
     * Moves the specified channel to the provided category.
     *
     * @param  entity
     *         The channel to move
     * @param  newParent
     *         The target category, or null to remove the parent
     * @param  lockPermissions
     *         Whether to sync the permissions overrides with the new parent
     *
     * @throws IllegalArgumentException
     *         If the provided category is not from the same guild,
     *         or if the channel to move is null or not tracked by this order action
     *
     * @return The current ChannelOrderAction instance
     */
    @Nonnull
    default ChannelOrderAction moveTo(@Nonnull GuildChannel entity, @Nullable Category newParent, boolean lockPermissions)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(entity).moveTo(newParent, lockPermissions);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return this;
    }

    /**
     * Moves the currently selected channel to the provided category.
     *
     * @param  newParent
     *         The target category, or null to remove the parent
     * @param  lockPermissions
     *         Whether to sync the permissions overrides with the new parent
     *
     * @throws IllegalStateException
     *         If no channel has been selected yet (See {@link #selectPosition(Object)})
     * @throws IllegalArgumentException
     *         If the provided category is not from the same guild
     *
     * @return The current ChannelOrderAction instance
     */
    @Nonnull
    ChannelOrderAction moveTo(@Nullable Category newParent, boolean lockPermissions);
}
