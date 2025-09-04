package net.dv8tion.jda.api.events.subscription;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.internal.entities.subscription.Subscription;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a Subscription for Premium app was updated
 */
public class SubscriptionUpdateEvent extends GenericSubscriptionEvent
{
    public SubscriptionUpdateEvent(@NotNull JDA api, long responseNumber, @NotNull Subscription subscription)
    {
        super(api, responseNumber, subscription);
    }
}
