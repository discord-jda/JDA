/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

/**
 * Implementation of {@link net.dv8tion.jda.core.requests.restaction.OrderAction OrderAction}
 * designed to modify the order of {@link net.dv8tion.jda.core.entities.Role Roles} of the
 * specified {@link net.dv8tion.jda.core.entities.Guild Guild}.
 * <br>To apply the changes you must finish the {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>This uses descending order!</b>
 *
 * @since 3.0
 */
public class RoleOrderAction extends OrderAction<Role, RoleOrderAction>
{
    protected final Guild guild;

    /**
     * Creates a new RoleOrderAction instance
     *
     * @param guild
     *        The target {@link net.dv8tion.jda.core.entities.Guild Guild} of which
     *        to change the {@link net.dv8tion.jda.core.entities.Role Role} order
     */
    public RoleOrderAction(Guild guild)
    {
        super(guild.getJDA(), false, Route.Guilds.MODIFY_ROLES.compile(guild.getId()));
        this.guild = guild;

        List<Role> roles = guild.getRoles();
        roles = roles.subList(0, roles.size() - 1); //Don't include the @everyone role.

        //Add roles to orderList in reverse due to role position ordering being descending
        // Top role starts at roles.size() - 1, bottom is 0.
        for (int i = roles.size() - 1; i >= 0; i--)
            this.orderList.add(roles.get(i));
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} which holds
     * the roles from {@link #getCurrentOrder()}
     *
     * @return The corresponding {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    protected void finalizeData()
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < orderList.size(); i++)
        {
            Role role = orderList.get(i);
            array.put(new JSONObject()
                    .put("id", role.getId())
                    .put("position", i + 1)); //plus 1 because position 0 is the @everyone position.
        }

        this.data = array;
    }

    @Override
    protected void validateInput(Role entity)
    {
        Args.check(entity.getGuild().equals(guild), "Provided selected role is not from this Guild!");
        Args.check(orderList.contains(entity), "Provided role is not in the list of orderable roles!");
    }
}

