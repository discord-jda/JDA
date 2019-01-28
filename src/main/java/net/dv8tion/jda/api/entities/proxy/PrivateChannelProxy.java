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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.requests.RestAction;

public class PrivateChannelProxy implements PrivateChannel, MessageChannelProxy
{
    private final long id;
    private final JDA api;

    public PrivateChannelProxy(PrivateChannel channel)
    {
        this.id = channel.getIdLong();
        this.api = channel.getJDA();
    }

    @Override
    public PrivateChannel getSubject()
    {
        PrivateChannel channel = api.getPrivateChannelById(id);
        if (channel == null)
            throw new ProxyResolutionException("PrivateChannel(" + getId() + ")");
        return channel;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public RestAction<Void> close()
    {
        return getSubject().close();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public PrivateChannelProxy getProxy()
    {
        return this;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return getSubject().getLatestMessageIdLong();
    }

    @Override
    public boolean hasLatestMessage()
    {
        return getSubject().hasLatestMessage();
    }

    @Override
    public String getName()
    {
        return getSubject().getName();
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.PRIVATE;
    }

    @Override
    public User getUser()
    {
        return getSubject().getUser();
    }

    @Override
    public boolean isFake()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        return getSubject().hashCode();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj)
    {
        return obj == this || getSubject().equals(obj);
    }

    @Override
    public String toString()
    {
        return getSubject().toString();
    }
}
