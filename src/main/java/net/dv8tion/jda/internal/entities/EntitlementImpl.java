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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class EntitlementImpl implements Entitlement
{
    private long id;
    private long skuId;
    private long applicationId;
    private long userId;
    private long guildId;
    private EntitlementType type;
    private boolean deleted;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;

    public EntitlementImpl(long id, long skuId, long applicationId, long userId, long guildId, EntitlementType type, boolean deleted, @Nullable OffsetDateTime startsAt, @Nullable OffsetDateTime endsAt)
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

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public long getSkuIdLong()
    {
        return skuId;
    }

    @Override
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    @Override
    public long getUserIdLong()
    {
        return userId;
    }

    @Override
    public long getGuildIdLong()
    {
        return guildId;
    }

    @Nonnull
    @Override
    public EntitlementType getType()
    {
        return type;
    }

    @Override
    public boolean isDeleted()
    {
        return deleted;
    }

    @Nullable
    @Override
    public OffsetDateTime getTimeStarting()
    {
        return startsAt;
    }

    @Nullable
    @Override
    public OffsetDateTime getTimeEnding()
    {
        return endsAt;
    }
}
