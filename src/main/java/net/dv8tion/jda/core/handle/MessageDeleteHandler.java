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

import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.message.group.GroupMessageDeleteEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageDeleteEvent;
import org.json.JSONObject;

public class MessageDeleteHandler extends SocketHandler
{

    public MessageDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
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
        if (channel == null && getJDA().getAccountType() == AccountType.CLIENT)
        {
            channel = getJDA().asClient().getGroupById(channelId);
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
            getJDA().getEventManager().handle(
                    new GuildMessageDeleteEvent(
                            getJDA(), responseNumber,
                            messageId, tChan));
        }
        else if (channel instanceof PrivateChannel)
        {
            PrivateChannelImpl pChan = (PrivateChannelImpl) channel;
            if (channel.hasLatestMessage() && messageId == channel.getLatestMessageIdLong())
                pChan.setLastMessageId(0); // Reset latest message id as it was deleted.
            getJDA().getEventManager().handle(
                    new PrivateMessageDeleteEvent(
                            getJDA(), responseNumber,
                            messageId, pChan));
        }
        else
        {
            GroupImpl group = (GroupImpl) channel;
            if (channel.hasLatestMessage() && messageId == channel.getLatestMessageIdLong())
                group.setLastMessageId(0); // Reset latest message id as it was deleted.
            getJDA().getEventManager().handle(
                    new GroupMessageDeleteEvent(
                            getJDA(), responseNumber,
                            messageId, group));
        }

        //Combo event
        getJDA().getEventManager().handle(
                new MessageDeleteEvent(
                        getJDA(), responseNumber,
                        messageId, channel));
        return null;
    }
}
