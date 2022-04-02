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

package net.dv8tion.jda.api.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a value of an entity was updated
 *
 * @param <E>
 *        The entity type
 * @param <T>
 *        The value type
 */
public interface UpdateEvent<E, T> extends GenericEvent
{
    /**
     * Class representation of the affected entity, useful when dealing with refection.
     *
     * @return The class of the affected entity
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    default Class<E> getEntityType()
    {
        return (Class<E>) getEntity().getClass();
    }

    /**
     * The field name for the updated property
     *
     * <h4>Example</h4>
     * <pre><code>
     * {@literal @Override}
     * public void onGenericRoleUpdate(GenericRoleUpdateEvent event)
     * {
     *     switch (event.getPropertyIdentifier())
     *     {
     *     case RoleUpdateColorEvent.IDENTIFIER:
     *         System.out.printf("Updated color for role: %s%n", event);
     *         break;
     *     case RoleUpdatePositionEvent.IDENTIFIER:
     *         RoleUpdatePositionEvent update = (RoleUpdatePositionEvent) event;
     *         System.out.printf("Updated position for role: %s raw(%s{@literal ->}%s)%n", event, update.getOldPositionRaw(), update.getNewPositionRaw());
     *         break;
     *     default: return;
     *     }
     * }
     * </code></pre>
     *
     * @return The name of the updated property
     */
    @Nonnull
    String getPropertyIdentifier();

    /**
     * The affected entity
     *
     * @return The affected entity
     */
    @Nonnull
    E getEntity();

    /**
     * The old value
     *
     * @return The old value
     */
    @Nullable
    T getOldValue();

    /**
     * The new value
     *
     * @return The new value
     */
    @Nullable
    T getNewValue();
}
