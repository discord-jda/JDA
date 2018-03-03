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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GuildMemberUpdateHandler extends SocketHandler
{

    public GuildMemberUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("guild_id");
        if (api.getGuildLock().isLocked(id))
            return id;

        JSONObject userJson = content.getJSONObject("user");
        final long userId = userJson.getLong("id");
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(id);
        if (guild == null)
        {
            api.getEventCache().cache(EventCache.Type.GUILD, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Got GuildMember update but JDA currently does not have the Guild cached. {}", content);
            return null;
        }

        MemberImpl member = (MemberImpl) guild.getMembersMap().get(userId);
        if (member == null)
        {
            api.getEventCache().cache(EventCache.Type.USER, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Got GuildMember update but Member is not currently present in Guild. {}", content);
            return null;
        }

        Set<Role> currentRoles = member.getRoleSet();
        List<Role> newRoles = toRolesList(guild, content.getJSONArray("roles"));

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
            api.getEventManager().handle(
                    new GuildMemberRoleRemoveEvent(
                            api, responseNumber,
                            member, removedRoles));
        }
        if (newRoles.size() > 0)
        {
            api.getEventManager().handle(
                    new GuildMemberRoleAddEvent(
                            api, responseNumber,
                            member, newRoles));
        }
        if (content.has("nick"))
        {
            String prevNick = member.getNickname();
            String newNick = content.optString("nick", null);
            if (!Objects.equals(prevNick, newNick))
            {
                member.setNickname(newNick);
                api.getEventManager().handle(
                        new GuildMemberNickChangeEvent(
                                api, responseNumber,
                                member, prevNick, newNick));
            }
        }
        return null;
    }

    private List<Role> toRolesList(GuildImpl guild, JSONArray array)
    {
        LinkedList<Role> roles = new LinkedList<>();
        for(int i = 0; i < array.length(); i++)
        {
            final long id = array.getLong(i);
            Role r = guild.getRolesMap().get(id);
            if (r != null)
            {
                roles.add(r);
            }
            else
            {
                api.getEventCache().cache(EventCache.Type.ROLE, id, () ->
                {
                    handle(responseNumber, allContent);
                });
                EventCache.LOG.debug("Got GuildMember update but one of the Roles for the Member is not yet cached.");
                return null;
            }
        }
        return roles;
    }
}
