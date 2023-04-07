package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.entitlement.Entitlement;
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class EntitlementUpdateHandler extends SocketHandler
{
    public EntitlementUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new EntitlementUpdateEvent(getJDA(), responseNumber, new Entitlement(content)));
        return null;
    }
}
