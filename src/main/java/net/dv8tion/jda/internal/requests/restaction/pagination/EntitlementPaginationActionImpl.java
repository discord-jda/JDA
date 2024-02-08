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

package net.dv8tion.jda.internal.requests.restaction.pagination;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.EntitlementPaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntitlementPaginationActionImpl
    extends PaginationActionImpl<Entitlement, EntitlementPaginationAction>
    implements EntitlementPaginationAction
{
    protected List<String> skuIds;
    protected long guildId;
    protected long userId;
    protected boolean excludeEnded;

    public EntitlementPaginationActionImpl(JDA api)
    {
        super(api, Route.Applications.GET_ENTITLEMENTS.compile(api.getSelfUser().getApplicationId()), 1, 100, 100);
        this.skuIds = new ArrayList<>();
        this.guildId = 0;
        this.userId = 0;
    }

    @Nonnull
    @Override
    public EntitlementPaginationAction order(@Nonnull PaginationAction.PaginationOrder order)
    {
        if (order == PaginationOrder.BACKWARD && lastKey == 0)
            lastKey = Long.MAX_VALUE;
        else if (order == PaginationOrder.FORWARD && lastKey == Long.MAX_VALUE)
            lastKey = 0;
        return super.order(order);
    }

    @Nonnull
    @Override
    public EntitlementPaginationAction user(@Nullable UserSnowflake user)
    {
        if (user == null)
            userId = 0;
        else
            userId = user.getIdLong();
        return this;
    }

    @Nonnull
    @Override
    public EntitlementPaginationAction skuIds(long... skuIds)
    {
        this.skuIds.clear();
        for (long skuId : skuIds)
            this.skuIds.add(Long.toUnsignedString(skuId));
        return this;
    }

    @Nonnull
    @Override
    public EntitlementPaginationAction skuIds(@Nonnull String... skuIds)
    {
        Checks.noneNull(skuIds, "skuIds");
        for (String skuId : skuIds)
            Checks.isSnowflake(skuId, "skuId");

        this.skuIds.clear();

        Collections.addAll(this.skuIds, skuIds);
        return this;
    }

    @Nonnull
    @Override
    public EntitlementPaginationAction guild(long guildId)
    {
        this.guildId = guildId;
        return this;
    }

    @Nonnull
    @Override
    public EntitlementPaginationAction excludeEnded(boolean excludeEnded)
    {
        this.excludeEnded = excludeEnded;
        return this;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        if (userId != 0)
            route = route.withQueryParams("user_id", Long.toUnsignedString(userId));

        if (!skuIds.isEmpty())
            route = route.withQueryParams("sku_ids", String.join(",", skuIds));

        if (guildId != 0)
            route = route.withQueryParams("guild_id", Long.toUnsignedString(guildId));

        if (excludeEnded)
            route = route.withQueryParams("exclude_ended", String.valueOf(true));

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Entitlement>> request)
    {
        DataArray array = response.getArray();
        List<Entitlement> entitlements = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            try
            {
                DataObject object = array.getObject(i);
                Entitlement entitlement = builder.createEntitlement(object);
                entitlements.add(entitlement);
            }
            catch(ParsingException | NullPointerException e)
            {
                LOG.warn("Encountered an exception in EntitlementPaginationAction", e);
            }
        }

        if (!entitlements.isEmpty())
        {
            if (useCache)
                cached.addAll(entitlements);
            last = entitlements.get(entitlements.size() - 1);
            lastKey = last.getIdLong();
        }

        request.onSuccess(entitlements);
    }

    @Override
    protected long getKey(Entitlement it)
    {
        return it.getIdLong();
    }
}
