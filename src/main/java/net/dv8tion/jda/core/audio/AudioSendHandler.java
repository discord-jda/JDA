/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.audio;

import javax.sound.sampled.AudioFormat;

/**
 * Interface used to send audio to Discord through JDA.
 */
public interface AudioSendHandler
{
    /**
     * Audio Input Format expected by JDA if {@link #isOpus()} returns false. 48KHz 16bit stereo signed BigEndian PCM.
     */
    AudioFormat INPUT_FORMAT = new AudioFormat(48000f, 16, 2, true, true);

    /**
     * If this method returns true JDA will attempt to retrieve audio data from this handler by calling
     * {@link #provide20MsAudio()}. The return value is checked each time JDA attempts send audio, so if
     * the developer wanted to start and stop sending audio it could be done by changing the value returned
     * by this method at runtime.
     *
     * @return If true, JDA will attempt to retrieve audio data from {@link #provide20MsAudio()}
     */
    boolean canProvide();

    /**
     * If {@link #canProvide()} returns true JDA will call this method in an attempt to retrieve audio data from the
     * handler. This method need to provide 20 Milliseconds of audio data as a byte array.
     * <p>
     * Considering this system needs to be low-latency / high-speed, it is recommended that the loading of audio data
     * be done before hand or in parallel and not loaded from disk when this method is called by JDA. Attempting to load
     * all audio data from disk when this method is called will most likely cause issues due to IO blocking this thread.
     * <p>
     * The provided audio data needs to be in the format: 48KHz 16bit stereo signed BigEndian PCM.
     * <br>Defined by: {@link net.dv8tion.jda.core.audio.AudioSendHandler#INPUT_FORMAT AudioSendHandler.INPUT_FORMAT}.
     * <br>If {@link #isOpus()} is set to return true, then it should be in pre-encoded Opus format instead.
     *
     * @return Should return a byte[] containing 20 Milliseconds of audio.
     */
    byte[] provide20MsAudio();

    /**
     * If this method returns true JDA will treat the audio data provided by {@link #provide20MsAudio()} as a pre-encoded
     * 20 Millisecond packet of Opus audio. This means that JDA <b>WILL NOT</b> attempt to encode the audio as Opus, but
     * will provide it to Discord <b>exactly as it is given</b>.
     *
     * @return If true, JDA will not attempt to encode the provided audio data as Opus.
     *         <br>Default - False.
     */
    default boolean isOpus()
    {
        return false;
    }
}
