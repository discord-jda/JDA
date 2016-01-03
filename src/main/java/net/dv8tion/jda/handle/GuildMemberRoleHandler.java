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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        List<String> rolesNew = toStringList(content.getJSONArray("roles"));
        List<Role> rolesOld = guild.getUserRoles().get(user);

        //Find the roles removed.
        List<Role> removedRoles = new LinkedList<>();
        for (Role role : rolesOld)
        {
            boolean roleFound = false;
            for (Iterator<String> i = rolesNew.iterator(); i.hasNext();)
            {
                String roleId = i.next();
                if (role.getId().equals(roleId))
                {
                    i.remove();
                    roleFound = true;
                }
            }
            if (!roleFound)
                removedRoles.add(role);
        }

        //Make sure we either added or removed some number of roles.
        if ((removedRoles.size() != 0) && (rolesNew.size() != 0))
            throw new IllegalArgumentException("Provided a GUILD_MEMBER_UPDATE that attempted to add and remove roles at the same time. JSON: " + content);
        if ((removedRoles.size() == 0) && (rolesNew.size() == 0))
            throw new IllegalArgumentException("Provided a GUILD_MEMBER_UPDATE that did not change the role settings! JSON: " + content);

        //Remove the roles from the Guild's User-Roles map.
        if (removedRoles.size() > 0)
        {
            rolesOld.removeAll(removedRoles);
            api.getEventManager().handle(
                    new GuildMemberRoleRemoveEvent(
                            api, responseNumber,
                            guild, user, removedRoles));
        }
        else //If we didn't remove any roles, then we added roles. Add them to the Guild's User-Roles map.
        {
            Map<String, Role> guildRoles = guild.getRolesMap();
            LinkedList<Role> addedRoles = new LinkedList<>();
            for (String roleId : rolesNew)
            {
                Role r = guildRoles.get(roleId);
                if (r == null)
                    throw new IllegalArgumentException("GUILD_MEMBER_UPDATE attempted to give a User a role that doesn't exist on a Guild! JSON: " + content);
                rolesOld.add(r);
                addedRoles.add(r);
            }
            api.getEventManager().handle(
                    new GuildMemberRoleAddEvent(
                            api, responseNumber,
                            guild, user, addedRoles));
        }
    }

    private List<String> toStringList(JSONArray array)
    {
        LinkedList<String> strings = new LinkedList<>();
        for(int i = 0; i < array.length(); i++)
        {
            strings.add(array.getString(i));
        }
        return strings;
    }
}
