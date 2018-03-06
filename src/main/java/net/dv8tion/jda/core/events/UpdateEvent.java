/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.events;

/**
 * Indicates that a value of an entity was updated
 *
 * @param <E>
 *        The entity type
 * @param <T>
 *        The value type
 */
public interface UpdateEvent<E, T>
{
    /**
     * Class representation of the affected entity, useful when dealing with refection.
     *
     * @return The class of the affected entity
     */
    @SuppressWarnings("unchecked")
    default Class<E> getEntityType()
    {
        return (Class<E>) getEntity().getClass();
    }

    /**
     * The field name for the updated property
     *
     * <h1>Example</h1>
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
    String getPropertyIdentifier();

    /**
     * The affected entity
     *
     * @return The affected entity
     */
    E getEntity();

    /**
     * The old value
     *
     * @return The old value
     */
    T getOldValue();

    /**
     * The new value
     *
     * @return The new value
     */
    T getNewValue();
}
