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
package net.dv8tion.jda.api.events.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Message Message} was created/deleted/changed.
 * <br>Every MessageEvent is an instance of this event and can be casted.
 *
 * <p>Can be used to detect any MessageEvent.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGES GUILD_MESSAGES} to work in guild text channels</li>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGES DIRECT_MESSAGES} to work in private channels</li>
 * </ul>
 */
public abstract class GenericMessageEvent extends Event
{
    protected final long messageId;
    protected final MessageChannel channel;

    public GenericMessageEvent(@Nonnull JDA api, long responseNumber, long messageId, @Nonnull MessageChannel channel)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channel = channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel MessageChannel} for this Message
     *
     * @return The MessageChannel
     */
    @Nonnull
    public MessageChannelUnion getChannel()
    {
        return (MessageChannelUnion) channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel GuildMessageChannel} for this Message
     *  if it was sent in a Guild.
     * <br>If this Message was not received from a {@link net.dv8tion.jda.api.entities.Guild Guild},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a channel in a Guild.
     *
     * @return The GuildMessageChannel
     */
    @Nonnull
    public GuildMessageChannelUnion getGuildChannel()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This message event did not happen in a guild");
        return (GuildMessageChannelUnion) channel;
    }

    /**
     * The id for this message
     *
     * @return The id for this message
     */
    @Nonnull
    public String getMessageId()
    {
        return Long.toUnsignedString(messageId);
    }

    /**
     * The id for this message
     *
     * @return The id for this message
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * Indicates whether the message is from the specified {@link ChannelType ChannelType}
     *
     * @param  type
     *         The ChannelType
     *
     * @return True, if the message is from the specified channel type
     */
    public boolean isFromType(@Nonnull ChannelType type)
    {
        return channel.getType() == type;
    }

    /**
     * Whether this message was sent in a {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>If this is {@code false} then {@link #getGuild()} will throw an {@link java.lang.IllegalStateException}.
     *
     * @return True, if {@link #getChannelType()}.{@link ChannelType#isGuild() isGuild()} is true.
     */
    public boolean isFromGuild()
    {
        return getChannelType().isGuild();
    }

    /**
     * The {@link ChannelType ChannelType} for this message
     *
     * @return The ChannelType
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.api.entities.Guild Guild},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel}.
     *
     * @return The Guild the Message was received in
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public Guild getGuild()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This message event did not happen in a guild");

        return ((GuildChannel) channel).getGuild();
    }

    /**
     * Returns the jump-to URL for the received message.
     * <br>Clicking this URL in the Discord client will cause the client to jump to the specified message.
     *
     * @return A String representing the jump-to URL for the message
     */
    @Nonnull
    public String getJumpUrl()
    {
        return Helpers.format(Message.JUMP_URL, isFromGuild() ? getGuild().getId() : "@me", getChannel().getId(), getMessageId());
    }

    /**
     * If the message event was from a {@link ThreadChannel ThreadChannel}
     *
     * @return If the message event was from a ThreadChannel
     *
     * @see ChannelType#isThread()
     */
    public boolean isFromThread()
    {
        return getChannelType().isThread();
    }
}
