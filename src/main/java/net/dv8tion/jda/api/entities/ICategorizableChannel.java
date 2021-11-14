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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.managers.channel.attribute.ICategorizableChannelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO-v5: Need Docs
public interface ICategorizableChannel extends GuildChannel, IPermissionContainer
{
    //TODO-v5: Docs
    @Override
    @Nonnull
    ICategorizableChannelManager<?, ?> getManager();

    //TODO-v5: Docs
    long getParentCategoryIdLong();

    //TODO-v5: Docs
    default String getParentCategoryId()
    {
        return Long.toUnsignedString(getParentCategoryIdLong());
    }

    /**
     * Parent {@link net.dv8tion.jda.api.entities.Category Category} of this
     * GuildChannel. Channels don't need to have a parent Category.
     * <br>Note that a {@link net.dv8tion.jda.api.entities.Category Category} will
     * always return {@code null} for this method as nested categories are not supported.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Category Category} for this GuildChannel
     */
    @Nullable
    default Category getParentCategory()
    {
        return getGuild().getCategoryById(getParentCategoryId());
    }

    /**
     * Whether or not this GuildChannel's {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} match
     * those of {@link #getParentCategory() its parent category}. If the channel doesn't have a parent category, this will return true.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled.
     * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables this CacheFlag by default.
     *
     * @return True, if this channel is synced with its parent category
     *
     * @since  4.2.1
     */
    boolean isSynced();
}
