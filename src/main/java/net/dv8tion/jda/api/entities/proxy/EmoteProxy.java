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
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import java.util.List;

public class EmoteProxy implements Emote, ProxyEntity<Emote>
{
    private final GuildProxy guild;
    private final long id;

    public EmoteProxy(Emote emote)
    {
        this.guild = emote.getGuild().getProxy();
        this.id = emote.getIdLong();
    }

    @Override
    public Emote getSubject()
    {
        Emote emote = getGuild().getEmoteById(id);
        if (emote == null)
            throw new ProxyResolutionException("Emote(" + getId() + ")");
        return emote;
    }

    @Override
    public EmoteProxy getProxy()
    {
        return this;
    }

    @Override
    public Guild getGuild()
    {
        return guild.getSubject();
    }

    @Override
    public List<Role> getRoles()
    {
        return getSubject().getRoles();
    }

    @Override
    public boolean canProvideRoles()
    {
        return getSubject().canProvideRoles();
    }

    @Override
    public String getName()
    {
        return getSubject().getName();
    }

    @Override
    public boolean isManaged()
    {
        return getSubject().isManaged();
    }

    @Override
    public JDA getJDA()
    {
        return getGuild().getJDA();
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        return getSubject().delete();
    }

    @Override
    public EmoteManager getManager()
    {
        return getSubject().getManager();
    }

    @Override
    public boolean isAnimated()
    {
        return getSubject().isAnimated();
    }

    @Override
    public long getIdLong()
    {
        return id;
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
