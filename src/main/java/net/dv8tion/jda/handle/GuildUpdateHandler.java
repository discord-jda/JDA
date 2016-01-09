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

import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.guild.GuildUpdateEvent;
import org.json.JSONObject;

public class GuildUpdateHandler extends SocketHandler
{

    public GuildUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("id"));
        String name = content.getString("name");
        String iconId = content.isNull("icon") ? null : content.getString("icon");
        String afkChannelId = content.isNull("afk_channel_id") ? null : content.getString("afk_channel_id");
        Region region = Region.fromKey(content.getString("region"));
        int afkTimeout = content.getInt("afk_timeout");

        guild.setName(name)
            .setIconId(iconId)
            .setAfkChannelId(afkChannelId)
            .setRegion(region)
            .setAfkTimeout(afkTimeout);
        api.getEventManager().handle(
                new GuildUpdateEvent(
                        api, responseNumber,
                        guild));
    }
}
