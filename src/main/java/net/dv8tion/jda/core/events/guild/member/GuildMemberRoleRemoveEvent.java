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
package net.dv8tion.jda.core.events.guild.member;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.Collections;
import java.util.List;

/**
 * Indicates that one or more {@link net.dv8tion.jda.core.entities.Role Roles} were removed from a {@link net.dv8tion.jda.core.entities.Member Member}.
 *
 * <p>Can be used to retrieve affected member and guild. Provides a list of removed roles.
 */
public class GuildMemberRoleRemoveEvent extends GenericGuildMemberEvent
{
    private final List<Role> removedRoles;

    public GuildMemberRoleRemoveEvent(JDA api, long responseNumber, Member member, List<Role> removedRoles)
    {
        super(api, responseNumber, member);
        this.removedRoles = Collections.unmodifiableList(removedRoles);
    }

    /**
     * The removed roles
     *
     * @return The removed roles
     */
    public List<Role> getRoles()
    {
        return removedRoles;
    }
}
