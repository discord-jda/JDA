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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyActionImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionImpl implements Interaction
{
    protected final InteractionHookImpl hook;
    protected final long id;
    protected final int type;
    protected final String token;
    protected final Guild guild;
    protected final Member member;
    protected final User user;
    protected final Channel channel;
    protected final JDAImpl api;

    public InteractionImpl(JDAImpl jda, DataObject data)
    {
        this.api = jda;
        this.id = data.getUnsignedLong("id");
        this.token = data.getString("token");
        this.type = data.getInt("type");
        this.guild = jda.getGuildById(data.getUnsignedLong("guild_id", 0L));
        this.hook = new InteractionHookImpl(this, jda);
        if (guild != null)
        {
            member = jda.getEntityBuilder().createMember((GuildImpl) guild, data.getObject("member"));
            jda.getEntityBuilder().updateMemberCache((MemberImpl) member);
            user = member.getUser();
            channel = guild.getGuildChannelById(data.getUnsignedLong("channel_id"));
        }
        else
        {
            member = null;
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
            user = channel.getUser();
        }
    }

    public InteractionImpl(long id, int type, String token, Guild guild, Member member, User user, Channel channel)
    {
        this.id = id;
        this.type = type;
        this.token = token;
        this.guild = guild;
        this.member = member;
        this.user = user;
        this.channel = channel;
        this.api = (JDAImpl) user.getJDA();
        this.hook = new InteractionHookImpl(this, api);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public int getTypeRaw()
    {
        return type;
    }

    @Nonnull
    @Override
    public String getToken()
    {
        return token;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nullable
    @Override
    public Channel getChannel()
    {
        return channel;
    }

    @Nonnull
    @Override
    public InteractionHook getHook()
    {
        return hook;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return user;
    }

    @Nullable
    @Override
    public Member getMember()
    {
        return member;
    }

    @Override
    public boolean isAcknowledged()
    {
        return hook.isAck();
    }

    @Nonnull
    @Override
    public ReplyActionImpl deferReply()
    {
        return new ReplyActionImpl(this.hook);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
}
