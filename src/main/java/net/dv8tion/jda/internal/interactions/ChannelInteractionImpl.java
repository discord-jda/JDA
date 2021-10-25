/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.ChannelInteraction;
import net.dv8tion.jda.api.interactions.ChannelInteractionHook;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyActionImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChannelInteractionImpl extends InteractionImpl implements ChannelInteraction
{
    protected final ChannelInteractionHookImpl hook;
    protected final Channel channel;

    public ChannelInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        this.hook = new ChannelInteractionHookImpl(this, jda);
        if (guild != null)
        {
            channel = guild.getGuildChannelById(data.getUnsignedLong("channel_id"));
        }
        else
        {
            long channelId = data.getUnsignedLong("channel_id");
            PrivateChannel channel = jda.getPrivateChannelById(channelId);
            if (channel == null)
            {
                channel = jda.getEntityBuilder().createPrivateChannel(
                    DataObject.empty()
                        .put("id", channelId)
                        .put("recipient", data.getObject("user"))
                );
            }
            this.channel = channel;
        }
    }

    public ChannelInteractionImpl(long id, int type, String token, Guild guild, Member member, User user, Channel channel)
    {
        super(id, type, token, guild, member, user);
        this.channel = channel;
        this.hook = new ChannelInteractionHookImpl(this, api);
    }

    @Nullable
    @Override
    public Channel getChannel()
    {
        return channel;
    }

    @Nonnull
    @Override
    public ChannelInteractionHook getHook()
    {
        return hook;
    }

    @Nonnull
    @Override
    public ReplyActionImpl deferReply()
    {
        return new ReplyActionImpl(this.hook);
    }

}
