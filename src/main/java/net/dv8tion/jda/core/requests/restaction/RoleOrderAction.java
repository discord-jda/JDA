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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class RoleOrderAction extends OrderAction<Role, RoleOrderAction>
{
    public RoleOrderAction(Guild guild)
    {
        super(guild, Route.Guilds.MODIFY_ROLES.compile(guild.getId()));

        List<Role> roles = guild.getRoles();
        roles = roles.subList(0, roles.size() - 1); //Don't include the @everyone role.
        this.orderList.addAll(roles);
    }

    public RoleOrderAction selectPosition(Role selectedRole)
    {
        Args.notNull(selectedRole, "Role");
        Args.check(selectedRole.getGuild().equals(guild), "Provided selected role is not from this Guild!");

        return selectPosition(orderList.indexOf(selectedRole));
    }

    public RoleOrderAction swapPosition(Role swapRole)
    {
        Args.notNull(swapRole, "Provided swapRole");
        Args.check(swapRole.getGuild().equals(guild), "Provided selected role is not from this Guild!");

        return swapPosition(orderList.indexOf(swapRole));
    }

    public Role getSelectedRole()
    {
        if (selectedPosition == -1)
            throw new IllegalStateException("No position has been selected yet");

        return orderList.get(selectedPosition);
    }

    @Override
    protected void finalizeData()
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < orderList.size(); i++)
        {
            Role chan = orderList.get(i);
            array.put(new JSONObject()
                    .put("id", chan.getId())
                    .put("position", i));
        }

        this.data = array;
    }
}

