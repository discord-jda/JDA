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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

public class GuildMemberAddHandler extends SocketHandler
{

    public GuildMemberAddHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        if (guild == null)
        {
            EventCache.get(api).cache(EventCache.Type.GUILD, content.getString("guild_id"), () ->
            {
                handle(responseNumber, allContent);
            });
            return null;
        }

        Member member = EntityBuilder.get(api).createMember(guild, content);
        api.getEventManager().handle(
                new GuildMemberJoinEvent(
                        api, responseNumber,
                        guild, member));
        EventCache.get(api).playbackCache(EventCache.Type.USER, member.getUser().getId());
        return null;
    }
}
