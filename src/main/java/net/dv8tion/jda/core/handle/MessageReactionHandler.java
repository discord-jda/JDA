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

import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionAddEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionRemoveEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONObject;

public class MessageReactionHandler extends SocketHandler
{

    private final boolean add;

    public MessageReactionHandler(JDAImpl api, boolean add)
    {
        super(api);
        this.add = add;
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        JSONObject emoji = content.getJSONObject("emoji");

        final long userId    = content.getLong("user_id");
        final long messageId = content.getLong("message_id");
        final long channelId = content.getLong("channel_id");

        final Long emojiId = emoji.isNull("id") ? null : emoji.getLong("id");
        String emojiName = emoji.optString("name", null);
        final boolean emojiAnimated = emoji.optBoolean("animated");

        if (emojiId == null && emojiName == null)
        {
            WebSocketClient.LOG.debug("Received a reaction {} with no name nor id. json: {}",
                JDALogger.getLazyString(() -> add ? "add" : "remove"), content);
            return null;
        }

        User user = api.getUserById(userId);
        if (user == null)
            user = api.getFakeUserMap().get(userId);
        if (user == null)
        {
            api.getEventCache().cache(EventCache.Type.USER, userId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a reaction for a user that JDA does not currently have cached");
            return null;
        }

        MessageChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
        {
            channel = api.getPrivateChannelById(channelId);
        }
        if (channel == null)
        {
            channel = api.getFakePrivateChannelMap().get(channelId);
        }
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
        {
            channel = api.asClient().getGroupById(channelId);
        }
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
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
            rEmote = new MessageReaction.ReactionEmote(emote);
        }
        else
        {
            rEmote = new MessageReaction.ReactionEmote(emojiName, null, api);
        }
        MessageReaction reaction = new MessageReaction(channel, rEmote, messageId, user.equals(api.getSelfUser()), -1);

        if (add)
            onAdd(reaction, user);
        else
            onRemove(reaction, user);
        return null;
    }

    private void onAdd(MessageReaction reaction, User user)
    {
        IEventManager manager = api.getEventManager();
        switch (reaction.getChannelType())
        {
            case TEXT:
                manager.handle(
                    new GuildMessageReactionAddEvent(
                            api, responseNumber,
                            user, reaction));
                break;
            case GROUP:
                manager.handle(
                    new GroupMessageReactionAddEvent(
                            api, responseNumber,
                            user, reaction));
                break;
            case PRIVATE:
                manager.handle(
                    new PrivateMessageReactionAddEvent(
                            api, responseNumber,
                            user, reaction));
        }

        manager.handle(
            new MessageReactionAddEvent(
                    api, responseNumber,
                    user, reaction));
    }

    private void onRemove(MessageReaction reaction, User user)
    {
        IEventManager manager = api.getEventManager();
        switch (reaction.getChannelType())
        {
            case TEXT:
                manager.handle(
                    new GuildMessageReactionRemoveEvent(
                            api, responseNumber,
                            user, reaction));
                break;
            case GROUP:
                manager.handle(
                    new GroupMessageReactionRemoveEvent(
                            api, responseNumber,
                            user, reaction));
                break;
            case PRIVATE:
                manager.handle(
                    new PrivateMessageReactionRemoveEvent(
                            api, responseNumber,
                            user, reaction));
        }

        manager.handle(
            new MessageReactionRemoveEvent(
                    api, responseNumber,
                    user, reaction));
    }
}
