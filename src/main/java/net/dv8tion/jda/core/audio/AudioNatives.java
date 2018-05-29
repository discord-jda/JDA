/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.audio;

import club.minnced.opus.util.OpusLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class AudioNatives
{
    private static final Logger LOG = LoggerFactory.getLogger(AudioNatives.class);
    private static boolean initialized;
    private static boolean audioSupported;

    private AudioNatives() {}

    public static synchronized boolean loadFrom(String absolutePath)
    {
        if (initialized)
            return false;
        initialized = true;
        System.load(absolutePath);
        System.setProperty("opus.lib", absolutePath);
        return audioSupported = true;
    }

    public static synchronized boolean ensureOpus()
    {
        if (initialized)
            return audioSupported;
        initialized = true;
        try
        {
            if (OpusLibrary.isInitialized())
                return audioSupported = true;
            audioSupported = OpusLibrary.loadFromJar();
        }
        catch (Throwable e)
        {
            handleException(e);
        }
        finally
        {
            if (audioSupported)
                LOG.info("Audio System successfully setup!");
            else
                LOG.info("Audio System encountered problems while loading, thus, is disabled.");
        }
        return audioSupported;
    }

    private static void handleException(Throwable e)
    {
        if (e instanceof UnsupportedOperationException)
        {
            LOG.error("Sorry, JDA's audio system doesn't support this system.\n{}", e.getMessage());
        }
        else if (e instanceof NoClassDefFoundError)
        {
            LOG.error("Missing opus dependency, unable to initialize audio!");
        }
        else if (e instanceof IOException)
        {
            LOG.error("There was an IO Exception when setting up the temp files for audio.", e);
        }
        else if (e instanceof UnsatisfiedLinkError)
        {
            LOG.error("JDA encountered a problem when attempting to load the Native libraries. Contact a DEV.", e);
        }
        else if (e instanceof Error)
        {
            throw (Error) e;
        }
        else
        {
            LOG.error("An unknown exception occurred while attempting to setup JDA's audio system!", e);
        }
    }
}
