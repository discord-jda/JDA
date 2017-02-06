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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

public class GuildRoleCreateHandler extends SocketHandler
{

    public GuildRoleCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String guildId = content.getString("guild_id");
        if (GuildLock.get(api).isLocked(guildId))
        {
            return guildId;
        }

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
        if (guild == null)
        {
            EventCache.get(api).cache(EventCache.Type.GUILD, guildId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("GUILD_ROLE_CREATE was received for a Guild that is not yet cached: " + content);
            return null;
        }

        Role newRole = EntityBuilder.get(api).createRole(content.getJSONObject("role"), guild.getId());
        api.getEventManager().handle(
                new RoleCreateEvent(
                        api, responseNumber,
                        newRole));
        EventCache.get(api).playbackCache(EventCache.Type.ROLE, newRole.getId());
        return null;
    }
}
