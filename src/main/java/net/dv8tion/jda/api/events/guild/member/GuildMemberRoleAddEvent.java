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
package net.dv8tion.jda.api.events.guild.member;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Indicates that one or more {@link net.dv8tion.jda.api.entities.Role Roles} were assigned to a {@link net.dv8tion.jda.api.entities.Member Member}.
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Can be used to retrieve affected member and guild. Provides a list of added roles.
 */
public class GuildMemberRoleAddEvent extends GenericGuildMemberEvent
{
    private final List<Role> addedRoles;

    public GuildMemberRoleAddEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nonnull List<Role> addedRoles)
    {
        super(api, responseNumber, member);
        this.addedRoles = Collections.unmodifiableList(addedRoles);
    }

    /**
     * The list of roles that were added
     *
     * @return The list of roles that were added
     */
    @Nonnull
    public List<Role> getRoles()
    {
        return addedRoles;
    }
}
