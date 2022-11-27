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
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Top-level channel update event type indicating that a field of a {@link Channel} was updated.
 * <br>All channel update events JDA fires are derived from this class.
 *
 * @param <T>
 *        The value type of the field that has been updated.
 * @see ChannelField
 */
public class GenericChannelUpdateEvent<T> extends GenericChannelEvent implements UpdateEvent<Channel, T>
{
    protected final ChannelField channelField;
    protected final T oldValue;
    protected final T newValue;

    public GenericChannelUpdateEvent(@Nonnull JDA api, long responseNumber, Channel channel, ChannelField channelField, T oldValue, T newValue)
    {
        super(api, responseNumber, channel);

        this.channelField = channelField;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * The identifier of the {@link Channel Channel's} field that has just been updated.
     *
     * @return The identifier of the {@link Channel Channel's} field that has just been updated.
     */
    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return channelField.getFieldName();
    }

    /**
     * The {@link Channel} entity affected by this update event.
     * <br>Equivalent with {@code getChannel()}.
     *
     * @return The {@link Channel} entity affected by this update event.
     */
    @Nonnull
    @Override
    public Channel getEntity()
    {
        return getChannel();
    }

    /**
     * The {@link Channel Channel's} old value of the just updated field.
     *
     * @return The old value of the just updated field.
     */
    @Nullable
    @Override
    public T getOldValue()
    {
        return oldValue;
    }

    /**
     * The {@link Channel Channel's} new value of the just updated field.
     *
     * @return The new value of the just updated field.
     */
    @Nullable
    @Override
    public T getNewValue()
    {
        return newValue;
    }
}
