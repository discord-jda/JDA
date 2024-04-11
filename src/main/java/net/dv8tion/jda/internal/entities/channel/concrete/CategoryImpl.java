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

package net.dv8tion.jda.internal.entities.channel.concrete;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPositionableChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.CategoryManagerImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.Nonnull;

public class CategoryImpl extends AbstractGuildChannelImpl<CategoryImpl> implements
        Category,
        IPositionableChannelMixin<CategoryImpl>,
        IPermissionContainerMixin<CategoryImpl>
{
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    private int position;

    public CategoryImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public boolean isDetached()
    {
        return false;
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
        ChannelAction<TextChannel> action = getGuild().createTextChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name)
    {
        ChannelAction<NewsChannel> action = getGuild().createNewsChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name)
    {
        ChannelAction<VoiceChannel> action = getGuild().createVoiceChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@Nonnull String name)
    {
        ChannelAction<StageChannel> action = getGuild().createStageChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<ForumChannel> createForumChannel(@Nonnull String name)
    {
        ChannelAction<ForumChannel> action = getGuild().createForumChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public ChannelAction<MediaChannel> createMediaChannel(@Nonnull String name)
    {
        ChannelAction<MediaChannel> action = getGuild().createMediaChannel(name, this);
        return trySync(action);
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyTextChannelPositions()
    {
        return getGuild().modifyTextChannelPositions(this);
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions()
    {
        return getGuild().modifyVoiceChannelPositions(this);
    }

    @Nonnull
    @Override
    public ChannelAction<Category> createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction<Category> action = guild.createCategory(name);
        if (guild.equals(getGuild()))
        {
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                else
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
            }
        }
        return action;
    }

    @Nonnull
    @Override
    public ChannelAction<Category> createCopy()
    {
        return createCopy(getGuild());
    }

    @Nonnull
    @Override
    public CategoryManager getManager()
    {
        return new CategoryManagerImpl(this);
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap()
    {
        return overrides;
    }

    @Override
    public CategoryImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    private <T extends GuildChannel> ChannelAction<T> trySync(ChannelAction<T> action)
    {
        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.canSync(this))
        {
            long botPerms = PermissionUtil.getEffectivePermission(this, selfMember);
            for (PermissionOverride override : getPermissionOverrides())
            {
                long perms = override.getDeniedRaw() | override.getAllowedRaw();
                if ((perms & ~botPerms) != 0)
                    return action;
            }
        }
        return action.syncPermissionOverrides();
    }
}
