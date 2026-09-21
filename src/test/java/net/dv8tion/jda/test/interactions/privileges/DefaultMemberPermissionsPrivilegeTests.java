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
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.dv8tion.jda.internal.utils.interactions.commands.DefaultMemberPermissionsChecks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DefaultMemberPermissionsPrivilegeTests extends AbstractPrivilegeConfigTest
{

    @Test
    @DisplayName("Requires no permissions, can run")
    public void requiresNoPermissions()
    {
        // Setup command permissions
        when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.ENABLED);

        // Logic that needs to actually run
        defaultMemberPermissionsMock.when(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).isTrue();

        // Verify what got called
        defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
    }

    @Test
    @DisplayName("Is disabled, can't run")
    public void disabled()
    {
        // Setup command permissions
        when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.DISABLED);

        // Logic that needs to actually run
        defaultMemberPermissionsMock.when(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).thenCallRealMethod();

        // Do run
        assertThat(DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).isFalse();

        // Verify what got called
        defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
    }

    @Test
    @DisplayName("Requires permissions, can run")
    public void requiresPermissions()
    {
        try (MockedStatic<PermissionUtil> permissionUtilMock = mockStatic(PermissionUtil.class))
        {
            // Set up member permissions
            permissionUtilMock.when(() -> PermissionUtil.getEffectivePermission(channel, member)).thenReturn(Permission.MESSAGE_MANAGE.getRawValue());

            // Setup command permissions
            when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));

            // Logic that needs to actually run
            defaultMemberPermissionsMock.when(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).thenCallRealMethod();

            // Do run
            assertThat(DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).isTrue();

            // Verify what got called
            defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
            permissionUtilMock.verify(() -> PermissionUtil.getEffectivePermission(channel, member), times(1));
        }
    }

    @Test
    @DisplayName("Requires permissions, can't run")
    public void requiresMissingPermissions()
    {
        try (MockedStatic<PermissionUtil> permissionUtilMock = mockStatic(PermissionUtil.class))
        {
            // Set up member permissions
            permissionUtilMock.when(() -> PermissionUtil.getEffectivePermission(channel, member)).thenReturn(Permission.MESSAGE_SEND.getRawValue());

            // Setup command permissions
            when(command.getDefaultPermissions()).thenReturn(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));

            // Logic that needs to actually run
            defaultMemberPermissionsMock.when(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).thenCallRealMethod();

            // Do run
            assertThat(DefaultMemberPermissionsChecks.canMemberRun(channel, member, command)).isFalse();

            // Verify what got called
            defaultMemberPermissionsMock.verify(() -> DefaultMemberPermissionsChecks.canMemberRun(channel, member, command), times(1));
            permissionUtilMock.verify(() -> PermissionUtil.getEffectivePermission(channel, member), times(1));
        }
    }
}
