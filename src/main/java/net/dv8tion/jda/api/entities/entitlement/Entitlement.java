package net.dv8tion.jda.api.entities.entitlement;

import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class Entitlement
{
    private String id;
    private String skuId;
    private String applicationId;
    private String userId;
    @Nullable
    private String promotionId;
    private Integer type;
    private Boolean deleted;
    private Long giftCodeFlags;
    private Boolean consumed;
    @Nullable
    private OffsetDateTime startsAt;
    @Nullable
    private OffsetDateTime endsAt;
    @Nullable
    private String subscriptionId;

    public Entitlement(String id, String skuId, String applicationId, String userId, @Nullable String promotionId, Integer type, Boolean deleted, Long giftCodeFlags, Boolean consumed, @Nullable OffsetDateTime startsAt, @Nullable OffsetDateTime endsAt, @Nullable String subscriptionId)
    {
        this.id = id;
        this.skuId = skuId;
        this.applicationId = applicationId;
        this.userId = userId;
        this.promotionId = promotionId;
        this.type = type;
        this.deleted = deleted;
        this.giftCodeFlags = giftCodeFlags;
        this.consumed = consumed;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.subscriptionId = subscriptionId;
    }

    public Entitlement(DataObject dataObject) {
        this.id = dataObject.getString("id");
        this.skuId = dataObject.getString("sku_id");
        this.applicationId = dataObject.getString("application_id");
        this.userId = dataObject.getString("user_id");
        this.promotionId = dataObject.getString("promotion_id");
        this.type = dataObject.getInt("type");
        this.deleted = dataObject.getBoolean("deleted");
        this.giftCodeFlags = dataObject.getLong("gift_code_flags");
        this.consumed = dataObject.getBoolean("consumed");
        this.startsAt = dataObject.getOffsetDateTime("starts_at");
        this.endsAt = dataObject.getOffsetDateTime("ends_at");
        this.subscriptionId = dataObject.getString("subscription_id");
    }

    public String getId()
    {
        return id;
    }

    public String getSkuId()
    {
        return skuId;
    }

    public String getApplicationId()
    {
        return applicationId;
    }

    public String getUserId()
    {
        return userId;
    }

    @Nullable
    public String getPromotionId()
    {
        return promotionId;
    }

    public Integer getType()
    {
        return type;
    }

    public Boolean getDeleted()
    {
        return deleted;
    }

    public Long getGiftCodeFlags()
    {
        return giftCodeFlags;
    }

    public Boolean getConsumed()
    {
        return consumed;
    }

    @Nullable
    public OffsetDateTime getStartsAt()
    {
        return startsAt;
    }

    @Nullable
    public OffsetDateTime getEndsAt()
    {
        return endsAt;
    }

    @Nullable
    public String getSubscriptionId()
    {
        return subscriptionId;
    }
}
