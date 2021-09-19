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
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

public class GenericChannelEvent extends Event
{
    protected final Channel channel;

    public GenericChannelEvent(@Nonnull JDA api, long responseNumber, Channel channel)
    {
        super(api, responseNumber);

        this.channel = channel;
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

    //TODO-v5: Add getters for all channel types
}
