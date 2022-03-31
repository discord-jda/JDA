package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;

/**
 * {@link PaginationAction} that paginates the guild bans endpoint.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.api.entities.Guild Guild} to compile a valid
 * pagination route.</b>
 *
 * <h2>Limits:</h2>
 * Minimum - 1
 * <br>Maximum - 1000
 *
 * <h1>Example</h1>
 * <pre>{@code
 * // Revoke all bans from a guild with a certain reason
 * public static void findBansWithReason(Guild guild, String reason) {
 *     BanPaginationAction bans = guild.retrieveBanList();
 *     bans.forEachAsync((ban) -> {
 *         if (ban.getReason().equals(reason)) {
 *             guild.unban(ban.getUser()).queue();
 *         }
 *     });
 * }
 * }</pre>
 *
 * @since 5.0
 *
 * @see Guild#retrieveBanList()
 * @see Guild#retrieveBanById(long)
 */
public interface BanPaginationAction extends PaginationAction<Guild.Ban, BanPaginationAction>
{
    /**
     * The current target {@link net.dv8tion.jda.api.entities.Guild Guild} for
     * this BanPaginationAction.
     *
     * @return The never-null target Guild
     */
    @Nonnull
    Guild getGuild();
}
