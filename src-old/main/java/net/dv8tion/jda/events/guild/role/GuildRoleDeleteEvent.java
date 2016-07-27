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

package net.dv8tion.jda.events.guild.role;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.events.guild.GenericGuildEvent;

/**
 * <b><u>GuildRoleDeleteEvent</u></b><br/>
 * Fired if a {@link net.dv8tion.jda.entities.Role Role} is deleted.<br/>
 * <br/>
 * Use: Retrieve deleted Role and it's Guild.
 */
public class GuildRoleDeleteEvent extends GenericGuildEvent
{
    private final Role deletedRole;

    public GuildRoleDeleteEvent(JDA api, int responseNumber, Guild guild, Role deletedRole)
    {
        super(api, responseNumber, guild);
        this.deletedRole = deletedRole;
    }

    public Role getRole()
    {
        return deletedRole;
    }
}
