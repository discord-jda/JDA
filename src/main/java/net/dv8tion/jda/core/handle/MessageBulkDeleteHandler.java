/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageBulkDeleteHandler extends SocketHandler
{
    public MessageBulkDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String channelId = content.getString("channel_id");

        if (api.isBulkDeleteSplittingEnabled())
        {
            SocketHandler handler = api.getClient().getHandler("MESSAGE_DELETE");
            content.getJSONArray("ids").forEach(id ->
            {

                handler.handle(responseNumber, new JSONObject()
                    .put("d", new JSONObject()
                        .put("channel_id", channelId)
                        .put("id", id)));
            });
        }
        else
        {
            TextChannel channel = api.getTextChannelMap().get(channelId);
            if (channel == null)
            {
                EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
                {
                    handle(responseNumber, allContent);
                });
                EventCache.LOG.debug("Received a Bulk Message Delete for a TextChannel that is not yet cached.");
                return null;
            }

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
        return null;
    }
}
