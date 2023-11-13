package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntitlementImpl;

public class EntitlementCreateHandler extends SocketHandler
{
    public EntitlementCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new EntitlementCreateEvent(getJDA(), responseNumber, new EntitlementImpl(content)));
        return null;
    }
}
