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

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Manager providing functionality to update one or more fields for an {@link net.dv8tion.jda.api.entities.Emote Emote}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("minn")
 *        .setRoles(null)
 *        .queue();
 * manager.reset(EmoteManager.NAME | EmoteManager.ROLES)
 *        .setName("dv8")
 *        .setRoles(roles)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.api.entities.Emote#getManager()
 */
public interface EmoteManager extends Manager<EmoteManager>
{
    /** Used to reset the name field */
    long NAME  = 1;
    /** Used to reset the roles field */
    long ROLES = 1 << 1;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(EmoteManager.NAME | EmoteManager.ROLES);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ROLES}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return EmoteManager for chaining convenience
     */
    @Nonnull
    @Override
    EmoteManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(EmoteManager.NAME, EmoteManager.ROLES);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ROLES}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return EmoteManager for chaining convenience
     */
    @Nonnull
    @Override
    EmoteManager reset(long... fields);

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.api.entities.Emote Emote} is in.
     * <br>This is logically the same as calling {@code getEmote().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    default Guild getGuild()
    {
        return getEmote().getGuild();
    }

    /**
     * The target {@link net.dv8tion.jda.api.entities.Emote Emote}
     * that will be modified by this Manager
     *
     * @return The target Emote
     */
    @Nonnull
    Emote getEmote();

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.api.entities.Emote Emote}.
     *
     * <p>An emote name <b>must</b> be between 2-32 characters long!
     * <br>Emote names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code tatDab} or {@code fmgSUP}
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.api.entities.Emote Emote}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-32 characters long
     *
     * @return EmoteManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    EmoteManager setName(@Nonnull String name);

    /**
     * Sets the <b><u>restriction roles</u></b> of the selected {@link net.dv8tion.jda.api.entities.Emote Emote}.
     * <br>If these are empty the Emote will be available to everyone otherwise only available to the specified roles.
     *
     * <p>An emote's restriction roles <b>must not</b> contain {@code null}!
     *
     * @param  roles
     *         The new set of {@link net.dv8tion.jda.api.entities.Role Roles} for the selected {@link net.dv8tion.jda.api.entities.Emote Emote}
     *         to be restricted to, or {@code null} to clear the roles
     *
     * @throws IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return EmoteManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    EmoteManager setRoles(@Nullable Set<Role> roles);
}
