package net.dv8tion.jda.test.entities.channel;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.*;

public class MessagePinDeadlineTest
{
    private static final Instant BEFORE_DEADLINE = Instant.parse("2025-08-21T00:00:00Z");
    private static final Instant AFTER_DEADLINE = GuildMessageChannelMixin.PIN_PERMISSION_DEADLINE.plusSeconds(1);

    @MethodSource("validPermissions")
    @ParameterizedTest
    void testPermissionsAreValidatedInOrder(Instant timeOfCheck, Set<Permission> grantedPermissions, Set<Permission> expectedCheckedPermissions)
    {
        final GuildMessageChannelMixin<?> channel = mock(GuildMessageChannelMixin.class);
        doCallRealMethod().when(channel).checkCanControlMessagePins();
        doReturn(timeOfCheck).when(channel).currentInstant();
        doAnswer(invocation -> {
            final Permission permission = invocation.getArgument(0);
            return grantedPermissions.contains(permission);
        }).when(channel).hasPermission(any());
        doCallRealMethod().when(channel).checkPermission(any(), any());

        channel.checkCanControlMessagePins();

        // Make sure the permissions are checked in the given order (to check short-circuiting)
        final InOrder channelInOrder = inOrder(channel);
        for (Permission expectedCheckedPermission : expectedCheckedPermissions)
            channelInOrder.verify(channel).hasPermission(expectedCheckedPermission);
        channelInOrder.verifyNoMoreInteractions();
    }

    static Stream<Arguments> validPermissions()
    {
        return Stream.of(
                Arguments.argumentSet("Before deadline, check MESSAGE_MANAGE first then PIN_MESSAGES",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* grantedPermissions */ EnumSet.of(Permission.PIN_MESSAGES),
                        /* expectedCheckedPermissions */ EnumSet.of(Permission.MESSAGE_MANAGE, Permission.PIN_MESSAGES)
                ),
                Arguments.argumentSet("Before deadline, check MESSAGE_MANAGE first then short-circuit",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* grantedPermissions */ EnumSet.of(Permission.MESSAGE_MANAGE),
                        /* expectedCheckedPermissions */ EnumSet.of(Permission.MESSAGE_MANAGE)),
                Arguments.argumentSet("After deadline, check PIN_MESSAGES",
                        /* timeOfCheck */ AFTER_DEADLINE,
                        /* grantedPermissions */ EnumSet.of(Permission.PIN_MESSAGES),
                        /* expectedCheckedPermissions */ EnumSet.of(Permission.PIN_MESSAGES))
        );
    }

    @MethodSource("invalidPermissions")
    @ParameterizedTest
    void testPermissionsAreInvalid(Instant timeOfCheck, Set<Permission> expectedCheckedPermissions)
    {
        final GuildMessageChannelMixin<?> channel = mock(GuildMessageChannelMixin.class);
        doCallRealMethod().when(channel).checkCanControlMessagePins();
        doReturn(timeOfCheck).when(channel).currentInstant();
        doReturn(false).when(channel).hasPermission(any());
        doReturn(mock(Guild.class)).when(channel).getGuild();
        doCallRealMethod().when(channel).checkPermission(any(), any());

        assertThatException()
                .isThrownBy(channel::checkCanControlMessagePins)
                .isInstanceOf(InsufficientPermissionException.class);

        // Make sure the permissions are checked in the given order
        final InOrder channelInOrder = inOrder(channel);
        for (Permission expectedCheckedPermission : expectedCheckedPermissions)
            channelInOrder.verify(channel).hasPermission(expectedCheckedPermission);
        // Make sure no extra permissions were checked
        channelInOrder.verify(channel, never()).hasPermission(any());
    }

    static Stream<Arguments> invalidPermissions()
    {
        return Stream.of(
                Arguments.argumentSet("Before deadline, fail if no MESSAGE_MANAGE or PIN_MESSAGES",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* expectedCheckedPermissions */ EnumSet.of(Permission.MESSAGE_MANAGE, Permission.PIN_MESSAGES)),
                Arguments.argumentSet("Before deadline, fail if no MESSAGE_MANAGE",
                        /* timeOfCheck */ BEFORE_DEADLINE,
                        /* expectedCheckedPermissions */ EnumSet.of(Permission.MESSAGE_MANAGE, Permission.PIN_MESSAGES)),
                Arguments.argumentSet("After deadline, fail if no PIN_MESSAGES",
                        /* timeOfCheck */ AFTER_DEADLINE,
                        /* expectedCheckedPermissions */ EnumSet.of(Permission.PIN_MESSAGES))
        );
    }
}
