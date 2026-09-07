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
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Action to update target users of an {@link net.dv8tion.jda.api.entities.Invite Invite}.
 *
 * @see net.dv8tion.jda.api.entities.Invite#updateTargetUsers() Invite.updateTargetUsers()
 * @see net.dv8tion.jda.api.entities.Invite#updateTargetUsers(net.dv8tion.jda.api.JDA, String) Invite.updateTargetUsers(JDA, String)
 */
public interface InviteUpdateTargetUsersAction extends RestAction<Void> {
    /**
     * Sets the users allowed to use the targeted invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  users
     *         The users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided collection is or contains {@code null}</li>
     *             <li>If the collection is empty</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUsers(@Nonnull Collection<? extends UserSnowflake> users);

    /**
     * Sets the users allowed to use the targeted invite.
     * <br>This requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission!
     *
     * <p>If unknown users are found,
     * Discord will respond to the request with {@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}.
     *
     * @param  users
     *         The users allowed to use the invite
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided array is or contains {@code null}</li>
     *             <li>If the array is empty</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUsers(@Nonnull UserSnowflake... users);

    /**
     * Sets IDs of users allowed to use the targeted invite.
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
     *             <li>If the provided collection is or contains {@code null}</li>
     *             <li>If the collection is empty</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUserIds(@Nonnull Collection<Long> ids);

    /**
     * Sets IDs of users allowed to use the targeted invite.
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
     *             <li>If the array is empty</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUserIds(@Nonnull long... ids);

    /**
     * Sets IDs of users allowed to use the targeted invite.
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
     *             <li>If the array is empty</li>
     *             <li>If one of the strings is empty</li>
     *         </ul>
     * @throws NumberFormatException
     *         If one of the IDs is an invalid snowflake
     *
     * @return This instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUserIds(@Nonnull String... ids);

    @Nonnull
    @Override
    @CheckReturnValue
    InviteUpdateTargetUsersAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    InviteUpdateTargetUsersAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    @CheckReturnValue
    InviteUpdateTargetUsersAction deadline(long timestamp);
}
