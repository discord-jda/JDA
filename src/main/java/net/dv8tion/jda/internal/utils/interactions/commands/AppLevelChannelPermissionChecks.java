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

import static net.dv8tion.jda.internal.utils.interactions.commands.PrivilegeHelper.findPrivilege;
import static net.dv8tion.jda.internal.utils.interactions.commands.PrivilegeHelper.matchingChannel;

public class AppLevelChannelPermissionChecks
{
    public static boolean canMemberRun(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        final IntegrationPrivilege appChannelPermissions = findPrivilege(config.getApplicationPrivileges(), matchingChannel(channel));
        if (appChannelPermissions != null)
        {
            if (appChannelPermissions.isEnabled())
                return CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command);
            return false;
        }
        else
            return isAllowedInAllChannels(config, channel, member, command);
    }

    public static boolean isAllowedInAllChannels(PrivilegeConfig config, GuildChannel channel, Member member, Command command)
    {
        final IntegrationPrivilege appChannelPermissions = findPrivilege(config.getApplicationPrivileges(), IntegrationPrivilege::targetsAllChannels);
        if (appChannelPermissions != null)
        {
            if (appChannelPermissions.isEnabled())
                return CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command);
            return false;
        }
        else
            return CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command);
    }
}
