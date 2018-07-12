/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.client.managers.EmoteManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a Custom Emote. (Emoji in official Discord API terminology)
 *
 * @since  2.2
 */
public class EmoteImpl implements Emote
{
    private final long id;
    private final GuildImpl guild;
    private final JDAImpl api;
    private final Set<Role> roles;

    private final ReentrantLock mngLock = new ReentrantLock();
    private volatile EmoteManager manager = null;

    private boolean managed = false;
    private boolean animated = false;
    private String name;

    public EmoteImpl(long id, GuildImpl guild)
    {
        this.id = id;
        this.guild = guild;
        this.api = guild.getJDA();
        this.roles = Collections.synchronizedSet(new HashSet<>());
    }

    public EmoteImpl(long id, JDAImpl api)
    {
        this.id = id;
        this.api = api;
        this.guild = null;
        this.roles = null;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public List<Role> getRoles()
    {
        if (isFake())
            throw new IllegalStateException("Unable to return roles because this emote is fake. (We do not know the origin Guild of this emote)");
        return Collections.unmodifiableList(new LinkedList<>(roles));
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    @Override
    public boolean isFake()
    {
        return guild == null;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public EmoteManager getManager()
    {
        EmoteManager m = manager;
        if (m == null)
        {
            m = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new EmoteManager(this);
                return manager;
            });
        }
        return m;
    }

    @Override
    public boolean isAnimated()
    {
        return animated;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (isFake())
            throw new IllegalStateException("The emote you are trying to delete is not an actual emote we have access to (it is fake)!");
        if (managed)
            throw new UnsupportedOperationException("You cannot delete a managed emote!");
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_EMOTES))
            throw new InsufficientPermissionException(Permission.MANAGE_EMOTES);

        Route.CompiledRoute route = Route.Emotes.DELETE_EMOTE.compile(getGuild().getId(), getId());
        return new AuditableRestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    // -- Setters --

    public EmoteImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public EmoteImpl setAnimated(boolean animated)
    {
        this.animated = animated;
        return this;
    }

    public EmoteImpl setManaged(boolean val)
    {
        this.managed = val;
        return this;
    }

    // -- Set Getter --

    public Set<Role> getRoleSet()
    {
        return this.roles;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof EmoteImpl))
            return false;

        EmoteImpl oEmote = (EmoteImpl) obj;
        return this.id == oEmote.id && getName().equals(oEmote.getName());
    }


    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "E:" + getName() + '(' + getIdLong() + ')';
    }

    @Override
    public EmoteImpl clone()
    {
        if (isFake()) return null;
        EmoteImpl copy = new EmoteImpl(id, guild).setManaged(managed).setAnimated(animated).setName(name);
        copy.roles.addAll(roles);
        return copy;

    }
}
