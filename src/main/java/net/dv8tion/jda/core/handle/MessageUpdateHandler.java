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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.events.message.group.GroupMessageEmbedEvent;
import net.dv8tion.jda.client.events.message.group.GroupMessageUpdateEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.MessageEmbedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageUpdateHandler extends SocketHandler
{

    public MessageUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (content.has("author"))
        {
            if (content.has("type"))
            {
                MessageType type = MessageType.fromId(content.getInt("type"));
                switch (type)
                {
                    case DEFAULT:
                        return handleDefaultMessage(content);
                    default:
                        WebSocketClient.LOG.debug("JDA received a message of unknown type. Type: " + type + "  JSON: " + content);
                        return null;
                }
            }
            else
            {
                //TODO: handle partial message update info. Example:
                //From webhook/rich-embed.
                //{"author":{"bot":true,"id":"233501884294365184","avatar":"27ae7496b3b30cddab2feaaed06d862a","username":"GitHub","discriminator":"0000"},"id":"234838126969880596","embeds":[{"color":15109472,"author":{"icon_url":"https://avatars.githubusercontent.com/u/2415829?v=3","name":"abalabahaha","proxy_icon_url":"https://images-ext-2.discordapp.net/eyJ1cmwiOiJodHRwczovL2F2YXRhcnMuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3UvMjQxNTgyOT92PTMifQ.mMTBuOMUKYowcUU1H8Gzc7g4fFQ","url":"https://github.com/abalabahaha"},"description":"It was removed from the unofficial docs and other places because devs didn't want automated registration.","type":"rich","title":"[hammerandchisel/discord-api-docs] New comment on issue #148: Register API call","url":"https://github.com/hammerandchisel/discord-api-docs/issues/148#issuecomment-252524279"}],"channel_id":"168311874624946176"}
                return null;
            }
        }
        else if (content.has("call"))
        {
            handleCallMessage(content);
            return null;
        }
        else
            return handleMessageEmbed(content);
    }

    private String handleDefaultMessage(JSONObject content)
    {
        Message message;
        try
        {
            message = EntityBuilder.get(api).createMessage(content);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("channel_id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("Received a message update for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    EventCache.get(api).cache(EventCache.Type.USER, content.getJSONObject("author").getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("Received a message update for a user that JDA does not currently have cached");
                    return null;
                }
                default:
                    throw e;
            }
        }

        switch (message.getChannelType())
        {
            case TEXT:
            {
                TextChannel channel = message.getTextChannel();
                if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
                {
                    return channel.getGuild().getId();
                }
                api.getEventManager().handle(
                        new GuildMessageUpdateEvent(
                                api, responseNumber,
                                message));
                break;
            }
            case PRIVATE:
            {
                api.getEventManager().handle(
                        new PrivateMessageUpdateEvent(
                                api, responseNumber,
                                message));
            }
            case GROUP:
            {
                api.getEventManager().handle(
                        new GroupMessageUpdateEvent(
                                api, responseNumber,
                                message));
                break;
            }

            default:
                WebSocketClient.LOG.warn("Received a MESSAGE_UPDATE with a unknown MessageChannel ChannelType. JSON: " + content);
                return null;
        }

        //Combo event
        api.getEventManager().handle(
                new MessageUpdateEvent(
                        api, responseNumber,
                        message));
        return null;
    }

    private String handleMessageEmbed(JSONObject content)
    {
        EntityBuilder builder = EntityBuilder.get(api);
        String messageId = content.getString("id");
        String channelId = content.getString("channel_id");
        LinkedList<MessageEmbed> embeds = new LinkedList<>();
        MessageChannel channel = api.getTextChannelMap().get(channelId);
        if (channel == null)
            channel = api.getPrivateChannelMap().get(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
            channel = api.asClient().getGroupById(channelId);
        if (channel == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received message update for embeds for a channel/group that JDA does not have cached yet.");
            return null;
        }

        JSONArray embedsJson = content.getJSONArray("embeds");
        for (int i = 0; i < embedsJson.length(); i++)
        {
            embeds.add(builder.createMessageEmbed(embedsJson.getJSONObject(i)));
        }

        if (channel instanceof TextChannel)
        {
            TextChannel tChannel = (TextChannel) channel;
            if (GuildLock.get(api).isLocked(tChannel.getGuild().getId()))
            {
                return tChannel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageEmbedEvent(
                            api, responseNumber,
                            messageId, tChannel, embeds));
        }
        else if (channel instanceof PrivateChannel)
        {
            api.getEventManager().handle(
                    new PrivateMessageEmbedEvent(
                            api, responseNumber,
                            messageId, (PrivateChannel) channel, embeds));
        }
        else
        {
            api.getEventManager().handle(
                    new GroupMessageEmbedEvent(
                            api, responseNumber,
                            messageId, (Group) channel, embeds));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageEmbedEvent(
                        api, responseNumber,
                        messageId, channel, embeds));
        return null;
    }

    public void handleCallMessage(JSONObject content)
    {
        WebSocketClient.LOG.debug("Received a MESSAGE_UPDATE of type CALL:  " + content.toString());
        //Called when someone joins call for first time.
        //  It is not called when they leave or rejoin. That is all dictated by VOICE_STATE_UPDATE.
        //  Probably can ignore the above due to VOICE_STATE_UPDATE
        // Could have a mapping of all users who were participants at one point or another during the call
        //  in comparison to the currently participants.
        // and when the call is ended. Ending defined by ended_timestamp != null
    }
}
