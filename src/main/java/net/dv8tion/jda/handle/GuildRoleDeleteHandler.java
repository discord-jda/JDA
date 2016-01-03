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
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.guild.GuildRoleDeleteEvent;
import org.json.JSONObject;

import java.util.List;

public class GuildRoleDeleteHandler extends SocketHandler
{

    public GuildRoleDeleteHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        Role removedRole = guild.getRolesMap().remove(content.getString("role_id"));
        if (removedRole == null)
            throw new IllegalArgumentException("GUILD_ROLE_DELETE attempted to delete a role that didn't exist! JSON: " + content);

        //Now that the role is removed from the Guild, remove it from all users.
        for (List<Role> userRoles : guild.getUserRoles().values())
        {
            userRoles.remove(removedRole);
        }
        api.getEventManager().handle(
                new GuildRoleDeleteEvent(
                        api, responseNumber,
                        guild, removedRole));
    }
}
