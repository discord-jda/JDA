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

import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat

/**
 * Interface used to send audio to Discord through JDA.
 */
interface AudioSendHandler {
    /**
     * If this method returns true JDA will attempt to retrieve audio data from this handler by calling
     * [.provide20MsAudio]. The return value is checked each time JDA attempts send audio, so if
     * the developer wanted to start and stop sending audio it could be done by changing the value returned
     * by this method at runtime.
     *
     * @return If true, JDA will attempt to retrieve audio data from [.provide20MsAudio]
     */
    fun canProvide(): Boolean

    /**
     * If [.canProvide] returns true JDA will call this method in an attempt to retrieve audio data from the
     * handler. This method need to provide 20 Milliseconds of audio data as a **array-backed** [java.nio.ByteBuffer].
     * Use either [java.nio.ByteBuffer.allocate] or [java.nio.ByteBuffer.wrap].
     *
     *
     * Considering this system needs to be low-latency / high-speed, it is recommended that the loading of audio data
     * be done before hand or in parallel and not loaded from disk when this method is called by JDA. Attempting to load
     * all audio data from disk when this method is called will most likely cause issues due to IO blocking this thread.
     *
     *
     * The provided audio data needs to be in the format: 48KHz 16bit stereo signed BigEndian PCM.
     * <br></br>Defined by: [AudioSendHandler.INPUT_FORMAT][net.dv8tion.jda.api.audio.AudioSendHandler.INPUT_FORMAT].
     * <br></br>If [.isOpus] is set to return true, then it should be in pre-encoded Opus format instead.
     *
     * @return Should return a [java.nio.ByteBuffer] containing 20 Milliseconds of audio.
     *
     * @see .isOpus
     * @see .canProvide
     * @see java.nio.ByteBuffer.allocate
     * @see java.nio.ByteBuffer.wrap
     */
    fun provide20MsAudio(): ByteBuffer?
    val isOpus: Boolean
        /**
         * If this method returns true JDA will treat the audio data provided by [.provide20MsAudio] as a pre-encoded
         * 20 Millisecond packet of Opus audio. This means that JDA **WILL NOT** attempt to encode the audio as Opus, but
         * will provide it to Discord **exactly as it is given**.
         *
         * @return If true, JDA will not attempt to encode the provided audio data as Opus.
         * <br></br>Default - False.
         */
        get() = false

    companion object {
        /**
         * Audio Input Format expected by JDA if [.isOpus] returns false. 48KHz 16bit stereo signed BigEndian PCM.
         */
        val INPUT_FORMAT = AudioFormat(48000f, 16, 2, true, true)
    }
}
