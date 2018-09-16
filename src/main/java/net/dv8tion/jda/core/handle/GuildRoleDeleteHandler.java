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

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class GuildRoleDeleteHandler extends SocketHandler
{
    public GuildRoleDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(guildId);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("GUILD_ROLE_DELETE was received for a Guild that is not yet cached: {}", content);
            return null;
        }

        final long roleId = content.getLong("role_id");
        Role removedRole = guild.getRolesMap().remove(roleId);
        if (removedRole == null)
        {
            //getJDA().getEventCache().cache(EventCache.Type.ROLE, roleId, () -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug("GUILD_ROLE_DELETE was received for a Role that is not yet cached: {}", content);
            return null;
        }

        //Now that the role is removed from the Guild, remove it from all users and emotes.
        for (Member m : guild.getMembersMap().valueCollection())
        {
            MemberImpl member = (MemberImpl) m;
            member.getRoleSet().remove(removedRole);
        }

        for (Emote emote : guild.getEmoteCache())
        {
            EmoteImpl impl = (EmoteImpl) emote;
            if (impl.canProvideRoles())
                impl.getRoleSet().remove(removedRole);
        }

        getJDA().getEventManager().handle(
            new RoleDeleteEvent(
                getJDA(), responseNumber,
                removedRole));
        getJDA().getEventCache().clear(EventCache.Type.ROLE, roleId);
        return null;
    }
}
