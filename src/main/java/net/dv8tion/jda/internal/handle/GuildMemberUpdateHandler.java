/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

import java.util.LinkedList;
import java.util.List;

public class GuildMemberUpdateHandler extends SocketHandler
{

    public GuildMemberUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long id = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(id))
            return id;

        DataObject userJson = content.getObject("user");
        final long userId = userJson.getLong("id");
        GuildImpl guild = (GuildImpl) getJDA().getGuildById(id);
        if (guild == null)
        {
            //Do not cache this here, it will be outdated once we receive the GUILD_CREATE and could cause invalid cache
            //getJDA().getEventCache().cache(EventCache.Type.GUILD, userId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Got GuildMember update but JDA currently does not have the Guild cached. Ignoring. {}", content);
            return null;
        }

        MemberImpl member = (MemberImpl) guild.getMembersView().get(userId);
        if (member == null)
        {
            EntityBuilder.LOG.debug("Creating member from GUILD_MEMBER_UPDATE {}", content);
            member = getJDA().getEntityBuilder().createMember(guild, content);
        }

        List<Role> newRoles = toRolesList(guild, content.getArray("roles"));
        getJDA().getEntityBuilder().updateMember(guild, member, content, newRoles);
        return null;
    }

    private List<Role> toRolesList(GuildImpl guild, DataArray array)
    {
        LinkedList<Role> roles = new LinkedList<>();
        for(int i = 0; i < array.length(); i++)
        {
            final long id = array.getLong(i);
            Role r = guild.getRolesView().get(id);
            if (r != null)
            {
                roles.add(r);
            }
            else
            {
                getJDA().getEventCache().cache(EventCache.Type.ROLE, id, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Got GuildMember update but one of the Roles for the Member is not yet cached.");
                return null;
            }
        }
        return roles;
    }
}
