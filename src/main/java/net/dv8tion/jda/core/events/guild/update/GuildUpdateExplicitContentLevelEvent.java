/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.entities.Guild;

/**
 * <b><u>GuildUpdateExplicitContentLevelEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.core.entities.Guild Guild}
 * updates its {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel ExplicitContentLevel}.<br>
 * <br>
 * Use: Detect what Guild updated its level and what level was set prior to that update.
 */
public class GuildUpdateExplicitContentLevelEvent extends GenericGuildUpdateEvent
{
    protected final Guild.ExplicitContentLevel oldLevel;

    public GuildUpdateExplicitContentLevelEvent(JDA api, long responseNumber, Guild guild, Guild.ExplicitContentLevel oldLevel)
    {
        super(api, responseNumber, guild);
        this.oldLevel = oldLevel;
    }

    /**
     * The old {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel ExplicitContentLevel} for the
     * {@link net.dv8tion.jda.core.entities.Guild Guild} prior to this event.
     *
     * @return The old explicit content level
     */
    public Guild.ExplicitContentLevel getOldLevel()
    {
        return oldLevel;
    }
}
