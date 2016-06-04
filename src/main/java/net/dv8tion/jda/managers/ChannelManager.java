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
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manager used to modify aspects of a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
 * or {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}.
 */
public class ChannelManager
{
    private final Channel channel;

    private String name = null;
    private String topic = null;
    private int position = -1;
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
        if (!PermissionUtil.checkPermission(channel.getJDA().getSelfInfo(), Permission.MANAGE_CHANNEL, channel.getGuild()))
            throw new PermissionException("Do not have " + Permission.MANAGE_CHANNEL + " for this Guild. Cannot change the position of channels.");
        newPositions.clear();

        if (newPosition < 0 || newPosition == channel.getPosition())
        {
            return this;
        }
        this.position = newPosition;

        Map<Integer, Channel> currentPositions  = (channel instanceof TextChannel
                ? channel.getGuild().getTextChannels()
                : channel.getGuild().getVoiceChannels())
                .stream().collect(Collectors.toMap(
                        chan -> chan.getPosition(),
                        chan -> chan));

        //We create a search index to make sure we insert at the right place. If the position we are inserting at
        // is greater than where the channel was before, that means it will be skipping over its original position
        // in the channel list, thus the iterating index will be 1 less than expected, so we decrement the
        // search index by 1 to account for this.
        int searchIndex = newPosition > channel.getPosition() ? newPosition - 1 : newPosition;
        int index = 0;
        for (Channel chan : currentPositions.values())
        {
            //When we encounter the old position of the channel, ignore it.
            if (chan == channel)
                continue;
            if (index == searchIndex)
            {
                newPositions.put(index, channel);
                index++;
            }
            newPositions.put(index, chan);
            index++;
        }
        //If the channel was moved to the very bottom, this will make sure it is properly handled.
        if (!newPositions.containsValue(channel))
            newPositions.put(newPosition, channel);
        return this;
    }

    /**
     * Deletes this Channel
     * This method takes immediate effect
     */
    public void delete()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        ((JDAImpl) channel.getJDA()).getRequester().delete(Requester.DISCORD_API_PREFIX + "channels/" + channel.getId());
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset() {
        name = null;
        topic = null;
        position = -1;
        newPositions.clear();
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
        if (position != -1 && !newPositions.isEmpty())
        {
            updatePosition();
            frame.put("position", this.position);
        }

        update(channel, frame);
        reset();
    }

    private void updatePosition()
    {
        JSONArray bulkUpdate = new JSONArray();
        newPositions.forEach((pos, chan) ->
        {
            bulkUpdate.put(new JSONObject()
                .put("id", chan.getId())
                .put("position", pos));
        });
        ((JDAImpl) channel.getJDA()).getRequester().patch(Requester.DISCORD_API_PREFIX
                + "guilds/" + channel.getGuild().getId() + "/channels", bulkUpdate);
    }

    private JSONObject getFrame(Channel chan)
    {
        return new JSONObject()
                .put("name", chan.getName())
                .put("topic", chan.getTopic() == null ? "" : chan.getTopic())
                .put("position", chan.getPositionRaw());
//                .put("bitrate", chan instanceof VoiceChannel ? ((VoiceChannel) chan).getBitrate() : 64000)
//                .put("user_limit", chan instanceof VoiceChannel ? ((VoiceChannel) chan).getUserLimit() : 0)
    }

    private void update(Channel chan, JSONObject o)
    {
        ((JDAImpl) chan.getJDA()).getRequester().patch(Requester.DISCORD_API_PREFIX + "channels/" + chan.getId(), o);
    }

    private void checkPermission(Permission perm)
    {
        if (!channel.checkPermission(channel.getJDA().getSelfInfo(), perm))
            throw new PermissionException(perm);
    }
}