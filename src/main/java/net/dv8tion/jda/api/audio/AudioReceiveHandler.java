/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.audio;

import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
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
    default boolean canReceiveCombined()
    {
        return false;
    }

    /**
     * If this method returns true, then JDA will provide audio data to the {@link #handleUserAudio(UserAudio)} method.
     *
     * @return If true, JDA enables subsystems to provide user specific audio data.
     */
    default boolean canReceiveUser()
    {
        return false;
    }

    /**
     * If this method returns true, then JDA will provide raw OPUS encoded packets to {@link #handleEncodedAudio(OpusPacket)}.
     * <br>This can be used in combination with the other receive methods but will not be combined audio of multiple users.
     *
     * <p>Each user sends their own stream of OPUS encoded audio and each packet is assigned with a user id and SSRC.
     * The decoder will be provided by JDA but need not be used.
     *
     * @return True, if {@link #handleEncodedAudio(OpusPacket)} should receive opus packets.
     *
     * @since  4.0.0
     */
    default boolean canReceiveEncoded()
    {
        return false;
    }

    /**
     * If {@link #canReceiveEncoded()} returns true, JDA will provide raw {@link net.dv8tion.jda.api.audio.OpusPacket OpusPackets}
     * to this method <b>every 20 milliseconds</b>. These packets are for specific users rather than a combined packet
     * of all users like {@link #handleCombinedAudio(CombinedAudio)}.
     *
     * <p>This is useful for systems that want to either do lazy decoding of audio through {@link net.dv8tion.jda.api.audio.OpusPacket#getAudioData(double)}
     * or for systems that can decode and transform the audio data manually without JDA involvement.
     *
     * @param packet
     *        The {@link net.dv8tion.jda.api.audio.OpusPacket}
     *
     * @since  4.0.0
     */
    default void handleEncodedAudio(@Nonnull OpusPacket packet) {}

    /**
     * If {@link #canReceiveCombined()} returns true, JDA will provide a {@link net.dv8tion.jda.api.audio.CombinedAudio CombinedAudio}
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
     * <br>and is defined by: {@link net.dv8tion.jda.api.audio.AudioReceiveHandler#OUTPUT_FORMAT AudioRecieveHandler.OUTPUT_FORMAT}
     *
     * @param  combinedAudio
     *         The combined audio data.
     */
    default void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {}

    /**
     * If {@link #canReceiveUser()} returns true, JDA will provide a {@link net.dv8tion.jda.api.audio.UserAudio UserAudio}
     * object to this method <b>every time the user speaks.</b> Continuing with the last statement: This method is only fired
     * when discord provides us audio data which is very different from the scheduled firing time of
     * {@link #handleCombinedAudio(CombinedAudio)}.
     * <p>
     * The {@link net.dv8tion.jda.api.audio.UserAudio UserAudio} object provided to this method will contain the
     * {@link net.dv8tion.jda.api.entities.User User} that spoke along with <b>only</b> the audio data sent by the specific user.
     * <p>
     * The main use of this method is for listening to specific users. Whether that is for audio recording,
     * custom mixing (possibly for user muting), or even voice recognition, this is the method you will want.
     * <p>
     * If you are wanting to do audio recording, please consider {@link #handleCombinedAudio(CombinedAudio)} as it was created
     * just for that reason.
     * <p>
     * Output audio format: 48KHz 16bit stereo signed BigEndian PCM
     * <br>and is defined by: {@link net.dv8tion.jda.api.audio.AudioReceiveHandler#OUTPUT_FORMAT AudioRecieveHandler.OUTPUT_FORMAT}
     *
     * @param  userAudio
     *         The user audio data
     */
    default void handleUserAudio(@Nonnull UserAudio userAudio) {}

    /**
     * This method is a filter predicate used by JDA to determine whether or not to include a
     * {@link net.dv8tion.jda.api.entities.User User}'s audio when creating a CombinedAudio packet.
     * <p>
     * This method is especially useful in creating whitelist / blacklist functionality for receiving audio.
     * <p>
     * A few possible examples:
     * <ul>
     *  <li>Have this method always return false for Users that are bots.</li>
     *  <li>Have this method return false for users who have been placed on a blacklist for abusing the bot's functionality.</li>
     *  <li>Have this method only return true if the user is in a special whitelist of power users.</li>
     * </ul>
     * @param  user
     *         The user whose audio was received
     *
     * @return If true, JDA will include the user's audio when merging audio sources when created packets
     *         for {@link #handleCombinedAudio(CombinedAudio)}
     */
    default boolean includeUserInCombinedAudio(@Nonnull User user)
    {
        return true;
    }
}
