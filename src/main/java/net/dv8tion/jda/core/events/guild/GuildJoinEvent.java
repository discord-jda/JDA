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
package net.dv8tion.jda.core.events.guild;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Indicates that a you joined a {@link net.dv8tion.jda.core.entities.Guild Guild}.
 *
 * <p><b>Warning: Discord already triggered a mass amount of these events due to a downtime. Be careful!</b>
 */
public class GuildJoinEvent extends GenericGuildEvent
{
    public GuildJoinEvent(JDA api, long responseNumber, Guild guild)
    {
        super(api, responseNumber, guild);
    }
}
