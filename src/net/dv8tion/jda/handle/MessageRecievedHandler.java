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
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

public class MessageRecievedHandler implements ISocketHandler
{
    private final JDA api;

    public MessageRecievedHandler(JDA api)
    {
        this.api = api;
    }

    @Override
    public void handle(JSONObject content)
    {
        String channel_id = content.getString("channel_id");
        TextChannel channel = api.getChannelMap().get(channel_id);
        if (channel != null)
        {
            api.getEventManager().handle(
                    new MessageReceivedEvent(
                            api,
                            new EntityBuilder(api).createMessage(content)));
        }
        else
        {
            //TODO PRIVATE MESSAGE
        }
    }
}
