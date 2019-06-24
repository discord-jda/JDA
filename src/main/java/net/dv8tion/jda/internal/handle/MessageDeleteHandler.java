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

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.PrivateChannelImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;

public class MessageDeleteHandler extends SocketHandler
{

    public MessageDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long messageId = content.getLong("id");
        final long channelId = content.getLong("channel_id");

        MessageChannel channel = getJDA().getTextChannelById(channelId);
        if (channel == null)
        {
            channel = getJDA().getPrivateChannelById(channelId);
        }
        if (channel == null)
        {
            channel = getJDA().getFakePrivateChannelMap().get(channelId);
        }
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Got message delete for a channel/group that is not yet cached. ChannelId: {}", channelId);
            return null;
        }

        if (channel instanceof TextChannel)
        {
            TextChannelImpl tChan = (TextChannelImpl) channel;
            if (getJDA().getGuildSetupController().isLocked(tChan.getGuild().getIdLong()))
                return tChan.getGuild().getIdLong();
            if (tChan.hasLatestMessage() && messageId == channel.getLatestMessageIdLong())
                tChan.setLastMessageId(0); // Reset latest message id as it was deleted.
            getJDA().handleEvent(
                    new GuildMessageDeleteEvent(
                            getJDA(), responseNumber,
                            messageId, tChan));
        }
        else
        {
            PrivateChannelImpl pChan = (PrivateChannelImpl) channel;
            if (channel.hasLatestMessage() && messageId == channel.getLatestMessageIdLong())
                pChan.setLastMessageId(0); // Reset latest message id as it was deleted.
            getJDA().handleEvent(
                    new PrivateMessageDeleteEvent(
                            getJDA(), responseNumber,
                            messageId, pChan));
        }

        //Combo event
        getJDA().handleEvent(
                new MessageDeleteEvent(
                        getJDA(), responseNumber,
                        messageId, channel));
        return null;
    }
}
