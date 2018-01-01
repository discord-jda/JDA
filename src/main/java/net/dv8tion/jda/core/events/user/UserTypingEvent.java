/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.events.user;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * <b><u>UserTypingUpdateEvent</u></b><br>
 * Fired if a {@link net.dv8tion.jda.core.entities.User User} starts typing. (Similar to the typing indicator in the Discord client)<br>
 * <br>
 * Use: Retrieve the User who started typing and when and in which MessageChannel they started typing.
 */
public class UserTypingEvent extends GenericUserEvent
{
    private final MessageChannel channel;
    private final OffsetDateTime timestamp;

    public UserTypingEvent(JDA api, long responseNumber, User user, MessageChannel channel, OffsetDateTime timestamp)
    {
        super(api, responseNumber, user);
        this.channel = channel;
        this.timestamp = timestamp;
    }

    @Nonnull
    public OffsetDateTime getTimestamp()
    {
        return timestamp;
    }

    @Nonnull
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Deprecated
    public boolean isPrivate()
    {
        return channel instanceof PrivateChannel;
    }

    public boolean isFromType(ChannelType type)
    {
        return channel.getType() == type;
    }

    @Nonnull
    public ChannelType getType()
    {
        return channel.getType();
    }

    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

    public Member getMember()
    {
        return isFromType(ChannelType.TEXT) ? getGuild().getMember(getUser()) : null;
    }

    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }

    @Nullable
    public Category getCategory()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getParent() : null;
    }
}
