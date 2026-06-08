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

package net.dv8tion.jda.test.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PermissionUtilTest extends IntegrationTest {
    private static final EnumSet<Permission> ALL_PERMISSIONS = Arrays.stream(Permission.values())
            .filter(perm -> perm != Permission.UNKNOWN)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));

    private static final EnumSet<Permission> ALL_CHANNEL_PERMISSIONS = Arrays.stream(Permission.values())
            .filter(Permission::isChannel)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));

    private static final long ALL_PERMISSIONS_RAW = Permission.getRaw(ALL_PERMISSIONS);

    @Mock
    private Guild guild;

    @Mock
    private Member member;

    @Mock
    private TextChannel textChannel;

    @Mock
    private VoiceChannel voiceChannel;

    @Mock
    private Role role;

    @Mock
    private Role publicRole;

    @Mock
    private PermissionOverride roleOverride;

    @Mock
    private PermissionOverride publicRoleOverride;

    @Mock
    private PermissionOverride memberOverride;

    @BeforeEach
    void setupMocks() {
        when(member.getGuild()).thenReturn(guild);
        when(guild.getPublicRole()).thenReturn(publicRole);
        when(member.getRoles()).thenReturn(Collections.singletonList(role));
        when(member.getUnsortedRoles()).thenReturn(Collections.singleton(role));

        mockChannel(textChannel, ChannelType.TEXT);
        mockChannel(voiceChannel, ChannelType.VOICE);
    }

    private void mockChannel(IPermissionContainer channel, ChannelType type) {
        when(channel.getType()).thenReturn(type);
        when(channel.getGuild()).thenReturn(guild);
        when(channel.getPermissionContainer()).thenReturn(channel);
        when(channel.getPermissionOverride(any(Member.class))).thenReturn(memberOverride);
        when(channel.getPermissionOverride(any(Role.class))).thenReturn(roleOverride);
        when(channel.getPermissionOverride(eq(publicRole))).thenReturn(publicRoleOverride);
    }

    @Test
    void testNoPermissionHasNoPermission() {
        when(role.getPermissionsRaw()).thenReturn(0L);

        assertThat(PermissionUtil.getExplicitPermission(member)).isEqualTo(0L);
    }

    @Test
    void testPermissionInheritedFromPublicRole() {
        when(role.getPermissionsRaw()).thenReturn(Permission.MESSAGE_HISTORY.getRawValue());
        when(publicRole.getPermissionsRaw()).thenReturn(Permission.MESSAGE_SEND.getRawValue());

        assertThat(PermissionUtil.getExplicitPermission(member))
                .isEqualTo(Permission.MESSAGE_HISTORY.getRawValue() | Permission.MESSAGE_SEND.getRawValue());
    }

    @Test
    void testAdminHasAllEffectivePermissions() {
        when(role.getPermissionsRaw()).thenReturn(Permission.ADMINISTRATOR.getRawValue());
        when(roleOverride.getDeniedRaw()).thenReturn(Permission.VIEW_CHANNEL.getRawValue());
        when(memberOverride.getDeniedRaw()).thenReturn(Permission.VIEW_CHANNEL.getRawValue());
        when(publicRoleOverride.getDeniedRaw()).thenReturn(Permission.VIEW_CHANNEL.getRawValue());

        assertThat(PermissionUtil.getEffectivePermission(member)).isEqualTo(ALL_PERMISSIONS_RAW);
        assertThat(PermissionUtil.getEffectivePermission(textChannel, member)).isEqualTo(ALL_PERMISSIONS_RAW);
        assertThat(ALL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(member, permission))
                        .isTrue());
        assertThat(ALL_CHANNEL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(textChannel, member, permission))
                        .isTrue());
    }

    @Test
    void testOwnerHasAllEffectivePermissions() {
        when(member.isOwner()).thenReturn(true);

        assertThat(PermissionUtil.getEffectivePermission(member)).isEqualTo(ALL_PERMISSIONS_RAW);
        assertThat(PermissionUtil.getEffectivePermission(textChannel, member)).isEqualTo(ALL_PERMISSIONS_RAW);
        assertThat(ALL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(member, permission))
                        .isTrue());
        assertThat(ALL_CHANNEL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(textChannel, member, permission))
                        .isTrue());
    }

    @Test
    void testMemberDoesNotHaveViewChannel() {
        when(publicRole.getPermissionsRaw()).thenReturn(Permission.VIEW_CHANNEL.getRawValue());
        when(publicRoleOverride.getDeniedRaw()).thenReturn(Permission.VIEW_CHANNEL.getRawValue());

        // denies permissions in all channels

        assertThat(PermissionUtil.getEffectivePermission(textChannel, member)).isEqualTo(0L);
        assertThat(ALL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(textChannel, member, permission))
                        .isFalse());

        assertThat(PermissionUtil.getEffectivePermission(voiceChannel, member)).isEqualTo(0L);
        assertThat(ALL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(voiceChannel, member, permission))
                        .isFalse());
    }

    @Test
    void testMemberDoesNotHaveConnectChannel() {
        when(publicRole.getPermissionsRaw()).thenReturn(Permission.VIEW_CHANNEL.getRawValue());
        when(publicRoleOverride.getDeniedRaw()).thenReturn(Permission.VOICE_CONNECT.getRawValue());

        // denies permissions in voice channels
        assertThat(PermissionUtil.getEffectivePermission(voiceChannel, member)).isEqualTo(0L);
        assertThat(ALL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(voiceChannel, member, permission))
                        .isFalse());

        // but should not affect text channel
        assertThat(PermissionUtil.getEffectivePermission(textChannel, member))
                .isEqualTo(Permission.VIEW_CHANNEL.getRawValue());
        assertThat(ALL_PERMISSIONS)
                .allSatisfy(permission -> assertThat(PermissionUtil.checkPermission(textChannel, member, permission))
                        .isEqualTo(permission == Permission.VIEW_CHANNEL));
    }
}
