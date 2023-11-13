package net.dv8tion.jda.api.entities;


import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Represents a Discord {@link Entitlement Entitlement} for premium App subscriptions.
 * <br>This should contain all information provided from Discord about an Entitlement.
 *
 * @author Giulio Pimenoff V.
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
     * @return The id of the parent application
     */
    String getApplicationId();

    /**
     *
     * @return The id of the user that purchased the entitlement
     */
    String getUserId();

    /**
     *
     * @return The id of the guild that is granted access to the entitlement's sku,
     * or Null if this entitlement is related to a User Subscription type
     */
    @Nullable
    String getGuildId();

    @Nullable
    String getPromotionId();

    Integer getType();

    Boolean getDeleted();

    Long getGiftCodeFlags();

    Boolean getConsumed();

    @Nullable
    OffsetDateTime getStartsAt();

    @Nullable
    OffsetDateTime getEndsAt();

    @Nullable
    String getSubscriptionId();
}
