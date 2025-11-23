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

import net.dv8tion.jda.api.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for the logged in account.
 *
 * @see Guild#getSelfMember()
 * @see SelfUser#getManager()
 */
public interface SelfMemberManager extends Manager<SelfMemberManager> {
    /** Used to reset the name field */
    long NICKNAME = 1;
    /** Used to reset the avatar field */
    long AVATAR = 1 << 1;
    /** Used to reset the banner field */
    long BANNER = 1 << 2;
    /** Used to reset the bio field */
    long BIO = 1 << 3;

    /**
     * The {@link SelfMember} that will be modified by this SelfMemberManager.
     *
     * @return The corresponding member
     */
    @Nonnull
    SelfMember getMember();

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(SelfMemberManager.NAME | SelfMemberManager.AVATAR);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NICKNAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #BANNER}</li>
     *     <li>{@link #BIO}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return SelfMemberManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    SelfMemberManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(SelfMemberManager.NAME, SelfMemberManager.AVATAR);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NICKNAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #BANNER}</li>
     *     <li>{@link #BIO}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return SelfMemberManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    SelfMemberManager reset(@Nonnull long... fields);

    /**
     * Sets the nickname for the guild member of the currently logged in account.
     *
     * <p>The nickname is a name specific to the guild, when not set, your user's name will display.
     *
     * @param  nickname
     *         The new nickname, max {@value Member#MAX_NICKNAME_LENGTH} characters in length,
     *         {@code null} to remove the nickname
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>
     *                 If the provided nickname is non-null
     *                 and is blank or more than {@value Member#MAX_NICKNAME_LENGTH} characters in length
     *             </li>
     *             <li>
     *                 If the guild member of the current logged in account does not have
     *                 the {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE NICKNAME_CHANGE} permission
     *             </li>
     *         </ul>
     *
     * @return SelfMemberManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SelfMemberManager setNickname(@Nullable String nickname);

    /**
     * Sets the avatar for the guild member of the currently logged in account.
     *
     * @param  avatar
     *         An {@link Icon} instance representing the new Avatar,
     *         {@code null} to reset the avatar to the user avatar.
     *
     * @return SelfMemberManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SelfMemberManager setAvatar(@Nullable Icon avatar);

    /**
     * Sets the banner for the guild member of the currently logged in account.
     *
     * @param  banner
     *         An {@link Icon} instance representing the new banner,
     *         {@code null} to reset the banner to the user banner.
     *
     * @return SelfMemberManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SelfMemberManager setBanner(@Nullable Icon banner);

    /**
     * Sets the bio for the guild member of the currently logged in account.
     *
     * @param  bio
     *         The new bio,
     *         {@code null} to remove the bio
     *
     * @throws IllegalArgumentException
     *         If the provided bio is non-null
     *         and is blank or more than {@value SelfMember#MAX_BIO_LENGTH} characters in length
     *
     * @return SelfMemberManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SelfMemberManager setBio(@Nullable String bio);
}
