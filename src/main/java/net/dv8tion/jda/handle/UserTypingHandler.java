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

import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.user.GenericUserEvent;
import net.dv8tion.jda.events.user.UserTypingEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UserTypingHandler extends SocketHandler
{

    public UserTypingHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        MessageChannel channel = api.getChannelMap().get(content.getString("channel_id"));

        if (channel == null)
        {
            channel = api.getPmChannelMap().get(content.getString("channel_id"));
        }
        else if (GuildLock.get(api).isLocked(((TextChannel) channel).getGuild().getId()))
        {
            return ((TextChannel) channel).getGuild().getId();
        }

        User user = api.getUserMap().get(content.getString("user_id"));
        if (user == null)
            return null;

        OffsetDateTime timestamp = Instant.ofEpochSecond(content.getInt("timestamp")).atOffset(ZoneOffset.UTC);
        api.getEventManager().handle(new UserTypingEvent(api, responseNumber, user, channel, timestamp));
        api.getEventManager().handle(new GenericUserEvent(api, responseNumber, user));
        return null;
    }
}
