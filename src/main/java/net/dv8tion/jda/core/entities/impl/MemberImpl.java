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
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

public class MemberImpl implements Member
{
    private final Guild guild;
    private final User user;
    private final TreeSet<Role> roles = new TreeSet<>((r1, r2) -> r2.compareTo(r1));
    private final VoiceState voiceState;

    private String nickname;
    private OffsetDateTime joinDate;
    private Game game;
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;

    public MemberImpl(Guild guild, User user)
    {
        this.guild = guild;
        this.user = user;
        this.voiceState = new VoiceStateImpl(guild, this);
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public JDA getJDA()
    {
        return user.getJDA();
    }

    @Override
    public OffsetDateTime getJoinDate()
    {
        return joinDate;
    }

    @Override
    public VoiceState getVoiceState()
    {
        return voiceState;
    }

    @Override
    public Game getGame()
    {
        return game;
    }

    @Override
    public OnlineStatus getOnlineStatus()
    {
        return onlineStatus;
    }

    @Override
    public String getNickname()
    {
        return nickname;
    }

    @Override
    public String getEffectiveName()
    {
        return nickname != null ? nickname : user.getName();
    }

    @Override
    public List<Role> getRoles()
    {
        return Collections.unmodifiableList(new ArrayList<>(roles));
    }

    @Override
    public Color getColor()
    {
        for (Role r : getRoles())
        {
            if (r.getColor() != null)
                return r.getColor();
        }
        return null;
    }

    @Override
    public List<Permission> getPermissions()
    {
        return Collections.unmodifiableList(
                Permission.getPermissions(
                        PermissionUtil.getEffectivePermission(guild, this)));
    }

    @Override
    public List<Permission> getPermissions(Channel channel)
    {
        if (!guild.equals(channel.getGuild()))
            throw new IllegalArgumentException("Provided channel is not in the same guild as this member!");

        return Collections.unmodifiableList(
                Permission.getPermissions(
                        PermissionUtil.getEffectivePermission(channel, this)));
    }

    @Override
    public boolean hasPermission(Permission... permissions)
    {
        return PermissionUtil.checkPermission(guild, this, permissions);
    }

    @Override
    public boolean hasPermission(Channel channel, Permission... permissions)
    {
        return PermissionUtil.checkPermission(channel, this, permissions);
    }

    public MemberImpl setNickname(String nickname)
    {
        this.nickname = nickname;
        return this;
    }

    public MemberImpl setJoinDate(OffsetDateTime joinDate)
    {
        this.joinDate = joinDate;
        return this;
    }

    public MemberImpl setGame(Game game)
    {
        this.game = game;
        return this;
    }

    public MemberImpl setOnlineStatus(OnlineStatus onlineStatus)
    {
        this.onlineStatus = onlineStatus;
        return this;
    }

    public TreeSet<Role> getRoleSet()
    {
        return roles;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Member))
            return false;

        Member oMember = (Member) o;
        return this == oMember || (oMember.getUser().equals(user) && oMember.getGuild().equals(guild));
    }

    @Override
    public int hashCode()
    {
        return (guild.getId() + user.getId()).hashCode();
    }

    @Override
    public String toString()
    {
        return "MB:" + getEffectiveName() + '(' + user.toString() + " / " + guild.toString() +')';
    }

    @Override
    public String getAsMention()
    {
        return user.getAsMention();
    }
}
