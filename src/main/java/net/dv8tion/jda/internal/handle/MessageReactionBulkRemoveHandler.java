/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class MessageReactionBulkRemoveHandler extends SocketHandler
{
    public MessageReactionBulkRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long messageId = content.getLong("message_id");
        final long channelId = content.getLong("channel_id");
        TextChannel channel = getJDA().getTextChannelById(channelId);
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a reaction for a channel that JDA does not currently have cached channel_id: {} message_id: {}", channelId, messageId);
            return null;
        }
        IEventManager manager = getJDA().getEventManager();

        switch (channel.getType())
        {
            case TEXT:
               manager.handle(
                   new GuildMessageReactionRemoveAllEvent(
                           getJDA(), responseNumber,
                           messageId, channel));
               break;
            case GROUP:
                WebSocketClient.LOG.error("Received a reaction bulk delete for a group which should not be possible");
                return null;
        }

        manager.handle(
            new MessageReactionRemoveAllEvent(
                    getJDA(), responseNumber,
                    messageId, channel));
        return null;
    }
}
