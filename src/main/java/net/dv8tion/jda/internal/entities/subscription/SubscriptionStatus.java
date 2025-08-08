package net.dv8tion.jda.internal.entities.subscription;

import javax.annotation.Nonnull;

/**
 * Representation of a Discord Subscription Status
 *
 * @see <a href="https://discord.com/developers/docs/resources/subscription#subscription-statuses" target="_blank">Discord Docs about Subscription Statuses</a>
 */
public enum SubscriptionStatus
{
    UNKNOWN(-1), ACTIVE(0), ENDING(1), INACTIVE(2);

    private final int id;

    SubscriptionStatus(int id)
    {
        this.id = id;
    }

    /**
     * Gets the Subscription status related to the provided key.
     * <br>If an unknown key is provided, this returns {@link #UNKNOWN}
     *
     * @param  key
     *         The Discord key referencing a Subscription status.
     *
     * @return The Subscription status that has the key provided, or {@link #UNKNOWN} for unknown key.
     */
    @Nonnull
    public static SubscriptionStatus fromKey(int key)
    {
        for (SubscriptionStatus status : values())
        {
            if (status.id == key)
                return status;
        }
        return UNKNOWN;
    }

    public int getId()
    {
        return id;
    }
}
