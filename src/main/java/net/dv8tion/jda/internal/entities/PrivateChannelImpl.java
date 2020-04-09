/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PrivateChannelImpl implements PrivateChannel
{
    private final long id;
    private final User user;
    private long lastMessageId;

    public PrivateChannelImpl(long id, User user)
    {
        this.id = id;
        this.user = user;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public long getLatestMessageIdLong()
    {
        final long messageId = lastMessageId;
        if (messageId < 0)
            throw new IllegalStateException("No last message id found.");
        return messageId;
    }

    @Override
    public boolean hasLatestMessage()
    {
        return lastMessageId > 0;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return getUser().getName();
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.PRIVATE;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return user.getJDA();
    }

    @Nonnull
    @Override
    public RestAction<Void> close()
    {
        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public List<CompletableFuture<Void>> purgeMessages(@Nonnull List<? extends Message> messages)
    {
        if (messages == null || messages.isEmpty())
            return Collections.emptyList();
        for (Message m : messages)
        {
            if (m.getAuthor().equals(getJDA().getSelfUser()))
                continue;
            throw new IllegalArgumentException("Cannot delete messages of other users in a private channel");
        }
        return PrivateChannel.super.purgeMessages(messages);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean isFake()
    {
        return user.isFake();
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull CharSequence text)
    {
        checkBot();
        return PrivateChannel.super.sendMessage(text);
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull MessageEmbed embed)
    {
        checkBot();
        return PrivateChannel.super.sendMessage(embed);
    }

    @Nonnull
    @Override
    public MessageAction sendMessage(@Nonnull Message msg)
    {
        checkBot();
        return PrivateChannel.super.sendMessage(msg);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkBot();
        return PrivateChannel.super.sendFile(data, fileName, options);
    }

    @Nonnull
    @Override
    public MessageAction sendFile(@Nonnull File file, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        checkBot();
        final long maxSize = getJDA().getSelfUser().getAllowedFileSize();
        Checks.check(file == null || file.length() <= maxSize,
                    "File may not exceed the maximum file length of %d bytes!", maxSize);
        return PrivateChannel.super.sendFile(file, fileName, options);
    }

    public PrivateChannelImpl setLastMessageId(long id)
    {
        this.lastMessageId = id;
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

    private void checkBot()
    {
        if (getUser().isBot() && getJDA().getAccountType() == AccountType.BOT)
            throw new UnsupportedOperationException("Cannot send a private message between bots.");
    }
}
