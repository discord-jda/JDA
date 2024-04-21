package net.dv8tion.jda.api.requests.restaction.pagination

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * [PaginationAction] that paginates the application entitlements endpoint.
 *
 * By default, JDA will include [Entitlement]s which have ended, that is, [Entitlement]s which
 * have gone past their [timeEnding][Entitlement.getTimeEnding]. You may use [excludeEnded(true)][EntitlementPaginationAction.excludeEnded]
 * to only return [Entitlement]s which are still active
 *
 *
 * **Limits**<br></br>
 * Minimum - 1<br></br>
 * Maximum - 100
 * <br></br>Default - 100
 *
 *
 * **Example**<br></br>
 * <pre>`//Fetch all entitlements for a given SKU id
 * public static void fetchEntitlements(JDA api, String skuId, Consumer<List<Entitlement>> callback) {
 * List<Entitlement> entitlements = new ArrayList<>()
 * EntitlementPaginationAction action = api.retrieveEntitlements().skuIds(skuId).excludeEnded(true)
 * action.forEachAsync((entitlement) -> {
 * entitlements.add(entitlement)
 * return true; //continues to retrieve all entitlements until there are none left to retrieve
 * }.thenRun(() -> callback.accept(entitlements));
 * }
`</pre> *
 */
interface EntitlementPaginationAction : PaginationAction<Entitlement?, EntitlementPaginationAction?> {
    /**
     * Filter [Entitlement]s to retrieve by the given user ID
     *
     * @param user
     * The [UserSnowflake] used to filter or `null` to remove user filtering.
     * This can be a member or user instance of [User.fromId]
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun user(user: UserSnowflake?): EntitlementPaginationAction?

    /**
     * Filters [Entitlement]s by their SKU id
     *
     * @param skuIds
     * The SKU ids to filter by
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun skuIds(vararg skuIds: Long): EntitlementPaginationAction?

    /**
     * Filters [Entitlement]s by their SKU id
     *
     * @param skuIds
     * The SKU ids to filter by
     *
     * @throws java.lang.IllegalArgumentException
     * If any of the provided `skuIds` are `null`, empty or are not a valid snowflake
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun skuIds(@Nonnull vararg skuIds: String?): EntitlementPaginationAction?

    /**
     * Filters [Entitlement]s by their SKU id
     *
     * @param skuIds
     * The SKU ids to filter by
     *
     * @throws java.lang.IllegalArgumentException
     * If any of the provided `skuIds` are `null`, empty or invalid snowflakes
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun skuIds(@Nonnull skuIds: Collection<String?>?): EntitlementPaginationAction?

    /**
     * Filters [Entitlement]s by a guild id
     *
     * @param guildId
     * The guild id to filter by
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun guild(guildId: Long): EntitlementPaginationAction?

    /**
     * Filters [Entitlement]s by a guild id
     *
     * @param guildId
     * The guild id to filter by
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided `guildId` is `null`, empty or is not a valid snowflake
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun guild(@Nonnull guildId: String?): EntitlementPaginationAction? {
        Checks.notNull(guildId, "guildId")
        Checks.isSnowflake(guildId, "guildId")
        return guild(java.lang.Long.parseUnsignedLong(guildId))
    }

    /**
     * Filters [Entitlement]s by a [Guild]
     *
     * @param guild
     * The [Guild] to filter by
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided `guild` is `null`
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun guild(@Nonnull guild: Guild): EntitlementPaginationAction? {
        Checks.notNull(guild, "guild")
        return guild(guild.idLong)
    }

    /**
     * Whether to exclude subscriptions which have gone past their end date.
     *
     * Test entitlements which are created through the API do not have an end date.
     *
     * @param excludeEnded
     * Whether to exclude ended subscriptions from returned [Entitlement]s
     *
     * @return The current [EntitlementPaginationAction] for chaining convenience
     */
    @Nonnull
    fun excludeEnded(excludeEnded: Boolean): EntitlementPaginationAction?
}
