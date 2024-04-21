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
package net.dv8tion.jda.api.audio

import net.dv8tion.jda.api.entities.User
import javax.annotation.Nonnull

/**
 * Represents a packet of User specific audio.
 */
class UserAudio(
    /**
     * The [User][net.dv8tion.jda.api.entities.User] that provided the audio data.
     *
     * @return Never-null [User][net.dv8tion.jda.api.entities.User] object.
     */
    @get:Nonnull
    @param:Nonnull var user: User, @param:Nonnull protected var audioData: ShortArray
) {
    /**
     * Provides 20 Milliseconds of combined audio data in 48KHz 16bit stereo signed BigEndian PCM.
     * <br></br>Format defined by: [AudioReceiveHandler.OUTPUT_FORMAT][net.dv8tion.jda.api.audio.AudioReceiveHandler.OUTPUT_FORMAT].
     *
     *
     * The output volume of the data can be modified by the provided `` `volume` `` parameter. `` `1.0` `` is considered to be 100% volume.
     * <br></br>Going above `` `1.0` `` can increase the volume further, but you run the risk of audio distortion.
     *
     * @param  volume
     * Value used to modify the "volume" of the returned audio data. 1.0 is normal volume.
     *
     * @return Never-null byte array of PCM data defined by [AudioReceiveHandler.OUTPUT_FORMAT][net.dv8tion.jda.api.audio.AudioReceiveHandler.OUTPUT_FORMAT]
     */
    @Nonnull
    fun getAudioData(volume: Double): ByteArray {
        return OpusPacket.Companion.getAudioData(audioData, volume)
    }
}
