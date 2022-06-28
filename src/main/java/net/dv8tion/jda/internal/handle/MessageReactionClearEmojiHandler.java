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
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class MessageReactionClearEmojiHandler extends SocketHandler
{
    public MessageReactionClearEmojiHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getUnsignedLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;
        Guild guild = getJDA().getGuildById(guildId);
        if (guild == null)
        {
            EventCache.LOG.debug("Caching MESSAGE_REACTION_REMOVE_EMOJI event for unknown guild {}", guildId);
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        long channelId = content.getUnsignedLong("channel_id");

        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, channelId);
        if (channel == null)
        {
            GuildChannel guildChannel = guild.getGuildChannelById(channelId);
            if (guildChannel != null)
            {
                WebSocketClient.LOG.debug("Discarding MESSAGE_REACTION_REMOVE_EMOJI event for unexpected channel type. Channel: {}", guildChannel);
                return null;
            }

            EventCache.LOG.debug("Caching MESSAGE_REACTION_REMOVE_EMOJI event for unknown channel {}", channelId);
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            return null;
        }

        long messageId = content.getUnsignedLong("message_id");
        DataObject emoji = content.getObject("emoji");
        if (emoji.isNull("name"))
            emoji.put("name", "");
        EmojiUnion reactionEmoji = Emoji.fromData(emoji);

        MessageReaction reaction = new MessageReaction(channel, reactionEmoji, messageId, false, 0);

        getJDA().handleEvent(new MessageReactionRemoveEmojiEvent(getJDA(), responseNumber, messageId, channel, reaction));
        return null;
    }
}
