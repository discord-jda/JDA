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
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

import java.util.List;
import java.util.stream.Collectors;

public class MessageBulkDeleteHandler extends SocketHandler
{
    public MessageBulkDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        Guild guild = null;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getLong("guild_id");
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;

            guild = api.getGuildById(guildId);
            if (guild == null)
            {
                EventCache.LOG.debug("Caching MESSAGE_DELETE event for guild that is not currently cached. GuildID: {}", guildId);
                api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                return null;
            }
        }
        final long channelId = content.getLong("channel_id");

        if (getJDA().isBulkDeleteSplittingEnabled())
        {
            SocketHandler handler = getJDA().getClient().getHandlers().get("MESSAGE_DELETE");
            content.getArray("ids").forEach(id ->
            {
                handler.handle(responseNumber, DataObject.empty()
                    .put("t", "MESSAGE_DELETE")
                    .put("d", DataObject.empty()
                        .put("channel_id", Long.toUnsignedString(channelId))
                        .put("id", id)));
            });
        }
        else
        {
            GuildMessageChannel channel = getJDA().getChannelById(GuildMessageChannel.class, channelId);

            if (channel == null)
            {
                if (guild != null)
                {
                    GuildChannel guildChannel = guild.getGuildChannelById(channelId);
                    if (guildChannel != null)
                    {
                        WebSocketClient.LOG.debug("Discarding MESSAGE_DELETE event for unexpected channel type. Channel: {}", guildChannel);
                        return null;
                    }
                }

                getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received a Bulk Message Delete for a GuildMessageChannel that is not yet cached.");
                return null;
            }

            if (getJDA().getGuildSetupController().isLocked(channel.getGuild().getIdLong()))
                return channel.getGuild().getIdLong();

            DataArray array = content.getArray("ids");
            List<String> messages = array.stream(DataArray::getString).collect(Collectors.toList());
            getJDA().handleEvent(
                new MessageBulkDeleteEvent(
                    getJDA(), responseNumber,
                    channel, messages));
        }
        return null;
    }
}
