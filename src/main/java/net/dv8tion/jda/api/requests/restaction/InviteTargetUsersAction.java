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

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Base interface for actions which manipulate an invitation's target users.
 *
 * @see InviteAction
 * @see InviteUpdateTargetUsersAction
 */
public interface InviteTargetUsersAction {
    /**
     * Sets the users allowed to use this invite.
     *
     * <p>If unknown users are found,
     * Discord will return an {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}.
     *
     * @param  users
     *         The users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided collection is or contains {@code null}</li>
     *             <li>For {@link InviteUpdateTargetUsersAction}, if the collection is empty</li>
     *         </ul>
     * @throws InsufficientPermissionException
     *         For {@link InviteAction},
     *         if the {@linkplain net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteTargetUsersAction setUsers(@Nonnull Collection<? extends UserSnowflake> users);

    /**
     * Sets the users allowed to use this invite.
     *
     * <p>If unknown users are found,
     * Discord will return an {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}.
     *
     * @param  users
     *         The users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided array is or contains {@code null}</li>
     *             <li>For {@link InviteUpdateTargetUsersAction}, if the array is empty</li>
     *         </ul>
     * @throws InsufficientPermissionException
     *         For {@link InviteAction},
     *         if the {@linkplain net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteTargetUsersAction setUsers(@Nonnull UserSnowflake... users);

    /**
     * Sets IDs of users allowed to use this invite.
     *
     * <p>If unknown users are found,
     * Discord will return an {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}.
     *
     * @param  ids
     *         IDs of users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided collection is or contains {@code null}</li>
     *             <li>For {@link InviteUpdateTargetUsersAction}, if the collection is empty</li>
     *         </ul>
     * @throws InsufficientPermissionException
     *         For {@link InviteAction},
     *         if the {@linkplain net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteTargetUsersAction setUserIds(@Nonnull Collection<Long> ids);

    /**
     * Sets IDs of users allowed to use this invite.
     *
     * <p>If unknown users are found,
     * Discord will return an {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}.
     *
     * @param  ids
     *         IDs of users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided array is or contains {@code null}</li>
     *             <li>For {@link InviteUpdateTargetUsersAction}, if the array is empty</li>
     *         </ul>
     * @throws InsufficientPermissionException
     *         For {@link InviteAction},
     *         if the {@linkplain net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteTargetUsersAction setUserIds(@Nonnull long... ids);

    /**
     * Sets IDs of users allowed to use this invite.
     *
     * <p>If unknown users are found,
     * Discord will return an {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponse}.
     *
     * @param  ids
     *         IDs of users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided array is or contains {@code null}</li>
     *             <li>If one of the strings is empty</li>
     *             <li>For {@link InviteUpdateTargetUsersAction}, if the array is empty</li>
     *         </ul>
     * @throws InsufficientPermissionException
     *         For {@link InviteAction},
     *         if the {@linkplain net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}
     *         does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission
     * @throws NumberFormatException
     *         If one of the IDs are invalid snowflakes
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteTargetUsersAction setUserIds(@Nonnull String... ids);
}
