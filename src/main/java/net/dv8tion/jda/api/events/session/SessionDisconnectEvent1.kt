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
package net.dv8tion.jda.api.events.session

import com.neovisionaries.ws.client.WebSocketFrame
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.CloseCode
import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * Indicates that JDA has been disconnected from the remote server.
 * <br></br>When this event is fired JDA will try to reconnect if possible
 * unless [JDABuilder.setAutoReconnect(Boolean)][net.dv8tion.jda.api.JDABuilder.setAutoReconnect]
 * has been provided `false` or the disconnect was too fatal in which case a [ShutdownEvent] is fired.
 *
 *
 * When reconnecting was successful either a [SessionRecreateEvent] **or** [SessionResumeEvent] is fired.
 */
class SessionDisconnectEvent(
    @Nonnull api: JDA,
    /**
     * The close frame discord sent to us
     *
     * @return The [WebSocketFrame][com.neovisionaries.ws.client.WebSocketFrame] discord sent as closing handshake
     */
    val serviceCloseFrame: WebSocketFrame?,
    /**
     * The close frame we sent to discord
     *
     * @return The [WebSocketFrame][com.neovisionaries.ws.client.WebSocketFrame] we sent as closing handshake
     */
    val clientCloseFrame: WebSocketFrame?,
    /**
     * Whether the connection was closed by discord
     *
     * @return True, if discord closed our connection
     */
    val isClosedByServer: Boolean,
    /**
     * Time at which we noticed the disconnection
     *
     * @return Time of closure
     */
    @get:Nonnull
    @param:Nonnull val timeDisconnected: OffsetDateTime
) : GenericSessionEvent(api, SessionState.DISCONNECTED) {

    val closeCode: CloseCode?
        /**
         * Possibly-null [CloseCode][net.dv8tion.jda.api.requests.CloseCode]
         * representing the meaning for this DisconnectEvent
         *
         *
         * **This is `null` if this disconnect did either not happen because the Service closed the session
         * (see [.isClosedByServer]) or if there is no mapped CloseCode enum constant for the service close code!**
         *
         * @return Possibly-null [CloseCode][net.dv8tion.jda.api.requests.CloseCode]
         */
        get() = if (serviceCloseFrame != null) CloseCode.from(serviceCloseFrame.getCloseCode()) else null
}
