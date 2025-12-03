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

package net.dv8tion.jda.test.entities.channel;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import net.dv8tion.jda.internal.utils.ClockProvider;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.*;

public class MessagePinDeadlineTest {
    private static final Instant BEFORE_DEADLINE = Instant.parse("2025-08-21T00:00:00Z");
    private static final Instant AFTER_DEADLINE = PermissionUtil.FEB_23_2026_DEADLINE.plusSeconds(1);

    @MethodSource("validPermissions")
    @ParameterizedTest
    void testPermissionsAreValidatedInOrder(
            Instant timeOfCheck, List<Permission> grantedPermissions, List<Permission> expectedCheckedPermissions) {
        SelfMember selfMember = mock(SelfMember.class);
        when(selfMember.hasPermission(ArgumentMatchers.<GuildChannel>any(), ArgumentMatchers.<Permission>any()))
                .thenAnswer(invocation -> {
                    Permission permission = invocation.getArgument(1);
                    return grantedPermissions.contains(permission);
                });

        Guild guild = mock(Guild.class);
        doReturn(selfMember).when(guild).getSelfMember();

        GuildMessageChannelMixin<?> channel = mock(GuildMessageChannelMixin.class);
        doCallRealMethod().when(channel).checkCanControlMessagePins();
        doReturn(guild).when(channel).getGuild();

        ClockProvider.withFixedTime(timeOfCheck, channel::checkCanControlMessagePins);

        // Make sure the permissions are checked in the given order (to check short-circuiting)
        InOrder selfMemberInOrder = inOrder(selfMember);
        for (Permission expectedCheckedPermission : expectedCheckedPermissions) {
            selfMemberInOrder.verify(selfMember).hasPermission(channel, expectedCheckedPermission);
        }
        selfMemberInOrder.verifyNoMoreInteractions();
    }

    static Stream<Arguments> validPermissions() {
        return Stream.of(
                Arguments.argumentSet(
                        "Before deadline, check MESSAGE_MANAGE first then PIN_MESSAGES",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* grantedPermissions */ List.of(Permission.PIN_MESSAGES),
                        /* expectedCheckedPermissions */ List.of(Permission.MESSAGE_MANAGE, Permission.PIN_MESSAGES)),
                Arguments.argumentSet(
                        "Before deadline, check MESSAGE_MANAGE first then short-circuit",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* grantedPermissions */ List.of(Permission.MESSAGE_MANAGE),
                        /* expectedCheckedPermissions */ List.of(Permission.MESSAGE_MANAGE)),
                Arguments.argumentSet(
                        "After deadline, check PIN_MESSAGES",
                        /* timeOfCheck */ AFTER_DEADLINE,
                        /* grantedPermissions */ List.of(Permission.PIN_MESSAGES),
                        /* expectedCheckedPermissions */ List.of(Permission.PIN_MESSAGES)));
    }

    @MethodSource("invalidPermissions")
    @ParameterizedTest
    void testPermissionsAreInvalid(Instant timeOfCheck, List<Permission> expectedCheckedPermissions) {
        SelfMember selfMember = mock(SelfMember.class);
        doReturn(false)
                .when(selfMember)
                .hasPermission(ArgumentMatchers.<GuildChannel>any(), ArgumentMatchers.<Permission>any());

        Guild guild = mock(Guild.class);
        doReturn(selfMember).when(guild).getSelfMember();

        GuildMessageChannelMixin<?> channel = mock(GuildMessageChannelMixin.class);
        doCallRealMethod().when(channel).checkCanControlMessagePins();
        doReturn(guild).when(channel).getGuild();
        doCallRealMethod().when(channel).checkPermission(any(), any());

        assertThatException()
                .isThrownBy(() -> ClockProvider.withFixedTime(timeOfCheck, channel::checkCanControlMessagePins))
                .isInstanceOf(InsufficientPermissionException.class);

        // Make sure the permissions are checked in the given order
        InOrder selfMemberInOrder = inOrder(selfMember);
        for (Permission expectedCheckedPermission : expectedCheckedPermissions) {
            selfMemberInOrder.verify(selfMember).hasPermission(channel, expectedCheckedPermission);
        }
        // Make sure no extra permissions were checked
        selfMemberInOrder
                .verify(selfMember, never())
                .hasPermission(ArgumentMatchers.<GuildChannel>any(), ArgumentMatchers.<Permission>any());
    }

    static Stream<Arguments> invalidPermissions() {
        return Stream.of(
                Arguments.argumentSet(
                        "Before deadline, fail if no MESSAGE_MANAGE or PIN_MESSAGES",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* expectedCheckedPermissions */ List.of(Permission.MESSAGE_MANAGE, Permission.PIN_MESSAGES)),
                Arguments.argumentSet(
                        "After deadline, fail if no PIN_MESSAGES",
                        /* timeOfCheck */ AFTER_DEADLINE,
                        /* expectedCheckedPermissions */ List.of(Permission.PIN_MESSAGES)));
    }
}
