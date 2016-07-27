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
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>GuildRoleUpdateEvent</u></b><br/>
 * Fired whenever a {@link net.dv8tion.jda.entities.Role Role} is updated.<br/>
 * <br/>
 * Use: Retrieve affected Role. <i>(No real use for JDA user)</i>
 */
public class GuildRoleUpdateEvent extends Event
{
    protected final Role role;
    public GuildRoleUpdateEvent(JDA api, int responseNumber, Role role)
    {
        super(api, responseNumber);
        this.role = role;
    }

    public Role getRole()
    {
        return role;
    }
}
