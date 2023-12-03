package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link PaginationAction PaginationAction} that paginates the application entitlements endpoint.
 * <br>
 * <p><b>Limits</b><br>
 * Minimum - 1<br>
 * Maximum - 100
 * <br>Default - 100
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * //Fetch all entitlements for a given SKU id
 * public static void fetchEntitlements(JDA api, String skuId, Consumer<List<Entitlement>> callback) {
 *     List<Entitlement> entitlements = new ArrayList<>()
 *     EntitlementPaginationAction action = api.retrieveEntitlements().skuIds(skuId).excludeEnded(true)
 *     action.forEachAsync((entitlement) -> {
 *           entitlements.add(entitlement)
 *           return true; //continues to retrieve all entitlements until there are none left to retrieve
 *     }.thenRun(() -> callback.accept(entitlements));
 * }
 * }</pre>
 */
public interface EntitlementPaginationAction extends PaginationAction<Entitlement, EntitlementPaginationAction>
{
    /**
     * Filter {@link Entitlement Entitlement}s to retrieve by the given user ID
     *
     * @param user
     *        The {@link UserSnowflake UserSnowflake} used to filter or {@code null} to remove user filtering.
     *        This can be a member or user instance of {@link User#fromId(long)}
     *
     * @return The current EntitlementPaginationAction for chaining convenience
     */
    @NotNull
    EntitlementPaginationAction user(@Nullable UserSnowflake user);

    /**
     * Filters {@link Entitlement Entitlement}s by their SKU id
     *
     * @param skuIds
     *        The SKU ids to filter by or {@code null} to remove SKU filtering
     *
     * @return The current EntitlementPaginationAction for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction skuIds(@Nullable String... skuIds);

    /**
     * Filters {@link Entitlement Entitlement} by a guild id
     *
     * @param guildId
     *        The guild id used to filter or {@code null} to remove guild filtering
     *
     * @return The current EntitlementPaginationAction for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction guild(@Nullable Long guildId);

    /**
     * Whether to exclude subscriptions which have gone past their end date.
     *
     * Test entitlements which are created through the API do not have an end date.
     *
     * @param excludeEnded
     *        Whether to exclude ended subscriptions from returned {@link Entitlement Entitlement}s
     *
     * @return The current EntitlementPaginationAction for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction excludeEnded(boolean excludeEnded);
}
