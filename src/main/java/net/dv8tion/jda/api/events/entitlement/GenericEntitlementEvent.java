package net.dv8tion.jda.api.events.entitlement;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.entitlement.Entitlement;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

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
