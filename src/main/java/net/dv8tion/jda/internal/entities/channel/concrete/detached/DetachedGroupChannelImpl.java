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
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.concrete.GroupChannelMixin;
import net.dv8tion.jda.internal.entities.detached.mixin.IDetachableEntityMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DetachedGroupChannelImpl extends AbstractChannelImpl<DetachedGroupChannelImpl>
    implements
        GroupChannelMixin<DetachedGroupChannelImpl>,
        IDetachableEntityMixin
{
    private long latestMessageId;
    private UserSnowflake ownerId;
    private String icon;

    public DetachedGroupChannelImpl(JDA api, long id)
    {
        super(id, api);
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.GROUP;
    }

    @Override
    public boolean isDetached()
    {
        return true;
    }

    @Nonnull
    @Override
    public UserSnowflake getOwnerId()
    {
        return ownerId;
    }

    @Nullable
    public String getIconId()
    {
        return icon;
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
    public void checkCanAccess()
    {
        throw detachedException();
    }

    @Override
    public void checkCanSendMessage()
    {
        throw detachedException();
    }

    @Override
    public void checkCanSendMessageEmbeds()
    {
        throw detachedException();
    }

    @Override
    public void checkCanSendFiles()
    {
        throw detachedException();
    }

    @Override
    public void checkCanViewHistory()
    {
        throw detachedException();
    }

    @Override
    public void checkCanAddReactions()
    {
        throw detachedException();
    }

    @Override
    public void checkCanRemoveReactions()
    {
        throw detachedException();
    }

    @Override
    public void checkCanControlMessagePins()
    {
        throw detachedException();
    }

    @Override
    public boolean canDeleteOtherUsersMessages()
    {
        return false;
    }

    @Override
    public DetachedGroupChannelImpl setLatestMessageIdLong(long latestMessageId)
    {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public DetachedGroupChannelImpl setOwnerId(long ownerId)
    {
        this.ownerId = UserSnowflake.fromId(ownerId);
        return this;
    }

    @Override
    public DetachedGroupChannelImpl setIcon(@Nullable String icon)
    {
        this.icon = icon;
        return this;
    }
}
