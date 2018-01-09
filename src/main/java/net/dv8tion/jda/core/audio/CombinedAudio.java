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

import net.dv8tion.jda.core.entities.User;

import java.util.Collections;
import java.util.List;

/**
 * Represents a packet of combined audio data from 0 to n Users.
 */
public class CombinedAudio
{
    protected List<User> users;
    protected short[] audioData;

    public CombinedAudio(List<User> users, short[] audioData)
    {
        this.users = Collections.unmodifiableList(users);
        this.audioData = audioData;
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.User Users} that provided audio that was combined.
     * <br>Basically: This is a list of all users that can be heard in the data returned by {@link #getAudioData(double)}
     * <p>
     * <b>NOTE: If no users were speaking, this list is empty and {@link #getAudioData(double)} provides silent audio data.</b>
     *
     * @return Never-null list of all users that provided audio.
     */
    public List<User> getUsers()
    {
        return users;
    }

    /**
     * Provides 20 Milliseconds of combined audio data in 48KHz 16bit stereo signed BigEndian PCM.
     * <br>Format defined by: {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#OUTPUT_FORMAT AudioReceiveHandler.OUTPUT_FORMAT}.
     * <p>
     * The output volume of the data can be modified by the provided {@code `volume`} parameter. {@code `1.0`} is considered to be 100% volume.
     * <br>Going above `{@code 1.0`} can increase the volume further, but you run the risk of audio distortion.
     * <p>
     * <b>NOTE: If no users were speaking, this provides silent audio and {@link #getUsers()} returns an empty list!</b>
     *
     * @param  volume
     *         Value used to modify the "volume" of the returned audio data. 1.0 is normal volume.
     *
     * @return Never-null byte array of PCM data defined by {@link net.dv8tion.jda.core.audio.AudioReceiveHandler#OUTPUT_FORMAT AudioReceiveHandler.OUTPUT_FORMAT}
     */
    public byte[] getAudioData(double volume)
    {
        short s;
        int byteIndex = 0;
        byte[] audio = new byte[audioData.length * 2];
        for (int i = 0; i < audioData.length; i++)
        {
            s = audioData[i];
            if (volume != 1.0)
                s = (short) (s * volume);

            byte leftByte = (byte) ((0x000000FF) & (s >> 8));
            byte rightByte =  (byte) (0x000000FF & s);
            audio[byteIndex] = leftByte;
            audio[byteIndex + 1] = rightByte;
            byteIndex += 2;
        }
        return audio;
    }
}
