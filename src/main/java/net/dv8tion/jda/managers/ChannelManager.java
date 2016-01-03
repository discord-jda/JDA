/**
 * Copyright 2015 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.managers;

import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChannelManager
{
    private final Channel channel;

    public ChannelManager(Channel channel)
    {
        this.channel = channel;
    }

    /**
     * Sets the name of this Channel.
     *
     * @param name
     *      The new name of the Channel
     * @return
     *      this
     */
    public ChannelManager setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name must not be null!");
        }
        if (name.equals(channel.getName()))
        {
            return this;
        }
        update(channel, getFrame(channel).put("name", name));
        return this;
    }

    /**
     * Sets the topic of this Channel.
     * This is not available for {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}
     * and will result in a {@link java.lang.UnsupportedOperationException UnsupportedOperationException}
     *
     * @param topic
     *      The new topic of the Channel
     * @return
     *      this
     */
    public ChannelManager setTopic(String topic)
    {
        if (channel instanceof VoiceChannel)
        {
            throw new UnsupportedOperationException("Setting a Topic on VoiceChannels is not allowed!");
        }
        if (StringUtils.equals(topic, channel.getTopic()))
        {
            return this;
        }
        update(channel, getFrame(channel).put("topic", topic == null ? JSONObject.NULL : topic));
        return this;
    }

    /**
     * Sets the position of this Channel.
     * If another Channel of the same Type and target newPosition already exists in this Guild,
     * this channel will get placed above the existing one (newPosition gets decremented)
     *
     * @param newPosition
     *      The new position of the Channel
     * @return
     *      this
     */
    public ChannelManager setPosition(int newPosition)
    {
        newPosition = Math.max(0, newPosition);
        if (newPosition == channel.getPosition())
        {
            return this;
        }
        Map<Integer, Channel> currentPositions = new HashMap<>();
        Map<Integer, Channel> toChange = new HashMap<>();

        if (channel instanceof TextChannel)
        {
            channel.getGuild().getTextChannels().forEach(chan -> currentPositions.put(chan.getPosition(), chan));
        }
        else
        {
            channel.getGuild().getVoiceChannels().forEach(chan -> currentPositions.put(chan.getPosition(), chan));
        }
        if (currentPositions.containsKey(newPosition))
        {
            int newPos = newPosition;
            toChange.put(newPos, channel);
            //check if there is space above this channel (a hole to fill)
            if (currentPositions.keySet().stream().filter(n -> n < newPos).count() < newPos)
            {
                for (int i = newPos; i > 0; i--)
                {
                    if (currentPositions.containsKey(i))
                    {
                        toChange.put(i - 1, currentPositions.get(i));
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else    //no space above, shift below channels further down
            {
                for (int i = newPos; true; i++)
                {
                    if (currentPositions.containsKey(i))
                    {
                        toChange.put(i + 1, currentPositions.get(i));
                    }
                    else
                    {
                        break;
                    }
                }
            }
            toChange.forEach((key, val) -> {
                update(val, getFrame(val).put("position", key));
            });
        }
        return this;
    }

    /**
     * Deletes this Channel
     */
    public void delete()
    {
        ((JDAImpl) channel.getJDA()).getRequester().delete("https://discordapp.com/api/channels/" + channel.getId());
    }

    private JSONObject getFrame(Channel chan)
    {
        return new JSONObject()
                .put("name", chan.getName())
                .put("position", chan.getPosition())
                .put("topic", chan.getTopic() == null ? JSONObject.NULL : chan.getTopic());
    }

    private void update(Channel chan, JSONObject o)
    {
        ((JDAImpl) chan.getJDA()).getRequester().patch("https://discordapp.com/api/channels/" + chan.getId(), o);
    }
}