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

package net.dv8tion.jda.api.interactions.commands.context;

import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import javax.annotation.Nonnull;

/**
 * Represents application comments which are invoked as context menu items in the client UI.
 *
 * @param <T>
 *        The target type of this context interaction
 *
 * @see   #getTarget()
 */
public interface ContextInteraction<T> extends CommandInteraction
{
    /**
     * The target type of this context interaction
     *
     * @return The context target type
     */
    @Nonnull
    ContextTarget getTargetType();

    /**
     * The target entity of this context interaction
     *
     * @return The target entity
     */
    @Nonnull
    T getTarget();

    /**
     * The target type, of a context interaction.
     */
    enum ContextTarget
    {
        USER, MESSAGE
    }
}
