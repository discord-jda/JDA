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

    public EntitlementImpl(DataObject data) {
        this.id = data.getString("id");
        this.skuId = data.getString("sku_id");
        this.applicationId = data.getString("application_id");
        this.userId = data.getString("user_id", null);
        this.guildId = data.getString("guild_id", null);
        this.promotionId = data.getString("promotion_id", null);
        this.type = data.getInt("type");
        this.deleted = data.getBoolean("deleted");
        this.giftCodeFlags = data.getLong("gift_code_flags");
        this.consumed = data.getBoolean("consumed");
        this.startsAt = data.getOffsetDateTime("starts_at", null);
        this.endsAt = data.getOffsetDateTime("ends_at", null);
        this.subscriptionId = data.getString("subscription_id", null);
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
