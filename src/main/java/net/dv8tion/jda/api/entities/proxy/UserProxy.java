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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;

public class UserProxy implements User, ProxyEntity<User>
{
    private final long id;
    private final JDA api;

    public UserProxy(User user)
    {
        this.id = user.getIdLong();
        this.api = user.getJDA();
    }

    @Override
    public User getSubject()
    {
        User user = api.getUserById(id);
        if (user == null)
            throw new ProxyResolutionException("User(" + getId() + ")");
        return user;
    }

    @Override
    public UserProxy getProxy()
    {
        return this;
    }

    @Override
    public String getName()
    {
        return getSubject().getName();
    }

    @Override
    public String getDiscriminator()
    {
        return getSubject().getDiscriminator();
    }

    @Override
    public String getAvatarId()
    {
        return getSubject().getAvatarId();
    }

    @Override
    public String getAvatarUrl()
    {
        return getSubject().getAvatarUrl();
    }

    @Override
    public String getDefaultAvatarId()
    {
        return getSubject().getDefaultAvatarId();
    }

    @Override
    public String getDefaultAvatarUrl()
    {
        return getSubject().getDefaultAvatarUrl();
    }

    @Override
    public String getEffectiveAvatarUrl()
    {
        return getSubject().getEffectiveAvatarUrl();
    }

    @Override
    public String getAsTag()
    {
        return getSubject().getAsTag();
    }

    @Override
    public boolean hasPrivateChannel()
    {
        return getSubject().hasPrivateChannel();
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        return getSubject().openPrivateChannel();
    }

    @Override
    public List<Guild> getMutualGuilds()
    {
        return getSubject().getMutualGuilds();
    }

    @Override
    public boolean isBot()
    {
        return getSubject().isBot();
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public boolean isFake()
    {
        return false; // can't be fake
    }

    @Override
    public String getAsMention()
    {
        return getSubject().getAsMention();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return getSubject().hashCode();
    }

    @Override
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
