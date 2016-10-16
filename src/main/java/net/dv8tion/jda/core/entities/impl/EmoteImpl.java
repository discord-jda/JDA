/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.client.managers.EmoteManagerUpdatable;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.client.managers.EmoteManager;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EmoteImpl implements Emote, IFakeable
{

    private final String id;
    private String name;
    private final Guild guild;
    private boolean managed = false;
    private JDA api;
    private Set<Role> roles = new HashSet<>();
    private volatile EmoteManager manager = null;
    private volatile EmoteManagerUpdatable managerUpdatable = null;
    private Object mngLock = new Object();

    public EmoteImpl(String id,  Guild guild)
    {
        this.id = id;
        this.guild = guild;
        this.api = guild.getJDA();
    }

    public EmoteImpl(String id,  JDA api)
    {
        this.id = id;
        this.api = api;
        this.guild = null;
    }

    public EmoteImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public EmoteImpl overrideRoles(Role... roles)
    {
        Collections.addAll((this.roles = new HashSet<>()), roles);
        return this;
    }

    public EmoteImpl setManaged(boolean val)
    {
        this.managed = val;
        return this;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public Set<Role> getRoles()
    {
        return Collections.unmodifiableSet(new HashSet<>(roles));
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
    public String toString()
    {
        return "E:" + getName() + '(' + getId() + ')';
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public EmoteManager getManager()
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        if (this.isFake())
            throw new PermissionException("You can't modify or delete a fake Emote.");
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
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        if (this.isFake())
            throw new PermissionException("You can't modify or delete a fake Emote.");
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
    public boolean equals(Object obj)
    {
        return obj instanceof ISnowflake
                ? ((ISnowflake) obj).getId().equals(id)
                : obj instanceof String && obj.equals(getId());
    }
}
