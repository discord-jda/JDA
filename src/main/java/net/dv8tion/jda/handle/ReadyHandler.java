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

import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.ReadyEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ReadyHandler extends SocketHandler
{
    private final EntityBuilder builder;

    public ReadyHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
        this.builder = new EntityBuilder(api);
    }

    @Override
    public void handle(final JSONObject content)
    {
        //TODO: User-Setings; read_state
        builder.createSelfInfo(content.getJSONObject("user"));
//        JSONArray muted = content.getJSONObject("user_settings").getJSONArray("muted_channels");
//        List<String> mutedChannelIds = new ArrayList<>();
//        List<TextChannel> mutedChannels = new ArrayList<>();
//        for (int i = 0; i < muted.length(); i++)
//        {
//            mutedChannelIds.add(muted.getString(i));
//        }
        JSONArray guilds = content.getJSONArray("guilds");
        final List<String> guildIds = new LinkedList<>();
        for (int i = 0; i < guilds.length(); i++)
        {
            JSONObject guildJson = guilds.getJSONObject(i);
            if(guildJson.has("large") && guildJson.getBoolean("large"))
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
//            Iterator<String> iterator = mutedChannelIds.iterator();
//            while (iterator.hasNext())
//            {
//                String id = iterator.next();
//                TextChannel chan = ((GuildImpl) guild).getTextChannelsMap().get(id);
//                if (chan != null)
//                {
//                    mutedChannels.add(chan);
//                    iterator.remove();
//                }
//            }
        }
        if (guildIds.isEmpty())
        {
            finishReady(content);
        }
//        ((SelfInfoImpl) api.getSelfInfo()).setMutedChannels(mutedChannels);
    }

    public void finishReady(JSONObject content)
    {
        JSONArray priv_chats = content.getJSONArray("private_channels");
        for (int i = 0; i < priv_chats.length(); i++)
        {
            builder.createPrivateChannel(priv_chats.getJSONObject(i));
        }

        System.out.println("Finished Loading!");    //TODO: Replace with Logger.INFO
        api.getEventManager().handle(new ReadyEvent(api, responseNumber));
        api.getClient().ready();
    }
}
