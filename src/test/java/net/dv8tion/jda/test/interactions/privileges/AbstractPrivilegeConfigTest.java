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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.PrivilegeConfig;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.utils.interactions.commands.AppLevelChannelPermissionChecks;
import net.dv8tion.jda.internal.utils.interactions.commands.AppLevelUserOrRolePermissionChecks;
import net.dv8tion.jda.internal.utils.interactions.commands.CommandLevelChannelPermissionChecks;
import net.dv8tion.jda.internal.utils.interactions.commands.CommandLevelUserOrRolePermissionChecks;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

public abstract class AbstractPrivilegeConfigTest extends IntegrationTest
{

    protected static final boolean ENABLED = true;
    protected static final boolean DISABLED = false;

    @Mock
    protected Guild guild;
    @Mock
    protected GuildChannel channel;
    @Mock
    protected Member member;
    @Mock
    protected Command command;
    @Mock
    protected PrivilegeConfig config;

    protected MockedStatic<CommandLevelChannelPermissionChecks> commandLevelChannelMock;
    protected MockedStatic<AppLevelChannelPermissionChecks> appLevelChannelMock;
    protected MockedStatic<CommandLevelUserOrRolePermissionChecks> commandLevelUserRoleMock;
    protected MockedStatic<AppLevelUserOrRolePermissionChecks> appLevelUserRoleMock;

    @BeforeEach
    void setupMocks()
    {
        when(jda.getSelfUser()).thenReturn(new SelfUserImpl(Constants.BUTLER_USER_ID, jda));

        Role everyoneRole = mock(Role.class);
        when(everyoneRole.getIdLong()).thenReturn(Constants.GUILD_ID);
        when(guild.getIdLong()).thenReturn(Constants.GUILD_ID);
        when(guild.getPublicRole()).thenReturn(everyoneRole);

        when(channel.getIdLong()).thenReturn(Constants.CHANNEL_ID);
        when(channel.getGuild()).thenReturn(guild);

        Role memberRole = mock(Role.class);
        when(memberRole.getIdLong()).thenReturn(Constants.ROLE_ID);
        when(member.getIdLong()).thenReturn(Constants.MINN_USER_ID);
        when(member.getRoles()).thenReturn(Collections.singletonList(memberRole));

        commandLevelChannelMock = mockStatic(CommandLevelChannelPermissionChecks.class);
        appLevelChannelMock = mockStatic(AppLevelChannelPermissionChecks.class);
        commandLevelUserRoleMock = mockStatic(CommandLevelUserOrRolePermissionChecks.class);
        appLevelUserRoleMock = mockStatic(AppLevelUserOrRolePermissionChecks.class);
    }

    @AfterEach
    void teardownMocks()
    {
        try
        {
            // Make sure all interactions were checked by the test subclasses
            commandLevelChannelMock.verifyNoMoreInteractions();
            appLevelChannelMock.verifyNoMoreInteractions();
            commandLevelUserRoleMock.verifyNoMoreInteractions();
            appLevelUserRoleMock.verifyNoMoreInteractions();
        }
        finally
        {
            commandLevelChannelMock.close();
            appLevelChannelMock.close();
            commandLevelUserRoleMock.close();
            appLevelUserRoleMock.close();
        }
    }

    protected void useCommandPrivileges(IntegrationPrivilege... privileges)
    {
        when(config.getCommandPrivileges(command)).thenReturn(Arrays.asList(privileges));
    }

    protected void useAppPrivileges(IntegrationPrivilege... privileges)
    {
        when(config.getApplicationPrivileges()).thenReturn(Arrays.asList(privileges));
    }

    protected IntegrationPrivilege channelPrivilege(boolean enabled)
    {
        return new IntegrationPrivilege(guild, IntegrationPrivilege.Type.CHANNEL, enabled, Constants.CHANNEL_ID);
    }

    protected IntegrationPrivilege allChannelsPrivilege(boolean enabled)
    {
        return new IntegrationPrivilege(guild, IntegrationPrivilege.Type.CHANNEL, enabled, Constants.GUILD_ID - 1);
    }

    protected IntegrationPrivilege userPrivilege(boolean enabled)
    {
        return new IntegrationPrivilege(guild, IntegrationPrivilege.Type.USER, enabled, Constants.MINN_USER_ID);
    }

    protected IntegrationPrivilege rolePrivilege(boolean enabled)
    {
        return new IntegrationPrivilege(guild, IntegrationPrivilege.Type.ROLE, enabled, Constants.ROLE_ID);
    }

    protected IntegrationPrivilege everyonePrivilege(boolean enabled)
    {
        return new IntegrationPrivilege(guild, IntegrationPrivilege.Type.ROLE, enabled, guild.getIdLong());
    }
}
