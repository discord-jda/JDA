package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;

/**
 * Webhook API for an interaction. Valid for up to 15 minutes after the interaction.
 *
 * <p>The interaction has to be acknowledged before any of these actions can be performed.
 */
public interface InteractionHook
{
    /**
     * The interaction attached to this hook.
     *
     * @return The {@link Interaction}
     */
    @Nonnull
    Interaction getInteraction();

    /**
     * The unix millisecond timestamp for the expiration of this interaction hook.
     * <br>An interaction hook expires after 15 minutes of its creation.
     *
     * @return The timestamp in millisecond precision
     *
     * @see    System#currentTimeMillis()
     * @see    #isExpired()
     */
    default long getExpirationTimestamp()
    {
        return getInteraction().getTimeCreated().plus(15, ChronoUnit.MINUTES).toEpochSecond() * 1000;
    }

    /**
     * Whether this interaction has expired.
     * <br>An interaction hook is only valid for 15 minutes.
     *
     * @return True, if this interaction hook has expired
     *
     * @see    #getExpirationTimestamp()
     */
    default boolean isExpired()
    {
        return System.currentTimeMillis() > getExpirationTimestamp();
    }

    /**
     * The JDA instance for this interaction
     *
     * @return The JDA instance
     */
    @Nonnull
    JDA getJDA();
}
