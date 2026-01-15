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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link net.dv8tion.jda.api.entities.Invite Invite} Builder system created as an extension of {@link net.dv8tion.jda.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.api.entities.Invite Invites}.
 *
 * @see net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer#createInvite()
 */
public interface InviteAction extends AuditableRestAction<Invite> {
    @Nonnull
    @Override
    @CheckReturnValue
    InviteAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    InviteAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    @CheckReturnValue
    InviteAction deadline(long timestamp);

    /**
     * Sets the max age in seconds for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     * {@code null} will reset this to the default value.
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     *
     * @throws IllegalArgumentException
     *         If maxAge is negative.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setMaxAge(@Nullable Integer maxAge);

    /**
     * Sets the max age for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     * {@code null} will reset this to the default value.
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     * @param  timeUnit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} type of {@code maxAge}.
     *
     * @throws IllegalArgumentException
     *         If maxAge is negative or maxAge is positive and timeUnit is null.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setMaxAge(@Nullable Long maxAge, @Nonnull TimeUnit timeUnit);

    /**
     * Sets the max uses for the invite. Set this to {@code 0} if the invite should have unlimited uses. Default is {@code 0}.
     * {@code null} will reset this to the default value.
     *
     * @param  maxUses
     *         The max uses for this invite or {@code null} to use the default value.
     *
     * @throws IllegalArgumentException
     *         If maxUses is negative.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setMaxUses(@Nullable Integer maxUses);

    /**
     * Sets whether the invite should only grant temporary membership. Default is {@code false}.
     *
     * @param  temporary
     *         Whether the invite should only grant temporary membership or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTemporary(@Nullable Boolean temporary);

    /**
     * Sets whether discord should reuse a similar invite. Default is {@code false}.
     *
     * @param  unique
     *         Whether discord should reuse a similar invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setUnique(@Nullable Boolean unique);

    /**
     * Sets the id of the targeted application.
     * <br>The invite has to point to a voice channel.
     * The invite will have the {@link Invite.TargetType#EMBEDDED_APPLICATION} target.
     *
     * @param applicationId
     *        The id of the embedded application to target or {@code 0} to remove
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetApplication(long applicationId);

    /**
     * Sets the id of the targeted application.
     * <br>The invite has to point to a voice channel.
     * The invite will have the {@link Invite.TargetType#EMBEDDED_APPLICATION} target.
     *
     * @param applicationId
     *        The id of the embedded application to target
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setTargetApplication(@Nonnull String applicationId) {
        return setTargetApplication(MiscUtil.parseSnowflake(applicationId));
    }

    /**
     * Sets the user whose stream to target for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param userId
     *        The id of the user whose stream to target or {@code 0} to remove.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetStream(long userId);

    /**
     * Sets the user whose stream to display for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param userId
     *        The id of the user whose stream to target.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setTargetStream(@Nonnull String userId) {
        return setTargetStream(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Sets the user whose stream to display for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param user
     *        The user whose stream to target.
     *
     * @throws IllegalArgumentException
     *         If the provided user is {@code null}
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setTargetStream(@Nonnull User user) {
        Checks.notNull(user, "User");
        return setTargetStream(user.getIdLong());
    }

    /**
     * Sets the user whose stream to display for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param member
     *        The member whose stream to target.
     *
     * @throws IllegalArgumentException
     *         If the provided member is {@code null}
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setTargetStream(@Nonnull Member member) {
        Checks.notNull(member, "Member");
        return setTargetStream(member.getIdLong());
    }

    /**
     * Sets the users allowed to use this invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  users
     *         The users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         If the provided collection is or contains {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetUsers(@Nonnull Collection<? extends UserSnowflake> users);

    /**
     * Sets the users allowed to use this invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  users
     *         The users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         If the provided array is or contains {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetUsers(@Nonnull UserSnowflake... users);

    /**
     * Sets IDs of users allowed to use this invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  ids
     *         IDs of users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         If the provided collection is or contains {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetUserIds(@Nonnull Collection<Long> ids);

    /**
     * Sets IDs of users allowed to use this invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  ids
     *         IDs of users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         If the provided array is or contains {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetUserIds(@Nonnull long... ids);

    /**
     * Sets IDs of users allowed to use this invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  ids
     *         IDs of users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided array is or contains {@code null}</li>
     *             <li>If one of the strings is empty</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     * @throws NumberFormatException
     *         If one of the IDs is an invalid snowflake
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setTargetUserIds(@Nonnull String... ids);

    /**
     * Sets roles to be assigned when accepting the created invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission!
     *
     * @param  roles
     *         The roles to assign upon invite acceptation
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         cannot {@linkplain SelfMember#canInteract(Role) interact} with one of the roles
     *         due to the role being higher than the bot's highest role
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided collection is {@code null} or contains {@code null}</li>
     *             <li>If one of the roles isn't from the target guild</li>
     *         </ul>
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setRoles(@Nonnull Collection<? extends Role> roles);

    /**
     * Sets roles to be assigned when accepting the created invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission!
     *
     * @param  roles
     *         The roles to assign upon invite acceptation
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         cannot {@linkplain SelfMember#canInteract(Role) interact} with one of the roles
     *         due to the role being higher than the bot's highest role
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided array is {@code null} or contains {@code null}</li>
     *             <li>If one of the roles isn't from the target guild</li>
     *         </ul>
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setRoles(@Nonnull Role... roles) {
        Checks.noneNull(roles, "Roles");
        return setRoles(Arrays.asList(roles));
    }

    /**
     * Sets IDs of roles to be assigned when accepting the created invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission!
     *
     * <p>IDs that do not point to an existing role in the targeted guild will be ignored.
     *
     * @param  ids
     *         The IDs of the roles to assign upon invite acceptation
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         cannot {@linkplain SelfMember#canInteract(Role) interact} with one of the roles
     *         due to the role being higher than the bot's highest role
     * @throws IllegalArgumentException
     *         If the provided collection is {@code null} or contains {@code null}
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setRoleIds(@Nonnull Collection<Long> ids) {
        Checks.noneNull(ids, "IDs");
        return setRoleIds(ids.stream().mapToLong(Long::longValue).toArray());
    }

    /**
     * Sets IDs of roles to be assigned when accepting the created invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission!
     *
     * <p>IDs that do not point to an existing role in the targeted guild will be ignored.
     *
     * @param  ids
     *         The IDs of the roles to assign upon invite acceptation
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         cannot {@linkplain SelfMember#canInteract(Role) interact} with one of the roles
     *         due to the role being higher than the bot's highest role
     * @throws IllegalArgumentException
     *         If the provided array is {@code null}
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    InviteAction setRoleIds(@Nonnull long... ids);

    /**
     * Sets IDs of roles to be assigned when accepting the created invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission!
     *
     * <p>IDs that do not point to an existing role in the targeted guild will be ignored.
     *
     * @param  ids
     *         The IDs of the roles to assign upon invite acceptation
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} permission
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the {@linkplain Guild#getSelfMember() self member}
     *         cannot {@linkplain SelfMember#canInteract(Role) interact} with one of the roles
     *         due to the role being higher than the bot's highest role
     * @throws IllegalArgumentException
     *         If the provided array is {@code null}, contains {@code null}, or, has an empty string
     * @throws NumberFormatException
     *         If one of the IDs is not a valid snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    default InviteAction setRoleIds(@Nonnull String... ids) {
        Checks.noneNull(ids, "IDs");

        long[] arr = new long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            arr[i] = MiscUtil.parseSnowflake(ids[i]);
        }
        return setRoleIds(arr);
    }
}
