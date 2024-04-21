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
import javax.sound.sampled.AudioFormat

/**
 * Interface used to receive audio from Discord through JDA.
 */
interface AudioReceiveHandler {
    /**
     * If this method returns true, then JDA will generate combined audio data and provide it to the handler.
     * <br></br>**Only enable if you specifically want combined audio because combining audio is costly if unused.**
     *
     * @return If true, JDA enables subsystems to combine all user audio into a single provided data packet.
     */
    fun canReceiveCombined(): Boolean {
        return false
    }

    /**
     * If this method returns true, then JDA will provide audio data to the [.handleUserAudio] method.
     *
     * @return If true, JDA enables subsystems to provide user specific audio data.
     */
    fun canReceiveUser(): Boolean {
        return false
    }

    /**
     * If this method returns true, then JDA will provide raw OPUS encoded packets to [.handleEncodedAudio].
     * <br></br>This can be used in combination with the other receive methods but will not be combined audio of multiple users.
     *
     *
     * Each user sends their own stream of OPUS encoded audio and each packet is assigned with a user id and SSRC.
     * The decoder will be provided by JDA but need not be used.
     *
     * @return True, if [.handleEncodedAudio] should receive opus packets.
     *
     * @since  4.0.0
     */
    fun canReceiveEncoded(): Boolean {
        return false
    }

    /**
     * If [.canReceiveEncoded] returns true, JDA will provide raw [OpusPackets][net.dv8tion.jda.api.audio.OpusPacket]
     * to this method **every 20 milliseconds**. These packets are for specific users rather than a combined packet
     * of all users like [.handleCombinedAudio].
     *
     *
     * This is useful for systems that want to either do lazy decoding of audio through [net.dv8tion.jda.api.audio.OpusPacket.getAudioData]
     * or for systems that can decode and transform the audio data manually without JDA involvement.
     *
     * @param packet
     * The [net.dv8tion.jda.api.audio.OpusPacket]
     *
     * @since  4.0.0
     */
    fun handleEncodedAudio(@Nonnull packet: OpusPacket?) {}

    /**
     * If [.canReceiveCombined] returns true, JDA will provide a [CombinedAudio][net.dv8tion.jda.api.audio.CombinedAudio]
     * object to this method **every 20 milliseconds**. The data provided by CombinedAudio is all audio that occurred
     * during the 20 millisecond period mixed together into a single 20 millisecond packet. If no users spoke, this method
     * will still be provided with a CombinedAudio object containing 20 milliseconds of silence and
     * [CombinedAudio.getUsers]'s list will be empty.
     *
     *
     * The main use of this method is if you are wanting to record audio. Because it automatically combines audio and
     * maintains timeline (no gaps in audio due to silence) it is an incredible resource for audio recording.
     *
     *
     * If you are wanting to do audio processing (voice recognition) or you only want to deal with a single user's audio,
     * please consider [.handleUserAudio].
     *
     *
     * Output audio format: 48KHz 16bit stereo signed BigEndian PCM
     * <br></br>and is defined by: [AudioRecieveHandler.OUTPUT_FORMAT][net.dv8tion.jda.api.audio.AudioReceiveHandler.OUTPUT_FORMAT]
     *
     * @param  combinedAudio
     * The combined audio data.
     */
    fun handleCombinedAudio(@Nonnull combinedAudio: CombinedAudio?) {}

    /**
     * If [.canReceiveUser] returns true, JDA will provide a [UserAudio][net.dv8tion.jda.api.audio.UserAudio]
     * object to this method **every time the user speaks.** Continuing with the last statement: This method is only fired
     * when discord provides us audio data which is very different from the scheduled firing time of
     * [.handleCombinedAudio].
     *
     *
     * The [UserAudio][net.dv8tion.jda.api.audio.UserAudio] object provided to this method will contain the
     * [User][net.dv8tion.jda.api.entities.User] that spoke along with **only** the audio data sent by the specific user.
     *
     *
     * The main use of this method is for listening to specific users. Whether that is for audio recording,
     * custom mixing (possibly for user muting), or even voice recognition, this is the method you will want.
     *
     *
     * If you are wanting to do audio recording, please consider [.handleCombinedAudio] as it was created
     * just for that reason.
     *
     *
     * Output audio format: 48KHz 16bit stereo signed BigEndian PCM
     * <br></br>and is defined by: [AudioRecieveHandler.OUTPUT_FORMAT][net.dv8tion.jda.api.audio.AudioReceiveHandler.OUTPUT_FORMAT]
     *
     * @param  userAudio
     * The user audio data
     */
    fun handleUserAudio(@Nonnull userAudio: UserAudio?) {}

    /**
     * This method is a filter predicate used by JDA to determine whether or not to include a
     * [User][net.dv8tion.jda.api.entities.User]'s audio when creating a CombinedAudio packet.
     *
     *
     * This method is especially useful in creating whitelist / blacklist functionality for receiving audio.
     *
     *
     * A few possible examples:
     *
     *  * Have this method always return false for Users that are bots.
     *  * Have this method return false for users who have been placed on a blacklist for abusing the bot's functionality.
     *  * Have this method only return true if the user is in a special whitelist of power users.
     *
     * @param  user
     * The user whose audio was received
     *
     * @return If true, JDA will include the user's audio when merging audio sources when created packets
     * for [.handleCombinedAudio]
     */
    fun includeUserInCombinedAudio(@Nonnull user: User?): Boolean {
        return true
    }

    companion object {
        /**
         * Audio Output Format used by JDA. 48KHz 16bit stereo signed BigEndian PCM.
         */
        val OUTPUT_FORMAT = AudioFormat(48000.0f, 16, 2, true, true)
    }
}
