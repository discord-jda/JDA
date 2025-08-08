package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.entities.subscription.Subscription;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SubscriptionPaginationAction extends PaginationAction<Subscription, SubscriptionPaginationAction>
{
    @Nonnull
    @CheckReturnValue
    SubscriptionPaginationAction user(@Nullable UserSnowflake user);
}
