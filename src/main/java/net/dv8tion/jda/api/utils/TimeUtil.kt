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

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.internal.utils.Checks
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.Nonnull

/**
 * Utility for various time related features of the API.
 */
object TimeUtil {
    const val DISCORD_EPOCH = 1420070400000L
    const val TIMESTAMP_OFFSET: Long = 22
    private val dtFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

    /**
     * Converts the provided epoch millisecond timestamp to a Discord Snowflake.
     * <br></br>This can be used as a marker/pivot for [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] creation.
     *
     * @param  millisTimestamp
     * The epoch millis to convert
     *
     * @return Shifted epoch millis for Discord
     */
    @JvmStatic
    fun getDiscordTimestamp(millisTimestamp: Long): Long {
        return millisTimestamp - DISCORD_EPOCH shl TIMESTAMP_OFFSET.toInt()
    }

    /**
     * Gets the creation-time of a JDA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Discords side, not inside JDA.
     *
     * @param  entityId
     * The id of the JDA entity where the creation-time should be determined for
     *
     * @return The creation time of the JDA entity as OffsetDateTime
     */
    @JvmStatic
    @Nonnull
    fun getTimeCreated(entityId: Long): OffsetDateTime {
        val timestamp = (entityId ushr TIMESTAMP_OFFSET.toInt()) + DISCORD_EPOCH
        val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        gmt.setTimeInMillis(timestamp)
        return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId())
    }

    /**
     * Gets the creation-time of a JDA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Discords side, not inside JDA.
     *
     * @param  entity
     * The JDA entity where the creation-time should be determined for
     *
     * @throws IllegalArgumentException
     * If the provided entity is `null`
     *
     * @return The creation time of the JDA entity as OffsetDateTime
     */
    @Nonnull
    fun getTimeCreated(@Nonnull entity: ISnowflake): OffsetDateTime {
        Checks.notNull(entity, "Entity")
        return getTimeCreated(entity.idLong)
    }

    /**
     * Returns a prettier String-representation of a OffsetDateTime object
     *
     * @param  time
     * The OffsetDateTime object to format
     *
     * @return The String of the formatted OffsetDateTime
     */
    @Nonnull
    fun getDateTimeString(@Nonnull time: OffsetDateTime): String {
        return time.format(dtFormatter)
    }
}
