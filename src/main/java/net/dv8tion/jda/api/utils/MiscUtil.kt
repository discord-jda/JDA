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

import gnu.trove.impl.sync.TSynchronizedLongObjectMap
import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier

/**
 * Utility methods for various aspects of the API.
 */
object MiscUtil {
    /**
     * Returns the shard id the given guild will be loaded on for the given amount of shards.
     *
     *
     * Discord determines which guilds a shard is connect to using the following format:
     * `shardId == (guildId >>> 22) % totalShards`
     * <br></br>Source for formula: [Discord Documentation](https://discord.com/developers/docs/topics/gateway#sharding)
     *
     * @param guildId
     * The guild id.
     * @param shards
     * The amount of shards.
     *
     * @return The shard id for the guild.
     */
    fun getShardForGuild(guildId: Long, shards: Int): Int {
        return ((guildId ushr 22) % shards).toInt()
    }

    /**
     * Returns the shard id the given guild will be loaded on for the given amount of shards.
     *
     *
     * Discord determines which guilds a shard is connect to using the following format:
     * `shardId == (guildId >>> 22) % totalShards`
     * <br></br>Source for formula: [Discord Documentation](https://discord.com/developers/docs/topics/gateway#sharding)
     *
     * @param guildId
     * The guild id.
     * @param shards
     * The amount of shards.
     *
     * @return The shard id for the guild.
     */
    fun getShardForGuild(guildId: String, shards: Int): Int {
        return getShardForGuild(parseSnowflake(guildId), shards)
    }

    /**
     * Returns the shard id the given [Guild][net.dv8tion.jda.api.entities.Guild] will be loaded on for the given amount of shards.
     *
     *
     * Discord determines which guilds a shard is connect to using the following format:
     * `shardId == (guildId >>> 22) % totalShards`
     * <br></br>Source for formula: [Discord Documentation](https://discord.com/developers/docs/topics/gateway#sharding)
     *
     * @param guild
     * The guild.
     * @param shards
     * The amount of shards.
     *
     * @return The shard id for the guild.
     */
    fun getShardForGuild(guild: Guild, shards: Int): Int {
        return getShardForGuild(guild.idLong, shards)
    }

    /**
     * Generates a new thread-safe [TLongObjectMap][gnu.trove.map.TLongObjectMap]
     *
     * @param  <T>
     * The Object type
     *
     * @return a new thread-safe [TLongObjectMap][gnu.trove.map.TLongObjectMap]
    </T> */
    @JvmStatic
    fun <T> newLongMap(): TLongObjectMap<T> {
        return TSynchronizedLongObjectMap(TLongObjectHashMap(), Any())
    }

    fun parseLong(input: String): Long {
        return if (input.startsWith("-")) input.toLong() else java.lang.Long.parseUnsignedLong(input)
    }

    @JvmStatic
    fun parseSnowflake(input: String): Long {
        Checks.notEmpty(input, "ID")
        return try {
            parseLong(input)
        } catch (ex: NumberFormatException) {
            throw NumberFormatException(
                Helpers.format("The specified ID is not a valid snowflake (%s). Expecting a valid long value!", input)
            )
        }
    }

    @JvmStatic
    fun <E> locked(lock: ReentrantLock, task: Supplier<E>): E {
        tryLock(lock)
        return try {
            task.get()
        } finally {
            lock.unlock()
        }
    }

    @JvmStatic
    fun locked(lock: ReentrantLock, task: Runnable) {
        tryLock(lock)
        try {
            task.run()
        } finally {
            lock.unlock()
        }
    }

    /**
     * Tries to acquire the provided lock in a 10 second timeframe.
     *
     * @param  lock
     * The lock to acquire
     *
     * @throws IllegalStateException
     * If the lock could not be acquired
     */
    @JvmStatic
    fun tryLock(lock: Lock) {
        try {
            check(
                !(!lock.tryLock() && !lock.tryLock(
                    10,
                    TimeUnit.SECONDS
                ))
            ) { "Could not acquire lock in a reasonable timeframe! (10 seconds)" }
        } catch (e: InterruptedException) {
            throw IllegalStateException("Unable to acquire lock while thread is interrupted!")
        }
    }

    /**
     * Can be used to append a String to a formatter.
     *
     * @param formatter
     * The [Formatter][java.util.Formatter]
     * @param width
     * Minimum width to meet, filled with space if needed
     * @param precision
     * Maximum amount of characters to append
     * @param leftJustified
     * Whether or not to left-justify the value
     * @param out
     * The String to append
     */
    @JvmStatic
    fun appendTo(formatter: Formatter, width: Int, precision: Int, leftJustified: Boolean, out: String) {
        try {
            val appendable = formatter.out()
            if (precision > -1 && out.length > precision) {
                appendable.append(Helpers.truncate(out, precision))
                return
            }
            if (leftJustified) appendable.append(Helpers.rightPad(out, width)) else appendable.append(
                Helpers.leftPad(
                    out,
                    width
                )
            )
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
