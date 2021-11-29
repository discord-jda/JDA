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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;

public class PrivateChannelImpl extends AbstractChannelImpl<PrivateChannelImpl> implements PrivateChannel, MessageChannelMixin<PrivateChannelImpl>
{
    private User user;
    private long latestMessageId;

    public PrivateChannelImpl(long id, User user)
    {
        super(id, user.getJDA());
        this.user = user;
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.PRIVATE;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        updateUser();
        return user;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return getUser().getName();
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return user.getJDA();
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
        return new RestActionImpl<>(getJDA(), route);
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Override
    public void checkCanAccessChannel() {}

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

    @Override
    public PrivateChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    // -- Object --

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

    @Override
    public String toString()
    {
        return "PC:" + getUser().getName() + '(' + getId() + ')';
    }

    private void updateUser()
    {
        // Load user from cache if one exists, otherwise we might have an outdated user instance
        User realUser = getJDA().getUserById(user.getIdLong());
        if (realUser != null)
            this.user = realUser;
    }

    private void checkBot()
    {
        if (getUser().isBot() && getJDA().getAccountType() == AccountType.BOT)
            throw new UnsupportedOperationException("Cannot send a private message between bots.");
    }
}
