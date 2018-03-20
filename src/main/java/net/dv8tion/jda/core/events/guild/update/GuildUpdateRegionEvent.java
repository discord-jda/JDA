/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Indicates that the {@link net.dv8tion.jda.core.Region Region} of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a Region changes and retrieve the old one
 *
 * <p>Identifier: {@code region}
 */
public class GuildUpdateRegionEvent extends GenericGuildUpdateEvent<Region>
{
    public static final String IDENTIFIER = "region";

    private final String oldRegion;
    private final String newRegion;

    public GuildUpdateRegionEvent(JDA api, long responseNumber, Guild guild, String oldRegion)
    {
        super(api, responseNumber, guild, Region.fromKey(oldRegion), guild.getRegion(), IDENTIFIER);
        this.oldRegion = oldRegion;
        this.newRegion = guild.getRegionRaw();
    }

    /**
     * The old {@link net.dv8tion.jda.core.Region Region} of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>If this region cannot be resolved to an enum constant this will return {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}!
     *
     * <p>You can use {@link #getOldRegionRaw()} to get the raw name that discord provides for this region!
     *
     * @return Resolved {@link net.dv8tion.jda.core.Region Region} constant from the raw name
     */
    public Region getOldRegion()
    {
        return getOldValue();
    }

    /**
     * The raw voice region name that was used prior to this update by the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This can be resolved using {@link #getOldRegion()} to a constant of the enum. If that returns {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}
     * this region is not currently registered in JDA.
     *
     * @return Raw name of the old voice region
     */
    public String getOldRegionRaw()
    {
        return oldRegion;
    }

    /**
     * The new {@link net.dv8tion.jda.core.Region Region} of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>If this region cannot be resolved to an enum constant this will return {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}!
     *
     * <p>You can use {@link #getNewRegionRaw()} to get the raw name that discord provides for this region!
     *
     * @return Resolved {@link net.dv8tion.jda.core.Region Region} constant from the raw name
     */
    public Region getNewRegion()
    {
        return getNewValue();
    }

    /**
     * The raw voice region name that was updated to in the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This can be resolved using {@link #getNewRegion()} to a constant of the enum. If that returns {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}
     * this region is not currently registered in JDA.
     *
     * @return Raw name of the updated voice region
     */
    public String getNewRegionRaw()
    {
        return newRegion;
    }
}
