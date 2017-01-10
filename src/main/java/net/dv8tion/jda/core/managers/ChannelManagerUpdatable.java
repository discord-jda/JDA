
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
import net.dv8tion.jda.core.managers.fields.ChannelField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;

public class ChannelManagerUpdatable
{
    protected final Channel channel;

    protected ChannelField<String> name;
    protected ChannelField<String> topic;
    protected ChannelField<Integer> userLimit;
    protected ChannelField<Integer> bitrate;

    public ChannelManagerUpdatable(Channel channel)
    {
        this.channel = channel;
        setupFields();
    }

    public JDA getJDA()
    {
        return channel.getJDA();
    }

    public Channel getChannel()
    {
        return channel;
    }

    public Guild getGuild()
    {
        return channel.getGuild();
    }

    public ChannelField<String> getNameField()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        return name;
    }

    public ChannelField<String> getTopicField()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof VoiceChannel)
            throw new UnsupportedOperationException("Setting a Topic on VoiceChannels is not allowed!");

        return topic;
    }

    public ChannelField<Integer> getUserLimitField()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof TextChannel)
            throw new UnsupportedOperationException("Setting user limit for TextChannels is not allowed!");

        return userLimit;
    }

    public ChannelField<Integer> getBitrateField()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (channel instanceof TextChannel)
            throw new UnsupportedOperationException("Setting user limit for TextChannels is not allowed!");

        return bitrate;
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset()
    {
        this.name.reset();
        if (channel instanceof TextChannel)
        {
            this.topic.reset();
        }
        else
        {
            this.bitrate.reset();
            this.userLimit.reset();
        }
    }

    /*
     * This method will apply all accumulated changes received by setters
     */
    public RestAction<Void> update()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (!needToUpdate())
            return new RestAction.EmptyRestAction<Void>(null);

        JSONObject frame = new JSONObject().put("name", channel.getName());
        if (name.shouldUpdate())
            frame.put("name", name.getValue());
        if (topic != null && topic.shouldUpdate())
            frame.put("topic", topic.getValue() == null ? JSONObject.NULL : topic.getValue());
        if (userLimit != null && userLimit.shouldUpdate())
            frame.put("user_limit", userLimit.getValue());
        if (bitrate != null && bitrate.shouldUpdate())
            frame.put("bitrate", bitrate.getValue());

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
        return name.shouldUpdate()
                || (topic != null && topic.shouldUpdate())
                || (userLimit != null && userLimit.shouldUpdate())
                || (bitrate != null && bitrate.shouldUpdate());
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(channel, getGuild().getSelfMember(), perm))
            throw new PermissionException(perm);
    }

    protected void setupFields()
    {
        this.name = new ChannelField<String>(this, channel::getName)
        {
            @Override
            public void checkValue(String value)
            {
                checkNull(value, "name");
                if (value.length() < 2 || value.length() > 100)
                    throw new IllegalArgumentException("Provided channel name must be 2 to 100 characters in length");
            }
        };

        if (channel instanceof TextChannel)
        {
            TextChannel tc = (TextChannel) channel;
            this.topic = new ChannelField<String>(this, tc::getTopic)
            {
                @Override
                public void checkValue(String value)
                {
                    if (value != null && value.length() > 1024)
                        throw new IllegalArgumentException("Provided topic must less than or equal to 1024 characters in length");
                }
            };
        }
        else
        {
            VoiceChannel vc = (VoiceChannel) channel;
            this.userLimit = new ChannelField<Integer>(this, vc::getUserLimit)
            {
                @Override
                public void checkValue(Integer value)
                {
                    checkNull(value, "user limit");
                    if (value < 0 || value > 99)
                        throw new IllegalArgumentException("Provided user limit must be 0 to 99.");
                }
            };

            this.bitrate = new ChannelField<Integer>(this, vc::getBitrate)
            {
                @Override
                public void checkValue(Integer value)
                {
                    checkNull(value, "bitrate");
                    if (value < 8000 || value > 96000) // TODO: vip servers can go up to 128000
                        throw new IllegalArgumentException("Provided bitrate must be 8000 to 96000");
                }
            };
        }

    }
}