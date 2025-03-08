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

package net.dv8tion.jda.api.components;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents a components which contains a unique 32 bit identifier.
 *
 * <p>These identifiers can be set manually or automatically assigned by Discord,
 * and are useful to identify <b>any</b> component.
 */
public interface IdentifiableComponent extends Component
{
    /**
     * The unique identifier of this component.
     * <br>Can be set manually or automatically assigned by Discord (starting from {@code 1}).
     * If it has not been assigned yet, this will return {@code -1}.
     *
     * @return The unique identifier of this component, or {@code -1} if not assigned yet
     */
    int getUniqueId();

    /**
     * Creates a new component with the provided ID.
     *
     * @param  uniqueId
     *         The new ID, must not be negative
     *
     * @throws IllegalArgumentException
     *         If the ID is negative
     *
     * @return The new component
     */
    @Nonnull
    @CheckReturnValue
    IdentifiableComponent withUniqueId(int uniqueId);
}
