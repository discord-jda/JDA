package net.dv8tion.jda.api.events.subscription;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.subscription.Subscription;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link Subscription Subscription} was either created, updated, or deleted
 *
 * @see SubscriptionCreateEvent
 * @see SubscriptionUpdateEvent
 * @see SubscriptionDeleteEvent
 */
public abstract class GenericSubscriptionEvent extends Event
{
    protected final Subscription subscription;

    protected GenericSubscriptionEvent(@Nonnull JDA api, long responseNumber, @Nonnull Subscription subscription)
    {
        super(api, responseNumber);
        this.subscription = subscription;
    }

    /**
     * The subscription {@link Subscription}
     *
     * @return The subscription {@link Subscription}
     */
    @Nonnull
    public Subscription getSubscription()
    {
        return subscription;
    }
}
