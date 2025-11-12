package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.events.subscription.SubscriptionDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class SubscriptionDeleteHandler extends SocketHandler
{
    public SubscriptionDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new SubscriptionDeleteEvent(getJDA(), responseNumber, getJDA().getEntityBuilder().createSubscription(content)));
        return null;
    }
}
