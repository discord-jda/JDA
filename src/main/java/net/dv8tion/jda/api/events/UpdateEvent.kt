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
package net.dv8tion.jda.api.events

import javax.annotation.Nonnull

/**
 * Indicates that a value of an entity was updated
 *
 * @param <E>
 * The entity type
 * @param <T>
 * The value type
</T></E> */
interface UpdateEvent<E, T> : GenericEvent {
    val entityType: Class<E>?
        /**
         * Class representation of the affected entity, useful when dealing with refection.
         *
         * @return The class of the affected entity
         */
        @Nonnull get() = entity.javaClass as Class<E>?
    val propertyIdentifier: String
        /**
         * The field name for the updated property
         *
         *
         * **Example**<br></br>
         * <pre>`
         * @Override
         * public void onGenericRoleUpdate(GenericRoleUpdateEvent event)
         * {
         * switch (event.getPropertyIdentifier())
         * {
         * case RoleUpdateColorEvent.IDENTIFIER:
         * System.out.printf("Updated color for role: %s%n", event);
         * break;
         * case RoleUpdatePositionEvent.IDENTIFIER:
         * RoleUpdatePositionEvent update = (RoleUpdatePositionEvent) event;
         * System.out.printf("Updated position for role: %s raw(%s->%s)%n", event, update.getOldPositionRaw(), update.getNewPositionRaw());
         * break;
         * default: return;
         * }
         * }
        `</pre> *
         *
         * @return The name of the updated property
         */
        @Nonnull get
    val entity: E
        /**
         * The affected entity
         *
         * @return The affected entity
         */
        @Nonnull get

    /**
     * The old value
     *
     * @return The old value
     */
    val oldValue: T?

    /**
     * The new value
     *
     * @return The new value
     */
    val newValue: T?
}
