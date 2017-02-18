/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.events.guild.member;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <b><u>GuildMemberRoleAddEvent</u></b><br>
 * Fired if one or more {@link net.dv8tion.jda.core.entities.Role Roles} are assigned to a {@link net.dv8tion.jda.core.entities.Member Member}.<br>
 * <br>
 * Use: Retrieve affected member and guild. Provides a list of added roles.
 */
public class GuildMemberRoleAddEvent extends GenericGuildMemberEvent
{
    private final List<Role> addedRoles;

    public GuildMemberRoleAddEvent(JDA api, long responseNumber, Guild guild, Member member, List<Role> addedRoles)
    {
        super(api, responseNumber, guild, member);
        this.addedRoles = new LinkedList<>(addedRoles);
    }

    public List<Role> getRoles()
    {
        return Collections.unmodifiableList(addedRoles);
    }
}
