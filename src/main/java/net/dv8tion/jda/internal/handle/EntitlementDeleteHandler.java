package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.entitlement.Entitlement;
import net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class EntitlementDeleteHandler extends SocketHandler
{
    public EntitlementDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new EntitlementDeleteEvent(getJDA(), responseNumber, new Entitlement(content)));
        return null;
    }
}
