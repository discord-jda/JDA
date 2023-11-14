package net.dv8tion.jda.api.events.entitlement;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link Entitlement Entitlement} was either created, updated or deleted
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see EntitlementCreateEvent
 * @see EntitlementUpdateEvent
 * @see EntitlementDeleteEvent
 */
public abstract class GenericEntitlementEvent extends Event
{
    protected final Entitlement entitlement;

    GenericEntitlementEvent(@Nonnull JDA api, long responseNumber, @Nonnull Entitlement entitlement) {
        super(api, responseNumber);
        this.entitlement = entitlement;
    }

    public Entitlement getEntitlement()
    {
        return entitlement;
    }
}
