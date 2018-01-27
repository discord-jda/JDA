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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.json.JSONObject;

public class MessageReactionBulkRemoveHandler extends SocketHandler
{
    public MessageReactionBulkRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long messageId = content.getLong("message_id");
        final long channelId = content.getLong("channel_id");
        MessageChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a reaction for a channel that JDA does not currently have cached channel_id: {} message_id: {}", channelId, messageId);
            return null;
        }
        IEventManager manager = api.getEventManager();

        switch (channel.getType())
        {
            case TEXT:
               manager.handle(
                   new GuildMessageReactionRemoveAllEvent(
                           api, responseNumber,
                           messageId, (TextChannel) channel));
               break;
            case GROUP:
                manager.handle(
                    new GroupMessageReactionRemoveAllEvent(
                            api, responseNumber,
                            messageId, (Group) channel));
        }

        manager.handle(
            new MessageReactionRemoveAllEvent(
                    api, responseNumber,
                    messageId, channel));
        return null;
    }
}
