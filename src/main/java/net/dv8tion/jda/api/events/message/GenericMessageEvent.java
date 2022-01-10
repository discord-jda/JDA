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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Message Message} was created/deleted/changed.
 * <br>Every MessageEvent is an instance of this event and can be casted.
 *
 * <p>Can be used to detect any MessageEvent.
 *
 * <h2>Requirements</h2>
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
     * The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} for this Message
     *
     * @return The MessageChannel
     */
    @Nonnull
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.GuildMessageChannel GuildMessageChannel} for this Message
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
    public GuildMessageChannel getGuildChannel()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This message event did not happen in a guild");
        return (GuildMessageChannel) channel;
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
     * Indicates whether the message is from the specified {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
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
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} for this message
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
     * <br>If this Message was not received in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.GuildChannel}.
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
     * The {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.TextChannel}.
     *
     * @return The TextChannel the Message was received in
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public TextChannel getTextChannel()
    {
        if (!isFromType(ChannelType.TEXT))
            throw new IllegalStateException("This message event did not happen in a text channel");
        return (TextChannel) channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.NewsChannel}.
     *
     * @return The NewsChannel the Message was received in
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public NewsChannel getNewsChannel()
    {
        if (!isFromType(ChannelType.NEWS))
            throw new IllegalStateException("This message event did not happen in a news channel");
        return (NewsChannel) channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.PrivateChannel}.
     *
     * @return The PrivateChannel the Message was received in
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public PrivateChannel getPrivateChannel()
    {
        if (!isFromType(ChannelType.PRIVATE))
            throw new IllegalStateException("This message event did not happen in a private channel");
        return (PrivateChannel) channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent in a {@link net.dv8tion.jda.api.entities.ThreadChannel}.
     *
     * @return The ThreadChannel the Message was received in
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     * @see    #isFromThread()
     */
    @Nonnull
    public ThreadChannel getThreadChannel()
    {
        if (!isFromThread())
            throw new IllegalStateException("This message event did not happen in a thread channel");
        return (ThreadChannel) channel;
    }

    /**
     * If the message event was from a {@link net.dv8tion.jda.api.entities.ThreadChannel ThreadChannel}
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
