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

import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public class ChannelCreateHandler extends SocketHandler
{

    public ChannelCreateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }
    @Override
    protected String handleInternally(JSONObject content)
    {
        String type = content.getString("type");
        boolean isPrivate = content.getBoolean("is_private");

        if (!isPrivate && GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        if (!isPrivate)
        {
            if (type.equals("text"))
            {
                api.getEventManager().handle(
                        new TextChannelCreateEvent(
                                api, responseNumber,
                                new EntityBuilder(api).createTextChannel(content, content.getString("guild_id"))));
            }
            else if (type.equals("voice"))
            {
                api.getEventManager().handle(
                        new VoiceChannelCreateEvent(
                                api, responseNumber,
                                new EntityBuilder(api).createVoiceChannel(content, content.getString("guild_id"))));
            }
            else
                throw new IllegalArgumentException("ChannelCreateEvent provided an unrecognized guild channel type.  JSON: " + content);
        }
        else
        {
            if (type.equals("text"))
            {
                PrivateChannel pc = new EntityBuilder(api).createPrivateChannel(content);
                if (pc == null)
                {
                    JDAImpl.LOG.warn("Discord API sent us a Private CREATE_CHANNEL for a user we can't see, ignoring event.");
                }
                else
                {
                    api.getEventManager().handle(
                            new PrivateChannelCreateEvent(
                                    api, responseNumber,
                                    pc.getUser()));
                }
            }
            else if (type.equals("voice"))
            {
                JDAImpl.LOG.warn("Received a CHANNEL_CREATE for a Private Voice channel. Currently, we don't support, so ignoring. Might explode though.. ?");
            }
            else
                throw new IllegalArgumentException("ChannelCreateEvent provided an unrecognized private channel type.  JSON: " + content);
        }
        EventCache.get(api).playbackCache(EventCache.Type.CHANNEL, content.getString("id"));
        return null;
    }
}
