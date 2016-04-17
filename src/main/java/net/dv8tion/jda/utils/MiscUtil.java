/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.utils;

import net.dv8tion.jda.entities.impl.JDAImpl;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

public class MiscUtil
{
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    /**
     * Gets the creation-time of a JDA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Discords side, not inside JDA.
     *
     * @param entityId
     *      The id of the JDA entity where the creation-time should be determined for
     * @return
     *      The creation time of the JDA entity as OffsetDateTime
     */
    public static OffsetDateTime getCreationTime(String entityId) {
        try
        {
            long timestamp = ((Long.parseLong(entityId) >> 22) + 1420070400000L);
            Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            gmt.setTimeInMillis(timestamp);
            return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }

    /**
     * Gets the creation-time of a JDA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Discords side, not inside JDA.
     * This will not work on entities, that do not have a getId() method and will return null in those cases!
     *
     * @param entity
     *      The JDA entity where the creation-time should be determined for
     * @return
     *      The creation time of the JDA entity as OffsetDateTime
     */
    public static OffsetDateTime getCreationTime(Object entity)
    {
        try {
            Method idMethod = entity.getClass().getMethod("getId");
            if(idMethod.getReturnType() != String.class)
            {
                JDAImpl.LOG.warn("Tried to look up creation-time for entity of class " + entity.getClass().getName() + " which doesn't have the correct id getter");
                return null;
            }
            String objId = (String) idMethod.invoke(entity);
            return getCreationTime(objId);
        } catch(Exception e)
        {
            JDAImpl.LOG.warn("Tried to look up creation-time for entity of class " + entity.getClass().getName() + " which doesn't have the correct id getter");
            return null;
        }
    }

    /**
     * Returns a prettier String-representation of a OffsetDateTime object
     *
     * @param time
     *      The OffsetDateTime object to format
     * @return
     *      The String of the formatted OffsetDateTime
     */
    public static String getDateTimeString(OffsetDateTime time)
    {
        return time.format(dtFormatter);
    }
}
