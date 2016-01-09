/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelDeleteEvent;
import org.json.JSONObject;

public class ChannelDeleteHandler extends SocketHandler
{

    public ChannelDeleteHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        switch (content.getString("type"))
        {
            case "text":
            {
                TextChannel channel = api.getChannelMap().remove(content.getString("id"));
                if (channel == null)
                    throw new IllegalArgumentException("CHANNEL_DELETE attempted to delete a channel that doesn't exist! JSON: " + content);

                guild.getTextChannelsMap().remove(channel.getId());
                api.getEventManager().handle(
                        new TextChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case "voice":
            {
                VoiceChannel channel = guild.getVoiceChannelsMap().remove(content.getString("id"));
                if (channel == null)
                    throw new IllegalArgumentException("CHANNEL_DELETE attempted to delete a channel that doesn't exist! JSON: " + content);

                guild.getVoiceChannelsMap().remove(channel.getId());
                api.getEventManager().handle(
                        new VoiceChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_DELETE provided an unknown channel type. JSON: " + content);
        }
    }
}
