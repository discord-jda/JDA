/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.client.managers.EmoteManagerUpdatable;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Custom Emote. (Emoji in official Discord API terminology)
 *
 * @since  2.2
 * @author Florian Spieß
 */
public class EmoteImpl implements Emote
{

    private final String id;
    private final Guild guild;
    private final JDA api;

    private volatile EmoteManager manager = null;
    private volatile EmoteManagerUpdatable managerUpdatable = null;
    private Object mngLock = new Object();

    private boolean managed = false;
    private HashSet<Role> roles = null;
    private String name;

    public EmoteImpl(String id,  Guild guild)
    {
        this.id = id;
        this.guild = guild;
        this.api = guild.getJDA();
        this.roles = new HashSet<>();
    }

    public EmoteImpl(String id,  JDA api)
    {
        this.id = id;
        this.api = api;
        this.guild = null;
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
    public String getId()
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
            synchronized (mngLock)
            {
                m = manager;
                if (m == null)
                    m = manager = new EmoteManager(this);
            }
        }
        return m;
    }

    @Override
    public EmoteManagerUpdatable getManagerUpdatable()
    {
        EmoteManagerUpdatable m = managerUpdatable;
        if (m == null)
        {
            synchronized (mngLock)
            {
                m = managerUpdatable;
                if (m == null)
                    m = managerUpdatable = new EmoteManagerUpdatable(this);
            }
        }
        return m;
    }

    @Override
    public RestAction<Void> delete()
    {
        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        if (isFake())
            throw new IllegalStateException("The emote you are trying to delete is not an actual emote we have access to (it is fake)!");
        if (managed)
            throw new UnsupportedOperationException("You cannot delete a managed emote!");
        if (!PermissionUtil.checkPermission(guild, guild.getSelfMember(), Permission.MANAGE_EMOTES))
            throw new PermissionException(Permission.MANAGE_EMOTES);

        Route.CompiledRoute route = Route.Emotes.DELETE_EMOTE.compile(getGuild().getId(), getId());
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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

    public EmoteImpl setManaged(boolean val)
    {
        this.managed = val;
        return this;
    }

    // -- Set Getter --

    public HashSet<Role> getRoleSet()
    {
        return this.roles;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Emote))
            return false;

        Emote oEmote = (Emote) obj;
        return getId().equals(oEmote.getId()) && getName().equals(oEmote.getName());
    }


    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "E:" + getName() + '(' + getId() + ')';
    }
}
