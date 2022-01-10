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

    @Nonnull
    public ChannelType getChannelType()
    {
        return this.channel.getType();
    }

    public boolean isFromType(ChannelType type)
    {
        return getChannelType() == type;
    }

    @Nonnull
    public Channel getChannel()
    {
        return this.channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} in which this channel event happened.
     * <br>If this channel event was not received in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this channel event did not happen in a {@link net.dv8tion.jda.api.entities.GuildChannel}.
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

    //TODO-v5: Add getters for all channel types
}
