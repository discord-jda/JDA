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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntitlementPaginationActionImpl
    extends PaginationActionImpl<Entitlement, EntitlementPaginationAction>
    implements EntitlementPaginationAction
{
    protected String[] skuIds;
    protected Long guildId;
    protected Long userId;
    protected boolean excludeEnded;

    public EntitlementPaginationActionImpl(JDA api)
    {
        super(api, Route.Applications.GET_ENTITLEMENTS.compile(api.getSelfUser().getApplicationId()), 1, 100, 100);
    }

    @NotNull
    @Override
    public EntitlementPaginationAction order(@NotNull PaginationAction.PaginationOrder order)
    {
        if (order == PaginationOrder.BACKWARD && lastKey == 0)
            lastKey = Long.MAX_VALUE;
        else if (order == PaginationOrder.FORWARD && lastKey == Long.MAX_VALUE)
            lastKey = 0;
        return super.order(order);
    }

    @NotNull
    @Override
    public EntitlementPaginationAction user(@Nullable UserSnowflake user)
    {
        if (user == null)
            userId = null;
        else
            userId = user.getIdLong();
        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction skuIds(@Nullable String... skuIds)
    {
        if (skuIds == null)
            this.skuIds = null;
        else
        {
            Checks.noneNull(skuIds, "skuIds");
            this.skuIds = skuIds;
        }
        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction guild(@Nullable Long guildId)
    {
        this.guildId = guildId;
        return this;
    }

    @NotNull
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

        if (userId != null)
            route = route.withQueryParams("user_id", String.valueOf(userId));

        if (skuIds != null)
            route = route.withQueryParams("sku_ids", String.join(",", skuIds));

        if (guildId != null)
            route = route.withQueryParams("guild_id", String.valueOf(guildId));

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

        if (order == PaginationOrder.BACKWARD)
            Collections.reverse(entitlements);

        if (!entitlements.isEmpty())
        {
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
