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

import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Represents a user or guild that has access to a premium offering in your application.
 *
 * @see <a href="https://discord.com/developers/docs/monetization/entitlements" target="_blank">Discord Docs about Entitlements</a>
 */
public interface Entitlement extends ISnowflake
{

    /**
     * The id of the SKU related to this {@link Entitlement Entitlement}
     *
     * @return The id of the SKU related to this {@link Entitlement Entitlement}
     */
    long getSkuIdLong();

    /**
     * The id of the SKU related to this {@link Entitlement Entitlement}
     *
     * @return The id of the SKU related to this {@link Entitlement Entitlement}
     */
    @Nonnull
    default String getSkuId()
    {
        return Long.toUnsignedString(getSkuIdLong());
    }

    /**
     * The id of the parent application of this {@link Entitlement Entitlement}
     *
     * @return The id of the parent application of this {@link Entitlement Entitlement}
     */
    long getApplicationIdLong();

    /**
     * The id of the parent application of this {@link Entitlement Entitlement}
     *
     * @return The id of the parent application of this {@link Entitlement Entitlement}
     */
    @Nonnull
    default String getApplicationId()
    {
        return Long.toUnsignedString(getApplicationIdLong());
    }

    /**
     * The id of the user that purchased the {@link Entitlement Entitlement}
     *
     * @return The id of the user that purchased the {@link Entitlement Entitlement}
     */
    long getUserIdLong();

    /**
     * The id of the user that purchased the {@link Entitlement Entitlement}
     *
     * @return The id of the user that purchased the {@link Entitlement Entitlement}
     */
    default String getUserId()
    {
        return Long.toUnsignedString(getUserIdLong());
    }

    /**
     * The guild id that is granted access to the {@link Entitlement Entitlement}s SKU
     *
     * @return The id of the guild that purchased the {@link Entitlement Entitlement} or 0 if this is not a guild subscription
     */
    long getGuildIdLong();

    /**
     * The guild id that is granted access to the {@link Entitlement Entitlement}s SKU
     *
     * @return The id of the guild that purchased the {@link Entitlement Entitlement} or {@code null} if this is not a guild subscription
     */
    @Nullable
    default String getGuildId()
    {
        if (getGuildIdLong() == 0)
            return null;

        return Long.toUnsignedString(getGuildIdLong());
    }

    /**
     * The type of the Entitlement
     *
     * @return the {@link Entitlement Entitlement} type
     */
    @Nonnull
    EntitlementType getType();

    /**
     * Whether the {@link Entitlement Entitlement} has been deleted or not.
     *
     * @return True if the {@link Entitlement Entitlement} was deleted, False otherwise
     *
     * @see    net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent
     */
    boolean isDeleted();

    /**
     * The start date at which the {@link Entitlement Entitlement} is valid.
     *
     * @return Start date at which the {@link Entitlement Entitlement} is valid. Not present when using test entitlements.
     */
    @Nullable
    OffsetDateTime getTimeStarting();

    /**
     * Date at which the {@link Entitlement Entitlement} is no longer valid.
     *
     * @return Date at which the {@link Entitlement Entitlement} is no longer valid. Not present when using test entitlements.
     */
    @Nullable
    OffsetDateTime getTimeEnding();

    /**
     * Whether the {@link Entitlement Entitlement} was consumed or not.
     *
     * @return True if the {@link Entitlement Entitlement} was consumed, False otherwise
     */
    boolean isConsumed();

    /**
     * Consumes the {@link Entitlement Entitlement} if it has not already been consumed.
     *
     * @return A {@link RestAction} that will consume the {@link Entitlement Entitlement}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> consume();

    /**
     * Represents the type of this Entitlement
     */
    enum EntitlementType
    {
        /**
         * Entitlement was purchased by user
         */
        PURCHASE(1),
        /**
         * Entitlement for Discord Nitro subscription
         */
        PREMIUM_SUBSCRIPTION(2),
        /**
         * Entitlement was gifted by developer
         */
        DEVELOPER_GIFT(3),
        /**
         * Entitlement was purchased by a dev in application test mode
         */
        TEST_MODE_PURCHASE(4),
        /**
         * Entitlement was granted when the SKU was free
         */
        FREE_PURCHASE(5),
        /**
         * Entitlement was gifted by another user
         */
        USER_GIFT(6),
        /**
         * Entitlement was claimed by user for free as a Nitro Subscriber
         */
        PREMIUM_PURCHASE(7),
        /**
         * 	Entitlement was purchased as an app subscription
         */
        APPLICATION_SUBSCRIPTION(8),
        /**
         * Placeholder for unsupported types.
         */
        UNKNOWN(-1);

        private final int key;

        EntitlementType(int key)
        {
            this.key = key;
        }

        /**
         * The Discord defined id key for this EntitlementType.
         *
         * @return the id key.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Gets the EntitlementType related to the provided key.
         * <br>If an unknown key is provided, this returns {@link #UNKNOWN}
         *
         * @param  key
         *         The Discord key referencing a EntitlementType.
         *
         * @return The EntitlementType that has the key provided, or {@link #UNKNOWN} for unknown key.
         */
        @Nonnull
        public static EntitlementType fromKey(int key)
        {
            for (EntitlementType type : values())
            {
                if (type.getKey() == key)
                    return type;
            }
            return UNKNOWN;
        }
    }
}
