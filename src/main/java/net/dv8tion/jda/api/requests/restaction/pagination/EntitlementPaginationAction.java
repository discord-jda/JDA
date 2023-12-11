package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link PaginationAction PaginationAction} that paginates the application entitlements endpoint.
 * <p>By default, JDA will include {@link Entitlement Entitlement}s which have ended, that is, {@link Entitlement Entitlement}s which
 * have gone past their {@link Entitlement#getTimeEnding() timeEnding}. You may use {@link EntitlementPaginationAction#excludeEnded excludeEnded(true)}
 * to only return {@link Entitlement}s which are still active
 *
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
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction user(@Nullable UserSnowflake user);

    /**
     * Filters {@link Entitlement Entitlement}s by their SKU id
     *
     * @param skuIds
     *        The SKU ids to filter by
     *
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction skuIds(long... skuIds);

    /**
     * Filters {@link Entitlement Entitlement}s by their SKU id
     *
     * @param skuIds
     *        The SKU ids to filter by
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided {@code skuIds} are {@code null}, empty or are not a valid snowflake
     *
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction skuIds(@Nonnull String... skuIds);

    /**
     * Filters {@link Entitlement Entitlement}s by a guild id
     *
     * @param guildId
     *        The guild id to filter by
     *
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction guild(long guildId);

    /**
     * Filters {@link Entitlement Entitlement}s by a guild id
     *
     * @param guildId
     *        The guild id to filter by
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code guildId} is {@code null}, empty or is not a valid snowflake
     *
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    default EntitlementPaginationAction guild(@Nonnull String guildId)
    {
        Checks.notNull(guildId, "guildId");
        Checks.isSnowflake(guildId, "guildId");
        return guild(Long.parseUnsignedLong(guildId));
    }

    /**
     * Filters {@link Entitlement Entitlement}s by a {@link Guild Guild}
     *
     * @param guild
     *        The {@link Guild Guild} to filter by
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code guild} is {@code null}
     *
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    default EntitlementPaginationAction guild(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "guild");
        return guild(guild.getIdLong());
    }

    /**
     * Whether to exclude subscriptions which have gone past their end date.
     * <p>Test entitlements which are created through the API do not have an end date.
     *
     * @param excludeEnded
     *        Whether to exclude ended subscriptions from returned {@link Entitlement Entitlement}s
     *
     * @return The current {@link EntitlementPaginationAction EntitlementPaginationAction} for chaining convenience
     */
    @Nonnull
    EntitlementPaginationAction excludeEnded(boolean excludeEnded);
}
