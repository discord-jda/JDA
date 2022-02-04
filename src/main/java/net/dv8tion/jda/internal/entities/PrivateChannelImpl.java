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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrivateChannelImpl extends AbstractChannelImpl<PrivateChannelImpl> implements PrivateChannel, MessageChannelMixin<PrivateChannelImpl>
{
    private User user;
    private long latestMessageId;

    private PrivateChannelImpl(JDA api, long id, User user)
    {
        super(id, api);
        this.user = user;
    }

    public PrivateChannelImpl(long id, JDA jda)
    {
        this(jda, id, null);
    }

    public PrivateChannelImpl(long id, User user)
    {
        this(user.getJDA(), id, user);
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

    @NotNull
    @Override
    public RestAction<User> retrieveUser()
    {
        if (user != null){
            return new CompletedRestAction<>(getJDA(), user);
        }
        //even if the user blocks the bot, this does not fail.
        return getJDA().retrieveChannelById(id)
                .map(channel -> ((PrivateChannel) channel).getUser());
    }

    @Nonnull
    @Override
    public String getName()
    {
        if (getUser() == null)
            //don't break or override the contract of @NotNull
            return "";
        return getUser().getName();
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
    public boolean canTalk()
    {
        return user == null || !user.isBot();
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
        return "PC:" + getName() + '(' + getId() + ')';
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
        if (getUser() != null && getUser().isBot())
            throw new UnsupportedOperationException("Cannot send a private message between bots.");
    }
}
