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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.Emote;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.impl.EmoteImpl;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class GuildEmojisUpdateHandler extends SocketHandler
{
    public GuildEmojisUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
            return content.getString("guild_id");
        Guild guild = api.getGuildMap().get(content.getString("guild_id"));
        if (guild == null)
        {
            JDAImpl.LOG.warn("Received Emojis Update for Guild we can't see. Ignoring event...");
            return null;
        }
        List<Emote> oldEmotes = new LinkedList<>(guild.getEmotes());
        JSONArray array = content.getJSONArray("emojis");

        for (int i = 0; i < array.length(); i++)
        {
            JSONObject obj = array.getJSONObject(i);
            String id = obj.getString("id");
            String name = obj.getString("name");
            EmoteImpl emote = (EmoteImpl) api.getEmoteById(id);
            if (emote == null)
            {
                emote = new EmoteImpl(name, id, guild);
                api.getEmoteMap().put(id, emote);
            }
            ((GuildImpl) guild).getEmoteMap().put(id, emote);
            oldEmotes.remove(emote);
        }

        // Clean up emotes we can't see anymore
        for (Emote e : oldEmotes)
        {
            ((GuildImpl) guild).getEmoteMap().remove(e.getId());
            api.getEmoteMap().remove(e.getId());
        }

        return null;
    }

}
