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

package net.dv8tion.jda.events.guild;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>UnavailableGuildJoinedEvent</u></b><br/>
 * Fired if you joined a {@link net.dv8tion.jda.entities.Guild Guild} that is not yet available.<br/>
 * <br/>
 * Use: Retrieve id of unavailable Guild.
 */
public class UnavailableGuildJoinedEvent extends Event
{
    private final String guildId;

    public UnavailableGuildJoinedEvent(JDA api, int responseNumber, String guildId)
    {
        super(api, responseNumber);
        this.guildId = guildId;
    }

    public String getGuildId()
    {
        return guildId;
    }
}
