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
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.PrivateChannelImpl;
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

        if (emojiId == null && emojiName == null)
        {
            WebSocketClient.LOG.debug("Received a reaction {} with no name nor id. json: {}",
                JDALogger.getLazyString(() -> add ? "add" : "remove"), content);
            return null;
        }
        final long guildId = content.getUnsignedLong("guild_id", 0);
        Guild guild = api.getGuildById(guildId);
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
            // We expect there to be a user object already cached when we are in a guild and adding a new reaction as the user should be a member cached in the guild.
            // The event in the context of a guild will also provide a member object, if the required intents are present.
            // The only time we can receive a reaction add but not have the user cached would be if we receive the event in an uncached or partially built PrivateChannel.
            if (add && guild != null)
            {
                api.getEventCache().cache(EventCache.Type.USER, userId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received a reaction for a user that JDA does not currently have cached. " +
                        "UserID: {} ChannelId: {} MessageId: {}", userId, channelId, messageId);
                return null;
            }
        }

        MessageChannel channel = api.getChannelById(MessageChannel.class, channelId);
        if (channel == null)
        {
            // If discord adds message support for unexpected types in the future, drop the event instead of caching it
            if (guild != null)
            {
                GuildChannel actual = guild.getGuildChannelById(channelId);
                if (actual != null)
                {
                    WebSocketClient.LOG.debug("Dropping MESSAGE_REACTION event for unexpected channel of type {}", actual.getType());
                    return null;
                }
            }

            if (guildId != 0)
            {
                api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received a reaction for a channel that JDA does not currently have cached");
                return null;
            }

            //create a new private channel with minimal information for this event
            channel = getJDA().getEntityBuilder().createPrivateChannel(
                    DataObject.empty()
                            .put("id", channelId)
            );
        }

        // reaction remove has null name sometimes
        EmojiUnion rEmoji = EntityBuilder.createEmoji(emoji);

        MessageReaction reaction = new MessageReaction(channel, rEmoji, messageId, userId == api.getSelfUser().getIdLong(), -1);

        if (channel.getType() == ChannelType.PRIVATE)
        {
            api.usedPrivateChannel(reaction.getChannel().getIdLong());
            PrivateChannelImpl priv = (PrivateChannelImpl) channel;
            //try to add the user here if we need to, as we have their ID
            if (priv.getUser() == null && user != null)
            {
                priv.setUser(user);
            }
        }

        if (add)
        {
            api.handleEvent(
                new MessageReactionAddEvent(
                    api, responseNumber,
                    user, member, reaction, userId, content.getUnsignedLong("message_author_id", 0L)));
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
