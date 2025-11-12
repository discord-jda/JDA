package net.dv8tion.jda.internal.entities.subscription;

import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;


public class SubscriptionImpl implements Subscription
{
    private final long id;
    private final long subscriberId;
    private final List<Long> skuIDs;
    private final List<Long> entitlementIDs;
    private final List<Long> renewalSkuIDs;
    private final OffsetDateTime currentPeriodStart;
    private final OffsetDateTime currentPeriodEnd;
    private final OffsetDateTime canceledAt;
    private final SubscriptionStatus status;

    public SubscriptionImpl(final long id, final long subscriberId, @Nonnull final List<Long> skuIDs,
                            @Nonnull final List<Long> entitlementIDs, @Nullable final List<Long> renewalSkuIDs,
                            @Nonnull final OffsetDateTime currentPeriodStart, @Nonnull final OffsetDateTime currentPeriodEnd, @Nullable final OffsetDateTime canceledAt,
                            @Nonnull final SubscriptionStatus status)
    {

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
    @Nonnull public long getIdLong()
    {
        return id;
    }

    public @Nonnull long getSubscriberIdLong()
    {
        return subscriberId;
    }

    public @Nonnull List<Long> getSkuIdsLong()
    {
        return skuIDs;
    }

    public @Nonnull List<Long> getEntitlementIdsLong()
    {
        return entitlementIDs;
    }

    public @Nullable List<Long> getRenewalSkuIdsLong()
    {
        return renewalSkuIDs;
    }

    public @Nonnull OffsetDateTime getCurrentPeriodStart()
    {
        return currentPeriodStart;
    }

    public @Nonnull OffsetDateTime getCurrentPeriodEnd()
    {
        return currentPeriodEnd;
    }

    public @Nullable OffsetDateTime getCanceledAt()
    {
        return canceledAt;
    }

    public @Nonnull SubscriptionStatus getStatus()
    {
        return status;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof SubscriptionImpl))
            return false;

        SubscriptionImpl other = (SubscriptionImpl) obj;
        return other.id == this.id;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", id)
                .toString();
    }
}
