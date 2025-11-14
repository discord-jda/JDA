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

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Manager providing functionality to update the name field for an {@link ApplicationEmoji}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setName("minn")
 *        .queue();
 * }
 *
 * @see ApplicationEmoji#getManager()
 */
public interface ApplicationEmojiManager extends Manager<ApplicationEmojiManager> {
    /** Used to reset the name field */
    long NAME = 1;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ApplicationEmojiManager for chaining convenience.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    ApplicationEmojiManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ApplicationEmojiManager for chaining convenience.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    ApplicationEmojiManager reset(@Nonnull long... fields);

    /**
     * The target {@link ApplicationEmoji} that will be modified by this Manager
     *
     * @return The target emoji
     */
    @Nonnull
    ApplicationEmoji getEmoji();

    /**
     * Sets the <b><u>name</u></b> of the selected {@link ApplicationEmoji}.
     *
     * <p>An emoji name <b>must</b> be between 2-{@value CustomEmoji#EMOJI_NAME_MAX_LENGTH} characters long!
     * <br>Emoji names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code tatDab} or {@code fmgSUP}
     *
     * @param  name
     *         The new name for the selected {@link ApplicationEmoji}
     *
     * @throws IllegalArgumentException
     *         If the provided name is null, not alphanumeric, or not between 2 and {@value CustomEmoji#EMOJI_NAME_MAX_LENGTH} characters long
     *
     * @return ApplicationEmojiManager for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    ApplicationEmojiManager setName(@Nonnull String name);
}
