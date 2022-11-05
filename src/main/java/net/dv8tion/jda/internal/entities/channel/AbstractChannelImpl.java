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

package net.dv8tion.jda.internal.entities.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.ChannelMixin;
import net.dv8tion.jda.internal.utils.EntityString;
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
        return Helpers.safeChannelCast(this, PrivateChannel.class);
    }

    @Nonnull
    public TextChannel asTextChannel()
    {
        return Helpers.safeChannelCast(this, TextChannel.class);
    }

    @Nonnull
    public NewsChannel asNewsChannel()
    {
        return Helpers.safeChannelCast(this, NewsChannel.class);
    }

    @Nonnull
    public VoiceChannel asVoiceChannel()
    {
        return Helpers.safeChannelCast(this, VoiceChannel.class);
    }

    @Nonnull
    public StageChannel asStageChannel()
    {
        return Helpers.safeChannelCast(this, StageChannel.class);
    }

    @Nonnull
    public ThreadChannel asThreadChannel()
    {
        return Helpers.safeChannelCast(this, ThreadChannel.class);
    }

    @Nonnull
    public Category asCategory()
    {
        return Helpers.safeChannelCast(this, Category.class);
    }

    @Nonnull
    @Override
    public ForumChannel asForumChannel()
    {
        return Helpers.safeChannelCast(this, ForumChannel.class);
    }

    @Nonnull
    public MessageChannel asMessageChannel()
    {
        return Helpers.safeChannelCast(this, MessageChannel.class);
    }

    @Nonnull
    public AudioChannel asAudioChannel()
    {
        return Helpers.safeChannelCast(this, AudioChannel.class);
    }

    @Nonnull
    public IThreadContainer asThreadContainer()
    {
        return Helpers.safeChannelCast(this, IThreadContainer.class);
    }

    @Nonnull
    public GuildChannel asGuildChannel()
    {
        return Helpers.safeChannelCast(this, GuildChannel.class);
    }

    @Nonnull
    public GuildMessageChannel asGuildMessageChannel()
    {
        return Helpers.safeChannelCast(this, GuildMessageChannel.class);
    }

    @Nonnull
    public StandardGuildChannel asStandardGuildChannel()
    {
        return Helpers.safeChannelCast(this, StandardGuildChannel.class);
    }

    @Nonnull
    public StandardGuildMessageChannel asStandardGuildMessageChannel()
    {
        return Helpers.safeChannelCast(this, StandardGuildMessageChannel.class);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .toString();
    }
}
