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
package net.dv8tion.jda.managers;

import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager used to modify aspects of a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
 * or {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}.
 */
public class ChannelManager
{
    private final Channel channel;

    private String name = null;
    private String topic = null;
    private Map<Integer, Channel> newPositions = new HashMap<>();

    public ChannelManager(Channel channel)
    {
        this.channel = channel;
    }

    /**
     * Sets the name of this Channel.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param name
     *      The new name of the Channel, or null to keep current one
     * @return
     *      this
     */
    public ChannelManager setName(String name)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel.getName().equals(name))
        {
            this.name = null;
        }
        else
        {
            this.name = name;
        }
        return this;
    }

    /**
     * Returns the {@link net.dv8tion.jda.entities.Channel Channel} object of this Manager. Useful if this Manager was returned via a create function
     * @return
     *      the Channel of this Manager
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * Sets the topic of this Channel.
     * This is not available for {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}
     * and will result in a {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param topic
     *      The new topic of the Channel, or null to keep current one
     * @return
     *      this
     * @throws java.lang.UnsupportedOperationException
     *      thrown when attempting to set the topic for a {@link net.dv8tion.jda.entities.VoiceChannel}
     */
    public ChannelManager setTopic(String topic)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof VoiceChannel)
        {
            throw new UnsupportedOperationException("Setting a Topic on VoiceChannels is not allowed!");
        }
        if (StringUtils.equals(topic, channel.getTopic()))
        {
            this.topic = null;
        }
        else
        {
            this.topic = topic;
        }
        return this;
    }

    /**
     * Sets the position of this Channel.
     * If another Channel of the same Type and target newPosition already exists in this Guild,
     * this channel will get placed above the existing one (newPosition gets decremented).
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param newPosition
     *      The new position of the Channel, or -1 to keep current one
     * @return
     *      this
     */
    public ChannelManager setPosition(int newPosition)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        newPositions.clear();
        if (newPosition < 0 || newPosition == channel.getPosition())
        {
            return this;
        }
        Map<Integer, Channel> currentPositions = new HashMap<>();

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
            newPositions.put(newPosition, channel);
            //check if there is space above this channel (a hole to fill)
            if (currentPositions.keySet().stream().filter(n -> n < newPosition).count() < newPosition)
            {
                for (int i = newPosition; i > 0; i--)
                {
                    if (currentPositions.containsKey(i))
                    {
                        newPositions.put(i - 1, currentPositions.get(i));
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else    //no space above, shift below channels further down
            {
                for (int i = newPosition; true; i++)
                {
                    if (currentPositions.containsKey(i))
                    {
                        newPositions.put(i + 1, currentPositions.get(i));
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Deletes this Channel
     * This method takes immediate effect
     */
    public void delete()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        ((JDAImpl) channel.getJDA()).getRequester().delete("https://discordapp.com/api/channels/" + channel.getId());
    }

    /**
     * This method will apply all accumulated changes received by setters
     */
    public void update()
    {
        JSONObject frame = getFrame(channel);
        if (this.name != null)
        {
            frame.put("name", this.name);
        }
        if (this.topic != null)
        {
            frame.put("topic", this.topic);
        }
        for (Map.Entry<Integer, Channel> posEntry : newPositions.entrySet())
        {
            if (posEntry.getValue() == channel)
            {
                frame.put("position", posEntry.getKey());
            }
            else
            {
                update(posEntry.getValue(), getFrame(posEntry.getValue()).put("position", posEntry.getKey()));
            }
        }
        update(channel, frame);
    }

    private JSONObject getFrame(Channel chan)
    {
        return new JSONObject()
                .put("name", chan.getName())
                .put("position", chan.getPosition())
                .put("topic", chan.getTopic() == null ? "" : chan.getTopic());
    }

    private void update(Channel chan, JSONObject o)
    {
        ((JDAImpl) chan.getJDA()).getRequester().patch("https://discordapp.com/api/channels/" + chan.getId(), o);
    }

    private void checkPermission(Permission perm)
    {
        if (!channel.checkPermission(channel.getJDA().getSelfInfo(), perm))
            throw new PermissionException(perm);
    }
}