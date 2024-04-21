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

import club.minnced.opus.util.OpusLibrary
import net.dv8tion.jda.internal.utils.JDALogger
import java.io.IOException

/**
 * Controller used by JDA to ensure the native
 * binaries for opus en-/decoding are available.
 *
 * @see [opus-java source](https://github.com/discord-java/opus-java)
 */
object AudioNatives {
    private val LOG = JDALogger.getLog(AudioNatives::class.java)

    /**
     * Whether this class was already initialized or not.
     *
     * @return True, if this class was already initialized.
     *
     * @see .ensureOpus
     */
    var isInitialized = false
        private set

    /**
     * Whether the opus library is loaded or not.
     * <br></br>This is initialized by the first call to [.ensureOpus].
     *
     * @return True, opus library is loaded.
     */
    var isAudioSupported = false
        private set

    /**
     * Checks whether the opus binary was loaded, if not it will be initialized here.
     * <br></br>This is used by JDA to check at runtime whether the opus library is available or not.
     *
     * @return True, if the library could be loaded.
     */
    @JvmStatic
    @Synchronized
    fun ensureOpus(): Boolean {
        if (isInitialized) return isAudioSupported
        isInitialized = true
        try {
            if (OpusLibrary.isInitialized()) return true.also { isAudioSupported = it }
            isAudioSupported = OpusLibrary.loadFromJar()
        } catch (e: Throwable) {
            handleException(e)
        } finally {
            if (isAudioSupported) LOG.info("Audio System successfully setup!") else LOG.info("Audio System encountered problems while loading, thus, is disabled.")
        }
        return isAudioSupported
    }

    private fun handleException(e: Throwable) {
        if (e is UnsupportedOperationException) {
            LOG.error("Sorry, JDA's audio system doesn't support this system.\n{}", e.message)
        } else if (e is NoClassDefFoundError) {
            LOG.error("Missing opus dependency, unable to initialize audio!")
        } else if (e is IOException) {
            LOG.error("There was an IO Exception when setting up the temp files for audio.", e)
        } else if (e is UnsatisfiedLinkError) {
            LOG.error("JDA encountered a problem when attempting to load the Native libraries. Contact a DEV.", e)
        } else if (e is Error) {
            throw e
        } else {
            LOG.error("An unknown exception occurred while attempting to setup JDA's audio system!", e)
        }
    }
}
