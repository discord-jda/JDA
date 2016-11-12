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

import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageBulkDeleteHandler extends SocketHandler
{
    public MessageBulkDeleteHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String channelId = content.getString("channel_id");

        if (api.isBulkDeleteSplittingEnabled())
        {
            content.getJSONArray("ids").forEach(id ->
            {
                new MessageDeleteHandler(api, responseNumber).handle(new JSONObject()
                    .put("d", new JSONObject()
                        .put("channel_id", channelId)
                        .put("id", id)
                    ));
            });
        }
        else
        {
            TextChannel channel = api.getChannelMap().get(channelId);
            if (channel != null)
            {
                if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
                {
                    return channel.getGuild().getId();
                }
                LinkedList<String> msgIds = new LinkedList<>();
                content.getJSONArray("ids").forEach(id -> msgIds.add((String) id));
                api.getEventManager().handle(
                        new MessageBulkDeleteEvent(
                                api, responseNumber,
                                channel, msgIds));
            }
        }
        return null;
    }
}
