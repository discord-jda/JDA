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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import org.json.JSONObject;

public class MessageReactionBulkRemoveHandler extends SocketHandler
{
    public MessageReactionBulkRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String messageId = content.getString("message_id");
        String channelId = content.getString("channel_id");
        MessageChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
            channel = api.getPrivateChannelById(channelId);
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
            channel = api.asClient().getGroupById(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received a reaction for a channel that JDA does not currently have cached");
            return null;
        }
        api.getEventManager().handle(
                new MessageReactionRemoveAllEvent(
                        api, responseNumber,
                        messageId, channel));
        return null;
    }
}
