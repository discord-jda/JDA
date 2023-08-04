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

package net.dv8tion.jda.api.events.channel.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelField;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Indicates that the {@link Channel#getFlags() flags} of a {@link Channel} changed.
 *
 * <p>Can be used to retrieve the old flags and the new ones.
 *
 * @see ChannelField#FLAGS
 */
public class ChannelUpdateFlagsEvent extends GenericChannelUpdateEvent<EnumSet<ChannelFlag>>
{
    public static final ChannelField FIELD = ChannelField.FLAGS;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateFlagsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Channel channel, @Nonnull EnumSet<ChannelFlag> oldValue, @Nonnull EnumSet<ChannelFlag> newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }

    @Nonnull
    @Override
    public EnumSet<ChannelFlag> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public EnumSet<ChannelFlag> getNewValue()
    {
        return super.getNewValue();
    }
}
