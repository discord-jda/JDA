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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ReadyHandler extends SocketHandler
{
    private final Set<String> incompleteGuilds = new HashSet<>();
    private final Set<String> guildsRequiringChunking = new HashSet<>();

    public ReadyHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        EntityBuilder builder = new EntityBuilder(api);
//        System.out.println(content.toString(4));

        //Core
        JSONArray guilds = content.getJSONArray("guilds");
        JSONObject selfJson = content.getJSONObject("user");

        builder.createSelfInfo(selfJson);

        //Keep a list of all guilds in incompleteGuilds that need to be setup (GuildMemberChunk / GuildSync)
        //Send all guilds to the EntityBuilder's first pass to setup caching for when GUILD_CREATE comes
        // or, for Client accounts, to start the setup process (since we already have guild info)
        //Callback points to guildSetupComplete so that when MemberChunking and GuildSync processes are done, we can
        // "check off" the completed guild from the set of guilds in incompleteGuilds.
        for (int i = 0; i < guilds.length(); i++)
        {
            JSONObject guild = guilds.getJSONObject(i);
            incompleteGuilds.add(guild.getString("id"));
            builder.createGuildFirstPass(guild, null);
        }
        sendMemberChunkRequests();

        if (api.getAccountType() == AccountType.BOT)
        {

        }
        else
        {
            //GuildSync
            //GuildMemberChunk

            //Client
            JSONArray presences = content.getJSONArray("presences");
            JSONArray relationships = content.getJSONArray("relationships");
            JSONObject notes = content.getJSONObject("notes");
        }

        return null;
    }

    public void guildLoadComplete(JSONObject content)
    {
        JSONArray privateChannels = content.getJSONArray("private_channels");

        JSONArray readstates = content.has("read_state") ? content.getJSONArray("read_state") : null;
        JSONArray guildSettings = content.has("user_guild_settings") ? content.getJSONArray("user_guild_settings") : null;
        api.getClient().ready();
    }

    public void queueMemberChunkRequest(Guild guild)
    {
        guildsRequiringChunking.add(guild.getId());
    }

    public void guildSetupComplete(Guild guild)
    {
        incompleteGuilds.remove(guild.getId());
        System.out.println("Completed guild: " + guild.getId());
        if (incompleteGuilds.size() == 0)
        {
            guildLoadComplete(allContent.getJSONObject("d"));
        }
    }

    public void clearCache()
    {
        incompleteGuilds.clear();
        guildsRequiringChunking.clear();
    }

    private void sendMemberChunkRequests()
    {
        JSONArray guildIds = new JSONArray();
        for (String guildId : guildsRequiringChunking)
        {
            guildIds.put(guildId);

            //We can only request 50 guilds in a single request, so after we've reached 50, send them
            // and reset the
            if (guildIds.length() == 50)
            {
                api.getClient().send(new JSONObject()
                    .put("op", 8)
                    .put("d", new JSONObject()
                        .put("guild_id", guildIds)
                        .put("query", "")
                        .put("limit", 0)
                    ).toString());
                guildIds = new JSONArray();
            }
        }

        //Send the remaining guilds that need to be sent
        if (guildIds.length() > 0)
        {
            api.getClient().send(new JSONObject()
                .put("op", 8)
                .put("d", new JSONObject()
                        .put("guild_id", guildIds)
                        .put("query", "")
                        .put("limit", 0)
                ).toString());
        }
        guildsRequiringChunking.clear();
    }
}
