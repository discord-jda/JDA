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
    private Integer type;
    private Boolean deleted;
    @Nullable
    private OffsetDateTime startsAt;
    @Nullable
    private OffsetDateTime endsAt;

    public EntitlementImpl(String id, String skuId, String applicationId, @Nullable String userId, @Nullable String guildId, Integer type, Boolean deleted, @Nullable OffsetDateTime startsAt, @Nullable OffsetDateTime endsAt)
    {
        this.id = id;
        this.skuId = skuId;
        this.applicationId = applicationId;
        this.userId = userId;
        this.guildId = guildId;
        this.type = type;
        this.deleted = deleted;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public EntitlementImpl(DataObject data) {
        this.id = data.getString("id");
        this.skuId = data.getString("sku_id");
        this.applicationId = data.getString("application_id");
        this.userId = data.getString("user_id", null);
        this.guildId = data.getString("guild_id", null);
        this.type = data.getInt("type");
        this.deleted = data.getBoolean("deleted");
        this.startsAt = data.getOffsetDateTime("starts_at", null);
        this.endsAt = data.getOffsetDateTime("ends_at", null);
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
}
