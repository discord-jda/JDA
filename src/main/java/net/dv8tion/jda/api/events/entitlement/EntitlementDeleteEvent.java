package net.dv8tion.jda.api.events.entitlement;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates a user's entitlement is deleted. Entitlement deletions are infrequent, and occur when:
 * <ul>
 *     <li>Discord issues a refund for a subscription</li>
 *     <li>Discord removes an entitlement from a user via internal tooling</li>
 * </ul>
 * <p><b>Notice</b><br>
 * Entitlements are not deleted when they expire.
 * <br>
 * If a user's subscription is cancelled, you will not receive an {@link EntitlementDeleteEvent EntitlementDeleteEvent}.
 * <br>Instead, you will simply not receive an {@link EntitlementUpdateEvent EntitlementUpdateEvent} with a new {@link Entitlement#getEndsAt() endsAt} date at the end of the billing period.
 *
 * @see #getEntitlement()
 * @see EntitlementUpdateEvent
 */
public class EntitlementDeleteEvent extends GenericEntitlementEvent
{
    public EntitlementDeleteEvent(@NotNull JDA api, long responseNumber, @NotNull Entitlement entitlement)
    {
        super(api, responseNumber, entitlement);
    }
}
