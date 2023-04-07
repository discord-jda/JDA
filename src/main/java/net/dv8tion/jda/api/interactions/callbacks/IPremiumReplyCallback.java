package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface IPremiumReplyCallback extends Interaction
{
    @Nonnull
    @CheckReturnValue
    PremiumCallbackAction replyWithPremiumRequired();
}
