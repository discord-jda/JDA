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
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.handle.EntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class PrivateChannelImpl implements PrivateChannel
{
    private final String id;
    private final User user;
    private final JDAImpl api;

    public PrivateChannelImpl(String id, User user, JDAImpl api)
    {
        this.id = id;
        this.user = user;
        this.api = api;
    }
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public Message sendMessage(String text)
    {
        return sendMessage(new MessageBuilder().appendString(text).build());
    }

    @Override
    public Message sendMessage(Message msg)
    {
        try
        {
            JSONObject response = api.getRequester().post("https://discordapp.com/api/channels/" + getId() + "/messages",
                    new JSONObject().put("content", msg.getContent()));

            return new EntityBuilder(api).createMessage(response);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
            //sending failed
            return null;
        }
    }

    public void sendTyping()
    {
        api.getRequester().post("https://discordapp.com/api/channels/" + getId() + "/typing");
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PrivateChannel))
            return false;
        PrivateChannel oPChannel = (PrivateChannel) o;
        return this == oPChannel || this.getId().equals(oPChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}
