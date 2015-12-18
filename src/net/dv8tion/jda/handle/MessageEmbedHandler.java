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

import java.util.LinkedList;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.MessageEmbed;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageEmbedEvent;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageEmbedHandler extends SocketHandler
{

    public MessageEmbedHandler(JDA api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        EntityBuilder builder = new EntityBuilder(api);
        String messageId = content.getString("id");
        TextChannel channel = api.getChannelMap().get(content.getString("channel_id"));
        LinkedList<MessageEmbed> embeds = new LinkedList<MessageEmbed>();

        JSONArray embedsJson = content.getJSONArray("embeds");
        for (int i = 0; i < embedsJson.length(); i++)
        {
            embeds.add(builder.createMessageEmbed(embedsJson.getJSONObject(i)));
        }
        api.getEventManager().handle(
                new MessageEmbedEvent(
                        api, responseNumber,
                        messageId, channel, embeds));
    }
}
