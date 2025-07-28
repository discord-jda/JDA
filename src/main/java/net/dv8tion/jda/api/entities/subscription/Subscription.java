package net.dv8tion.jda.api.entities.subscription;

import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representation of a Discord Subscription
 * <br> This class is immutable
 */
public class Subscription
{
    private final JDAImpl api;
    private final long id;
    private final long subscriberId;
    private final List<Long> skuIDs;
    private final List<Long> entitlementIDs;
    private final List<Long> renewalSkuIDs;
    private final OffsetDateTime currentPeriodStart;
    private final OffsetDateTime currentPeriodEnd;
    private final OffsetDateTime canceledAt;
    private final SubscriptionStatus status;

    public Subscription(@Nonnull final JDAImpl api, final long id, final long subscriberId, @Nonnull final List<Long> skuIDs,
                        @Nonnull final List<Long> entitlementIDs, @Nullable final List<Long> renewalSkuIDs,
                        @Nonnull final OffsetDateTime currentPeriodStart, @Nonnull final OffsetDateTime currentPeriodEnd, @Nullable final OffsetDateTime canceledAt,
                        @Nonnull final SubscriptionStatus status)
    {

        this.api = api;
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

    public Long getId()
    {
        return id;
    }

    public Long getSubscriberId()
    {
        return subscriberId;
    }

    public List<Long> getSkuIDs()
    {
        return skuIDs;
    }

    public List<Long> getEntitlementIDs()
    {
        return entitlementIDs;
    }

    public List<Long> getRenewalSkuIDs()
    {
        return renewalSkuIDs;
    }

    public OffsetDateTime getCurrentPeriodStart()
    {
        return currentPeriodStart;
    }

    public OffsetDateTime getCurrentPeriodEnd()
    {
        return currentPeriodEnd;
    }

    public OffsetDateTime getCanceledAt()
    {
        return canceledAt;
    }

    public SubscriptionStatus getStatus()
    {
        return status;
    }


    public JDAImpl getApi()
    {
        return api;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;
        if(!(obj instanceof Subscription))
        {
            return false;
        }
        Subscription other = (Subscription) obj;
        return other.id == this.id;
    }

    @Override
    public String toString(){
        return new EntityString(this)
                .addMetadata("id", id)
                .toString();
    }
}
