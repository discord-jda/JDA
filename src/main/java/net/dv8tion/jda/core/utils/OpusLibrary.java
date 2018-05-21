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

package net.dv8tion.jda.core.utils;

import club.minnced.opus.util.NativeUtil;
import com.sun.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OpusLibrary
{
    private static final Logger LOG = LoggerFactory.getLogger(OpusLibrary.class);
    private static boolean initialized = false;
    private static boolean audioSupported = false;

    public static synchronized boolean isInitialized()
    {
        return initialized;
    }

    public static synchronized boolean isAudioSupported()
    {
        return audioSupported;
    }

    public static synchronized boolean ensureOpus()
    {
        if(initialized)
            return audioSupported;
        initialized = true;
        String nativesRoot  = null;
        try
        {
            //The libraries that this is referencing are available in the src/main/resources/opus/ folder.
            //Of course, when JDA is compiled that just becomes /opus/
            nativesRoot = "/natives/" + Platform.RESOURCE_PREFIX + "/%s";
            if (nativesRoot.contains("darwin")) //Mac
                nativesRoot += ".dylib";
            else if (nativesRoot.contains("win"))
                nativesRoot += ".dll";
            else if (nativesRoot.contains("linux"))
                nativesRoot += ".so";
            else
                throw new UnsupportedOperationException();

            NativeUtil.loadLibraryFromJar(String.format(nativesRoot, "libopus"));
        }
        catch (Throwable e)
        {
            if (e instanceof UnsupportedOperationException)
            {
                LOG.error("Sorry, JDA's audio system doesn't support this system.\n" +
                                  "Supported Systems: Windows(x86, x64), Mac(x86, x64) and Linux(x86, x64)\n" +
                                  "Operating system: " + Platform.RESOURCE_PREFIX);
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
            else
            {
                LOG.error("An unknown error occurred while attempting to setup JDA's audio system!", e);
            }

            nativesRoot = null;
        }
        finally
        {
            System.setProperty("opus.lib", nativesRoot != null ? String.format(nativesRoot, "libopus") : "");
            audioSupported = nativesRoot != null;

            if (audioSupported)
                LOG.info("Audio System successfully setup!");
            else
                LOG.info("Audio System encountered problems while loading, thus, is disabled.");
            return audioSupported;
        }
    }
}
