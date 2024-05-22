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
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationOwners;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.InteractionEntityBuilder;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.detached.DetachedGuildImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class InteractionImpl implements Interaction
{
    protected final long id;
    protected final long channelId;
    protected final int type;
    protected final String token;
    protected final Guild guild;
    protected final Member member;
    protected final User user;
    protected final Channel channel;
    protected final DiscordLocale userLocale;
    protected final List<Entitlement> entitlements;
    protected final InteractionContextType context;
    protected final IntegrationOwners integrationOwners;
    protected final JDAImpl api;
    protected final InteractionEntityBuilder interactionEntityBuilder;

    //This is used to give a proper error when an interaction is ack'd twice
    // By default, discord only responds with "unknown interaction" which is horrible UX so we add a check manually here
    private boolean isAck;

    public InteractionImpl(JDAImpl jda, DataObject data)
    {
        this.api = jda;
        final DataObject userObj = data.optObject("member").orElse(data).getObject("user");
        this.interactionEntityBuilder = new InteractionEntityBuilder(jda, data.getLong("channel_id"), userObj.getUnsignedLong("id"));
        this.id = data.getUnsignedLong("id");
        this.token = data.getString("token");
        this.type = data.getInt("type");
        this.guild = data.optObject("guild").map(interactionEntityBuilder::getOrCreateGuild).orElse(null);
        this.channelId = data.getUnsignedLong("channel_id", 0L);
        this.userLocale = DiscordLocale.from(data.getString("locale", "en-US"));
        this.context = InteractionContextType.fromKey(data.getString("context"));
        this.integrationOwners = new IntegrationOwnersImpl(data.getObject("authorizing_integration_owners"));

        DataObject channelJson = data.getObject("channel");
        if (guild instanceof GuildImpl)
        {
            member = jda.getEntityBuilder().createMember((GuildImpl) guild, data.getObject("member"));
            jda.getEntityBuilder().updateMemberCache((MemberImpl) member);
            user = member.getUser();

            GuildChannel channel = guild.getGuildChannelById(channelJson.getUnsignedLong("id"));
            if (channel == null && ChannelType.fromId(channelJson.getInt("type")).isThread())
                channel = api.getEntityBuilder().createThreadChannel((GuildImpl) guild, channelJson, guild.getIdLong(), false);
            if (channel == null)
                throw new IllegalStateException("Failed to create channel instance for interaction! Channel Type: " + channelJson.getInt("type"));
            this.channel = channel;
        }
        else if (guild instanceof DetachedGuildImpl)
        {
            member = interactionEntityBuilder.createMember(guild, data.getObject("member"));
            user = member.getUser();

            if (ChannelType.fromId(channelJson.getInt("type")).isThread())
                channel = interactionEntityBuilder.createThreadChannel(guild, channelJson);
            else
                channel = interactionEntityBuilder.createGuildChannel(guild, channelJson);
            if (channel == null)
                throw new IllegalStateException("Failed to create channel instance for interaction! Channel Type: " + channelJson.getInt("type"));
        }
        else
        {
            //(G)DMs
            user = jda.getEntityBuilder().createUser(userObj);
            member = null;
            ChannelType type = ChannelType.fromId(channelJson.getInt("type"));
            if (type == ChannelType.PRIVATE) {
                this.channel = interactionEntityBuilder.createPrivateChannel(channelJson, user);
            } else if (type == ChannelType.GROUP) {
                this.channel = interactionEntityBuilder.createGroupChannel(channelJson);
            } else {
                throw new IllegalArgumentException("Received interaction in unexpected channel type! Type " + type + " is not supported yet!");
            }
        }

        this.entitlements = data.optArray("entitlements").orElseGet(DataArray::empty)
                .stream(DataArray::getObject)
                .map(jda.getEntityBuilder()::createEntitlement)
                .collect(Helpers.toUnmodifiableList());
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

    @Override
    public long getChannelIdLong()
    {
        return channelId;
    }

    @Nonnull
    public DiscordLocale getUserLocale()
    {
        return userLocale;
    }

    @Nonnull
    @Override
    public InteractionContextType getContext()
    {
        return context;
    }

    @Nonnull
    @Override
    public IntegrationOwners getIntegrationOwners()
    {
        return integrationOwners;
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
    public List<Entitlement> getEntitlements()
    {
        return entitlements;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
}
