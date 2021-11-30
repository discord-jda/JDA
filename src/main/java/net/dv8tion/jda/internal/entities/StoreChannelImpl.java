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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.channel.concrete.StoreChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.ICategorizableChannelMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IPositionableChannelMixin;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

//TODO-v5: Remove this class entirely.
public class StoreChannelImpl extends AbstractGuildChannelImpl<StoreChannelImpl> implements
        StoreChannel,
        IPermissionContainerMixin<StoreChannelImpl>,
        IPositionableChannelMixin<StoreChannelImpl>,
        ICategorizableChannelMixin<StoreChannelImpl>
{
    public StoreChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.STORE;
    }

    @Override
    public long getParentCategoryIdLong()
    {
        return 0;
    }

    @Override
    public int getPositionRaw()
    {
        return 0;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public ChannelAction<StoreChannel> createCopy(@Nonnull Guild guild)
    {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public StoreChannelManager getManager()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap()
    {
        return MiscUtil.newLongMap();
    }

    @Override
    public StoreChannelImpl setParentCategory(long parentCategoryId)
    {
        return this;
    }

    @Override
    public StoreChannelImpl setPosition(int position)
    {
        return this;
    }

    @Override
    public String toString()
    {
        return "SC:" + getName() + '(' + getId() + ')';
    }
}
