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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.Collections;
import java.util.List;

public class PermissionOverrideImpl implements PermissionOverride
{
    private final Member member;
    private final Role role;
    private final Channel channel;
    private int allow;
    private int deny;

    public PermissionOverrideImpl(Channel channel, Member member, Role role)
    {
        this.channel = channel;
        this.member = member;
        this.role = role;
    }

    @Override
    public int getAllowedRaw()
    {
        return allow;
    }

    @Override
    public int getInheritRaw()
    {
        return ~(allow | deny);
    }

    @Override
    public int getDeniedRaw()
    {
        return deny;
    }

    @Override
    public List<Permission> getAllowed()
    {
        return Collections.unmodifiableList(Permission.getPermissions(allow));
    }

    @Override
    public List<Permission> getInherit()
    {
        return Collections.unmodifiableList(Permission.getPermissions(getInheritRaw()));
    }

    @Override
    public List<Permission> getDenied()
    {
        return Collections.unmodifiableList(Permission.getPermissions(deny));
    }

    @Override
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    @Override
    public Member getMember()
    {
        return member;
    }

    @Override
    public Role getRole()
    {
        return role;
    }

    @Override
    public Channel getChannel()
    {
        return channel;
    }

    @Override
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    @Override
    public boolean isMemberOverride()
    {
        return getMember() != null;
    }

    @Override
    public boolean isRoleOverride()
    {
        return getRole() != null;
    }

    public PermissionOverrideImpl setAllow(int allow)
    {
        this.allow = allow;
        return this;
    }

    public PermissionOverrideImpl setDeny(int deny)
    {
        this.deny = deny;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PermissionOverride))
            return false;
        PermissionOverride oPerm = (PermissionOverride) o;
        return this == oPerm || ((this.member == null ? oPerm.getMember() == null : this.member.equals(oPerm.getMember()))
                && this.channel.equals(oPerm.getChannel()) && (this.role == null ? oPerm.getRole() == null : this.role.equals(oPerm.getRole())));
    }

    @Override
    public int hashCode()
    {
        return member != null
                ? (channel.getId() + member.getUser().getId()).hashCode()
                : (channel.getId() + role.getId()).hashCode();
    }

    @Override
    public String toString()
    {
        ISnowflake snowflake = (member != null ? member.getUser() : role);
        return "PermOver:(" + (member != null ? "M" : "R") + ")"
                + "(" + channel.getId() + " | " + snowflake.getId() + ")";
    }
}
