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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class ReadyHandler extends SocketHandler
{
    private final Set<String> expectedGuilds = new HashSet<>();

    public ReadyHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        EntityBuilder builder = new EntityBuilder(api);
        System.out.println(content.toString(4));

        //Core
        JSONArray guilds = content.getJSONArray("guilds");
        JSONObject selfJson = content.getJSONObject("user");

        //Client
        JSONArray presences = content.getJSONArray("presences");
        JSONArray relationships = content.has("relationships") ? content.getJSONArray("relationships") : null;
        JSONObject notes = content.has("notes") ? content.getJSONObject("notes") : null;

        builder.createSelfInfo(selfJson);

        if (guilds.length() == 0)
        {
            guildLoadComplete(content);
            return null;
        }

        for (int i = 0; i < guilds.length(); i++)
        {
            JSONObject guild = guilds.getJSONObject(i);
            expectedGuilds.add(guild.getString("id"));

            if (guild.has("unavailable") && guild.getBoolean("unavailable"))
            {

            }
            else
            {

            }
            //TODO: send to EntityBuilder
        }

        return null;
    }

    public void guildLoadComplete(JSONObject content)
    {
        JSONArray privateChannels = content.getJSONArray("private_channels");

        JSONArray readstates = content.has("read_state") ? content.getJSONArray("read_state") : null;
        JSONArray guildSettings = content.has("user_guild_settings") ? content.getJSONArray("user_guild_settings") : null;
    }

    public void finish()
    {
        api.getClient().ready();
    }

    public void clearCache()
    {
        expectedGuilds.clear();
    }
}
