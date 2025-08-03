package net.dv8tion.jda.api.events.subscription;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.subscription.Subscription;

/**
 * Indicate that a Subscription for Premium app was created
 */
public class SubscriptionCreateEvent extends GenericSubscriptionEvent
{
    public SubscriptionCreateEvent(JDA api, long responseNumber, Subscription subscription)
    {
        super(api, responseNumber, subscription);
    }
}
