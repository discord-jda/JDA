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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.user.GenericUserEvent;
import net.dv8tion.jda.core.events.user.UserTypingEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TypingStartHandler extends SocketHandler
{

    public TypingStartHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String channelId = content.getString("channel_id");
        MessageChannel channel = api.getTextChannelMap().get(channelId);
        if (channel != null)
        {
            if (GuildLock.get(api).isLocked(((TextChannel) channel).getGuild().getId()))
            {
                return ((TextChannel) channel).getGuild().getId();
            }
        }
        else
        {
            channel = api.getPrivateChannelMap().get(channelId);
            if (channel == null)
                channel = api.getFakePrivateChannelMap().get(channelId);
            if (channel == null)
                return null;    //We don't have the channel cached yet. We chose not to cache this event
                                // because that happen very often and could easily fill up the EventCache if
                                // we, for some reason, never get the channel. Especially in an active channel.
        }

        User user;
        if (channel instanceof PrivateChannel && ((PrivateChannel) channel).isFake())
            user = ((PrivateChannel) channel).getUser();
        else
            user = api.getUserMap().get(content.getString("user_id"));

        if (user == null)
            return null;    //Just like in the comment above, if for some reason we don't have the user for some reason
                            // then we will just throw the event away.

        OffsetDateTime timestamp = Instant.ofEpochSecond(content.getInt("timestamp")).atOffset(ZoneOffset.UTC);
        api.getEventManager().handle(
                new UserTypingEvent(
                        api, responseNumber,
                        user, channel, timestamp));
        return null;
    }
}
