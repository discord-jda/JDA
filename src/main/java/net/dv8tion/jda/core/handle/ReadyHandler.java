/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.entities.impl.FriendImpl;
import net.dv8tion.jda.client.entities.impl.UserSettingsImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReadyHandler extends SocketHandler
{

    public ReadyHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        EntityBuilder builder = api.getEntityBuilder();

        //Core
        JSONArray guilds = content.getJSONArray("guilds");
        JSONObject selfJson = content.getJSONObject("user");

        builder.createSelfUser(selfJson);

        if (api.getAccountType() == AccountType.CLIENT && !content.isNull("user_settings"))
        {
            // handle user settings
            JSONObject userSettingsJson = content.getJSONObject("user_settings");
            UserSettingsImpl userSettingsObj = (UserSettingsImpl) api.asClient().getSettings();
            userSettingsObj
                    // TODO: set all information and handle updates
                    .setStatus(userSettingsJson.isNull("status") ? OnlineStatus.ONLINE : OnlineStatus.fromKey(userSettingsJson.getString("status")));
            // update presence information unless the status is ONLINE
            if (userSettingsObj.getStatus() != OnlineStatus.ONLINE)
                ((PresenceImpl) api.getPresence()).setCacheStatus(userSettingsObj.getStatus());
        }

        if (api.getGuildSetupController().setIncompleteCount(guilds.length()))
        {
            for (int i = 0; i < guilds.length(); i++)
            {
                JSONObject guild = guilds.getJSONObject(i);
                api.getGuildSetupController().onReady(guild.getLong("id"), guild);
            }
        }

        handleReady(content);
        return null;
    }

    public void handleReady(JSONObject content)
    {
//        api.getClient().setChunkingAndSyncing(false);
        EntityBuilder builder = api.getEntityBuilder();
        JSONArray privateChannels = content.getJSONArray("private_channels");

        if (api.getAccountType() == AccountType.CLIENT)
        {
            JSONArray relationships = content.getJSONArray("relationships");
            JSONArray presences = content.getJSONArray("presences");

            for (int i = 0; i < relationships.length(); i++)
            {
                JSONObject relationship = relationships.getJSONObject(i);
                Relationship r = builder.createRelationship(relationship);
                if (r == null)
                    JDAImpl.LOG.error("Provided relationship in READY with an unknown type! JSON: {}", relationship);
            }

            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                long userId = presence.getJSONObject("user").getLong("id");
                FriendImpl friend = (FriendImpl) api.asClient().getFriendById(userId);
                if (friend == null)
                    WebSocketClient.LOG.warn("Received a presence in the Presences array in READY that did not correspond to a cached Friend! JSON: {}", presence);
                else
                    builder.createPresence(friend, presence);
            }
        }

        for (int i = 0; i < privateChannels.length(); i++)
        {
            JSONObject chan = privateChannels.getJSONObject(i);
            ChannelType type = ChannelType.fromId(chan.getInt("type"));

            switch (type)
            {
                case PRIVATE:
                    builder.createPrivateChannel(chan);
                    break;
                case GROUP:
                    builder.createGroup(chan);
                    break;
                default:
                    WebSocketClient.LOG.warn("Received a Channel in the private_channels array in READY of an unknown type! JSON: {}", type);
            }
        }

//        api.getClient().ready();
    }
//
//    public void acknowledgeGuild(Guild guild, boolean available, boolean requiresChunking, boolean requiresSync)
//    {
//        acknowledgedGuilds.add(guild.getIdLong());
//        if (available)
//        {
//            //We remove from unavailable guilds because it is possible that we were told it was unavailable, but
//            // during a long READY load it could have become available and was sent to us.
//            unavailableGuilds.remove(guild.getIdLong());
//            if (requiresChunking)
//                guildsRequiringChunking.add(guild.getIdLong());
//            if (requiresSync)
//                guildsRequiringSyncing.add(guild.getIdLong());
//        }
//        else
//            unavailableGuilds.add(guild.getIdLong());
//
//        checkIfReadyToSendRequests();
//    }
//
//    public void guildSetupComplete(Guild guild)
//    {
//        if (!incompleteGuilds.remove(guild.getIdLong()))
//            WebSocketClient.LOG.error("Completed the setup for Guild: {} without matching id in ReadyHandler cache", guild);
//        if (incompleteGuilds.size() == unavailableGuilds.size())
//            guildLoadComplete(allContent.getJSONObject("d"));
//        else
//            checkIfReadyToSendRequests();
//    }
//
//
//    public void clearCache()
//    {
//        incompleteGuilds.clear();
//        acknowledgedGuilds.clear();
//        unavailableGuilds.clear();
//        guildsRequiringChunking.clear();
//        guildsRequiringSyncing.clear();
//    }
//
//    private void checkIfReadyToSendRequests()
//    {
//        if (acknowledgedGuilds.size() == incompleteGuilds.size())
//        {
//            api.getClient().setChunkingAndSyncing(true);
//            if (api.getAccountType() == AccountType.CLIENT)
//                sendGuildSyncRequests();
//            sendMemberChunkRequests();
//        }
//    }
//
//    private void sendGuildSyncRequests()
//    {
//        if (guildsRequiringSyncing.isEmpty())
//            return;
//
//        JSONArray guildIds = new JSONArray();
//
//        for (TLongIterator it = guildsRequiringSyncing.iterator(); it.hasNext(); )
//        {
//            guildIds.put(it.next());
//
//            //We can only request 50 guilds in a single request, so after we've reached 50, send them
//            // and reset the
//            if (guildIds.length() == 50)
//            {
//                api.getClient().chunkOrSyncRequest(new JSONObject()
//                        .put("op", WebSocketCode.GUILD_SYNC)
//                        .put("d", guildIds));
//                guildIds = new JSONArray();
//            }
//        }
//
//        //Send the remaining guilds that need to be sent
//        if (guildIds.length() > 0)
//        {
//            api.getClient().chunkOrSyncRequest(new JSONObject()
//                    .put("op", WebSocketCode.GUILD_SYNC)
//                    .put("d", guildIds));
//        }
//        guildsRequiringSyncing.clear();
//    }
//
//    private void sendMemberChunkRequests()
//    {
//        if (guildsRequiringChunking.isEmpty())
//            return;
//
//        JSONArray guildIds = new JSONArray();
//        for (TLongIterator it = guildsRequiringChunking.iterator(); it.hasNext(); )
//        {
//            guildIds.put(it.next());
//
//            //We can only request 50 guilds in a single request, so after we've reached 50, send them
//            // and reset the
//            if (guildIds.length() == 50)
//            {
//                api.getClient().chunkOrSyncRequest(new JSONObject()
//                    .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
//                    .put("d", new JSONObject()
//                        .put("guild_id", guildIds)
//                        .put("query", "")
//                        .put("limit", 0)
//                    ));
//                guildIds = new JSONArray();
//            }
//        }
//
//        //Send the remaining guilds that need to be sent
//        if (guildIds.length() > 0)
//        {
//            api.getClient().chunkOrSyncRequest(new JSONObject()
//                .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
//                .put("d", new JSONObject()
//                        .put("guild_id", guildIds)
//                        .put("query", "")
//                        .put("limit", 0)
//                ));
//        }
//        guildsRequiringChunking.clear();
//    }
}
