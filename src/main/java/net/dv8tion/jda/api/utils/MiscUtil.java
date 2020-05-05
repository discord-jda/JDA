/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.utils;

import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Formatter;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class MiscUtil
{
    /**
     * Returns the shard id the given guild will be loaded on for the given amount of shards.
     *
     * Discord determines which guilds a shard is connect to using the following format:
     * {@code shardId == (guildId >>> 22) % totalShards}
     * <br>Source for formula: <a href="https://discordapp.com/developers/docs/topics/gateway#sharding">Discord Documentation</a>
     *
     * @param guildId
     *        The guild id.
     * @param shards
     *        The amount of shards.
     * 
     * @return The shard id for the guild.
     */
    public static int getShardForGuild(long guildId, int shards)
    {
        return (int) ((guildId >>> 22) % shards);
    }

    /**
     * Returns the shard id the given guild will be loaded on for the given amount of shards.
     *
     * Discord determines which guilds a shard is connect to using the following format:
     * {@code shardId == (guildId >>> 22) % totalShards}
     * <br>Source for formula: <a href="https://discordapp.com/developers/docs/topics/gateway#sharding">Discord Documentation</a>
     *
     * @param guildId
     *        The guild id.
     * @param shards
     *        The amount of shards.
     *
     * @return The shard id for the guild.
     */
    public static int getShardForGuild(String guildId, int shards)
    {
        return getShardForGuild(parseSnowflake(guildId), shards);
    }

    /**
     * Returns the shard id the given {@link net.dv8tion.jda.api.entities.Guild Guild} will be loaded on for the given amount of shards.
     *
     * Discord determines which guilds a shard is connect to using the following format:
     * {@code shardId == (guildId >>> 22) % totalShards}
     * <br>Source for formula: <a href="https://discordapp.com/developers/docs/topics/gateway#sharding">Discord Documentation</a>
     *
     * @param guild
     *        The guild.
     * @param shards
     *        The amount of shards.
     *
     * @return The shard id for the guild.
     */
    public static int getShardForGuild(Guild guild, int shards)
    {
        return getShardForGuild(guild.getIdLong(), shards);
    }

    /**
     * Generates a new thread-safe {@link gnu.trove.map.TLongObjectMap TLongObjectMap}
     *
     * @param  <T>
     *         The Object type
     *
     * @return a new thread-safe {@link gnu.trove.map.TLongObjectMap TLongObjectMap}
     */
    public static <T> TLongObjectMap<T> newLongMap()
    {
        return new TSynchronizedLongObjectMap<>(new TLongObjectHashMap<T>(), new Object());
    }

    public static long parseSnowflake(String input)
    {
        Checks.notEmpty(input, "ID");
        try
        {
            if (!input.startsWith("-")) // if not negative -> parse unsigned
                return Long.parseUnsignedLong(input);
            else // if negative -> parse normal
                return Long.parseLong(input);
        }
        catch (NumberFormatException ex)
        {
            throw new NumberFormatException(
                String.format("The specified ID is not a valid snowflake (%s). Expecting a valid long value!", input));
        }
    }

    public static <E> E locked(ReentrantLock lock, Supplier<E> task)
    {
        try
        {
            lock.lockInterruptibly();
            return task.get();
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    public static void locked(ReentrantLock lock, Runnable task)
    {
        try
        {
            lock.lockInterruptibly();
            task.run();
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * Can be used to append a String to a formatter.
     *
     * @param formatter
     *        The {@link java.util.Formatter Formatter}
     * @param width
     *        Minimum width to meet, filled with space if needed
     * @param precision
     *        Maximum amount of characters to append
     * @param leftJustified
     *        Whether or not to left-justify the value
     * @param out
     *        The String to append
     */
    public static void appendTo(Formatter formatter, int width, int precision, boolean leftJustified, String out)
    {
        try
        {
            Appendable appendable = formatter.out();
            if (precision > -1 && out.length() > precision)
            {
                appendable.append(Helpers.truncate(out, precision));
                return;
            }

            if (leftJustified)
                appendable.append(Helpers.rightPad(out, width));
            else
                appendable.append(Helpers.leftPad(out, width));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
