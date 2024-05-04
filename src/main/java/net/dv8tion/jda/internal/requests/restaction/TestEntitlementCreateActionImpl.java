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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.TestEntitlementCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;

public class TestEntitlementCreateActionImpl extends RestActionImpl<Entitlement> implements TestEntitlementCreateAction
{

    private long skuId;
    private long ownerId;
    private OwnerType type;

    public TestEntitlementCreateActionImpl(JDA api, long skuId, long ownerId, OwnerType type)
    {
        super(api, Route.Applications.CREATE_TEST_ENTITLEMENT.compile(api.getSelfUser().getApplicationId()));
    }

    @Nonnull
    @Override
    public TestEntitlementCreateAction setSkuId(long skuId)
    {
        this.skuId = skuId;
        return this;
    }

    @Nonnull
    @Override
    public TestEntitlementCreateAction setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    @Nonnull
    @Override
    public TestEntitlementCreateAction setOwnerType(@Nonnull OwnerType type)
    {
        this.type = type;
        return this;
    }

    @Override
    protected void handleSuccess(Response response, Request<Entitlement> request)
    {
        DataObject object = response.getObject();
        request.onSuccess(api.getEntityBuilder().createEntitlement(object));
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        object.put("sku_id", skuId);
        object.put("owner_id", ownerId);
        object.put("owner_type", type.getKey());

        return getRequestBody(object);
    }
}
