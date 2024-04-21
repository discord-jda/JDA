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
package net.dv8tion.jda.api.audio.hooks

import net.dv8tion.jda.api.audio.SpeakingMode
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.Nonnull
import kotlin.concurrent.Volatile

/**
 * Internal implementation of [ConnectionListener], to handle possible exceptions thrown by user code.
 */
class ListenerProxy : ConnectionListener {
    @JvmField
    @Volatile
    var listener: ConnectionListener? = null
    override fun onPing(ping: Long) {
        if (listener == null) return
        val listener = listener
        try {
            listener?.onPing(ping)
        } catch (t: Throwable) {
            log.error("The ConnectionListener encountered and uncaught exception", t)
            if (t is Error) throw t
        }
    }

    override fun onStatusChange(@Nonnull status: ConnectionStatus?) {
        if (listener == null) return
        val listener = listener
        try {
            listener?.onStatusChange(status)
        } catch (t: Throwable) {
            log.error("The ConnectionListener encountered and uncaught exception", t)
            if (t is Error) throw t
        }
    }

    override fun onUserSpeaking(@Nonnull user: User?, @Nonnull modes: EnumSet<SpeakingMode?>?) {
        if (listener == null) return
        val listener = listener
        try {
            if (listener != null) {
                listener.onUserSpeaking(user, modes)
                listener.onUserSpeaking(user, modes!!.contains(SpeakingMode.VOICE))
                listener.onUserSpeaking(
                    user,
                    modes.contains(SpeakingMode.VOICE),
                    modes.contains(SpeakingMode.SOUNDSHARE)
                )
            }
        } catch (t: Throwable) {
            log.error("The ConnectionListener encountered and uncaught exception", t)
            if (t is Error) throw t
        }
    }

    override fun onUserSpeakingModeUpdate(@Nonnull user: UserSnowflake?, @Nonnull modes: EnumSet<SpeakingMode?>?) {
        if (listener == null) return
        val listener = listener
        try {
            if (listener != null) {
                listener.onUserSpeakingModeUpdate(user, modes)
                if (user is User) listener.onUserSpeakingModeUpdate(user as User?, modes)
            }
        } catch (t: Throwable) {
            log.error("The ConnectionListener encountered and uncaught exception", t)
            if (t is Error) throw t
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListenerProxy::class.java)
    }
}
