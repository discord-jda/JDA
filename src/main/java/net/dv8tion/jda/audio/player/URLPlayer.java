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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.requests.Requester;
import org.apache.http.HttpHost;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Austin on 1/24/2016.
 */
public class URLPlayer extends Player
{
    protected final JDA api;
    protected String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 " + Requester.USER_AGENT;
    protected URL urlOfResource = null;
    protected InputStream resourceStream = null;
    protected BufferedInputStream bufferedResourceStream = null;
    protected boolean started = false;
    protected boolean playing = false;
    protected boolean paused = false;
    protected boolean stopped = true;

    public URLPlayer(JDA api)
    {
        this.api = api;
    }
    public URLPlayer(JDA api, URL urlOfResource) throws IOException, UnsupportedAudioFileException
    {
        this.api = api;
        setAudioUrl(urlOfResource);
    }

    public void setAudioUrl(URL urlOfResource) throws IOException, UnsupportedAudioFileException
    {
        if (urlOfResource == null)
            throw new IllegalArgumentException("A null URL was provided to the Player! Cannot find resource to play from a null URL!");

        this.urlOfResource = urlOfResource;
        URLConnection conn = null;
        HttpHost jdaProxy = api.getGlobalProxy();
        if (jdaProxy != null)
        {
            InetSocketAddress proxyAddress = new InetSocketAddress(jdaProxy.getHostName(), jdaProxy.getPort());
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
            conn = urlOfResource.openConnection(proxy);
        }
        else
        {
            conn = urlOfResource.openConnection();
        }
        if (conn == null)
            throw new IllegalArgumentException("The provided URL resulted in a null URLConnection! Does the resource exist?");

        conn.setRequestProperty("user-agent", userAgent);
        this.resourceStream = conn.getInputStream();
        bufferedResourceStream=new BufferedInputStream(resourceStream);
        setAudioSource(AudioSystem.getAudioInputStream(bufferedResourceStream));
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
        try
        {
            resourceStream.close();//FIXME
            bufferedResourceStream.close();
        }
        catch (IOException e)
        {
            System.err.println("Attempted to close the URLPlayer resource stream during stop() cleanup, but hit an IOException");
            e.printStackTrace();
        }
    }

    @Override
    public void restart()
    {
        URL oldUrl = urlOfResource;
        try
        {
            resourceStream.close();//FIXME
            bufferedResourceStream.close();
            reset();
            setAudioUrl(oldUrl);
            play();
        }
        catch (IOException e)
        {
            System.err.println("Attempted to restart the URLStream playback, but something went wrong!");
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
        started = false;
        playing = false;
        paused = false;
        stopped = true;
        urlOfResource = null;
        resourceStream = null;
    }
}
