/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
 * Interface used to receive audio from Discord through JDA.
 */
public interface AudioReceiveHandler
{
    /**
     * Audio Output Format used by JDA. 48KHz 16bit stereo signed BigEndian PCM.
     */
    AudioFormat OUTPUT_FORMAT = new AudioFormat(48000.0f, 16, 2, true, true);

    /**
     * If this method returns true, then JDA will generate combined audio data and provide it to the handler.
     * <br><b>Only enable if you specifically want combined audio because combining audio is costly if unused.</b>
     *
     * @return If true, JDA enables subsystems to combine all user audio into a single provided data packet.
     */
    boolean canReceiveCombined();

    /**
     * If this method returns true, then JDA will provide audio data to the {@link #handleUserAudio(UserAudio)} method.
     *
     * @return If true, JDA enables subsystems to provide user specific audio data.
     */
    boolean canReceiveUser();

    /**
     * If {@link #canReceiveCombined()} returns true, JDA will provide a {@link net.dv8tion.jda.core.audio.CombinedAudio CombinedAudio}
     * object to this method <b>every 20 milliseconds</b>. The data provided by CombinedAudio is all audio that occurred
     * during the 20 millisecond period mixed together into a single 20 millisecond packet. If no users spoke, this method
     * will still be provided with a CombinedAudio object containing 20 milliseconds of silence and
     * {@link CombinedAudio#getUsers()}'s list will be empty.
     * <p>
     * The main use of this method is if you are wanting to record audio. Because it automatically combines audio and
     * maintains timeline (no gaps in audio due to silence) it is an incredible resource for audio recording.
     * <p>
     * If you are wanting to do audio processing (voice recognition) or you only want to deal with a single user's audio,
     * please consider {@link #handleUserAudio(UserAudio)}.
     * <p>
     * Output audio format: 48KHz 16bit stereo signed BigEndian PCM
     * <br>and is defined by: {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#OUTPUT_FORMAT AudioRecieveHandler.OUTPUT_FORMAT}
     *
     * @param  combinedAudio
     *         The combined audio data.
     */
    void handleCombinedAudio(CombinedAudio combinedAudio);

    /**
     * If {@link #canReceiveUser()} returns true, JDA will provide a {@link net.dv8tion.jda.core.audio.UserAudio UserAudio}
     * object to this method <b>every time the user speaks.</b> Continuing with the last statement: This method is only fired
     * when discord provides us audio data which is very different from the scheduled firing time of
     * {@link #handleCombinedAudio(CombinedAudio)}.
     * <p>
     * The {@link net.dv8tion.jda.core.audio.UserAudio UserAudio} object provided to this method will contain the
     * {@link net.dv8tion.jda.core.entities.User User} that spoke along with <b>only</b> the audio data sent by the specific user.
     * <p>
     * The main use of this method is for listening to specific users. Whether that is for audio recording,
     * custom mixing (possibly for user muting), or even voice recognition, this is the method you will want.
     * <p>
     * If you are wanting to do audio recording, please consider {@link #handleCombinedAudio(CombinedAudio)} as it was created
     * just for that reason.
     * <p>
     * Output audio format: 48KHz 16bit stereo signed BigEndian PCM
     * <br>and is defined by: {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#OUTPUT_FORMAT AudioRecieveHandler.OUTPUT_FORMAT}
     *
     * @param  userAudio
     *         The user audio data
     */
    void handleUserAudio(UserAudio userAudio);
}
