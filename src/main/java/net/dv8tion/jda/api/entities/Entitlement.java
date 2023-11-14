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

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Represents a Discord Entitlement for premium App subscriptions.
 * <br>This should contain all information provided from Discord about an {@link Entitlement Entitlement}.
 */
public interface Entitlement
{
    /**
     *
     * @return The id of the {@link Entitlement Entitlement}
     */
    String getId();

    /**
     *
     * @return The id of the SKU related to this {@link Entitlement Entitlement}
     */
    String getSkuId();

    /**
     *
     * @return The id of the parent application of this {@link Entitlement Entitlement}
     */
    String getApplicationId();

    /**
     *
     * @return The id of the user that purchased the {@link Entitlement Entitlement}
     */
    @Nullable
    String getUserId();

    /**
     *
     * @return The id of the guild that is granted access to the {@link Entitlement Entitlement}'s sku,
     * or Null if this entitlement is related to a subscription of type "User Subscription"
     */
    @Nullable
    String getGuildId();

    /*
     Mentioned by Discord in the example but not included in the api specification
    @Nullable
    String getPromotionId();
     */

    /**
     * The type of the Entitlement
     * <br>The only possible value of this property is 8 at the moment and indicates the "APPLICATION_SUBSCRIPTION" type
     * <br>Discord doesn't currently support other types for entitlements.
     *
     * @return the {@link Entitlement Entitlement} type, 8 is the only possible value
     */
    Integer getType();

    /**
     * @return True if the {@link Entitlement Entitlement} was deleted, False otherwise
     *
     * @see net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent
     */
    Boolean getDeleted();

    /*
     Mentioned by Discord in the example but not included in the api specification
    Long getGiftCodeFlags();
     */

    /*
     Mentioned by Discord in the example but not included in the api specification
    Boolean getConsumed();
     */

    /**
     *
     * @return Start date at which the {@link Entitlement Entitlement} is valid. Not present when using test entitlements.
     */
    @Nullable
    OffsetDateTime getStartsAt();

    /**
     *
     * @return 	Date at which the {@link Entitlement Entitlement} is no longer valid. Not present when using test entitlements.
     */
    @Nullable
    OffsetDateTime getEndsAt();

    /*
     Mentioned by Discord in the example but not included in the api specification
    @Nullable
    String getSubscriptionId();
     */
}
