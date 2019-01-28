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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;

import javax.annotation.Nullable;
import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class MemberProxy implements Member, ProxyEntity
{
    private final GuildProxy guild;
    private final long id;

    public MemberProxy(Member member)
    {
        this.guild = member.getGuild().getProxy();
        this.id = member.getUser().getIdLong();
    }

    @Override
    public Member getSubject()
    {
        Member member = getGuild().getMemberById(id);
        if (member == null)
            throw new ProxyResolutionException("Member(" + Long.toUnsignedString(id) + ")");
        return member;
    }

    @Override
    public MemberProxy getProxy()
    {
        return this;
    }

    @Override
    public User getUser()
    {
        return getSubject().getUser();
    }

    @Override
    public Guild getGuild()
    {
        return guild.getSubject();
    }

    @Override
    public EnumSet<Permission> getPermissions()
    {
        return getSubject().getPermissions();
    }

    @Override
    public boolean hasPermission(Permission... permissions)
    {
        return getSubject().hasPermission(permissions);
    }

    @Override
    public boolean hasPermission(Collection<Permission> permissions)
    {
        return getSubject().hasPermission(permissions);
    }

    @Override
    public boolean hasPermission(GuildChannel channel, Permission... permissions)
    {
        return getSubject().hasPermission(channel, permissions);
    }

    @Override
    public boolean hasPermission(GuildChannel channel, Collection<Permission> permissions)
    {
        return getSubject().hasPermission(channel, permissions);
    }

    @Override
    public JDA getJDA()
    {
        return getGuild().getJDA();
    }

    @Override
    public OffsetDateTime getTimeJoined()
    {
        return getSubject().getTimeJoined();
    }

    @Override
    public GuildVoiceState getVoiceState()
    {
        return getSubject().getVoiceState();
    }

    @Override
    public List<Activity> getActivities()
    {
        return getSubject().getActivities();
    }

    @Override
    public OnlineStatus getOnlineStatus()
    {
        return getSubject().getOnlineStatus();
    }

    @Override
    public String getNickname()
    {
        return getSubject().getNickname();
    }

    @Override
    public String getEffectiveName()
    {
        return getSubject().getEffectiveName();
    }

    @Override
    public List<Role> getRoles()
    {
        return getSubject().getRoles();
    }

    @Override
    public Color getColor()
    {
        return getSubject().getColor();
    }

    @Override
    public int getColorRaw()
    {
        return getSubject().getColorRaw();
    }

    @Override
    public EnumSet<Permission> getPermissions(GuildChannel channel)
    {
        return getSubject().getPermissions(channel);
    }

    @Override
    public boolean canInteract(Member member)
    {
        return getSubject().canInteract(member);
    }

    @Override
    public boolean canInteract(Role role)
    {
        return getSubject().canInteract(role);
    }

    @Override
    public boolean canInteract(Emote emote)
    {
        return getSubject().canInteract(emote);
    }

    @Override
    public boolean isOwner()
    {
        return getSubject().isOwner();
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel()
    {
        return getSubject().getDefaultChannel();
    }

    @Override
    public String getAsMention()
    {
        return getSubject().getAsMention();
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
