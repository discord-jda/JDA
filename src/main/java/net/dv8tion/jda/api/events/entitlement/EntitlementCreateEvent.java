package net.dv8tion.jda.api.events.entitlement;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a user subscribed to a SKU.
 *
 * @see #getEntitlement()
 */
public class EntitlementCreateEvent extends GenericEntitlementEvent
{
    public EntitlementCreateEvent(@NotNull JDA api, long responseNumber, @NotNull Entitlement entitlement)
    {
        super(api, responseNumber, entitlement);
    }
}
