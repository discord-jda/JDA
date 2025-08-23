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

package net.dv8tion.jda.api.interactions.components.selections;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;

import javax.annotation.Nonnull;

/**
 * Component Interaction for a {@link EntitySelectMenu}.
 *
 * @see EntitySelectInteractionEvent
 */
public interface EntitySelectInteraction extends SelectMenuInteraction<IMentionable, EntitySelectMenu>
{
    /**
     * The resolved {@link Mentions} for this selection.
     * <br>This supports {@link Mentions#getRoles() roles}, {@link Mentions#getUsers() users}, and {@link Mentions#getChannels() channels}.
     *
     * @return The mentions
     */
    @Nonnull
    Mentions getMentions();
}
