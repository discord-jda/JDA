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
        EntityBuilder builder = getJDA().getEntityBuilder();

        //Core
        JSONArray guilds = content.getJSONArray("guilds");
        JSONObject selfJson = content.getJSONObject("user");

        builder.createSelfUser(selfJson);

        if (getJDA().getAccountType() == AccountType.CLIENT && !content.isNull("user_settings"))
        {
            // handle user settings
            JSONObject userSettingsJson = content.getJSONObject("user_settings");
            UserSettingsImpl userSettingsObj = (UserSettingsImpl) getJDA().asClient().getSettings();
            userSettingsObj
                    // TODO: set all information and handle updates
                    .setStatus(userSettingsJson.isNull("status") ? OnlineStatus.ONLINE : OnlineStatus.fromKey(userSettingsJson.getString("status")));
            // update presence information unless the status is ONLINE
            if (userSettingsObj.getStatus() != OnlineStatus.ONLINE)
                ((PresenceImpl) getJDA().getPresence()).setCacheStatus(userSettingsObj.getStatus());
        }

        if (getJDA().getGuildSetupController().setIncompleteCount(guilds.length()))
        {
            for (int i = 0; i < guilds.length(); i++)
            {
                JSONObject guild = guilds.getJSONObject(i);
                getJDA().getGuildSetupController().onReady(guild.getLong("id"), guild);
            }
        }

        handleReady(content);
        return null;
    }

    public void handleReady(JSONObject content)
    {
        EntityBuilder builder = getJDA().getEntityBuilder();
        JSONArray privateChannels = content.getJSONArray("private_channels");

        if (getJDA().getAccountType() == AccountType.CLIENT)
        {
            JSONArray relationships = content.getJSONArray("relationships");
            JSONArray presences = content.getJSONArray("presences");

            for (int i = 0; i < relationships.length(); i++)
            {
                JSONObject relationship = relationships.getJSONObject(i);
                Relationship r = builder.createRelationship(relationship);
                if (r == null)
                    JDAImpl.LOG.warn("Provided relationship in READY with an unknown type! JSON: {}", relationship);
            }

            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                long userId = presence.getJSONObject("user").getLong("id");
                FriendImpl friend = (FriendImpl) getJDA().asClient().getFriendById(userId);
                if (friend == null)
                    WebSocketClient.LOG.debug("Received a presence in the Presences array in READY that did not correspond to a cached Friend! JSON: {}", presence);
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
    }
}
