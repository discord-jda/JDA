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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.SoundboardSoundSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Channel in which you can send soundboard sounds.
 */
public interface ISoundboardSoundChannel extends AudioChannel {
    /**
     * Sends this sound to this audio channel.
     * <br>You must be connected to this audio channel to use this method,
     * as well as be able to speak and hear.
     *
     * <p>The returned {@link RestAction} can encounter the following {@link ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The sound cannot be sent due to a permission discrepancy</li>
     *     <li>{@link ErrorResponse#CANNOT_SEND_VOICE_EFFECT CANNOT_SEND_VOICE_EFFECT}
     *     <br>The sound cannot be sent as the bot is either muted, deafened or suppressed</li>
     *     <li>{@link ErrorResponse#UNKNOWN_SOUND UNKNOWN_SOUND}
     *     <br>The sound was deleted/does not exist</li>
     * </ul>
     *
     * @param  sound
     *         The sound to send
     *
     * @throws InsufficientPermissionException
     *         If the bot does not have the following permissions:
     *         <ul>
     *             <li>{@link Permission#VOICE_SPEAK VOICE_SPEAK}, {@link Permission#VOICE_USE_SOUNDBOARD VOICE_USE_SOUNDBOARD}</li>
     *             <li>If the sound comes from other guilds, {@link Permission#VOICE_USE_EXTERNAL_SOUNDS VOICE_USE_EXTERNAL_SOUNDS} permission</li>
     *         </ul>
     * @throws IllegalArgumentException
     *         If the provided sound is {@code null}
     * @throws IllegalStateException
     *         <ul>
     *             <li>If the bot is not connected to this channel</li>
     *             <li>If the bot is deafened, muted or suppressed in this channel's guild</li>
     *         </ul>
     *
     * @return {@link RestAction} - Type: {@link Void}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> sendSoundboardSound(@Nonnull SoundboardSoundSnowflake sound, long sourceGuildId);
}
