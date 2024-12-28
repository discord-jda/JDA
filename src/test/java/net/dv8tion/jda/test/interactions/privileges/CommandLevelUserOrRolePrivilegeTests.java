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

import net.dv8tion.jda.internal.utils.interactions.commands.CommandLevelUserOrRolePermissionChecks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

public class CommandLevelUserOrRolePrivilegeTests extends AbstractPrivilegeConfigTest
{

    @Test
    @DisplayName("Permission for user exists and is disabled, " +
            "command can't be run")
    public void disabledUserExists()
    {
        // Setup privileges
        useAppPrivileges();
        useCommandPrivileges(userPrivilege(DISABLED));

        // Logic that needs to actually run
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user exists and is enabled, " +
            "command can be run")
    public void enabledUserExists()
    {
        // Setup privileges
        useAppPrivileges();
        useCommandPrivileges(userPrivilege(ENABLED));

        // Logic that needs to actually run
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isTrue();

        // Verify what got called
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user does not exist, " +
            "find roles with privileges, " +
            "check if at least one is enabled, " +
            "one is and one isn't, " +
            "command can be run")
    public void enabledUserDoesExistsAndTwoRoleConfigurationsExists()
    {
        // Setup privileges
        useAppPrivileges();
        useCommandPrivileges(rolePrivilege(DISABLED), rolePrivilege(ENABLED));

        // Logic that needs to actually run
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isTrue();

        // Verify what got called
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
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
        useAppPrivileges();
        useCommandPrivileges(rolePrivilege(DISABLED));

        // Logic that needs to actually run
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
    }

    @Test
    @DisplayName("Permission for user does not exist, " +
            "find roles with privileges, " +
            "none exists, " +
            "@everyone has privileges and is enabled, " +
            "command can be run")
    public void enabledUserDoesExistsAndNoRoleConfigurationExistsAndEveryoneIsEnabled()
    {
        // Setup privileges
        useAppPrivileges();
        useCommandPrivileges(everyonePrivilege(ENABLED));

        // Logic that needs to actually run
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isTrue();

        // Verify what got called
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command), times(1));
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
        useAppPrivileges();
        useCommandPrivileges(everyonePrivilege(DISABLED));

        // Logic that needs to actually run
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).thenCallRealMethod();
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command)).thenCallRealMethod();
        commandLevelUserRoleMock.when(() -> CommandLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command)).isFalse();

        // Verify what got called
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.canMemberRun(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.hasAtLeastOneConfiguredRole(config, channel, member, command), times(1));
        commandLevelUserRoleMock.verify(() -> CommandLevelUserOrRolePermissionChecks.isEveryoneAllowed(config, channel, member, command), times(1));
    }
}
