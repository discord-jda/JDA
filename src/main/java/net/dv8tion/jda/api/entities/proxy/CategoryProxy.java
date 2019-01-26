/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities.proxy;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryProxy extends GuildChannelProxy implements Category
{
    public CategoryProxy(Category channel)
    {
        super(channel);
    }

    @Override
    public Category getSubject()
    {
        Category channel = api.getCategoryById(id);
        if (channel == null)
            throw new ProxyResolutionException("Category(" + getId() + ")");
        return channel;
    }

    @Override
    public CategoryProxy getProxy()
    {
        return this;
    }

    @Override
    public List<GuildChannel> getChannels()
    {
        return getSubject().getChannels();
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        return getSubject().getTextChannels();
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return getSubject().getVoiceChannels();
    }

    @Override
    public ChannelAction<TextChannel> createTextChannel(String name)
    {
        return getSubject().createTextChannel(name);
    }

    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(String name)
    {
        return getSubject().createVoiceChannel(name);
    }

    @Override
    public CategoryOrderAction<TextChannel> modifyTextChannelPositions()
    {
        return getSubject().modifyTextChannelPositions();
    }

    @Override
    public CategoryOrderAction<VoiceChannel> modifyVoiceChannelPositions()
    {
        return getSubject().modifyVoiceChannelPositions();
    }

    @Override
    public ChannelAction<Category> createCopy(Guild guild)
    {
        return getSubject().createCopy(guild);
    }

    @Override
    public ChannelAction<Category> createCopy()
    {
        return getSubject().createCopy();
    }

    @Override
    public int compareTo(@NotNull Category o)
    {
        return getSubject().compareTo(o);
    }
}
