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
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class ReadyHandler extends SocketHandler
{
    private final Set<String> incompleteGuilds = new HashSet<>();
    private final Set<String> acknowledgedGuilds = new HashSet<>();
    private final Set<String> unavailableGuilds = new HashSet<>();
    private final Set<String> guildsRequiringChunking = new HashSet<>();
    private final Set<String> guildsRequiringSyncing = new HashSet<>();

    public ReadyHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        EntityBuilder builder = EntityBuilder.get(api);

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
        }

        //We use two different for-loops here so that we cache all of the ids before sending them off to the EntityBuilder
        //  due to the checks in checkIfReadyToSendRequests and guildSetupComplete triggering too soon otherwise.
        // Specifically: incompleteGuilds.size() == acknowledgedGuilds.size() and
        //  incompleteGuilds.size() == unavailableGuilds.size() respectively.

        for (int i = 0; i < guilds.length(); i++)
        {
            JSONObject guild = guilds.getJSONObject(i);

            //If a Guild isn't unavailable, then it is possible that we were given all information
            // needed to fully load the guild. In this case, we provide the method `guildSetupComplete`
            // as the secondPassCallback so it can immediately be called to signify that the provided guild
            // is loaded and ready to go.
            //If a Guild is unavailable it won't have the information needed, so we pass null as the secondPassCallback
            // for now and wait for the GUILD_CREATE event to give us the required information.
            if (guild.has("unavailable") && guild.getBoolean("unavailable"))
                builder.createGuildFirstPass(guild, null);
            else
                builder.createGuildFirstPass(guild, this::guildSetupComplete);
        }

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
        if (guilds.length() == 0)
            guildLoadComplete(content);

        return null;
    }

    public void guildLoadComplete(JSONObject content)
    {
        JSONArray privateChannels = content.getJSONArray("private_channels");

        EntityBuilder builder = EntityBuilder.get(api);
        for (int i = 0; i < privateChannels.length(); i++)
        {
            builder.createPrivateChannel(privateChannels.getJSONObject(i));
        }

        JSONArray readstates = content.has("read_state") ? content.getJSONArray("read_state") : null;
        JSONArray guildSettings = content.has("user_guild_settings") ? content.getJSONArray("user_guild_settings") : null;
        api.getClient().ready();
    }

    public void acknowledgeGuild(Guild guild, boolean available, boolean requiresChunking, boolean requiresSync)
    {
        acknowledgedGuilds.add(guild.getId());
        if (available)
        {
            //We remove from unavailable guilds because it is possible that we were told it was unavailable, but
            // during a long READY load it could have become available and was sent to us.
            unavailableGuilds.remove(guild.getId());
            if (requiresChunking)
                guildsRequiringChunking.add(guild.getId());
            if (requiresSync)
                guildsRequiringSyncing.add(guild.getId());
        }
        else
            unavailableGuilds.add(guild.getId());

        checkIfReadyToSendRequests();
    }

    public void guildSetupComplete(Guild guild)
    {
        incompleteGuilds.remove(guild.getId());
        if (incompleteGuilds.size() == unavailableGuilds.size())
            guildLoadComplete(allContent.getJSONObject("d"));
        else
            checkIfReadyToSendRequests();
    }


    public void clearCache()
    {
        incompleteGuilds.clear();
        acknowledgedGuilds.clear();
        unavailableGuilds.clear();
        guildsRequiringChunking.clear();
        guildsRequiringSyncing.clear();
    }

    private void checkIfReadyToSendRequests()
    {
        if (acknowledgedGuilds.size() == incompleteGuilds.size())
        {
            api.getClient().setChunkingAndSyncing();
            if (api.getAccountType() == AccountType.CLIENT)
                sendGuildSyncRequests();
            sendMemberChunkRequests();
        }
    }

    private void sendGuildSyncRequests()
    {
        if (guildsRequiringSyncing.isEmpty())
            return;

        JSONArray guildIds = new JSONArray();
        for (String guildId : guildsRequiringSyncing)
        {
            guildIds.put(guildId);

            //We can only request 50 guilds in a single request, so after we've reached 50, send them
            // and reset the
            if (guildIds.length() == 50)
            {
                api.getClient().send(new JSONObject()
                        .put("op", 12)
                        .put("d", guildIds).toString());
                guildIds = new JSONArray();
            }
        }

        //Send the remaining guilds that need to be sent
        if (guildIds.length() > 0)
        {
            api.getClient().send(new JSONObject()
                    .put("op", 12)
                    .put("d", guildIds).toString());
        }
        guildsRequiringSyncing.clear();
    }

    private void sendMemberChunkRequests()
    {
        if (guildsRequiringChunking.isEmpty())
            return;

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
