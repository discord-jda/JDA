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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.guild.SystemChannelFlag;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Indicates that {@link Guild#getSystemChannelFlags()} have been updated.
 *
 * <p>This event can be used to detect changes in system channel flags and retrieve the old one.
 *
 * <p>Identifier: {@code system_channel_flags}
 * */
public class GuildUpdateSystemChannelFlagsEvent extends GenericGuildUpdateEvent<EnumSet<SystemChannelFlag>>
{
    public static final String IDENTIFIER = "system_channel_flags";

    public GuildUpdateSystemChannelFlagsEvent(@Nonnull JDA api,
                                              long responseNumber,
                                              @Nonnull Guild guild,
                                              @Nonnull EnumSet<SystemChannelFlag> oldFlags,
                                              @Nonnull EnumSet<SystemChannelFlag> newFlags)
    {
        super(api, responseNumber, guild, oldFlags, newFlags, IDENTIFIER);
    }

    /**
     * The old system channel flags for this guild.
     *
     * @return The old system channel flags for this guild, or null if none was set.
     */
    @Nonnull
    public EnumSet<SystemChannelFlag>  getOldFlags()
    {
        return getOldValue();
    }

    /**
     * The new system channel flags for this guild.
     *
     * @return The new system channel flags for this guild, or null if none was set.
     */
    @Nonnull
    public EnumSet<SystemChannelFlag>  getNewFlags()
    {
        return getNewValue();
    }

}
