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
import net.dv8tion.jda.api.entities.channel.ChannelType;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link Channel Channel's} {@link ChannelType} was updated.
 *
 * <p>Can be used to retrieve the old {@link ChannelType} and the new one.
 *
 * <p>This event will most likely be fired when a {@link Channel}:
 * <ul>
 *     <li>of type {@link ChannelType#TEXT} is converted to type {@link ChannelType#NEWS}</li>
 *     <li>of type {@link ChannelType#NEWS} is converted to type {@link ChannelType#TEXT}</li>
 *     <li>of type {@link ChannelType#FORUM} is converted to type {@link ChannelType#MEDIA}</li>
 * </ul>
 *
 * @see Channel#getType()
 * @see ChannelField#TYPE
 */
public class ChannelUpdateTypeEvent extends GenericChannelUpdateEvent<ChannelType>
{
    public static final ChannelField FIELD = ChannelField.TYPE;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateTypeEvent(@Nonnull JDA api, long responseNumber, Channel channel, ChannelType oldValue, ChannelType newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }
}
