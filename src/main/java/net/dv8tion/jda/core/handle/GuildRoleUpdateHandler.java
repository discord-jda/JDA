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
import net.dv8tion.jda.core.entities.impl.RoleImpl;
import net.dv8tion.jda.core.events.role.update.*;
import org.json.JSONObject;

import java.util.Objects;

public class GuildRoleUpdateHandler extends SocketHandler
{
    public GuildRoleUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long guildId = content.getLong("guild_id");
        if (api.getGuildLock().isLocked(guildId))
            return guildId;

        JSONObject rolejson = content.getJSONObject("role");
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
        if (guild == null)
        {
            api.getEventCache().cache(EventCache.Type.GUILD, guildId, () ->
                    handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a Role Update for a Guild that is not yet cached: {}", content);
            return null;
        }

        final long roleId = rolejson.getLong("id");
        RoleImpl role = (RoleImpl) guild.getRolesMap().get(roleId);
        if (role == null)
        {
            api.getEventCache().cache(EventCache.Type.ROLE, roleId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a Role Update for Role that is not yet cached: {}", content);
            return null;
        }

        String name = rolejson.getString("name");
        int color = rolejson.getInt("color");
        if (color == 0)
            color = Role.DEFAULT_COLOR_RAW;
        int position = rolejson.getInt("position");
        long permissions = rolejson.getLong("permissions");
        boolean hoisted = rolejson.getBoolean("hoist");
        boolean mentionable = rolejson.getBoolean("mentionable");

        if (!Objects.equals(name, role.getName()))
        {
            String oldName = role.getName();
            role.setName(name);
            api.getEventManager().handle(
                    new RoleUpdateNameEvent(
                            api, responseNumber,
                            role, oldName));
        }
        if (color != role.getColorRaw())
        {
            int oldColor = role.getColorRaw();
            role.setColor(color);
            api.getEventManager().handle(
                    new RoleUpdateColorEvent(
                            api, responseNumber,
                            role, oldColor));
        }
        if (!Objects.equals(position, role.getPositionRaw()))
        {
            int oldPosition = role.getPosition();
            int oldPositionRaw = role.getPositionRaw();
            role.setRawPosition(position);
            api.getEventManager().handle(
                    new RoleUpdatePositionEvent(
                            api, responseNumber,
                            role, oldPosition, oldPositionRaw));
        }
        if (!Objects.equals(permissions, role.getPermissionsRaw()))
        {
            long oldPermissionsRaw = role.getPermissionsRaw();
            role.setRawPermissions(permissions);
            api.getEventManager().handle(
                    new RoleUpdatePermissionsEvent(
                            api, responseNumber,
                            role, oldPermissionsRaw));
        }

        if (hoisted != role.isHoisted())
        {
            boolean wasHoisted = role.isHoisted();
            role.setHoisted(hoisted);
            api.getEventManager().handle(
                    new RoleUpdateHoistedEvent(
                            api, responseNumber,
                            role, wasHoisted));
        }
        if (mentionable != role.isMentionable())
        {
            boolean wasMentionable = role.isMentionable();
            role.setMentionable(mentionable);
            api.getEventManager().handle(
                    new RoleUpdateMentionableEvent(
                            api, responseNumber,
                            role, wasMentionable));
        }
        return null;
    }
}
