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

package net.dv8tion.jda.internal.entities.channel.concrete;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.PrivateChannelMixin;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrivateChannelImpl extends AbstractChannelImpl<PrivateChannelImpl> implements
        PrivateChannel,
        PrivateChannelMixin<PrivateChannelImpl>
{
    private User user;
    private long latestMessageId;

    public PrivateChannelImpl(JDA api, long id, @Nullable User user)
    {
        super(id, api);
        this.user = user;
    }

    @Override
    public boolean isDetached()
    {
        return false;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.PRIVATE;
    }

    @Nullable
    @Override
    public User getUser()
    {
        updateUser();
        return user;
    }

    @Nonnull
    @Override
    public RestAction<User> retrieveUser()
    {
        User user = getUser();
        if (user != null)
            return new CompletedRestAction<>(getJDA(), user);
        //even if the user blocks the bot, this does not fail.
        return retrievePrivateChannel()
                .map(PrivateChannel::getUser);
    }

    @Nonnull
    @Override
    public String getName()
    {
        User user = getUser();
        if (user == null)
        {
            //don't break or override the contract of @NonNull
            return "";
        }
        return user.getName();
    }

    @Nonnull
    private RestAction<PrivateChannel> retrievePrivateChannel()
    {
        Route.CompiledRoute route = Route.Channels.GET_CHANNEL.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) -> ((JDAImpl) getJDA()).getEntityBuilder().createPrivateChannel(response.getObject()));
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Override
    public boolean canTalk()
    {
        //The only way user is null is when an event is dispatched that doesn't give us enough information to build the recipient user,
        // which only happens if this bot sends a message (or otherwise triggers an event) from a shard other than shard 0.
        // The event will be received on shard 0 and not have enough information to build the recipient user.
        //As such, since events will only happen in this channel if it is between the bot and the user, a null user is a valid channel state.
        // Events cannot happen between a bot and another bot, so the user would never be null in that case.
        return user == null || !user.isBot();
    }

    @Override
    public void checkCanAccess() {}

    @Override
    public void checkCanSendMessage() {
        checkBot();
    }

    @Override
    public void checkCanSendMessageEmbeds() {}

    @Override
    public void checkCanSendFiles() {}

    @Override
    public void checkCanViewHistory() {}

    @Override
    public void checkCanAddReactions() {}

    @Override
    public void checkCanRemoveReactions() {}

    @Override
    public void checkCanControlMessagePins() {}

    @Override
    public boolean canDeleteOtherUsersMessages()
    {
        return false;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    @Override
    public PrivateChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof PrivateChannelImpl))
            return false;
        PrivateChannelImpl impl = (PrivateChannelImpl) obj;
        return impl.id == this.id;
    }

    private void updateUser()
    {
        //if the user is null then we don't even know their ID, and so we have to check that first
        if (user == null)
            return;
        // Load user from cache if one exists, otherwise we might have an outdated user instance
        User realUser = getJDA().getUserById(user.getIdLong());
        if (realUser != null)
            this.user = realUser;
    }

    private void checkBot()
    {
        //The only way user is null is when an event is dispatched that doesn't give us enough information to build the recipient user,
        // which only happens if this bot sends a message (or otherwise triggers an event) from a shard other than shard 0.
        // The event will be received on shard 0 and not have enough information to build the recipient user.
        //As such, since events will only happen in this channel if it is between the bot and the user, a null user is a valid channel state.
        // Events cannot happen between a bot and another bot, so the user would never be null in that case.
        if (getUser() != null && getUser().isBot())
            throw new UnsupportedOperationException("Cannot send a private message between bots.");
    }
}
