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

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nonnull;

/**
 * Interaction on a message {@link ActionComponent}.
 *
 * <p>Instead of {@link #deferReply()} and {@link #reply(String)} you can use {@link #deferEdit()} and {@link #editMessage(String)} with these interactions!
 * <b>You can only acknowledge an interaction once!</b>
 */
public interface ComponentInteraction extends IReplyCallback, IMessageEditCallback
{
    /**
     * The custom component ID provided to the component when it was originally created.
     * <br>This value should be used to determine what action to take in regard to this interaction.
     *
     * <br>This id does not have to be numerical.
     *
     * @return The component ID
     *
     * @see    ActionComponent#getId()
     */
    @Nonnull
    String getComponentId();

    /**
     * The {@link ActionComponent} instance.
     *
     * @return The {@link ActionComponent}
     */
    @Nonnull
    ActionComponent getComponent();

    /**
     * The {@link Message} instance.
     *
     * @return The {@link Message}
     */
    @Nonnull
    Message getMessage();

    /**
     * The id of the message.
     *
     * @return The message id
     */
    long getMessageIdLong();

    /**
     * The id of the message.
     *
     * @return The message id
     */
    @Nonnull
    default String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * The {@link Component.Type}
     *
     * @return The {@link Component.Type}
     */
    @Nonnull
    Component.Type getComponentType();

    /**
     * The respective {@link MessageChannel} for this interaction.
     *
     * @return The {@link MessageChannel}
     */
    @Nonnull
    @Override
    MessageChannel getChannel();
}
