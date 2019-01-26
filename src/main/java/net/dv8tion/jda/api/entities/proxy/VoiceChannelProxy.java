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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;

public class VoiceChannelProxy extends GuildChannelProxy implements VoiceChannel
{
    public VoiceChannelProxy(VoiceChannel channel)
    {
        super(channel);
    }

    @Override
    public VoiceChannel getSubject()
    {
        VoiceChannel channel = api.getVoiceChannelById(id);
        if (channel == null)
            throw new ProxyResolutionException("VoiceChannel(" + id + ")");
        return channel;
    }

    @Override
    public VoiceChannelProxy getProxy()
    {
        return this;
    }

    @Override
    public ChannelAction<VoiceChannel> createCopy(Guild guild)
    {
        return getSubject().createCopy(guild);
    }

    @Override
    public ChannelAction<VoiceChannel> createCopy()
    {
        return getSubject().createCopy();
    }

    @Override
    public int getUserLimit()
    {
        return getSubject().getUserLimit();
    }

    @Override
    public int getBitrate()
    {
        return getSubject().getBitrate();
    }

    @Override
    public int compareTo(@NotNull VoiceChannel o)
    {
        return getSubject().compareTo(o);
    }
}
