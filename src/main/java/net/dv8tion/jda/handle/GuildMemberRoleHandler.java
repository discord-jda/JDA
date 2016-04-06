/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleRemoveEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GuildMemberRoleHandler extends SocketHandler
{

    public GuildMemberRoleHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        JSONObject userJson = content.getJSONObject("user");
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        User user = api.getUserMap().get(userJson.getString("id"));
        List<Role> rolesNew = toRolesList(guild, content.getJSONArray("roles"));
        List<Role> rolesOld = guild.getUserRoles().get(user);

        //Find the roles removed.
        List<Role> removedRoles = new LinkedList<>();
        for (Role role : rolesOld)
        {
            boolean roleFound = false;
            for (Iterator<Role> added = rolesNew.iterator(); added.hasNext();)
            {
                Role r = added.next();
                if (role.equals(r))
                {
                    added.remove();
                    roleFound = true;
                    break;
                }
            }
            if (!roleFound)
                removedRoles.add(role);
        }

        if (removedRoles.size() > 0)
        {
            rolesOld.removeAll(removedRoles);
        }
        if (rolesNew.size() > 0)
        {
            rolesOld.addAll(rolesNew);
        }
        Collections.sort(rolesOld, (r2, r1) -> Integer.compare(r1.getPosition(), r2.getPosition()));
        if (removedRoles.size() > 0)
        {
            api.getEventManager().handle(
                    new GuildMemberRoleRemoveEvent(
                            api, responseNumber,
                            guild, user, removedRoles));
        }
        if (rolesNew.size() > 0)
        {
            api.getEventManager().handle(
                    new GuildMemberRoleAddEvent(
                            api, responseNumber,
                            guild, user, rolesNew));
        }
    }

    private List<Role> toRolesList(GuildImpl guild, JSONArray array)
    {
        LinkedList<Role> roles = new LinkedList<>();
        for(int i = 0; i < array.length(); i++)
        {
            Role r = guild.getRolesMap().get(array.getString(i));
            if (r != null)
            {
                roles.add(r);
            }
        }
        return roles;
    }
}
