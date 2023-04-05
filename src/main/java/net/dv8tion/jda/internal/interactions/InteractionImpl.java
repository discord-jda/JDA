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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.PrivateChannelImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionImpl implements Interaction
{
    protected final long id;
    protected final int type;
    protected final String token;
    protected final Guild guild;
    protected final Member member;
    protected final User user;
    protected final Channel channel;
    protected final DiscordLocale userLocale;
    protected final JDAImpl api;

    //This is used to give a proper error when an interaction is ack'd twice
    // By default, discord only responds with "unknown interaction" which is horrible UX so we add a check manually here
    private boolean isAck;

    public InteractionImpl(JDAImpl jda, DataObject data)
    {
        this.api = jda;
        this.id = data.getUnsignedLong("id");
        this.token = data.getString("token");
        this.type = data.getInt("type");
        this.guild = jda.getGuildById(data.getUnsignedLong("guild_id", 0L));
        this.userLocale = DiscordLocale.from(data.getString("locale", "en-US"));

        DataObject channelJson = data.getObject("channel");
        if (guild != null)
        {
            member = jda.getEntityBuilder().createMember((GuildImpl) guild, data.getObject("member"));
            jda.getEntityBuilder().updateMemberCache((MemberImpl) member);
            user = member.getUser();
            channel = guild.getGuildChannelById(channelJson.getUnsignedLong("id"));
        }
        else
        {
            member = null;
            long channelId = channelJson.getUnsignedLong("id");
            ChannelType type = ChannelType.fromId(channelJson.getInt("type"));
            if (type != ChannelType.PRIVATE)
                throw new IllegalArgumentException("Received interaction in unexpected channel type! Type " + type + " is not supported yet!");
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

            User user = channel.getUser();
            if (user == null)
            {
                user = jda.getEntityBuilder().createUser(data.getObject("user"));
                ((PrivateChannelImpl) channel).setUser(user);
                ((UserImpl) user).setPrivateChannel(channel);
            }
            this.user = user;
        }
    }

    // Used to allow interaction hook to send messages after acknowledgements
    // This is implemented only in DeferrableInteractionImpl where a hook is present!
    public synchronized void releaseHook(boolean success) {}

    // Ensures that one cannot acknowledge an interaction twice
    public synchronized boolean ack()
    {
        boolean wasAck = isAck;
        this.isAck = true;
        return wasAck;
    }

    @Override
    public synchronized boolean isAcknowledged()
    {
        return isAck;
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
    public DiscordLocale getUserLocale()
    {
        return userLocale;
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

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
}
