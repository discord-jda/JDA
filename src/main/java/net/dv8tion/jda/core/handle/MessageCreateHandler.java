/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.entities.message.GroupIconMessage;
import net.dv8tion.jda.client.entities.message.GroupNameMessage;
import net.dv8tion.jda.client.entities.message.GroupRecipientMessage;
import net.dv8tion.jda.client.events.message.group.GroupCallMessageEvent;
import net.dv8tion.jda.client.events.message.group.GroupIconMessageEvent;
import net.dv8tion.jda.client.events.message.group.GroupMessageReceivedEvent;
import net.dv8tion.jda.client.events.message.group.GroupNameMessageEvent;
import net.dv8tion.jda.client.events.message.group.recipient.GroupRecipientAddMessageEvent;
import net.dv8tion.jda.client.events.message.group.recipient.GroupRecipientRemoveMessageEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.entities.message.*;
import net.dv8tion.jda.core.events.message.CallMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.PinMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMemberJoinMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateCallMessageEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class MessageCreateHandler extends SocketHandler
{
    public MessageCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        MessageType type = MessageType.fromId(content.getInt("type"));

        switch (type)
        {
            case DEFAULT:
                return handleDefaultMessage(content);
            case CHANNEL_PINNED_ADD:
                return handlePinSystemMessage(content);
            case CALL:
                return handleCallSystemMessage(content);
            case GUILD_MEMBER_JOIN:
                return handleWelcomeMessage(content);
            case CHANNEL_NAME_CHANGE:
            case CHANNEL_ICON_CHANGE:
            case RECIPIENT_ADD:
            case RECIPIENT_REMOVE:
                return handleGroupSystemMessage(content, type);
            default:
                WebSocketClient.LOG.debug("JDA received a message of unknown type. Type: " + type + "  JSON: " + content);
        }
        return null;
    }

    private Long handleDefaultMessage(JSONObject content)
    {
        Message message;
        try
        {
            message = api.getEntityBuilder().createMessage(content, true);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.getLong("channel_id");
                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("Received a message for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    final long authorId = content.getJSONObject("author").getLong("id");
                    api.getEventCache().cache(EventCache.Type.USER, authorId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("Received a message for a user that JDA does not currently have cached");
                    return null;
                }
                default:
                    throw e;
            }
        }

        final IEventManager manager = api.getEventManager();
        switch (message.getChannelType())
        {
            case TEXT:
            {
                TextChannelImpl channel = (TextChannelImpl) message.getTextChannel();
                if (api.getGuildLock().isLocked(channel.getGuild().getIdLong()))
                {
                    return channel.getGuild().getIdLong();
                }
                channel.setLastMessageId(message.getIdLong());
                manager.handle(
                    new GuildMessageReceivedEvent(
                        api, responseNumber,
                        message));
                break;
            }
            case PRIVATE:
            {
                PrivateChannelImpl channel = (PrivateChannelImpl) message.getPrivateChannel();
                channel.setLastMessageId(message.getIdLong());
                manager.handle(
                    new PrivateMessageReceivedEvent(
                        api, responseNumber,
                        message));
                break;
            }
            case GROUP:
            {
                GroupImpl channel = (GroupImpl) message.getGroup();
                channel.setLastMessageId(message.getIdLong());
                manager.handle(
                    new GroupMessageReceivedEvent(
                        api, responseNumber,
                        message));
                break;
            }
            default:
                WebSocketClient.LOG.warn("Received a MESSAGE_CREATE with a unknown MessageChannel ChannelType. JSON: " + content);
                return null;
        }

        //Combo event
        manager.handle(
            new MessageReceivedEvent(
                api, responseNumber,
                message));
        return null;
    }

    private User getUser(JSONObject author)
    {
        long authorId = author.getLong("id");
        User user = api.getUserById(authorId);
        if (user == null)
            user = api.getFakeUserMap().get(authorId);
        if (user == null)
        {
            api.getEventCache().cache(EventCache.Type.USER, authorId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a message for a user that JDA does not currently have cached");
        }
        return user;
    }

    private Long handleCallSystemMessage(JSONObject content)
    {
        long id = content.getLong("id");
        long channelId = content.getLong("channel_id");
        JSONObject author = content.getJSONObject("author");
        String msgContent = content.isNull("content") ? "" : content.getString("content");

        User user = getUser(author);
        if (user == null)
            return null;
        MessageChannel channel = api.getPrivateChannelById(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
            channel = api.asClient().getGroupById(channelId);
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId,
                    () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received CallMessage for a channel that is not yet cached. messageId: " + id + " channelId: " + channelId);
            return null;
        }
        CallMessage message = new CallMessage(user, channel, id, msgContent);
        final IEventManager manager = api.getEventManager();
        switch (channel.getType())
        {
            case GROUP:
                manager.handle(
                    new GroupCallMessageEvent(
                            api, responseNumber,
                            message, (Group) channel));
                break;
            case PRIVATE:
                manager.handle(
                    new PrivateCallMessageEvent(
                            api, responseNumber,
                            message, (PrivateChannel) channel));
            // default:
        }
        manager.handle(
            new CallMessageEvent(
                    api, responseNumber,
                    message, channel));
        return null;
    }

    private Long handlePinSystemMessage(JSONObject content)
    {
        long id = content.getLong("id");
        long channelId = content.getLong("channel_id");
        JSONObject author = content.getJSONObject("author");
        String msgContent = content.isNull("content") ? "" : content.getString("content");

        User user = getUser(author);
        if (user == null)
            return null;
        MessageChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
            channel = api.getPrivateChannelById(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
                channel = api.asClient().getGroupById(channelId);
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId,
                    () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received PinMessage for a channel that is not yet cached. messageId: " + id + " channelId: " + channelId);
            return null;
        }

        PinMessage message = new PinMessage(user, channel, id, msgContent);
        api.getEventManager().handle(
            new PinMessageEvent(
                api, responseNumber,
                message, channel));
        return null;
    }

    private Long handleWelcomeMessage(JSONObject content)
    {
        final long id = content.getLong("id");
        final long channelId = content.getLong("channel_id");
        final JSONObject authorObj = content.getJSONObject("author");
        final long authorId = authorObj.getLong("id");

        final TextChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received WELCOME message for channel that is not yet cached. channelId: " + channelId + " messageId: " + id);
            return null;
        }

        final User author = api.getUserById(authorId);
        if (author == null)
        {
            api.getEventCache().cache(EventCache.Type.USER, authorId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received WELCOME message for user that is not yet cached. userId: " + authorId + " messageId: " + id);
            return null;
        }
        WelcomeMessage message = new WelcomeMessage(author, channel, id, content.getString("content"));
        api.getEventManager().handle(
            new GuildMemberJoinMessageEvent(
                api, responseNumber,
                message, channel));
        return null;
    }

    private Long handleGroupSystemMessage(JSONObject content, MessageType type)
    {
        long id = content.getLong("id");
        long channelId = content.getLong("channel_id");
        if (api.getAccountType() != AccountType.CLIENT)
        {
            WebSocketClient.LOG.warn("Ignoring SystemMessage from Group with id: " + channelId + " on non-client account!");
            return null;
        }

        JSONObject author = content.getJSONObject("author");
        User user = getUser(author);
        if (user == null)
            return null;
        boolean add = false;

        Group group = api.asClient().getGroupById(channelId);
        String msgContent = content.isNull("content") ? "" : content.getString("content");

        final IEventManager manager = api.getEventManager();
        switch (type)
        {
            case RECIPIENT_ADD:
                add = true; // we fall through on purpose
            case RECIPIENT_REMOVE:
            {
                GroupRecipientMessage message = new GroupRecipientMessage(user, group, id, msgContent, add);
                if (add)
                {
                    manager.handle(
                        new GroupRecipientAddMessageEvent(
                            api, responseNumber,
                            message, group));
                }
                else
                {
                    manager.handle(
                        new GroupRecipientRemoveMessageEvent(
                            api, responseNumber,
                            message, group));
                }
                break;
            }
            case CHANNEL_ICON_CHANGE:
            {
                GroupIconMessage message = new GroupIconMessage(user, group, id, msgContent);
                manager.handle(
                    new GroupIconMessageEvent(
                        api, responseNumber,
                        message, group));
                break;
            }
            case CHANNEL_NAME_CHANGE:
            {
                GroupNameMessage message = new GroupNameMessage(user, group, id, msgContent);
                manager.handle(
                    new GroupNameMessageEvent(
                        api, responseNumber,
                        message, group));
                break;
            }
            default:
                WebSocketClient.LOG.debug("Received message type that has no handling specified " + type);
        }
        return null;
    }
}
