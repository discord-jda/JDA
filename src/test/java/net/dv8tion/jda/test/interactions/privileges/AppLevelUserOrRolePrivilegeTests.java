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

import net.dv8tion.jda.internal.utils.interactions.commands.AppLevelUserOrRolePermissionChecks;
import net.dv8tion.jda.internal.utils.interactions.commands.DefaultMemberPermissionsChecks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

public class AppLevelUserOrRolePrivilegeTests extends AbstractPrivilegeConfigTest
{

    @Test
    @DisplayName("Permission for user exists and is disabled, " +
            "command can't be run")
    public void disabledUserExists()
    {
        // Setup privileges
        useAppPrivileges(userPrivilege(DISABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user exists and is enabled, " +
            "forward to default_member_permissions checks")
    public void enabledUserExists()
    {
        // Setup privileges
        useAppPrivileges(userPrivilege(ENABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command); // Only test forwarding

        // Verify what got called
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user does not exist, " +
            "find roles with privileges, " +
            "check if at least one is enabled, " +
            "one is and one isn't, " +
            "forward to default_member_permissions checks")
    public void enabledUserDoesExistsAndTwoRoleConfigurationsExists()
    {
        // Setup privileges
        useAppPrivileges(rolePrivilege(DISABLED), rolePrivilege(ENABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();

        // Do run
        AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command); // Only test forwarding

        // Verify what got called
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
        defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user does not exist, " +
            "find roles with privileges, " +
            "check if at least one is enabled, " +
            "none is, " +
            "command can't be run")
    public void enabledUserDoesExistsAndRoleConfigurationExistsAndIsDisabled()
    {
        // Setup privileges
        useAppPrivileges(rolePrivilege(DISABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user does not exist, " +
            "find roles with privileges, " +
            "none exists, " +
            "@everyone has privileges and is enabled, " +
            "forward to default_member_permissions checks")
    public void enabledUserDoesExistsAndNoRoleConfigurationExistsAndEveryoneIsEnabled()
    {
        // Setup privileges
        useAppPrivileges(everyonePrivilege(ENABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command)).thenCallRealMethod();

        // Do run
        AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command); // Only test forwarding

        // Verify what got called
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command), times(1));
        defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user does not exist, " +
            "find roles with privileges, " +
            "none exists, " +
            "@everyone has privileges but is disabled, " +
            "command can't be run")
    public void enabledUserDoesExistsAndNoRoleConfigurationExistsAndEveryoneIsDisabled()
    {
        // Setup privileges
        useAppPrivileges(everyonePrivilege(DISABLED));
        useCommandPrivileges();

        // Logic that needs to actually run
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();
        appLevelUserRoleMock.when(() -> AppLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
        appLevelUserRoleMock.verify(() -> AppLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command), times(1));
    }
}
