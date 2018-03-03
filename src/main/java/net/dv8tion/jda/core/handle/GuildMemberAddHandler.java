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

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import org.json.JSONObject;

public class GuildMemberAddHandler extends SocketHandler
{

    public GuildMemberAddHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("guild_id");
        if (api.getGuildLock().isLocked(id))
            return id;

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(id);
        if (guild == null)
        {
            api.getEventCache().cache(EventCache.Type.GUILD, id, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Caching member for guild that is not yet cached. Guild ID: {} JSON: {}", id, content);
            return null;
        }

        Member member = api.getEntityBuilder().createMember(guild, content);
        api.getEventManager().handle(
                new GuildMemberJoinEvent(
                        api, responseNumber,
                        member));
        api.getEventCache().playbackCache(EventCache.Type.USER, member.getUser().getIdLong());
        return null;
    }
}
