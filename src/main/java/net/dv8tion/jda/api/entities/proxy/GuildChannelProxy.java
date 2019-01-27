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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;

import java.util.List;

public abstract class GuildChannelProxy implements GuildChannel, ProxyEntity
{
    protected final long id;
    protected final JDA api;

    public GuildChannelProxy(GuildChannel channel)
    {
        this.id = channel.getIdLong();
        this.api = channel.getJDA();
    }

    @Override
    public abstract GuildChannel getSubject();

    @Override
    public ChannelType getType()
    {
        return getSubject().getType();
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
    public String getName()
    {
        return getSubject().getName();
    }

    @Override
    public Guild getGuild()
    {
        return getSubject().getGuild();
    }

    @Override
    public Category getParent()
    {
        return getSubject().getParent();
    }

    @Override
    public List<Member> getMembers()
    {
        return getSubject().getMembers();
    }

    @Override
    public int getPosition()
    {
        return getSubject().getPosition();
    }

    @Override
    public int getPositionRaw()
    {
        return getSubject().getPositionRaw();
    }

    @Override
    public PermissionOverride getPermissionOverride(Member member)
    {
        return getSubject().getPermissionOverride(member);
    }

    @Override
    public PermissionOverride getPermissionOverride(Role role)
    {
        return getSubject().getPermissionOverride(role);
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        return getSubject().getPermissionOverrides();
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides()
    {
        return getSubject().getMemberPermissionOverrides();
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return getSubject().getRolePermissionOverrides();
    }

    @Override
    public ChannelManager getManager()
    {
        return getSubject().getManager();
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        return getSubject().delete();
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Member member)
    {
        return getSubject().createPermissionOverride(member);
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Role role)
    {
        return getSubject().createPermissionOverride(role);
    }

    @Override
    public PermissionOverrideAction putPermissionOverride(Member member)
    {
        return getSubject().putPermissionOverride(member);
    }

    @Override
    public PermissionOverrideAction putPermissionOverride(Role role)
    {
        return getSubject().putPermissionOverride(role);
    }

    @Override
    public InviteAction createInvite()
    {
        return getSubject().createInvite();
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        return getSubject().getInvites();
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
