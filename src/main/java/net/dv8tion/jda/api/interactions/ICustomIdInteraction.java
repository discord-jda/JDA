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

import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;

import javax.annotation.Nonnull;

/**
 * Marker interface for interactions with custom IDs.
 *
 * <p>This includes modals and components.
 */
public interface ICustomIdInteraction {
    /**
     * The relevant custom ID, that has been provided for the component or modal when it was originally created.
     * <br>This value should be used to determine what action to take in regard to this interaction.
     *
     * <br>This id does not have to be numerical.
     *
     * @return The custom ID
     *
     * @see ComponentInteraction#getComponentId()
     * @see ModalInteraction#getModalId()
     */
    @Nonnull
    String getCustomId();
}
