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

package net.dv8tion.jda.test.interactions.privileges;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.internal.utils.interactions.commands.CommandLevelChannelPermissionChecks;
import net.dv8tion.jda.internal.utils.interactions.commands.PrivilegeHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class PrivilegeHelperPrivilegeTests extends AbstractPrivilegeConfigTest
{

    @Test
    @DisplayName("Requires no permissions, forward to command level permission checks")
    public void requiresNoPermissions()
    {
        // Setup command permissions
        when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.ENABLED);

        // Logic that needs to actually run
        privilegeHelperMock.when(() -> PrivilegeHelper.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        PrivilegeHelper.canMemberRun(config, channel, member, command); // Only check forwarding

        // Verify what got called
        privilegeHelperMock.verify(() -> PrivilegeHelper.canMemberRun(config, channel, member, command), times(1));
        commandLevelChannelMock.verify(() -> CommandLevelChannelPermissionChecks.canMemberRun(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Is disabled, can't run")
    public void disabled()
    {
        // Setup command permissions
        when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.DISABLED);

        // Logic that needs to actually run
        privilegeHelperMock.when(() -> PrivilegeHelper.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(PrivilegeHelper.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        privilegeHelperMock.verify(() -> PrivilegeHelper.canMemberRun(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Is disabled, but is admin, can run")
    public void disabledBypassedByAdmin()
    {
        when(member.hasPermission(channel, Permission.ADMINISTRATOR)).thenReturn(true);

        // Setup command permissions
        when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.DISABLED);

        // Logic that needs to actually run
        privilegeHelperMock.when(() -> PrivilegeHelper.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(PrivilegeHelper.canMemberRun(config, channel, member, command)).isTrue();

        // Verify what got called
        privilegeHelperMock.verify(() -> PrivilegeHelper.canMemberRun(config, channel, member, command), times(1));
    }
}
