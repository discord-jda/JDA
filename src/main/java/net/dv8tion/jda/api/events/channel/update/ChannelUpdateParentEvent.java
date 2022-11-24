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
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link Channel Channel's} parent has been updated.
 *
 * <p>Can be used to retrieve the old parent and the new one.
 *
 * <p>Limited to {@link ICategorizableChannel Categorizable Channels} (and implementations).
 *
 * @see ICategorizableChannel#getParentCategory()
 * @see ChannelField#PARENT
 */
public class ChannelUpdateParentEvent extends GenericChannelUpdateEvent<Category>
{
    public static final ChannelField FIELD = ChannelField.PARENT;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateParentEvent(@Nonnull JDA api, long responseNumber, Channel channel, Category oldValue, Category newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }
}
