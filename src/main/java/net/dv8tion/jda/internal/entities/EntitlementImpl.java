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
