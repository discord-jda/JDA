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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Top-level channel event type
 * <br>All channel events JDA fires are derived from this class.
 *
 * <p>Can be used to check if an Object is a JDA event in {@link net.dv8tion.jda.api.hooks.EventListener EventListener} implementations to distinguish what event is being fired.
 * <br>Adapter implementation: {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter}
 */
public class GenericChannelEvent extends Event
{
    protected final Channel channel;

    public GenericChannelEvent(@Nonnull JDA api, long responseNumber, Channel channel)
    {
        super(api, responseNumber);

        this.channel = channel;
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
     * The {@link ChannelType} of the channel the event was fired from.
     *
     * @return The {@link ChannelType} of the channel the event was fired from.
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return this.channel.getType();
    }

    /**
     * Used to determine if this event was received from a {@link Channel}
     * of the {@link net.dv8tion.jda.api.entities.channel.ChannelType ChannelType} specified.
     *
     * <p>Useful for restricting functionality to a certain type of channels.
     *
     * @param  type
     *         The {@link ChannelType ChannelType} to check against.
     *
     * @return True if the {@link net.dv8tion.jda.api.entities.channel.ChannelType ChannelType} which this message was received
     *         from is the same as the one specified by {@code type}.
     */
    public boolean isFromType(@Nonnull ChannelType type)
    {
        return getChannelType() == type;
    }

    /**
     * The {@link Channel} the event was fired from.
     *
     * @return The {@link ChannelType} of the channel the event was fired from.
     */
    @Nonnull
    public ChannelUnion getChannel()
    {
        return (ChannelUnion) this.channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} in which this channel event happened.
     * <br>If this channel event was not received in a {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel GuildChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this channel event did not happen in a {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel}.
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
}
