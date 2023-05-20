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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.SelfUser;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for the logged in account.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("Minn")
 *        .setAvatar(null)
 *        .queue();
 * manager.reset(AccountManager.NAME | AccountManager.AVATAR)
 *        .setName("DV8FromTheWorld")
 *        .setAvatar(icon)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.api.JDA#getSelfUser() JDA.getSelfUser()
 * @see net.dv8tion.jda.api.entities.SelfUser#getManager()
 */
public interface AccountManager extends Manager<AccountManager>
{
    /**
     * Used to reset the name field
     *
     * @deprecated Bot usernames are set through the application name now.
     */
    @Deprecated
    @ForRemoval
    long NAME        = 1;
    /** Used to reset the avatar field */
    long AVATAR      = 1 << 1;

    /**
     * The {@link net.dv8tion.jda.api.entities.SelfUser SelfUser} that will be
     * modified by this AccountManager.
     * <br>This represents the currently logged in account.
     *
     * @return The corresponding SelfUser
     */
    @Nonnull
    SelfUser getSelfUser();

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AccountManager.NAME | AccountManager.AVATAR);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    AccountManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(AccountManager.NAME, AccountManager.AVATAR);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    AccountManager reset(long... fields);

    /**
     * Sets the username for the currently logged in account
     *
     * @param  name
     *         The new username
     *
     * @throws IllegalArgumentException
     *         If the provided name is:
     *         <ul>
     *             <li>Equal to {@code null}</li>
     *             <li>Less than {@code 2} or more than {@code 32} characters in length</li>
     *         </ul>
     *
     * @return AccountManager for chaining convenience
     *
     * @deprecated Bot usernames are set through the application name now.
     */
    @Nonnull
    @CheckReturnValue
    @Deprecated
    @ForRemoval
    AccountManager setName(@Nonnull String name);

    /**
     * Sets the avatar for the currently logged in account
     *
     * @param  avatar
     *         An {@link net.dv8tion.jda.api.entities.Icon Icon} instance representing
     *         the new Avatar for the current account, {@code null} to reset the avatar to the default avatar.
     *
     * @return AccountManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    AccountManager setAvatar(@Nullable Icon avatar);
}
