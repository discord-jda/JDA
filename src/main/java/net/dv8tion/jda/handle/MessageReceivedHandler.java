/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageType;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.requests.GuildLock;
import net.dv8tion.jda.utils.InviteUtil;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReceivedHandler extends SocketHandler
{
    private static final Pattern invitePattern = Pattern.compile("\\bhttps://(?:www\\.)?discord(?:\\.gg|app\\.com/invite)/([a-zA-Z0-9-]+)\\b");

    public MessageReceivedHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        MessageType type = MessageType.fromId(content.getInt("type"));

        switch (type)
        {
            case DEFAULT:
                return handleDefaultMessage(content);
            default:
                JDAImpl.LOG.debug("JDA received a message of unknown type. Type: " + type + "  JSON: " + content);
        }
        return null;
    }

    private String handleDefaultMessage(JSONObject content)
    {
        Message message;
        try
        {
            message = new EntityBuilder(api).createMessage(content);
        }
        catch (IllegalArgumentException e)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("channel_id"), () ->
            {
                handle(allContent);
            });
            EventCache.LOG.debug(e.getMessage());
            return null;
        }

        if (!message.isPrivate())
        {
            TextChannel channel = api.getChannelMap().get(message.getChannelId());
            if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
            {
                return channel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageReceivedEvent(
                            api, responseNumber,
                            message, channel));
        }
        else
        {
            api.getEventManager().handle(
                    new PrivateMessageReceivedEvent(
                            api, responseNumber,
                            message, api.getPmChannelMap().get(message.getChannelId())));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageReceivedEvent(
                        api, responseNumber,
                        message));

        //searching for invites
        Matcher matcher = invitePattern.matcher(message.getContent());
        while (matcher.find())
        {
            InviteUtil.Invite invite = InviteUtil.resolve(matcher.group(1));
            if (invite != null)
            {
                api.getEventManager().handle(
                        new InviteReceivedEvent(
                                api, responseNumber,
                                message,invite));
            }
        }
        return null;
    }
}
