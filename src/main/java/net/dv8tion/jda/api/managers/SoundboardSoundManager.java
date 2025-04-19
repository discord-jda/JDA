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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for a {@link SoundboardSound}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setVolume(0.33)
 *        .setEmoji(null)
 *        .queue();
 * manager.reset(SoundboardSoundManager.VOLUME | SoundboardSoundManager.EMOJI)
 *        .setVolume(1)
 *        .setEmoji(Emoji.fromUnicode("🤔"))
 *        .queue();
 * }</pre>
 *
 * @see SoundboardSound#getManager()
 */
public interface SoundboardSoundManager extends Manager<SoundboardSoundManager>
{
    /** Used to reset the name field */
    long NAME   = 1;
    /** Used to reset the volume field */
    long VOLUME = 1 << 1;
    /** Used to reset the emoji field */
    long EMOJI  = 1 << 2;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(SoundboardSoundManager.VOLUME | SoundboardSoundManager.EMOJI);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #VOLUME}</li>
     *     <li>{@link #EMOJI}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return SoundboardSoundManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    SoundboardSoundManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(SoundboardSoundManager.VOLUME | SoundboardSoundManager.EMOJI);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #VOLUME}</li>
     *     <li>{@link #EMOJI}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return SoundboardSoundManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    SoundboardSoundManager reset(long... fields);

    /**
     * The target {@link SoundboardSound} for this manager
     *
     * @return The target SoundboardSound
     */
    @Nonnull
    SoundboardSound getSoundboardSound();

    /**
     * The {@link Guild} this Manager's {@link SoundboardSound} is in.
     *
     * @return The parent {@link Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * Sets the <b><u>name</u></b> of the selected {@link SoundboardSound}.
     *
     * <p>A role name <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     *
     * @param  name
     *         The new name for the selected {@link SoundboardSound}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-32 characters long
     *
     * @return SoundboardSoundManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SoundboardSoundManager setName(@Nonnull String name);

    /**
     * Sets the <b><u>volume</u></b> of the selected {@link SoundboardSound}.
     *
     * @param  volume
     *         The new volume for the selected {@link SoundboardSound}
     *
     * @throws IllegalArgumentException
     *         If the provided volume is not between 0-1
     *
     * @return SoundboardSoundManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SoundboardSoundManager setVolume(double volume);

    /**
     * Sets the <b><u>emoji</u></b> of the selected {@link SoundboardSound}.
     *
     * @param  emoji
     *         The new emoji for the selected {@link SoundboardSound}, can be {@code null}
     *
     * @return SoundboardSoundManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SoundboardSoundManager setEmoji(@Nullable Emoji emoji);
}
