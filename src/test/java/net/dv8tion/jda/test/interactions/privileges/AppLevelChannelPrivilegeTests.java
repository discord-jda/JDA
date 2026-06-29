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

import net.dv8tion.jda.internal.utils.interactions.commands.AppLevelChannelPermissionChecks;
import net.dv8tion.jda.internal.utils.interactions.commands.CommandLevelUserOrRolePermissionChecks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

public class AppLevelChannelPrivilegeTests extends AbstractPrivilegeConfigTest
{

    @Test
    @DisplayName("Permission for channel exists and is disabled, " +
            "command can't be run")
    public void deniedChannelExists()
    {
        // Setup privileges
        useAppPrivileges(channelPrivilege(DISABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.canMemberRun(any(), any(), any(), any())).thenCallRealMethod();

        // Do run
        assertThat(AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for channel exists and is enabled, " +
            "forward to user/role checks")
    public void enabledChannelExists()
    {
        // Setup privileges
        useAppPrivileges(channelPrivilege(ENABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.canMemberRun(any(), any(), any(), any())).thenCallRealMethod();

        // Do run
        AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command); // No result assert, only test forwarding

        // Verify what got called
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(any(), any(), any(), any()), times(1));
    }

    @Test
    @DisplayName("Permission for channel does not exist, " +
            "finds one that applies to all channels but is disabled")
    public void channelDoesNotExistAndDisabledAllChannels()
    {
        // Setup privileges
        useAppPrivileges(allChannelsPrivilege(DISABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.canMemberRun(any(), any(), any(), any())).thenCallRealMethod();
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.isAllowedInAllChannels(any(), any(), any(), any())).thenCallRealMethod();

        // Do run
        assertThat(AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.isAllowedInAllChannels(any(), any(), any(), any()), times(1));
    }

    @Test
    @DisplayName("Permission for channel does not exist, " +
            "finds one that applies to all channels and is enabled, " +
            "forward to user/role checks")
    public void channelDoesNotExistButEnabledAllChannels()
    {
        // Setup privileges
        useAppPrivileges(allChannelsPrivilege(ENABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.canMemberRun(any(), any(), any(), any())).thenCallRealMethod();
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.isAllowedInAllChannels(any(), any(), any(), any())).thenCallRealMethod();

        // Do run
        AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command); // No result assert, only test forwarding

        // Verify what got called
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.isAllowedInAllChannels(any(), any(), any(), any()), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(any(), any(), any(), any()), times(1));
    }

    @Test
    @DisplayName("Permission for channel does not exist, " +
            "none applies to all channels, " +
            "forward to app-level channel checks")
    public void channelDoesNotExistAndNoneAllChannels()
    {
        // Setup privileges
        useAppPrivileges();
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.canMemberRun(any(), any(), any(), any())).thenCallRealMethod();
        appLevelChannelMock.when(() -> AppLevelChannelPermissionChecks.isAllowedInAllChannels(any(), any(), any(), any())).thenCallRealMethod();

        // Do run
        AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command); // No result assert, only test forwarding

        // Verify what got called
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelChannelMock.verify(() -> AppLevelChannelPermissionChecks.isAllowedInAllChannels(any(), any(), any(), any()), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(any(), any(), any(), any()), times(1));
    }
}
