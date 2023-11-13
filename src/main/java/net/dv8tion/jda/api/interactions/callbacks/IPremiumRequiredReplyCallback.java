package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface IPremiumRequiredReplyCallback extends Interaction
{
    @Nonnull
    @CheckReturnValue
    PremiumRequiredCallbackAction replyWithPremiumRequired();
}
