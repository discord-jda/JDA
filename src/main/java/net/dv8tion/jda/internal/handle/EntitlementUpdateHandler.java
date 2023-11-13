package net.dv8tion.jda.internal.handle;


import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntitlementImpl;

public class EntitlementUpdateHandler extends SocketHandler
{
    public EntitlementUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new EntitlementUpdateEvent(getJDA(), responseNumber, new EntitlementImpl(content)));
        return null;
    }
}
