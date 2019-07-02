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
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

public class GuildMemberUpdateHandler extends SocketHandler
{

    public GuildMemberUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long id = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(id))
            return id;

        DataObject userJson = content.getObject("user");
        final long userId = userJson.getLong("id");
        GuildImpl guild = (GuildImpl) getJDA().getGuildById(id);
        if (guild == null)
        {
            //Do not cache this here, it will be outdated once we receive the GUILD_CREATE and could cause invalid cache
            //getJDA().getEventCache().cache(EventCache.Type.GUILD, userId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Got GuildMember update but JDA currently does not have the Guild cached. Ignoring. {}", content);
            return null;
        }

        MemberImpl member = (MemberImpl) guild.getMembersView().get(userId);
        if (member == null)
        {
            long hashId = id ^ userId;
            getJDA().getEventCache().cache(EventCache.Type.MEMBER, hashId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Got GuildMember update but Member is not currently present in Guild. HASH_ID: {} JSON: {}", hashId, content);
            return null;
        }

        Set<Role> currentRoles = member.getRoleSet();
        List<Role> newRoles = toRolesList(guild, content.getArray("roles"));

        //If newRoles is null that means that we didn't find a role that was in the array and was cached this event
        if (newRoles == null)
            return null;

        //Find the roles removed.
        List<Role> removedRoles = new LinkedList<>();
        each: for (Role role : currentRoles)
        {
            for (Iterator<Role> it = newRoles.iterator(); it.hasNext();)
            {
                Role r = it.next();
                if (role.equals(r))
                {
                    it.remove();
                    continue each;
                }
            }
            removedRoles.add(role);
        }

        if (removedRoles.size() > 0)
            currentRoles.removeAll(removedRoles);
        if (newRoles.size() > 0)
            currentRoles.addAll(newRoles);

        if (removedRoles.size() > 0)
        {
            getJDA().handleEvent(
                    new GuildMemberRoleRemoveEvent(
                            getJDA(), responseNumber,
                            member, removedRoles));
        }
        if (newRoles.size() > 0)
        {
            getJDA().handleEvent(
                    new GuildMemberRoleAddEvent(
                            getJDA(), responseNumber,
                            member, newRoles));
        }
        if (content.hasKey("nick"))
        {
            String oldNick = member.getNickname();
            String newNick = content.getString("nick", null);
            if (!Objects.equals(oldNick, newNick))
            {
                member.setNickname(newNick);
                getJDA().handleEvent(
                        new GuildMemberUpdateNicknameEvent(
                                getJDA(), responseNumber,
                                member, oldNick));
            }
        }
        if (content.hasKey("premium_since"))
        {
            long epoch = 0;
            if (!content.isNull("premium_since"))
            {
                TemporalAccessor date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(content.getString("premium_since"));
                epoch = Instant.from(date).toEpochMilli();
            }
            if (epoch != member.getBoostDateRaw())
            {
                OffsetDateTime oldTime = member.getTimeBoosted();
                member.setBoostDate(epoch);
                getJDA().handleEvent(
                    new GuildMemberUpdateBoostTimeEvent(
                        getJDA(), responseNumber,
                        member, oldTime));
            }
        }
        return null;
    }

    private List<Role> toRolesList(GuildImpl guild, DataArray array)
    {
        LinkedList<Role> roles = new LinkedList<>();
        for(int i = 0; i < array.length(); i++)
        {
            final long id = array.getLong(i);
            Role r = guild.getRolesView().get(id);
            if (r != null)
            {
                roles.add(r);
            }
            else
            {
                getJDA().getEventCache().cache(EventCache.Type.ROLE, id, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Got GuildMember update but one of the Roles for the Member is not yet cached.");
                return null;
            }
        }
        return roles;
    }
}
