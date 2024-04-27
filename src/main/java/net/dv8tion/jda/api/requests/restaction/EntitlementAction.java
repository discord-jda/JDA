package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.requests.RestAction;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to retrieve a {@link Entitlement Entitlement}.
 * This extension allows requesting payment data of the entitlement.
 *
 * @see    net.dv8tion.jda.api.JDA
 * @see    net.dv8tion.jda.api.JDA#retrieveEntitlementById(long)
 */
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
