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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Filter function for member chunking of guilds.
 * <br></br>The filter decides based on the provided guild id whether chunking should be done
 * on guild initialization.
 *
 *
 * **To use chunking, the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent must be enabled!
 * Otherwise you <u>must</u> use [.NONE]!**
 *
 * @since 4.1.0
 *
 * @see .ALL
 *
 * @see .NONE
 *
 *
 * @see net.dv8tion.jda.api.JDABuilder.setChunkingFilter
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.setChunkingFilter
 */
fun interface ChunkingFilter {
    /**
     * Decide whether the specified guild should chunk members.
     *
     * @param  guildId
     * The guild id
     *
     * @return True, if this guild should chunk
     */
    fun filter(guildId: Long): Boolean

    companion object {
        /**
         * Factory method to chunk a whitelist of guild ids.
         * <br></br>All guilds that are not mentioned will use lazy loading.
         *
         *
         * This is useful to only chunk specific guilds like the hub server of a bot.
         *
         * @param  ids
         * The ids that should be chunked
         *
         * @return The resulting filter
         */
        @JvmStatic
        @Nonnull
        fun include(@Nonnull vararg ids: Long): ChunkingFilter? {
            Checks.notNull(ids, "ID array")
            return if (ids.size == 0) NONE else ChunkingFilter { guild: Long ->
                for (id in ids) {
                    if (id == guild) return@ChunkingFilter true
                }
                false
            }
        }

        /**
         * Factory method to disable chunking for a blacklist of guild ids.
         * <br></br>All guilds that are not mentioned will use chunking.
         *
         *
         * This is useful when the bot is only in one very large server that
         * takes most of its memory and should be ignored instead.
         *
         * @param  ids
         * The ids that should not be chunked
         *
         * @return The resulting filter
         */
        @JvmStatic
        @Nonnull
        fun exclude(@Nonnull vararg ids: Long): ChunkingFilter? {
            Checks.notNull(ids, "ID array")
            return if (ids.size == 0) ALL else ChunkingFilter { guild: Long ->
                for (id in ids) {
                    if (id == guild) return@ChunkingFilter false
                }
                true
            }
        }

        /** Chunk all guilds  */
        @JvmField
        val ALL = ChunkingFilter { x: Long -> true }

        /** Do not chunk any guilds (lazy loading)  */
        @JvmField
        val NONE = ChunkingFilter { x: Long -> false }
    }
}
