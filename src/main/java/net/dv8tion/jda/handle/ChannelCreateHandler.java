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

import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelCreateEvent;
import org.json.JSONObject;

public class ChannelCreateHandler extends SocketHandler
{

    public ChannelCreateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }
    @Override
    public void handle(JSONObject content)
    {
        String type;
        if (content.has("type"))
            type = content.getString("type");
        else if (content.has("recipient"))
            type = "private";
        else
            throw new IllegalArgumentException("ChannelCreateEvent provided an unrecognized ChannelCreate format.  JSON: " + content);

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
        else if (type.equalsIgnoreCase("private"))
        {
            api.getEventManager().handle(
                    new PrivateChannelCreateEvent(
                            api, responseNumber,
                            new EntityBuilder(api).createPrivateChannel(content).getUser()));
        }
        else
        {
            throw new IllegalArgumentException("ChannelCreateEvent provided an unregonized channel type.  JSON: " + content);
            //TODO:LOGGER ERROR - A channel type we don't know about?
        }
    }
}
