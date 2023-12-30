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

package net.dv8tion.jda.internal.entities.channel.middleman;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import net.dv8tion.jda.internal.utils.ChannelUtil;

import javax.annotation.Nonnull;

public abstract class AbstractGuildChannelImpl<T extends AbstractGuildChannelImpl<T>> extends AbstractChannelImpl<T> implements GuildChannelMixin<T>
{
    protected GuildImpl guild;

    public AbstractGuildChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild.getJDA());
        this.guild = guild;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        return guild;
    }

    @Override
    public int compareTo(@Nonnull GuildChannel o)
    {
        return ChannelUtil.compare(this, o);
    }
}
