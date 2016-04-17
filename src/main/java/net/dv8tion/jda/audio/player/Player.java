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
package net.dv8tion.jda.audio.player;

import net.dv8tion.jda.audio.AudioConnection;
import net.dv8tion.jda.audio.AudioSendHandler;
import net.dv8tion.jda.utils.SimpleLog;
import org.tritonus.dsp.ais.AmplitudeAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public abstract class Player implements AudioSendHandler
{
    protected AudioInputStream audioSource = null;
    protected AudioFormat audioFormat = null;
    protected AmplitudeAudioInputStream amplitudeAudioStream = null;

    protected float amplitude = 1.0F;

    public abstract void play();
    public abstract void pause();
    public abstract void stop();
    public abstract void restart();
//    public abstract void fastForward(long milliseconds);
//    public abstract void rewind(long milliseconds);
    public abstract boolean isStarted();
    public abstract boolean isPlaying();
    public abstract boolean isPaused();
    public abstract boolean isStopped();

    public void setAudioSource(AudioInputStream inSource)
    {
        if (inSource == null)
            throw new IllegalArgumentException("Cannot create an audio player from a null AudioInputStream!");

        if (audioSource != null)
        {
            try{
                audioSource.close();
            } catch(Exception ignored) {}
        }

        AudioFormat baseFormat = inSource.getFormat();

        //Converts first to PCM data. If the data is already PCM data, this will not change anything.
        AudioFormat toPCM = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),//AudioConnection.OPUS_SAMPLE_RATE,
                baseFormat.getSampleSizeInBits() != -1 ? baseFormat.getSampleSizeInBits() : 16,
                baseFormat.getChannels(),
                //If we are given a frame size, use it. Otherwise, assume 16 bits (2 8bit shorts) per channel.
                baseFormat.getFrameSize() != -1 ? baseFormat.getFrameSize() : 2 * baseFormat.getChannels(),
                baseFormat.getFrameRate() != -1 ? baseFormat.getFrameRate() : baseFormat.getSampleRate(),
                baseFormat.isBigEndian());
        AudioInputStream pcmStream = AudioSystem.getAudioInputStream(toPCM ,inSource);

        //Then resamples to a sample rate of 48000hz and ensures that data is Big Endian.
        audioFormat = new AudioFormat(
                toPCM.getEncoding(),
                AudioConnection.OPUS_SAMPLE_RATE,
                toPCM.getSampleSizeInBits(),
                toPCM.getChannels(),
                toPCM.getFrameSize(),
                AudioConnection.OPUS_SAMPLE_RATE,
                true);

        //Used to control volume
        amplitudeAudioStream = new AmplitudeAudioInputStream(pcmStream);
        amplitudeAudioStream.setAmplitudeLinear(amplitude);

        audioSource = AudioSystem.getAudioInputStream(audioFormat, amplitudeAudioStream);
    }

    public void setVolume(float volume)
    {
        this.amplitude = volume;
        if (amplitudeAudioStream != null)
        {
            amplitudeAudioStream.setAmplitudeLinear(amplitude);
        }
    }

    @Override
    public boolean canProvide()
    {
        return !isPaused() && !isStopped();
    }

    @Override
    public byte[] provide20MsAudio()
    {
        if (audioSource == null || audioFormat == null)
            throw new IllegalStateException("The Audio source was never set for this player!\n" +
                    "Please provide an AudioInputStream using setAudioSource.");
        try
        {
            int amountRead;
            byte[] audio = new byte[AudioConnection.OPUS_FRAME_SIZE * audioFormat.getFrameSize()];
            amountRead = audioSource.read(audio, 0, audio.length);
            if (amountRead > -1)
            {
                return audio;
            }
            else
            {
                stop();
                audioSource.close();
                return null;
            }
        }
        catch (IOException e)
        {
            SimpleLog.getLog("JDAPlayer").log(e);
        }
        return new byte[0];
    }
}
