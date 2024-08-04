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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.attribute.ICategorizableChannelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Represents a channel that can be a member of a {@link Category}.
 * Channels represented by this interface can have a parent {@link Category}.
 *
 * @see Category
 * @see net.dv8tion.jda.api.entities.Guild#getCategories()
 */
public interface ICategorizableChannel extends GuildChannel, IPermissionContainer, IPositionableChannel
{
    @Override
    @Nonnull
    ICategorizableChannelManager<?, ?> getManager();

    /**
     * Computes the relative position of this channel in the {@link #getParentCategory() parent category}.
     * <br>This is effectively the same as {@code getParentCategory().getChannels().indexOf(channel)}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return The relative position in the parent category, or {@code -1} if no parent is set
     */
    default int getPositionInCategory()
    {
        Category parent = getParentCategory();
        return parent == null ? -1 : parent.getChannels().indexOf(this);
    }

    /**
     * Get the snowflake of the {@link Category} that contains this channel.
     *
     * <p>This will return {@code 0} if this channel doesn't have a parent category.
     *
     * @return The Discord ID snowflake of the parent channel as a long.
     */
    long getParentCategoryIdLong();

    /**
     * Get the snowflake of the {@link Category Category} that contains this channel.
     *
     * <p>This will return {@code null} if this channel doesn't have a parent category.
     *
     * @return Possibly-null String representation of the Discord ID snowflake of the parent channel.
     */
    @Nullable
    default String getParentCategoryId()
    {
        long parentID = getParentCategoryIdLong();
        if (parentID == 0L)
            return null;
        return Long.toUnsignedString(parentID);
    }

    /**
     * Parent {@link Category Category} of this
     * GuildChannel. Channels don't need to have a parent Category.
     * <br>Note that a {@link Category Category} will
     * always return {@code null} for this method as nested categories are not supported.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return Possibly-null {@link Category Category} for this GuildChannel
     */
    @Nullable
    default Category getParentCategory()
    {
        return getGuild().getCategoryById(getParentCategoryIdLong());
    }

    /**
     * Whether or not this GuildChannel's {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} match
     * those of {@link #getParentCategory() its parent category}. If the channel doesn't have a parent category, this will return true.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled.
     * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables this CacheFlag by default.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return True, if this channel is synced with its parent category
     *
     * @since  4.2.1
     */
    boolean isSynced();
}
