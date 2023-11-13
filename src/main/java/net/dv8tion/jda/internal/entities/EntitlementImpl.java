package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class EntitlementImpl implements Entitlement
{
    private String id;
    private String skuId;
    private String applicationId;
    @Nullable
    private String userId;
    @Nullable
    private String guildId;
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

    public EntitlementImpl(String id, String skuId, String applicationId, @Nullable String userId, @Nullable String guildId, @Nullable String promotionId, Integer type, Boolean deleted, Long giftCodeFlags, Boolean consumed, @Nullable OffsetDateTime startsAt, @Nullable OffsetDateTime endsAt, @Nullable String subscriptionId)
    {
        this.id = id;
        this.skuId = skuId;
        this.applicationId = applicationId;
        this.userId = userId;
        this.guildId = guildId;
        this.promotionId = promotionId;
        this.type = type;
        this.deleted = deleted;
        this.giftCodeFlags = giftCodeFlags;
        this.consumed = consumed;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.subscriptionId = subscriptionId;
    }

    public EntitlementImpl(DataObject dataObject) {
        this.id = dataObject.getString("id");
        this.skuId = dataObject.getString("sku_id");
        this.applicationId = dataObject.getString("application_id");
        this.userId = dataObject.getString("user_id");
        this.guildId = dataObject.getString("guild_id");
        this.promotionId = dataObject.getString("promotion_id");
        this.type = dataObject.getInt("type");
        this.deleted = dataObject.getBoolean("deleted");
        this.giftCodeFlags = dataObject.getLong("gift_code_flags");
        this.consumed = dataObject.getBoolean("consumed");
        this.startsAt = dataObject.getOffsetDateTime("starts_at");
        this.endsAt = dataObject.getOffsetDateTime("ends_at");
        this.subscriptionId = dataObject.getString("subscription_id");
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getSkuId()
    {
        return skuId;
    }

    @Override
    public String getApplicationId()
    {
        return applicationId;
    }

    @Override
    @Nullable
    public String getUserId()
    {
        return userId;
    }

    @Override
    @Nullable
    public String getGuildId()
    {
        return guildId;
    }

    @Nullable
    public String getPromotionId()
    {
        return promotionId;
    }

    @Override
    public Integer getType()
    {
        return type;
    }

    @Override
    public Boolean getDeleted()
    {
        return deleted;
    }

    @Override
    public Long getGiftCodeFlags()
    {
        return giftCodeFlags;
    }

    @Override
    public Boolean getConsumed()
    {
        return consumed;
    }

    @Override
    @Nullable
    public OffsetDateTime getStartsAt()
    {
        return startsAt;
    }

    @Override
    @Nullable
    public OffsetDateTime getEndsAt()
    {
        return endsAt;
    }

    @Override
    @Nullable
    public String getSubscriptionId()
    {
        return subscriptionId;
    }
}
