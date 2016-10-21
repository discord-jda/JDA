
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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;

import java.util.Objects;

public class ChannelManagerUpdatable
{
    protected final Channel channel;
    private String name = null;
    private String topic = null;
    private int userLimit = -1;
    private int bitrate = -1;
//    private int position = -1;
//    private Map<Integer, Channel> newPositions = new HashMap<>();

    public ChannelManagerUpdatable(Channel channel)
    {
        this.channel = channel;
    }

    public JDA getJDA()
    {
        return channel.getJDA();
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Channel Channel} object of this Manager. Useful if this Manager was returned via a create function
     *
     * @return the Channel of this Manager
     */
    public Channel getChannel()
    {
        return channel;
    }

    public Guild getGuild()
    {
        return channel.getGuild();
    }

    /**
     * Sets the name of this Channel.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param name The new name of the Channel, or null to keep current one
     * @return this
     */
    public ChannelManagerUpdatable setName(String name)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (Objects.equals(name, channel.getName()))
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
     * Sets the topic of this Channel.
     * This is not available for {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}
     * and will result in a {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
     * <p>
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param topic The new topic of the Channel, or null to keep current one
     * @return this
     * @throws java.lang.UnsupportedOperationException thrown when attempting to set the topic for a {@link net.dv8tion.jda.core.entities.VoiceChannel}
     */
    public ChannelManagerUpdatable setTopic(String topic)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof VoiceChannel)
            throw new UnsupportedOperationException("Setting a Topic on VoiceChannels is not allowed!");

        if (Objects.equals(topic, ((TextChannel) channel).getTopic()))
            this.topic = null;
        else
            this.topic = topic;

        return this;
    }

    /**
     * Used to set the maximum amount of users that can be connected to a
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} at the same time.
     * <p>
     * The accepted range is 0-99, with 0 representing no limit. -1 can be provided to reset the value.<br>
     * The default is: 0
     *
     * @param userLimit The maximum amount of Users that can be connected to a voice channel at a time.
     * @return This ChannelManager
     * @throws java.lang.UnsupportedOperationException thrown when attempting to set the userLimit for a {@link net.dv8tion.jda.core.entities.TextChannel}
     * @throws java.lang.IllegalArgumentException      thrown if the provided userLimit it outside the range of 0 to 99, not including the reset value: -1
     */
    public ChannelManagerUpdatable setUserLimit(int userLimit)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof TextChannel)
            throw new UnsupportedOperationException("Setting user limit for TextChannels is not allowed!");
        if (userLimit < -1 || userLimit > 99)
            throw new IllegalArgumentException("Provided userlimit must be either within the bounds of 0-99 inclusive or -1 to reset.");

        if (userLimit == ((VoiceChannel) channel).getUserLimit())
            this.userLimit = -1;
        else
            this.userLimit = userLimit;

        return this;
    }

    /**
     * Used to set the bitrate that Discord clients will use when sending and receiving audio.
     * <p>
     * The accepted range is 8000-96000. -1 can be provided to reset the value.<br>
     * The default value is: 64000
     *
     * @param bitrate The bitrate which Discord clients will conform to when dealing with the audio from this channel.
     * @return This ChannelManager
     * @throws java.lang.UnsupportedOperationException thrown when attempting to set the bitrate for a {@link net.dv8tion.jda.core.entities.TextChannel}
     * @throws java.lang.IllegalArgumentException      thrown if the provided bitrate it outside the range of 8000 to 96000, not including the reset value: -1
     */
    public ChannelManagerUpdatable setBitrate(int bitrate)
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof TextChannel)
            throw new UnsupportedOperationException("Setting user limit for TextChannels is not allowed!");
        if (bitrate != -1 && (bitrate < 8000 || bitrate > 96000))
            throw new IllegalArgumentException("Provided bitrate must be within the range of 8000 to 96000, or -1 to reset. Recommended is 64000");

        if (bitrate == ((VoiceChannel) channel).getBitrate())
            this.bitrate = -1;
        else
            this.bitrate = bitrate;

        return this;
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset()
    {
        this.name = null;
        this.topic = null;
        this.bitrate = -1;
        this.userLimit = -1;
//        position = -1;
//        newPositions.clear();
    }

    /**
     * This method will apply all accumulated changes received by setters
     */
    public RestAction<Void> update()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (!needToUpdate())
            return new RestAction.EmptyRestAction<Void>(null);

        JSONObject frame = new JSONObject().put("name", channel.getName());
        if (this.name != null)
            frame.put("name", this.name);
        if (this.topic != null)
            frame.put("topic", this.topic);
        if (userLimit != -1)
            frame.put("user_limit", userLimit);
        if (bitrate != -1)
            frame.put("bitrate", bitrate);
//        if (position != -1 && !newPositions.isEmpty())
//        {
//            updatePosition();
//            frame.put("position", this.position);
//        }

        reset();    //now that we've built our JSON object, reset the manager back to the non-modified state
        Route.CompiledRoute route = Route.Channels.MODIFY_CHANNEL.compile(channel.getId());
        return new RestAction<Void>(channel.getJDA(), route, frame)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    protected boolean needToUpdate()
    {
        return name != null
                || topic != null
                || userLimit != -1
                || bitrate != -1;
    }

    private void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(channel, getGuild().getSelfMember(), perm))
            throw new PermissionException(perm);
    }

}