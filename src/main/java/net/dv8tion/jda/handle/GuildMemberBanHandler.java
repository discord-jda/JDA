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

import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.guild.member.GuildMemberBanEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberUnbanEvent;
import org.json.JSONObject;

public class GuildMemberBanHandler extends SocketHandler
{
    private final boolean banned;

    public GuildMemberBanHandler(JDAImpl api, int responseNumber, boolean banned)
    {
        super(api, responseNumber);
        this.banned = banned;
    }

    @Override
    public void handle(JSONObject content)
    {
        JSONObject userJson = content.getJSONObject("user");
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        if (banned)
        {
            User user = api.getUserMap().get(userJson.getString("id"));
            api.getEventManager().handle(
                    new GuildMemberBanEvent(
                            api, responseNumber,
                            guild, user));
        }
        else
        {
            String id = userJson.getString("id");
            String username = userJson.getString("username");
            String discriminator = userJson.get("discriminator").toString();
            api.getEventManager().handle(
                    new GuildMemberUnbanEvent(
                            api, responseNumber,
                            guild, id, username, discriminator));
        }
    }
}
