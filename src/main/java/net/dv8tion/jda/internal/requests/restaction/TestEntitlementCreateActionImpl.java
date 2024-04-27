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

public class TestEntitlementCreateActionImpl extends RestActionImpl<Entitlement> implements TestEntitlementCreateAction
{

    private long skuId;
    private long ownerId;
    private OwnerType type;

    public TestEntitlementCreateActionImpl(JDA api, long skuId, long ownerId, OwnerType type)
    {
        super(api, Route.Applications.CREATE_TEST_ENTITLEMENT.compile(api.getSelfUser().getApplicationId()));
    }

    @Override
    public void setSkuId(long skuId)
    {
        this.skuId = skuId;
    }

    @Override
    public void setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
    }

    @Override
    public void setOwnerType(OwnerType type)
    {
        this.type = type;
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
