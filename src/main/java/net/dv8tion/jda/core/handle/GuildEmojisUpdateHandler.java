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

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GuildEmojisUpdateHandler extends SocketHandler
{
    public GuildEmojisUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String guild_id = content.getString("guild_id");
        if (GuildLock.get(api).isLocked(guild_id))
        {
            return content.getString("guild_id");
        }

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(guild_id);
        if (guild == null)
        {
            EventCache.get(api).cache(EventCache.Type.GUILD, guild_id, () ->
                    handle(responseNumber, allContent));
            return null;
        }
        JSONArray array = content.getJSONArray("emojis");
        Map<String, Emote> emoteMap = guild.getEmoteMap();
        List<Emote> oldEmotes = new ArrayList<>(emoteMap.values()); //snapshot of emote cache
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject current = array.getJSONObject(i);
            String emoteId = current.getString("id");
            EmoteImpl emote = (EmoteImpl) emoteMap.get(emoteId);
            if (emote == null)
                emote = new EmoteImpl(emoteId, guild);
            else
                oldEmotes.remove(emote); // emote is in our cache which is why we don't want to remove it in cleanup later
            emote.setName(current.getString("name"))
                 .setManaged(current.getBoolean("managed"));
            //update roles
            JSONArray roles = current.getJSONArray("roles");
            Set<Role> newRoles = emote.getRoleSet();
            Set<Role> oldRoles = new HashSet<>(newRoles); //snapshot of cached roles
            for (int j = 0; j < roles.length(); j++)
            {
                Role role = guild.getRoleById(roles.getString(j));
                newRoles.add(role);
                oldRoles.remove(role);
            }
            //cleanup old cached roles that were not found in the JSONArray
            for (Role r : oldRoles)
                newRoles.remove(r); // newRoles directly writes to the set contained in the emote

            emoteMap.put(emote.getId(), emote); // finally, update the emote
        }
        //cleanup old emotes that don't exist anymore
        for (Emote e : oldEmotes)
            emoteMap.remove(e.getId());
        return null;
    }
}
