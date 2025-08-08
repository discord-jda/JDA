package net.dv8tion.jda.internal.entities.subscription;

import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of a Discord Subscription
 *
 * @see <a href="https://discord.com/developers/docs/resources/subscription" target="_blank">Discord Docs about Subscriptions</a>
 */
public interface Subscription extends ISnowflake
{
    /**
     * The user who subscribed
     *
     * @return a use who subscribed
     */
    @Nonnull
    long getSubscriberIdLong();

    /**
     * The user who subscribed
     *
     * @return a use who subscribed
     */
    @Nonnull
    default String getSubscriberId()
    {
        return Long.toUnsignedString(getSubscriberIdLong());
    }

    /**
     * The SKU id's related to this
     *
     * @return The list of sku id's related to this {@link Subscription}
     */
    @Nonnull
    List<Long> getSkuIdsLong();

    /**
     * The SKU id's related to this
     *
     * @return The list sku id's related to this {@link Subscription}
     */
    @Nonnull
    default List<String> getSkuIds()
    {
        return getSkuIdsLong().stream()
                .map(Long::toUnsignedString)
                .collect(Collectors.toList());
    }

    /**
     * The entitlements id's related to this {@link Subscription}
     *
     * @return The sku id's related to this {@link Subscription}
     */
    @Nonnull
    List<Long> getEntitlementIdsLong();

    /**
     * The entitlements id's related to this {@link Subscription}
     *
     * @return The entitlements id's related to this {@link Subscription}
     */
    @Nonnull
    default List<String> getEntitlementIds()
    {
        return getEntitlementIdsLong().stream()
                .map(Long::toUnsignedString)
                .collect(Collectors.toList());
    }

    /**
     * The renewal SKU id's related to this {@link Subscription}
     *
     * @return The  renewal sku id's related to this {@link Subscription}
     */
    @Nullable
    List<Long> getRenewalSkuIdsLong();

    /**
     * The renewal SKU id's related to this {@link Subscription}
     *
     * @return The  renewal sku id's related to this {@link Subscription}
     */
    @Nullable
    default List<String> getRenewalSkuIds()
    {
        return getRenewalSkuIdsLong().stream()
                .map(Long::toUnsignedString)
                .collect(Collectors.toList());
    }

    /**
     * The start of period of this {@link Subscription}
     *
     * @return The start of period of this {@link Subscription}
     */
    @Nonnull
    OffsetDateTime getCurrentPeriodStart();

    /**
     * The end of period of this {@link Subscription}
     *
     * @return The end of period of this {@link Subscription}
     */
    @Nonnull
    OffsetDateTime getCurrentPeriodEnd();

    /**
     * The canceled time of this {@link Subscription}
     *
     * @return The canceled time of this {@link Subscription}
     */
    @Nullable
    OffsetDateTime getCanceledAt();

    /**
     * The status of this {@link Subscription}
     *
     * @return The status of this {@link Subscription}
     */
    @Nonnull
    SubscriptionStatus getStatus();
}
