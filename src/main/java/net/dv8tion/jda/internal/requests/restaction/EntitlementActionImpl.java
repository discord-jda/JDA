package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.EntitlementAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;

public class EntitlementActionImpl extends RestActionImpl<Entitlement> implements EntitlementAction
{

    private boolean withPayment;

    public EntitlementActionImpl(JDA api, long entitlementId)
    {
        super(api, Route.Applications.GET_ENTITLEMENT.compile(api.getSelfUser().getApplicationId(), String.valueOf(entitlementId)));
    }

    @Override
    public void withPayment(boolean withPayment)
    {
        this.withPayment = withPayment;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        if (withPayment)
            route = route.withQueryParams("with_payment", "true");

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<Entitlement> request)
    {
        DataObject object = response.getObject();
        request.onSuccess(api.getEntityBuilder().createEntitlement(object));
    }
}
