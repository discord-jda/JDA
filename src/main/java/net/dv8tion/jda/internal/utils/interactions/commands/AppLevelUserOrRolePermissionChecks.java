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

package net.dv8tion.jda.internal.utils.interactions.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.PrivilegeConfig;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.dv8tion.jda.internal.utils.interactions.commands.PrivilegeHelper.*;

public class AppLevelUserOrRolePermissionChecks
{
    public static boolean canMemberRun(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        final List<IntegrationPrivilege> applicationPrivileges = config.getApplicationPrivileges();
        final IntegrationPrivilege appUserPermissions = findPrivilege(applicationPrivileges, matchingMember(member));
        if (appUserPermissions != null)
        {
            if (appUserPermissions.isEnabled())
                return DefaultMemberPermissionsChecks.canMemberRun(channel, member, command);
            return false;
        }
        else
        {
            return hasAtLeastOneConfiguredRole(config, channel, member, command);
        }
    }

    public static boolean hasAtLeastOneConfiguredRole(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        // If there's a role override, then at least one needs to be enabled
        // If there's no role override, check @everyone
        final List<IntegrationPrivilege> commandRolePermissionList = member.getRoles().stream()
                .map(r -> findPrivilege(config.getApplicationPrivileges(), matchingRole(r)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (commandRolePermissionList.isEmpty())
            return isEveryoneAllowed(config, channel, member, command);

        for (IntegrationPrivilege integrationPrivilege : commandRolePermissionList)
        {
            if (integrationPrivilege.isEnabled())
                return DefaultMemberPermissionsChecks.canMemberRun(channel, member, command);
        }
        return false;
    }

    public static boolean isEveryoneAllowed(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        final IntegrationPrivilege commandEveryonePermissions = findPrivilege(config.getApplicationPrivileges(), matchingRole(channel.getGuild().getPublicRole()));
        if (commandEveryonePermissions != null)
        {
            if (commandEveryonePermissions.isEnabled())
                return DefaultMemberPermissionsChecks.canMemberRun(channel, member, command);
            return false;
        }
        return DefaultMemberPermissionsChecks.canMemberRun(channel, member, command);
    }
}
