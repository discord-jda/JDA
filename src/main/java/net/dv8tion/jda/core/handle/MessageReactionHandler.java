/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.requests.WebSocketClient;
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
    protected String handleInternally(JSONObject content)
    {
        JSONObject emoji = content.getJSONObject("emoji");

        String userId = content.getString("user_id");
        String messageId = content.getString("message_id");
        String channelId = content.getString("channel_id");

        String emojiId = emoji.isNull("id") ? null : emoji.getString("id");
        String emojiName = emoji.isNull("name") ? null : emoji.getString("name");

        if (emojiId == null && emojiName == null)
        {
            WebSocketClient.LOG.debug("Received a reaction " + (add ? "add" : "remove") + " with no name nor id. json: " + content);
            return null;
        }

        User user = api.getUserById(userId);
        if (user == null)
            user = api.getFakeUserMap().get(userId);
        if (user == null)
        {
            EventCache.get(api).cache(EventCache.Type.USER, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received a reaction for a user that JDA does not currently have cached");
            return null;
        }

        MessageChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
            channel = api.getPrivateChannelById(channelId);
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
            channel = api.asClient().getGroupById(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
            {
                handle(responseNumber, allContent);
            });
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
                    emote = new EmoteImpl(emojiId, api).setName(emojiName);
                }
                else
                {
                    WebSocketClient.LOG.debug("Received a reaction " + (add ? "add" : "remove") + " with a null name. json: " + content);
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
        {
            api.getEventManager().handle(
                    new MessageReactionAddEvent(
                            api, responseNumber,
                            user, reaction));
        }
        else
        {
            api.getEventManager().handle(
                    new MessageReactionRemoveEvent(
                            api, responseNumber,
                            user, reaction));
        }
        return null;
    }
}
