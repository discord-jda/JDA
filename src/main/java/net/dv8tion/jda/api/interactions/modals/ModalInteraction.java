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

package net.dv8tion.jda.api.interactions.modals;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Interaction on a {@link Modal}
 *
 * <p>If the modal of this interaction was a reply to a {@link ComponentInteraction ComponentInteraction},
 * you can also use {@link #deferEdit()} to edit the original message that contained the component instead of replying.
 *
 * @see    net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
 */
public interface ModalInteraction extends IReplyCallback, IMessageEditCallback
{
    /**
     * Returns the custom id of the Modal in question
     *
     * @return Custom id
     * 
     * @see    Modal.Builder#setId(String)
     */
    @Nonnull
    String getModalId();

    /**
     * Returns a List of {@link net.dv8tion.jda.api.interactions.modals.ModalMapping ModalMappings} representing the values input by the user for each field when the modal was submitted.
     *
     * @return Immutable List of {@link net.dv8tion.jda.api.interactions.modals.ModalMapping ModalMappings}
     *
     * @see    #getValue(String)
     */
    @Nonnull
    @Unmodifiable
    List<ModalMapping> getValues();

    /**
     * Convenience method to get a {@link net.dv8tion.jda.api.interactions.modals.ModalMapping ModalMapping} by its id from the List of {@link net.dv8tion.jda.api.interactions.modals.ModalMapping ModalMappings}
     *
     * <p>Returns null if no component with that id has been found
     *
     * @param  customId
     *         The custom id
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     *
     * @return ModalMapping with this id, or null if not found
     *
     * @see    #getValues()
     */
    @Nullable
    default ModalMapping getValue(@Nonnull String customId)
    {
        Checks.notNull(customId, "ID");
        return getValues().stream()
                .filter(mapping -> mapping.getCustomId().equals(customId))
                .findFirst().orElse(null);
    }

    /**
     * Convenience method to get a {@link net.dv8tion.jda.api.interactions.modals.ModalMapping ModalMapping} by its numeric id from the List of {@link net.dv8tion.jda.api.interactions.modals.ModalMapping ModalMappings}
     *
     * <p>Returns null if no component with that id has been found
     *
     * @param  id
     *         The numeric id
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     *
     * @return ModalMapping with this numeric id, or null if not found
     *
     * @see    #getValues()
     */
    @Nullable
    default ModalMapping getValue(int id)
    {
        return getValues().stream()
                .filter(mapping -> mapping.getUniqueId() == id)
                .findFirst().orElse(null);
    }

    /**
     * Message this modal came from, if it was a reply to a {@link ComponentInteraction ComponentInteraction}.
     *
     * @return The message the component is attached to, or {@code null}
     */
    @Nullable
    Message getMessage();

    @Nonnull
    @Override
    MessageChannelUnion getChannel();

    @Nonnull
    @Override
    default GuildMessageChannelUnion getGuildChannel()
    {
        return (GuildMessageChannelUnion) IReplyCallback.super.getGuildChannel();
    }
}
