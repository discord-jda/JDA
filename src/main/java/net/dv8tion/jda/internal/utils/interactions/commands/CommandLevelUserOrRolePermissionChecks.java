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

public class CommandLevelUserOrRolePermissionChecks
{
    public static boolean canMemberRun(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        final IntegrationPrivilege commandUserPermissions = findPrivilege(config.getCommandPrivileges(command), matchingMember(member));
        if (commandUserPermissions != null)
            return commandUserPermissions.isEnabled();
        else
            return hasAtLeastOneConfiguredRole(config, channel, member, command);
    }

    public static boolean hasAtLeastOneConfiguredRole(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        // If there's a role override, then at least one needs to be enabled
        // If there's no role override, check @everyone
        final List<IntegrationPrivilege> commandRolePermissionList = member.getRoles().stream()
                .flatMap(r -> findAllPrivileges(config.getCommandPrivileges(command), matchingRole(r)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (commandRolePermissionList.isEmpty())
            return isEveryoneAllowed(config, channel, member, command);

        for (IntegrationPrivilege integrationPrivilege : commandRolePermissionList)
        {
            if (integrationPrivilege.isEnabled())
                return true;
        }
        return false;
    }

    public static boolean isEveryoneAllowed(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        final IntegrationPrivilege commandEveryonePermissions = findPrivilege(config.getCommandPrivileges(command), matchingRole(channel.getGuild().getPublicRole()));
        if (commandEveryonePermissions != null)
            return commandEveryonePermissions.isEnabled();
        return AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command);
    }
}
