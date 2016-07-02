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

import net.dv8tion.jda.utils.SimpleLog;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * <p>This implementation of an {@link net.dv8tion.jda.audio.AudioSendHandler AudioSendHandler} is able to send audio to a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} from a local file.
 * <br/>For that the user has to provide a valid {@link java.io.File File} in a supported audio format.<br/>
 * <p><b>To use external files that are uploaded to a service use: {@link URLPlayer URLPlayer}</b></p>
 * </p>
 */
public class FilePlayer extends Player
{
    private File audioFile = null;
    private boolean started = false;
    private boolean playing = false;
    private boolean paused = false;
    private boolean stopped = true;

	/**
     * Creates a new instance of a {@link FilePlayer}.
     * <p>To directly set a source file: </br>
     * <pre><code>   new {@link #FilePlayer(File)}</code></pre></p>
     */
    public FilePlayer() {}

	/**
	 * Creates a new instance of a {@link FilePlayer} and sets the given File as audio source.
     * @param file
     *          An audio file to use as audio source.
     * @throws IOException
     *          If the file is not available.
     * @throws UnsupportedAudioFileException
     *          If the file is not supported by the player.
     */
    public FilePlayer(File file) throws IOException, UnsupportedAudioFileException
    {
        setAudioFile(file);
    }

	/**
     * Sets the given file as the player's audio source if and only if it exists.
     * @param file
     *          An audio file to use as audio source.
     * @throws IOException
     *          If the file is not available.
     * @throws UnsupportedAudioFileException
     *          If the file is not supported by the player.
     */
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
                SimpleLog.getLog("JDAPlayer").fatal("Attempted to restart the FilePlayer playback, but the provided file no longer exists!");
            SimpleLog.getLog("JDAPlayer").log(e);
        }
        catch (UnsupportedAudioFileException e)
        {
            SimpleLog.getLog("JDAPlayer").log(e);
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
