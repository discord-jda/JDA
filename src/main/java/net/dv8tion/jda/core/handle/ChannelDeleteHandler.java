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

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

public class ChannelDeleteHandler extends SocketHandler
{
    public ChannelDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        if (type == ChannelType.TEXT || type == ChannelType.VOICE)
        {
            if (GuildLock.get(api).isLocked(content.getString("guild_id")))
            {
                return content.getString("guild_id");
            }
        }

        switch (type)
        {
            case TEXT:
            {
                GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
                TextChannel channel = api.getTextChannelMap().remove(content.getString("id"));
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a channel that doesn't exist! JSON: " + content);
                    return null;
                }

                guild.getTextChannelsMap().remove(channel.getId());
                api.getEventManager().handle(
                        new TextChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case VOICE:
            {
                GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
                VoiceChannel channel = guild.getVoiceChannelMap().remove(content.getString("id"));
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a channel that doesn't exist! JSON: " + content);
                    return null;
                }
                //We use this instead of getAudioManager(Guild) so we don't create a new instance. Efficiency!
//                AudioManager manager = api.getAudioManagersMap().get(guild);
//                if (manager != null && manager.isConnected()
//                        && manager.getConnectedChannel().getId().equals(channel.getId()))
//                {
//                    manager.closeAudioConnection();
//                }
                guild.getVoiceChannelMap().remove(channel.getId());
                api.getEventManager().handle(
                        new VoiceChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case PRIVATE:
            {
                String channelId = content.getString("id");
                PrivateChannel channel = api.getPrivateChannelMap().remove(channelId);

                if (channel == null)
                    channel = api.getFakePrivateChannelMap().remove(channelId);
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a channel that doesn't exist! JSON: " + content);
                    return null;
                }

                if (channel.getUser().isFake())
                    api.getFakeUserMap().remove(channel.getUser().getId());

                ((UserImpl) channel.getUser()).setPrivateChannel(null);

                api.getEventManager().handle(
                        new PrivateChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case GROUP:
            {
                JDAImpl.LOG.debug("Received CHANNEL_DELETE for a group, but JDA doesn't support groups. (Use JDA-Client)");
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_DELETE provided an unknown channel type. JSON: " + content);
        }
        return null;
    }
}
