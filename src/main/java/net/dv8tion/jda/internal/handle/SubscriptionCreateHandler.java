package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.events.subscription.SubscriptionCreateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class SubscriptionCreateHandler extends SocketHandler
{
    public SubscriptionCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new SubscriptionCreateEvent(getJDA(), responseNumber, getJDA().getEntityBuilder().createSubscription(content)));
        return null;
    }
}
