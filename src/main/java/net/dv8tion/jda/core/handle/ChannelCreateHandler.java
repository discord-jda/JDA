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

import net.dv8tion.jda.client.events.group.GroupJoinEvent;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

public class ChannelCreateHandler extends SocketHandler
{
    public ChannelCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));

        if ((type == ChannelType.TEXT || type == ChannelType.VOICE ) && GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        switch (type)
        {
            case TEXT:
            {
                api.getEventManager().handle(
                        new TextChannelCreateEvent(
                                api, responseNumber,
                                EntityBuilder.get(api).createTextChannel(content, content.getString("guild_id"))));
                break;
            }
            case VOICE:
            {
                api.getEventManager().handle(
                        new VoiceChannelCreateEvent(
                                api, responseNumber,
                                EntityBuilder.get(api).createVoiceChannel(content, content.getString("guild_id"))));
                break;
            }
            case PRIVATE:
            {
                api.getEventManager().handle(
                        new PrivateChannelCreateEvent(
                                api, responseNumber,
                                EntityBuilder.get(api).createPrivateChannel(content)));
                break;
            }
            case GROUP:
            {
                api.getEventManager().handle(
                        new GroupJoinEvent(
                                api, responseNumber,
                                EntityBuilder.get(api).createGroup(content)));
                break;
            }
            default:
                throw new IllegalArgumentException("Discord provided an CREATE_CHANNEL event with an unknown channel type! JSON: " + content);
        }
        EventCache.get(api).playbackCache(EventCache.Type.CHANNEL, content.getString("id"));
        return null;
    }
}
