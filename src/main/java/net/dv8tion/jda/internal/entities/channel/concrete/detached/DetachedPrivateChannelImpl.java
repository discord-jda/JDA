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

package net.dv8tion.jda.internal.entities.channel.concrete.detached;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.exceptions.DetachedEntityException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.PrivateChannelMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DetachedPrivateChannelImpl extends AbstractChannelImpl<DetachedPrivateChannelImpl> implements
        PrivateChannel,
        PrivateChannelMixin<DetachedPrivateChannelImpl>
{
    private long latestMessageId;

    public DetachedPrivateChannelImpl(JDA api, long id)
    {
        super(id, api);
    }

    @Nonnull
    @Override
    public DetachedEntityException detachedException()
    {
        return new DetachedEntityException("Cannot perform action in friend DMs");
    }

    @Override
    public boolean isDetached()
    {
        return true;
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
        return null;
    }

    @Nonnull
    @Override
    public RestAction<User> retrieveUser()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public String getName()
    {
        //don't break or override the contract of @NonNull
        return "";
    }

    @Override
    public long getLatestMessageIdLong()
    {
        return latestMessageId;
    }

    @Override
    public boolean canTalk()
    {
        return false;
    }

    @Override
    public void checkCanAccess() {
        throw detachedException();
    }

    @Override
    public void checkCanSendMessage() {
        throw detachedException(); // Should be checked by checkCanAccess first!
    }

    @Override
    public void checkCanSendMessageEmbeds() {
        throw detachedException(); // Should be checked by checkCanSendMessage first!
    }

    @Override
    public void checkCanSendFiles() {
        throw detachedException(); // Should be checked by checkCanSendMessage first!
    }

    @Override
    public void checkCanViewHistory() {
        throw detachedException();
    }

    @Override
    public void checkCanAddReactions() {
        throw detachedException();
    }

    @Override
    public void checkCanRemoveReactions() {
        throw detachedException();
    }

    @Override
    public void checkCanControlMessagePins() {
        throw detachedException();
    }

    @Override
    public boolean canDeleteOtherUsersMessages()
    {
        return false;
    }

    @Override
    public DetachedPrivateChannelImpl setLatestMessageIdLong(long latestMessageId)
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
        if (!(obj instanceof DetachedPrivateChannelImpl))
            return false;
        DetachedPrivateChannelImpl impl = (DetachedPrivateChannelImpl) obj;
        return impl.id == this.id;
    }
}
