package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.entities.subscription.Subscription;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link PaginationAction PaginationAction} that paginates over
 * {@link Subscription Subscriptions} returned from the
 * <a href="https://discord.com/developers/docs/resources/subscription#list-sku-subscriptions"
 * target="_blank">List SKU Subscriptions</a> endpoint.
 * <br>This action allows retrieval of subscriptions for a specific SKU, optionally filtered by a user.
 *
 * <p>Use {@link #user(UserSnowflake)} to limit results to a specific user, or {@code null} to remove the filter.
 * <br>Results are ordered according to {@link PaginationOrder} and support typical pagination
 * parameters such as {@link #limit(int)} and {@link #cache(boolean)}.
 *
 * @see PaginationAction
 * @see Subscription
 */
public interface SubscriptionPaginationAction extends PaginationAction<Subscription, SubscriptionPaginationAction>
{

    /**
     * Filter {@link Subscription Subscription}s to retrieve by the given user ID
     *
     * @param user
     *        The {@link UserSnowflake UserSnowflake} used to filter or {@code null} to remove user filtering.
     *        This can be a member or user instance of {@link User#fromId(long)}
     *
     * @return The current {@link SubscriptionPaginationAction SubscriptionPaginationAction} for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SubscriptionPaginationAction user(@Nullable UserSnowflake user);
}
