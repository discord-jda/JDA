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

package net.dv8tion.jda.api.events.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

//TODO-v5: Docs
public abstract class GenericChannelEvent extends Event
{
    protected final Channel channel;

    public GenericChannelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Channel channel)
    {
        super(api, responseNumber);

        this.channel = channel;
    }

    @Nonnull
    public ChannelType getChannelType()
    {
        return this.channel.getType();
    }

    public boolean isFromType(@Nonnull ChannelType type)
    {
        return getChannelType() == type;
    }

    /**
     * Whether this channel event happened in a {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>If this is {@code false} then {@link #getGuild()} will throw an {@link java.lang.IllegalStateException}.
     *
     * @return True, if {@link #getChannelType()}.{@link ChannelType#isGuild() isGuild()} is true.
     */
    public boolean isFromGuild()
    {
        return getChannelType().isGuild();
    }

    /**
     * If the event was from a {@link ThreadChannel ThreadChannel}
     *
     * @return If the event was from a ThreadChannel
     *
     * @see    ChannelType#isThread()
     */
    public boolean isFromThread()
    {
        return getChannelType().isThread();
    }

    /**
     * If the event was from a {@link MessageChannel}
     *
     * @return If the event was from a MessageChannel
     *
     * @see    ChannelType#isMessage()
     */
    public boolean isFromMessage()
    {
        return getChannelType().isMessage();
    }

    /**
     * If the event was from a {@link AudioChannel}
     *
     * @return If the event was from a AudioChannel
     *
     * @see    ChannelType#isAudio()
     */
    public boolean isFromAudio()
    {
        return getChannelType().isAudio();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} in which this channel event happened.
     * <br>If this channel event was not received in a {@link TextChannel TextChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this channel event did not happen in a {@link GuildChannel}.
     *
     * @return The Guild in which this channel event happened
     *
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public Guild getGuild()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This channel event did not happen in a guild");
        return ((GuildChannel) channel).getGuild();
    }

    @Nonnull
    public Channel getChannel()
    {
        return this.channel;
    }

    /**
     * The {@link GuildChannel} this event was received for.
     * <br>If this event was not received from a {@link net.dv8tion.jda.api.entities.Guild Guild},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent for a channel in a Guild.
     *
     * @return The GuildChannel
     */
    @Nonnull
    public GuildChannel getGuildChannel()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This event did not happen for a guild");
        return (GuildChannel) channel;
    }


    // MessageChannel types


    @Nonnull
    public MessageChannel getMessageChannel()
    {
        if (!getChannelType().isMessage())
            throw new IllegalStateException("This event did not happen for a message channel");
        return (MessageChannel) channel;
    }

    @Nonnull
    public GuildMessageChannel getGuildMessageChannel()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This event did not happen for a guild message channel");
        return (GuildMessageChannel) getMessageChannel();
    }

    /**
     * The {@link TextChannel} this event was received for.
     * <br>If this event was not received in a {@link TextChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent for a {@link TextChannel}.
     *
     * @return The TextChannel
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public TextChannel getTextChannel()
    {
        if (!isFromType(ChannelType.TEXT))
            throw new IllegalStateException("This event did not happen for a text channel");
        return (TextChannel) channel;
    }

    /**
     * The {@link NewsChannel} this event was received for.
     * <br>If this event was not received in a {@link NewsChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent for a {@link NewsChannel}.
     *
     * @return The NewsChannel
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public NewsChannel getNewsChannel()
    {
        if (!isFromType(ChannelType.NEWS))
            throw new IllegalStateException("This event did not happen for a news channel");
        return (NewsChannel) channel;
    }

    /**
     * The {@link PrivateChannel} this event was received for.
     * <br>If this event was not received in a {@link PrivateChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent for a {@link PrivateChannel}.
     *
     * @return The PrivateChannel
     *
     * @see    #isFromGuild()
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @Nonnull
    public PrivateChannel getPrivateChannel()
    {
        if (!isFromType(ChannelType.PRIVATE))
            throw new IllegalStateException("This event did not happen for a private channel");
        return (PrivateChannel) channel;
    }

    /**
     * The {@link ThreadChannel} this event was received for.
     * <br>If this event was not received in a {@link ThreadChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this was not sent for a {@link ThreadChannel}.
     *
     * @return The ThreadChannel
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
            throw new IllegalStateException("This event did not happen for a thread channel");
        return (ThreadChannel) channel;
    }


    // AudioChannel types


    @Nonnull
    public AudioChannel getAudioChannel()
    {
        if (!getChannelType().isAudio())
            throw new IllegalStateException("This event did not happen for an audio channel");
        return (AudioChannel) channel;
    }

    @Nonnull
    public VoiceChannel getVoiceChannel()
    {
        if (!isFromType(ChannelType.VOICE))
            throw new IllegalStateException("This event did not happen for a voice channel");
        return (VoiceChannel) channel;
    }

    @Nonnull
    public StageChannel getStageChannel()
    {
        if (!isFromType(ChannelType.STAGE))
            throw new IllegalStateException("This event did not happen for a stage channel");
        return (StageChannel) channel;
    }


    // Misc types


    @Nonnull
    public Category getCategory()
    {
        if (!isFromType(ChannelType.CATEGORY))
            throw new IllegalStateException("This event did not happen for a category");
        return (Category) channel;
    }
    
    @Nonnull
    public StoreChannel getStoreChannel()
    {
        if (!isFromType(ChannelType.STORE))
            throw new IllegalStateException("This event did not happen for a store channel");
        return (StoreChannel) channel;
    }
}
