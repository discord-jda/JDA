/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.audio;

import net.dv8tion.jda.entities.User;

public class UserAudio
{
    protected User user;
    protected short[] audioData;

    public UserAudio(User user, short[] audioData)
    {
        this.user = user;
        this.audioData = audioData;
    }

    public User getUser()
    {
        return user;
    }

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
