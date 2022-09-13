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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
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
        JDAImpl jda = getJDA();

        Guild guild = null;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getUnsignedLong("guild_id");
            if (api.getGuildSetupController().isLocked(guildId))
                return guildId;

            guild = getJDA().getGuildById(guildId);
            if (guild == null)
            {
                jda.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Got MESSAGE_REACTION_REMOVE_ALL for a guild that is not yet cached. GuildId: {}", guildId);
                return null;
            }
        }

        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel == null)
        {
            // If discord adds message support for unexpected types in the future, drop the event instead of caching it
            if (guild != null)
            {
                GuildChannel actual = guild.getGuildChannelById(channelId);
                if (actual != null)
                {
                    WebSocketClient.LOG.debug("Dropping MESSAGE_REACTION_REMOVE_ALL for unexpected channel of type {}", actual.getType());
                    return null;
                }
            }

            jda.getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a reaction for a channel that JDA does not currently have cached channel_id: {} message_id: {}", channelId, messageId);
            return null;
        }

        jda.handleEvent(
            new MessageReactionRemoveAllEvent(
                jda, responseNumber,
                messageId, channel));
        return null;
    }
}
