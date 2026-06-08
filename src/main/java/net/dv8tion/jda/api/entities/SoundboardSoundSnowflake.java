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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.SoundboardSoundSnowflakeImpl;

import javax.annotation.Nonnull;

/**
 * Represents a soundboard sound's snowflake ID.
 *
 * @see SoundboardSound
 */
public interface SoundboardSoundSnowflake extends ISnowflake {
    /**
     * Creates a SoundboardSound instance which only wraps an ID.
     *
     * @param  id
     *         The soundboard sound id
     *
     * @return A soundboard sound snowflake instance
     *
     * @see JDA#retrieveDefaultSoundboardSounds()
     * @see Guild#getSoundboardSounds()
     * @see Guild#retrieveSoundboardSounds()
     */
    @Nonnull
    static SoundboardSoundSnowflake fromId(long id) {
        return new SoundboardSoundSnowflakeImpl(id);
    }

    /**
     * Creates a SoundboardSound instance which only wraps an ID.
     *
     * @param  id
     *         The soundboard sound id
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not a valid snowflake
     *
     * @return A soundboard sound snowflake instance
     *
     * @see JDA#retrieveDefaultSoundboardSounds()
     * @see Guild#getSoundboardSounds()
     * @see Guild#retrieveSoundboardSounds()
     */
    @Nonnull
    static SoundboardSoundSnowflake fromId(@Nonnull String id) {
        return fromId(MiscUtil.parseSnowflake(id));
    }

    /**
     * Returns the URL to the sound asset.
     *
     * <p>The format used may be MP3 or Ogg.
     *
     * @return A String representing this sound's asset
     */
    @Nonnull
    default String getUrl() {
        return String.format("https://cdn.discordapp.com/soundboard-sounds/%s", getId());
    }
}
