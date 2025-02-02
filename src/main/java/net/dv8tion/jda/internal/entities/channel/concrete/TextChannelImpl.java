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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.TextChannelMixin;
import net.dv8tion.jda.internal.managers.channel.concrete.TextChannelManagerImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;

public class TextChannelImpl extends AbstractStandardGuildMessageChannelImpl<TextChannelImpl> implements
        TextChannel,
        DefaultGuildChannelUnion,
        TextChannelMixin<TextChannelImpl>
{
    private int slowmode;

    public TextChannelImpl(long id, GuildImpl guild)
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
    public GuildImpl getGuild()
    {
        return (GuildImpl) super.getGuild();
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.TEXT;
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        return getGuild().getMembersView().stream()
            .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
            .collect(Helpers.toUnmodifiableList());
    }

    @Override
    public int getSlowmode()
    {
        return slowmode;
    }

    @Nonnull
    @Override
    public TextChannelManager getManager()
    {
        return new TextChannelManagerImpl(this);
    }

    @Override
    public TextChannelImpl setSlowmode(int slowmode)
    {
        this.slowmode = slowmode;
        return this;
    }
}
