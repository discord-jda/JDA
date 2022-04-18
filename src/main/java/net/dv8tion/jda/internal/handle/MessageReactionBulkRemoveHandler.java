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
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
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

            guild = api.getGuildById(guildId);
            if (guild == null)
            {
                EventCache.LOG.debug("Caching MESSAGE_REACTION_REMOVE_ALL event for guild that is not currently cached. GuildID: {}", guildId);
                api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                return null;
            }
        }

        MessageChannel channel = getJDA().getChannelById(MessageChannel.class, channelId);
        if (channel == null)
        {
            if (guild != null)
            {
                GuildChannel guildChannel = guild.getGuildChannelById(channelId);
                if (guildChannel != null)
                {
                    WebSocketClient.LOG.debug("Discarding MESSAGE_REACTION_REMOVE_ALL event for unexpected channel type. Channel: {}", guildChannel);
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
