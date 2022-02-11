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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Interaction on a {@link net.dv8tion.jda.api.interactions.components.text.Modal Modal}
 *
 * @see net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
 */
public interface ModalInteraction extends IReplyCallback
{
    /**
     * Returns the custom id of the Modal in question
     *
     * @return Custom id
     */
    @Nonnull
    String getModalId();

    /**
     * Returns a List of {@link ModalMapping ModalMappings} the modal in question contains
     *
     * @return List of {@link ModalMapping ModalMappings}
     */
    @Nonnull
    List<ModalMapping> getValues();

    /**
     * Convenience method to get a {@link ModalMapping ModalMapping} by its id from the List of {@link ModalMapping ModalMappings}
     *
     * <p>Returns null if no component with that id has been found
     *
     * @param  id
     *         The custom id
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     *
     * @return ModalMapping with this id, or null if not found
     */
    @Nullable
    default ModalMapping getValue(@Nonnull String id)
    {
        Checks.notNull(id, "ID");
        return getValues().stream()
                .filter(mapping -> mapping.getId().equals(id))
                .findFirst().orElse(null);
    }
}
