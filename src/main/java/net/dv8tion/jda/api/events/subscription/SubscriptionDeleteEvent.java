package net.dv8tion.jda.api.events.subscription;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.subscription.Subscription;
import org.jetbrains.annotations.NotNull;

/**
 * Indicate that a Subscription for Premium app was deleted
 */
public class SubscriptionDeleteEvent extends GenericSubscriptionEvent
{
    public SubscriptionDeleteEvent(@NotNull JDA api, long responseNumber, @NotNull Subscription subscription)
    {
        super(api, responseNumber, subscription);
    }
}
