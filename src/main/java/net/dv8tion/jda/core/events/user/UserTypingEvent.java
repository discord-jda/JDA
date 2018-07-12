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

import java.time.OffsetDateTime;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.User User} started typing. (Similar to the typing indicator in the Discord client)
 *
 * <p>Can be used to retrieve the User who started typing and when and in which MessageChannel they started typing.
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

    /**
     * The time when the user started typing
     *
     * @return The time when the typing started
     */
    public OffsetDateTime getTimestamp()
    {
        return timestamp;
    }

    /**
     * The channel where the typing was started
     *
     * @return The channel
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * Whether the user started typing in a channel of the specified type.
     *
     * @param  type
     *         {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     *
     * @return True, if the user started typing in a channel of the specified type
     */
    public boolean isFromType(ChannelType type)
    {
        return channel.getType() == type;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     *
     * @return The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     */
    public ChannelType getType()
    {
        return channel.getType();
    }

    /**
     * {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} in which this users started typing,
     * or {@code null} if this was not in a PrivateChannel.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     */
    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    /**
     * {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} in which this users started typing,
     * or {@code null} if this was not in a TextChannel.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    /**
     * {@link net.dv8tion.jda.client.entities.Group Group} in which this users started typing,
     * or {@code null} if this was not in a Group.
     *
     * @return Possibly-null {@link net.dv8tion.jda.client.entities.Group Group}
     */
    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }

    /**
     * {@link net.dv8tion.jda.core.entities.Guild Guild} in which this users started typing,
     * or {@code null} if this was not in a Guild.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

    /**
     * {@link net.dv8tion.jda.core.entities.Member Member} instance for the User, or null if this was not in a Guild.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Member Member}
     */
    public Member getMember()
    {
        return isFromType(ChannelType.TEXT) ? getGuild().getMember(getUser()) : null;
    }
}
