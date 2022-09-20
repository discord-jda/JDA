/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * <p><b>Limits:</b><br>
 * Minimum - 1
 * <br>Maximum - 1000
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * // Revoke all bans from a guild with a certain reason
 * public static void findBansWithReason(Guild guild, String reason) {
 *     BanPaginationAction bans = guild.retrieveBanList();
 *     bans.forEachAsync((ban) -> {
 *         if (reason.equals(ban.getReason())) {
 *             guild.unban(ban.getUser()).queue();
 *         }
 *         return true; // continues iterating if this returns true
 *     });
 * }
 * }</pre>
 *
 * @see Guild#retrieveBanList()
 * @see Guild#retrieveBan(net.dv8tion.jda.api.entities.UserSnowflake)
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
