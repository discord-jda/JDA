/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.TextChannelCreateEvent;

import org.json.JSONObject;

public class ChannelCreateHandler implements ISocketHandler
{
    private JDA api;

    public ChannelCreateHandler(JDA api)
    {
        this.api = api;
    }
    @Override
    public void handle(JSONObject content)
    {
        String type = content.getString("type");
        if (type.equals("text"))
        {
            api.getEventManager().handle(
                    new TextChannelCreateEvent(
                            new EntityBuilder(api).createTextChannel(content, content.getString("guild_id"))));
        }
        else if (type.equals("voice"))
        {
            //TODO:IMPLEMENT - implement the VoiceChannelCreate event fire after the EntitiyBuilder.createVoiceChannel has been implemented
//            api.getEventManager().handle(
//                    new VoiceChannelCreateEvent(
//                            new EntityBuilder(api).createVoiceChannel(content, content.getString("guild_id"))));
        }
        //TODO:LOGGER ERROR - A channel type we don't know about?
    }
}
