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
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;

import javax.annotation.Nonnull;

/**
 * Indicates that the {@link IThreadContainer#getDefaultThreadSlowmode() default thread slowmode} of a {@link IThreadContainer thread container channel} changed.
 *
 * <p>Can be used to retrieve the old default thread slowmode and the new one.
 *
 * @see ChannelField#DEFAULT_THREAD_SLOWMODE
 */
public class ChannelUpdateDefaultThreadSlowmodeEvent extends GenericChannelUpdateEvent<Integer>
{

    public static final ChannelField FIELD = ChannelField.DEFAULT_THREAD_SLOWMODE;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateDefaultThreadSlowmodeEvent(@Nonnull JDA api, long responseNumber, @Nonnull Channel channel, int oldValue, int newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }

    @Nonnull
    @Override
    public Integer getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Integer getNewValue()
    {
        return super.getNewValue();
    }
}
