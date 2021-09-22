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

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;

/**
 * Indicates that the {@link net.dv8tion.jda.api.Region Region} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a Region changes and retrieve the old one
 *
 * <p>Identifier: {@code region}
 *
 * @deprecated This event is no longer supported by discord
 */
@Deprecated
@ForRemoval(deadline = "5.0.0")
@ReplaceWith("VoiceChannelUpdateRegionEvent")
@DeprecatedSince("4.3.0")
public class GuildUpdateRegionEvent extends GenericGuildUpdateEvent<Region>
{
    public static final String IDENTIFIER = "region";

    private final String oldRegion;
    private final String newRegion;

    public GuildUpdateRegionEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull String oldRegion)
    {
        super(api, responseNumber, guild, Region.fromKey(oldRegion), guild.getRegion(), IDENTIFIER);
        this.oldRegion = oldRegion;
        this.newRegion = guild.getRegionRaw();
    }

    /**
     * The old {@link net.dv8tion.jda.api.Region Region} of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>If this region cannot be resolved to an enum constant this will return {@link net.dv8tion.jda.api.Region#UNKNOWN UNKNOWN}!
     *
     * <p>You can use {@link #getOldRegionRaw()} to get the raw name that discord provides for this region!
     *
     * @return Resolved {@link net.dv8tion.jda.api.Region Region} constant from the raw name
     */
    @Nonnull
    public Region getOldRegion()
    {
        return getOldValue();
    }

    /**
     * The raw voice region name that was used prior to this update by the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>This can be resolved using {@link #getOldRegion()} to a constant of the enum. If that returns {@link net.dv8tion.jda.api.Region#UNKNOWN UNKNOWN}
     * this region is not currently registered in JDA.
     *
     * @return Raw name of the old voice region
     */
    @Nonnull
    public String getOldRegionRaw()
    {
        return oldRegion;
    }

    /**
     * The new {@link net.dv8tion.jda.api.Region Region} of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>If this region cannot be resolved to an enum constant this will return {@link net.dv8tion.jda.api.Region#UNKNOWN UNKNOWN}!
     *
     * <p>You can use {@link #getNewRegionRaw()} to get the raw name that discord provides for this region!
     *
     * @return Resolved {@link net.dv8tion.jda.api.Region Region} constant from the raw name
     */
    @Nonnull
    public Region getNewRegion()
    {
        return getNewValue();
    }

    /**
     * The raw voice region name that was updated to in the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>This can be resolved using {@link #getNewRegion()} to a constant of the enum. If that returns {@link net.dv8tion.jda.api.Region#UNKNOWN UNKNOWN}
     * this region is not currently registered in JDA.
     *
     * @return Raw name of the updated voice region
     */
    @Nonnull
    public String getNewRegionRaw()
    {
        return newRegion;
    }

    @Nonnull
    @Override
    public Region getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Region getNewValue()
    {
        return super.getNewValue();
    }
}
