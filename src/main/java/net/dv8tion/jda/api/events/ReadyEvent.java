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
package net.dv8tion.jda.api.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.GuildSetupController;

import javax.annotation.Nonnull;

/**
 * Indicates that JDA finished loading all entities.
 * <br>Before this event was fired all entity related functions were not guaranteed to work as expected.
 *
 * <p>Can be used to indicate when JDA finished populating internal objects and is ready to be used.
 * When this is fired all <b>available</b> entities are cached and accessible.
 */
public class ReadyEvent extends Event
{
    private final int availableGuilds;
    private final int unavailableGuilds;

    public ReadyEvent(@Nonnull JDA api, long responseNumber)
    {
        super(api, responseNumber);
        this.availableGuilds = (int) getJDA().getGuildCache().size();
        this.unavailableGuilds = ((JDAImpl) getJDA()).getGuildSetupController().getSetupNodes(GuildSetupController.Status.UNAVAILABLE).size();
    }

    /**
     * Number of available guilds for this session.
     * <br>When discord fails to connect guilds for our gateway session they will not be in cache here yet
     * but instead will fire a {@link net.dv8tion.jda.api.events.guild.GuildReadyEvent GuildReadyEvent} later.
     *
     * @return Number of available guilds for this session
     *
     * @see    #getGuildTotalCount()
     * @see    #getGuildUnavailableCount()
     */
    public int getGuildAvailableCount()
    {
        return availableGuilds;
    }

    /**
     * Number of guilds currently not available to this session
     * <br>Discord failed to connect these guilds to our gateway and we had to discard them for now.
     * These might become available again later and will then fire a {@link net.dv8tion.jda.api.events.guild.GuildReadyEvent GuildReadyEvent}.
     *
     * @return Number of currently unavailable guilds
     */
    public int getGuildUnavailableCount()
    {
        return unavailableGuilds;
    }

    /**
     * Sum of both {@link #getGuildAvailableCount()} and {@link #getGuildUnavailableCount()}.
     *
     * @return Total numbers of guilds known to this JDA session
     */
    public int getGuildTotalCount()
    {
        return getGuildAvailableCount() + getGuildUnavailableCount();
    }
}
