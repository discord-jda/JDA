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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.PrivilegeConfig;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PrivilegeHelper
{
    public static boolean canMemberRun(@Nonnull PrivilegeConfig config, @Nonnull GuildChannel channel, @Nonnull Member member, @Nonnull Command command)
    {
        if (command.getDefaultPermissions().equals(DefaultMemberPermissions.DISABLED))
            return member.hasPermission(channel, Permission.ADMINISTRATOR);
        return CommandLevelChannelPermissionChecks.canMemberRun(config, channel, member, command);
    }

    @Nullable
    public static IntegrationPrivilege findPrivilege(@Nullable Collection<IntegrationPrivilege> privileges, @Nonnull Predicate<IntegrationPrivilege> predicate)
    {
        if (privileges == null)
            return null;
        return privileges.stream()
                .filter(predicate)
                .findAny()
                .orElse(null);
    }

    @Nullable
    public static Stream<IntegrationPrivilege> findAllPrivileges(@Nullable Collection<IntegrationPrivilege> privileges, @Nonnull Predicate<IntegrationPrivilege> predicate)
    {
        if (privileges == null)
            return null;
        return privileges.stream().filter(predicate);
    }

    @Nonnull
    public static Predicate<IntegrationPrivilege> matchingChannel(@Nonnull GuildChannel channel)
    {
        return p -> p.getType() == IntegrationPrivilege.Type.CHANNEL && p.getIdLong() == channel.getIdLong();
    }

    @Nonnull
    public static Predicate<IntegrationPrivilege> matchingMember(@Nonnull Member member)
    {
        return p -> p.getType() == IntegrationPrivilege.Type.USER && p.getIdLong() == member.getIdLong();
    }

    @Nonnull
    public static Predicate<IntegrationPrivilege> matchingRole(@Nonnull Role role)
    {
        return p -> p.getType() == IntegrationPrivilege.Type.ROLE && p.getIdLong() == role.getIdLong();
    }
}
