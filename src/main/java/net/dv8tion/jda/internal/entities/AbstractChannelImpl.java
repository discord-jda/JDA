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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.ChannelMixin;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;

public abstract class AbstractChannelImpl<T extends AbstractChannelImpl<T>> implements ChannelMixin<T>
{
    protected final long id;
    protected final JDAImpl api;

    protected String name;
    
    public AbstractChannelImpl(long id, JDA api)
    {
        this.id = id;
        this.api = (JDAImpl) api;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setName(String name)
    {
        this.name = name;
        return (T) this;
    }

    // -- Union Hooks --

    @Nonnull
    public PrivateChannel asPrivateChannel()
    {
        return transformChannel(PrivateChannel.class);
    }

    @Nonnull
    public TextChannel asTextChannel()
    {
        return transformChannel(TextChannel.class);
    }

    @Nonnull
    public NewsChannel asNewsChannel()
    {
        return transformChannel(NewsChannel.class);
    }

    @Nonnull
    public VoiceChannel asVoiceChannel()
    {
        return transformChannel(VoiceChannel.class);
    }

    @Nonnull
    public StageChannel asStageChannel()
    {
        return transformChannel(StageChannel.class);
    }

    @Nonnull
    public ThreadChannel asThreadChannel()
    {
        return transformChannel(ThreadChannel.class);
    }

    @Nonnull
    public GuildMessageChannel asGuildMessageChannel()
    {
        return transformChannel(GuildMessageChannel.class);
    }

    @Nonnull
    public StandardGuildChannel asStandardGuildChannel()
    {
        return transformChannel(StandardGuildChannel.class);
    }

    @Nonnull
    public StandardGuildMessageChannel asStandardGuildMessageChannel()
    {
        return transformChannel(StandardGuildMessageChannel.class);
    }

    private <TOut extends Channel> TOut transformChannel(Class<TOut> toObjectClass)
    {
        if (toObjectClass.isInstance(this))
            return toObjectClass.cast(this);

        String cleanedClassName = this.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalStateException(Helpers.format("Cannot convert channel of type %s to %s!", cleanedClassName, toObjectClass.getSimpleName()));
    }
}
