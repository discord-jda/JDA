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
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.SubscriptionPaginationAction;
import net.dv8tion.jda.api.entities.subscription.Subscription;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SubscriptionPaginationActionImpl extends PaginationActionImpl<Subscription, SubscriptionPaginationAction>
        implements SubscriptionPaginationAction {
    protected long userId;

    public SubscriptionPaginationActionImpl(JDA api, String skuId) {
        super(api, Route.Sku.GET_SUBSCRIPTIONS.compile(skuId), 1, 100, 100);
        this.userId = 0;
    }

    @Nonnull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders() {
        return EnumSet.of(PaginationOrder.BACKWARD, PaginationOrder.FORWARD);
    }

    @Nonnull
    @Override
    public SubscriptionPaginationAction user(@Nullable UserSnowflake user) {
        if (user == null) {
            userId = 0;
        } else {
            userId = user.getIdLong();
        }
        return this;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute() {
        Route.CompiledRoute route = super.finalizeRoute();

        if (userId != 0) {
            route = route.withQueryParams("user_id", Long.toUnsignedString(userId));
        }

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Subscription>> request) {
        DataArray array = response.getArray();
        List<Subscription> subscriptions = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();

        for (int i = 0; i < array.length(); i++) {
            try {
                DataObject object = array.getObject(i);
                Subscription subscription = builder.createSubscription(object);
                subscriptions.add(subscription);
            } catch (ParsingException | NullPointerException e) {
                LOG.warn("Encountered an exception in SubscriptionPaginationAction", e);
            }
        }
        if (!subscriptions.isEmpty()) {
            if (useCache) {
                cached.addAll(subscriptions);
            }
            last = subscriptions.get(subscriptions.size() - 1);
            lastKey = last.getIdLong();
        }

        request.onSuccess(subscriptions);
    }

    @Override
    protected long getKey(Subscription it) {
        return it.getIdLong();
    }
}
