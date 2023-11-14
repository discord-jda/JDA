package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interactions which allow sending the "Premium required" interaction response.
 *
 * <p>Replying with {@link #replyWithPremiumRequired()} will automatically acknowledge this interaction.
 */
public interface IPremiumRequiredReplyCallback extends Interaction
{
    @Nonnull
    @CheckReturnValue
    PremiumRequiredCallbackAction replyWithPremiumRequired();
}
