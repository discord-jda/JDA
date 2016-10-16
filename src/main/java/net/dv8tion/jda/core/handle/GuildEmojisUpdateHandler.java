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

package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuildEmojisUpdateHandler extends SocketHandler
{
    public GuildEmojisUpdateHandler(JDAImpl api)
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
                    handle(responseNumber, allContent));
            return null;
        }
        JSONArray array = content.getJSONArray("emojis");
        List<Emote> oldEmotes = new ArrayList<>(guild.getEmotes());
        Map<String, Emote> emoteMap = guild.getEmoteMap();
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject current = array.getJSONObject(i);
            EmoteImpl emote = (EmoteImpl) emoteMap.get(current.getString("id"));
            if (emote == null)
                emote = new EmoteImpl(current.getString("id"), guild);
            else
                oldEmotes.remove(emote);
            JSONArray roles = current.getJSONArray("roles");
            Role[] newRoles = new Role[roles.length()];
            for (int j = 0; j < roles.length(); j++)
                newRoles[j] = guild.getRoleById(roles.getString(j));
            emote.overrideRoles(newRoles).setName(current.getString("name")).setManaged(current.getBoolean("managed"));
            emoteMap.put(emote.getId(), emote);
        }
        for (Emote e : oldEmotes)
            emoteMap.remove(e.getId());
        return null;
    }
}
