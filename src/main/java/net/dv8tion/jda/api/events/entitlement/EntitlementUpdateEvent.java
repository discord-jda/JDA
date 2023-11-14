package net.dv8tion.jda.api.events.entitlement;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates a user's subscription has renewed for the next billing period.
 * The {@link Entitlement#getEndsAt() endsAt} field will have an updated value with the new expiration date.
 *
 * <p><b>Notice</b><br>
 * If a user's subscription is cancelled, you will not receive an {@link EntitlementDeleteEvent EntitlementDeleteEvent}.
 * <br>Instead, you will simply not receive an {@link EntitlementUpdateEvent EntitlementUpdateEvent} with a new {@link Entitlement#getEndsAt() endsAt} date at the end of the billing period.
 *
 * @see #getEntitlement()
 */
public class EntitlementUpdateEvent extends GenericEntitlementEvent
{
    public EntitlementUpdateEvent(@NotNull JDA api, long responseNumber, @NotNull Entitlement entitlement)
    {
        super(api, responseNumber, entitlement);
    }
}
