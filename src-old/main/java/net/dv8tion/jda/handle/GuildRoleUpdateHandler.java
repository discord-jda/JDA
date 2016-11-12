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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.RoleImpl;
import net.dv8tion.jda.events.guild.role.*;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public class GuildRoleUpdateHandler extends SocketHandler
{
    public GuildRoleUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        JSONObject rolejson = content.getJSONObject("role");
        RoleImpl role = (RoleImpl) ((GuildImpl) api.getGuildMap().get(content.getString("guild_id"))).getRolesMap().get(rolejson.getString("id"));

        if (role == null)
        {
            EventCache.get(api).cache(EventCache.Type.ROLE, rolejson.getString("id"), () ->
            {
                handle(allContent);
            });
            EventCache.LOG.debug("Received a Role Update for a non-existent role! JSON: " + content);
            return null;
        }

        if (!role.getName().equals(rolejson.getString("name")))
        {
            role.setName(rolejson.getString("name"));
            api.getEventManager().handle(new GuildRoleUpdateNameEvent(api, responseNumber, role));
        }
        if (role.getPositionRaw() != rolejson.getInt("position"))
        {
            role.setPosition(rolejson.getInt("position"));
            api.getEventManager().handle(new GuildRoleUpdatePositionEvent(api, responseNumber, role));
        }
        if (role.getPermissionsRaw() != rolejson.getInt("permissions"))
        {
            role.setPermissions(rolejson.getInt("permissions"));
            api.getEventManager().handle(new GuildRoleUpdatePermissionEvent(api, responseNumber, role));
        }
        if (role.getColor() != rolejson.getInt("color"))
        {
            role.setColor(rolejson.getInt("color"));
            api.getEventManager().handle(new GuildRoleUpdateColorEvent(api, responseNumber, role));
        }
        if (role.isGrouped() != rolejson.getBoolean("hoist"))
        {
            role.setGrouped(rolejson.getBoolean("hoist"));
            api.getEventManager().handle(new GuildRoleUpdateGroupedEvent(api, responseNumber, role));
        }
        if (role.isMentionable() != rolejson.getBoolean("mentionable"))
            role.setMentionable(rolejson.getBoolean("mentionable"));
        //TODO: Add event?
        api.getEventManager().handle(new GuildRoleUpdateEvent(api, responseNumber, role));
        return null;
    }
}
