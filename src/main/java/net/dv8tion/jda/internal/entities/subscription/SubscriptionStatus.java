package net.dv8tion.jda.internal.entities.subscription;

import javax.annotation.Nonnull;

/**
 * Representation of a Discord Subscription Status
 *
 * @see <a href="https://discord.com/developers/docs/resources/subscription#subscription-statuses" target="_blank">Discord Docs about Subscription Statuses</a>
 */
public enum SubscriptionStatus
{
    ACTIVE(0), ENDING(2), INACTIVE(1);

    private final int id;

    SubscriptionStatus(int id)
    {
        this.id = id;
    }

    /**
     * Gets the Subscription status related to the provided key.
     *
     * @param  key
     *         The Discord key referencing a Subscription status.
     *
     * @return The Subscription status that has the key provided
     */
    @Nonnull
    public static SubscriptionStatus fromKey(int key)
    {
        return SubscriptionStatus.values()[key];
    }

    public int getId()
    {
        return id;
    }
}
