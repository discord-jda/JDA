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

package net.dv8tion.jda.internal.entities.subscription;

import net.dv8tion.jda.api.entities.subscription.Subscription;
import net.dv8tion.jda.api.entities.subscription.SubscriptionStatus;
import net.dv8tion.jda.internal.utils.EntityString;

import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SubscriptionImpl implements Subscription {
    private final long id;
    private final long subscriberId;
    private final List<Long> skuIDs;
    private final List<Long> entitlementIDs;
    private final List<Long> renewalSkuIDs;
    private final OffsetDateTime currentPeriodStart;
    private final OffsetDateTime currentPeriodEnd;
    private final OffsetDateTime canceledAt;
    private final SubscriptionStatus status;

    public SubscriptionImpl(
            long id,
            long subscriberId,
            @Nonnull List<Long> skuIDs,
            @Nonnull List<Long> entitlementIDs,
            @Nullable List<Long> renewalSkuIDs,
            @Nonnull OffsetDateTime currentPeriodStart,
            @Nonnull OffsetDateTime currentPeriodEnd,
            @Nullable OffsetDateTime canceledAt,
            @Nonnull SubscriptionStatus status) {

        this.id = id;
        this.subscriberId = subscriberId;
        this.skuIDs = skuIDs;
        this.entitlementIDs = entitlementIDs;
        this.renewalSkuIDs = renewalSkuIDs;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.canceledAt = canceledAt;
        this.status = status;
    }

    @Override
    @Nonnull
    public long getIdLong() {
        return id;
    }

    @Override
    public @Nonnull long getSubscriberIdLong() {
        return subscriberId;
    }

    @Override
    public @Nonnull List<Long> getSkuIdsLong() {
        return skuIDs;
    }

    @Override
    public @Nonnull List<Long> getEntitlementIdsLong() {
        return entitlementIDs;
    }

    @Override
    public @Nullable List<Long> getRenewalSkuIdsLong() {
        return renewalSkuIDs;
    }

    @Override
    public @Nonnull OffsetDateTime getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    @Override
    public @Nonnull OffsetDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    @Override
    public @Nullable OffsetDateTime getCanceledAt() {
        return canceledAt;
    }

    @Override
    public @Nonnull SubscriptionStatus getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SubscriptionImpl)) {
            return false;
        }

        SubscriptionImpl other = (SubscriptionImpl) obj;
        return other.id == this.id;
    }

    @Override
    public String toString() {
        return new EntityString(this).addMetadata("id", id).toString();
    }
}
