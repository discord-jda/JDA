/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FilePlayer extends Player
{
    private File audioFile = null;
    private boolean started = false;
    private boolean playing = false;
    private boolean paused = false;
    private boolean stopped = true;

    public FilePlayer() {}
    public FilePlayer(File file) throws IOException, UnsupportedAudioFileException
    {
        setAudioFile(file);
    }

    public void setAudioFile(File file) throws IOException, UnsupportedAudioFileException
    {
        if (file == null)
            throw new IllegalArgumentException("A null File was provided to the FilePlayer! Cannot play a null file!");
        if (!file.exists())
            throw new IllegalArgumentException("A non-existent file was provided to the FilePlayer! Cannot play a file that doesn't exist!");

        reset();
        audioFile = file;
        setAudioSource(AudioSystem.getAudioInputStream(file));
    }

    @Override
    public void play()
    {
        if (started && stopped)
            throw new IllegalStateException("Cannot start a player after it has been stopped.\n" +
                    "Please use the restart method or load a new file.");
        started = true;
        playing = true;
        paused = false;
        stopped = false;
    }

    @Override
    public void pause()
    {
        playing = false;
        paused = true;
    }

    @Override
    public void stop()
    {
        playing = false;
        paused = false;
        stopped = true;
    }

    @Override
    public void restart()
    {
        try
        {
            File oldFile = audioFile;
            reset();
            setAudioFile(oldFile);
            play();
        }
        catch (IOException e)
        {
            if (!audioFile.exists())
                System.err.println("Attempted to restart the FilePlayer playback, but the provided file no longer exists!");
            e.printStackTrace();
        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isStarted()
    {
        return started;
    }

    @Override
    public boolean isPlaying()
    {
        return playing;
    }

    @Override
    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public boolean isStopped()
    {
        return stopped;
    }

    protected void reset()
    {
        audioFile = null;
        started = false;
        playing = false;
        paused = false;
        stopped = true;
    }
}
