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

package net.dv8tion.jda.internal.entities.channel.concrete.detached;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.CategoryMixin;
import net.dv8tion.jda.internal.entities.detached.DetachedGuildImpl;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;

import javax.annotation.Nonnull;

public class DetachedCategoryImpl extends AbstractGuildChannelImpl<DetachedCategoryImpl>
    implements
        Category,
        CategoryMixin<DetachedCategoryImpl>,
        IInteractionPermissionMixin<DetachedCategoryImpl>
{
    private ChannelInteractionPermissions interactionPermissions;

    private int position;

    public DetachedCategoryImpl(long id, DetachedGuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public boolean isDetached()
    {
        return true;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.CATEGORY;
    }

    @Override
    public int getPositionRaw()
    {
        return position;
    }

    @Nonnull
    @Override
    public ChannelAction<TextChannel> createTextChannel(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<ForumChannel> createForumChannel(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<MediaChannel> createMediaChannel(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyTextChannelPositions()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<Category> createCopy()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CategoryManager getManager()
    {
        throw detachedException();
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions()
    {
        return interactionPermissions;
    }

    @Override
    public DetachedCategoryImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    @Nonnull
    @Override
    public DetachedCategoryImpl setInteractionPermissions(@Nonnull ChannelInteractionPermissions interactionPermissions)
    {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
