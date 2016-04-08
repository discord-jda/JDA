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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ReadyHandler extends SocketHandler
{
    private final EntityBuilder builder;

    private static Map<JDA, Set<String>> guildIds = new HashMap<>();
    private static Map<JDA, Set<String>> chunkIds = new HashMap<>();
    private static Map<JDA, JSONObject> cachedJson = new HashMap<>();

    public ReadyHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
        this.builder = new EntityBuilder(api);
        if (!guildIds.containsKey(api))
            guildIds.put(api, new HashSet<>());
        if (!chunkIds.containsKey(api))
            chunkIds.put(api, new HashSet<>());
    }

    @Override
    protected String handleInternally(final JSONObject content)
    {
        String oldGame = null;
        OnlineStatus oldStatus = null;

        if (api.getSelfInfo() != null)
        {
            oldGame = api.getSelfInfo().getCurrentGame();
            oldStatus = api.getSelfInfo().getOnlineStatus();
        }

        builder.createSelfInfo(content.getJSONObject("user"));

        if (oldGame != null)
            api.getAccountManager().setGame(oldGame);
        if (oldStatus != null && oldStatus.equals(OnlineStatus.AWAY))
            api.getAccountManager().setIdle(true);

        cachedJson.put(api, content);
        JSONArray guilds = content.getJSONArray("guilds");
        final Set<String> guildIds = ReadyHandler.guildIds.get(api);
        for (int i = 0; i < guilds.length(); i++)
        {
            JSONObject guildJson = guilds.getJSONObject(i);
            if (guildJson.has("unavailable") && guildJson.getBoolean("unavailable"))
            {
                guildIds.add(guildJson.getString("id"));
                builder.createGuildFirstPass(guildJson, null);
            }
            else if(guildJson.has("large") && guildJson.getBoolean("large"))
            {
                guildIds.add(guildJson.getString("id"));
                builder.createGuildFirstPass(guildJson, guild ->
                {
                    guildIds.remove(guild.getId());
                    if (guildIds.isEmpty())
                    {
                        finishReady(content);
                    }
                });
            }
            else
            {
                builder.createGuildFirstPass(guildJson, null);
            }
        }
        if (guildIds.isEmpty())
        {
            finishReady(content);
        }
        return null;
    }

    public void onGuildNeedsMembers(Guild g)
    {
        Set<String> chunks = chunkIds.get(api);
        chunks.add(g.getId());
        if (chunks.size() == guildIds.get(api).size())
        {
            sendChunks();
        }
    }

    public void onGuildInit(Guild guild)
    {
        Set<String> ids = guildIds.get(api);
        ids.remove(guild.getId());
        if (ids.isEmpty())
        {
            finishReady(cachedJson.get(api));
        }
        else if (ids.size() == chunkIds.get(api).size())
        {
            sendChunks();
        }
    }

    private void sendChunks()
    {
        Iterator<String> iterator = chunkIds.get(api).iterator();
        JSONArray arr = new JSONArray();
        while (iterator.hasNext())
        {
            arr.put(iterator.next());
            if (arr.length() == 50)
            {
                JSONObject obj = new JSONObject()
                        .put("op", 8)
                        .put("d", new JSONObject()
                                .put("guild_id", arr)
                                .put("query","")
                                .put("limit", 0)
                        );
                api.getClient().send(obj.toString());
                arr = new JSONArray();
            }
        }
        if (arr.length() > 0)
        {
            JSONObject obj = new JSONObject()
                    .put("op", 8)
                    .put("d", new JSONObject()
                            .put("guild_id", arr)
                            .put("query","")
                            .put("limit", 0)
                    );
            api.getClient().send(obj.toString());
        }
    }

    public void finishReady(JSONObject content)
    {
        JSONArray priv_chats = content.getJSONArray("private_channels");
        for (int i = 0; i < priv_chats.length(); i++)
        {
            builder.createPrivateChannel(priv_chats.getJSONObject(i));
        }
        api.getClient().ready();
    }
}
