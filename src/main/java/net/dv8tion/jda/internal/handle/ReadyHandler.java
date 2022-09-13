/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.handle;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class ReadyHandler extends SocketHandler
{

    public ReadyHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        EntityBuilder builder = getJDA().getEntityBuilder();

        DataArray guilds = content.getArray("guilds");
        //Make sure we don't have any duplicates here!
        TLongObjectMap<DataObject> distinctGuilds = new TLongObjectHashMap<>();
        for (int i = 0; i < guilds.length(); i++)
        {
            DataObject guild = guilds.getObject(i);
            long id = guild.getUnsignedLong("id");
            DataObject previous = distinctGuilds.put(id, guild);
            if (previous != null)
                WebSocketClient.LOG.warn("Found duplicate guild for id {} in ready payload", id);
        }

        DataObject selfJson = content.getObject("user");
        // Inject the application id which isn't added to the self user by default
        selfJson.put("application_id", // Used to update SelfUser#getApplicationId
            content.optObject("application")
                .map(obj -> obj.getUnsignedLong("id"))
                .orElse(selfJson.getUnsignedLong("id"))
        );
        // SelfUser is already created in login(...) but this just updates it to the current state from the api, and injects the application id
        builder.createSelfUser(selfJson);

        if (getJDA().getGuildSetupController().setIncompleteCount(distinctGuilds.size()))
        {
            distinctGuilds.forEachEntry((id, guild) ->
            {
                getJDA().getGuildSetupController().onReady(id, guild);
                return true;
            });
        }

        handleReady(content);
        return null;
    }

    public void handleReady(DataObject content)
    {
        EntityBuilder builder = getJDA().getEntityBuilder();
        DataArray privateChannels = content.getArray("private_channels");

        for (int i = 0; i < privateChannels.length(); i++)
        {
            DataObject chan = privateChannels.getObject(i);
            ChannelType type = ChannelType.fromId(chan.getInt("type"));

            //noinspection SwitchStatementWithTooFewBranches
            switch (type)
            {
                case PRIVATE:
                    builder.createPrivateChannel(chan);
                    break;
                default:
                    WebSocketClient.LOG.warn("Received a Channel in the private_channels array in READY of an unknown type! Type: {}", type);
            }
        }
    }
}
