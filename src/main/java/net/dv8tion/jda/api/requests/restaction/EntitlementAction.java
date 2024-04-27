package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.requests.RestAction;

public interface EntitlementAction extends RestAction<Entitlement>
{

    /**
     * Whether the entitlement should include payment information
     *
     * @param payment
     *        True, if the entitlement should include payment information
     */
    void withPayment(boolean payment);
}
