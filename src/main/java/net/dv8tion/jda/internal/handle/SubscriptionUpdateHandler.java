package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.events.subscription.SubscriptionUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class SubscriptionUpdateHandler extends SocketHandler
{
    public SubscriptionUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        getJDA().handleEvent(new SubscriptionUpdateEvent(getJDA(), responseNumber, getJDA().getEntityBuilder().createSubscription(content)));
        return null;
    }
}
