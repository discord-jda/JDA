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

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MessageReactionHandler extends SocketHandler
{

    private final boolean add;

    public MessageReactionHandler(JDAImpl api, boolean add)
    {
        super(api);
        this.add = add;
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getLong("guild_id");
            if (api.getGuildSetupController().isLocked(guildId))
                return guildId;
        }

        DataObject emoji = content.getObject("emoji");

        final long userId    = content.getLong("user_id");
        final long messageId = content.getLong("message_id");
        final long channelId = content.getLong("channel_id");

        final Long emojiId = emoji.isNull("id") ? null : emoji.getLong("id");
        String emojiName = emoji.getString("name", null);
        final boolean emojiAnimated = emoji.getBoolean("animated");

        if (emojiId == null && emojiName == null)
        {
            WebSocketClient.LOG.debug("Received a reaction {} with no name nor id. json: {}",
                JDALogger.getLazyString(() -> add ? "add" : "remove"), content);
            return null;
        }

        Guild guild = api.getGuildById(content.getUnsignedLong("guild_id", 0));
        MemberImpl member = null;
        if (guild != null)
        {
            member = (MemberImpl) guild.getMemberById(userId);
            // Attempt loading the member if possible
            Optional<DataObject> memberJson = content.optObject("member");
            if (memberJson.isPresent()) // Check if we can load a member here
            {
                DataObject json = memberJson.get();
                if (member == null || !member.hasTimeJoined()) // do we need to load a member?
                    member = api.getEntityBuilder().createMember((GuildImpl) guild, json);
                else // otherwise update the cache
                {
                    List<Role> roles = json.getArray("roles")
                            .stream(DataArray::getUnsignedLong)
                            .map(guild::getRoleById)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    api.getEntityBuilder().updateMember((GuildImpl) guild, member, json, roles);
                }
                // update internal references
                api.getEntityBuilder().updateMemberCache(member);
            }
            if (member == null && add && guild.isLoaded())
            {
                WebSocketClient.LOG.debug("Dropping reaction event for unknown member {}", content);
                return null;
            }
        }

        User user = api.getUserById(userId);
        if (user == null && member != null)
            user = member.getUser(); // this happens when we have guild subscriptions disabled
        if (user == null)
        {
            if (add && guild != null)
            {
                api.getEventCache().cache(EventCache.Type.USER, userId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received a reaction for a user that JDA does not currently have cached. " +
                        "UserID: {} ChannelId: {} MessageId: {}", userId, channelId, messageId);
                return null;
            }
        }

        //TODO-v5-unified-channel-cache
        MessageChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
            channel = api.getNewsChannelById(channelId);
        if (channel == null)
            channel = api.getThreadChannelById(channelId);
        if (channel == null)
            channel = api.getPrivateChannelById(channelId);
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a reaction for a channel that JDA does not currently have cached");
            return null;
        }

        MessageReaction.ReactionEmote rEmote;
        if (emojiId != null)
        {
            Emote emote = api.getEmoteById(emojiId);
            if (emote == null)
            {
                if (emojiName != null)
                {
                    emote = new EmoteImpl(emojiId, api).setAnimated(emojiAnimated).setName(emojiName);
                }
                else
                {
                    WebSocketClient.LOG.debug("Received a reaction {} with a null name. json: {}",
                        JDALogger.getLazyString(() -> add ? "add" : "remove"), content);
                    return null;
                }
            }
            rEmote = MessageReaction.ReactionEmote.fromCustom(emote);
        }
        else
        {
            rEmote = MessageReaction.ReactionEmote.fromUnicode(emojiName, api);
        }
        MessageReaction reaction = new MessageReaction(channel, rEmote, messageId, userId == api.getSelfUser().getIdLong(), -1);

        if (channel.getType() == ChannelType.PRIVATE)
            api.usedPrivateChannel(reaction.getChannel().getIdLong());

        if (add)
        {
            api.handleEvent(
                new MessageReactionAddEvent(
                    api, responseNumber,
                    user, member, reaction, userId));
        }
        else
        {
            api.handleEvent(
                new MessageReactionRemoveEvent(
                    api, responseNumber,
                    user, member, reaction, userId));
        }
        return null;
    }
}
