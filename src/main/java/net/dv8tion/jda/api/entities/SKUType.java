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

package net.dv8tion.jda.api.entities;

/**
 * SKU Types represent the type of the offered SKU.
 *
 * <p>For subscriptions, SKUs will have a type of either {@link #SUBSCRIPTION} or {@link #SUBSCRIPTION_GROUP}.
 * For any current implementations, you will want to use the SKU type {@link #SUBSCRIPTION}.
 * A {@link #SUBSCRIPTION_GROUP} is automatically created for each SUBSCRIPTION SKU and are not used at this time.
 */
public enum SKUType
{
    /**
     * Durable one-time purchase
     */
    DURABLE(2),
    /**
     * Consumable one-time purchase
     */
    CONSUMABLE(3),
    /**
     * Represents a recurring subscription
     */
    SUBSCRIPTION(5),
    /**
     * System-generated group for each SUBSCRIPTION SKU created
     */
    SUBSCRIPTION_GROUP(6),
    /**
     * Unknown type.
     */
    UNKNOWN(-1);

    private final int id;

    SKUType(int id)
    {
        this.id = id;
    }

    /**
     * Parse a SKU type from the id
     *
     * @param  type
     *         The type id
     *
     * @return SKU type or {@link #UNKNOWN} if the SKU is not known.
     */
    public static SKUType fromId(int type)
    {
        for (SKUType value : values())
        {
            if (value.id == type) return value;
        }
        return UNKNOWN;
    }

    /**
     * The Discord id of the SKU type
     *
     * @return The discord id
     */
    public int getId()
    {
        return id;
    }
}
