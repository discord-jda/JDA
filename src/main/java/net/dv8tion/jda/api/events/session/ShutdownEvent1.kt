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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.CloseCode
import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * Indicates that JDA has fully disconnected from Discord and will not attempt to reconnect again.
 * <br></br>At this stage all internal cache is invalid!
 */
class ShutdownEvent(
    @Nonnull api: JDA,
    /**
     * Time of WebSocket disconnect
     *
     * @return [OffsetDateTime][java.time.OffsetDateTime] representing
     * the point in time when the connection was dropped.
     */
    @get:Nonnull
    @param:Nonnull val timeShutdown: OffsetDateTime,
    /**
     * The server close code that was in the disconnect close frame
     * of this JDA instance.
     *
     * @return int close code of the Server Close-Frame
     */
    val code: Int
) : GenericSessionEvent(api, SessionState.SHUTDOWN) {

    val closeCode: CloseCode?
        /**
         * Possibly-null [CloseCode][net.dv8tion.jda.api.requests.CloseCode]
         * representing the meaning for this ShutdownEvent.
         * <br></br>The raw close code can be retrieved from [.getCode]
         * <br></br>If this is `null`, JDA does not know what the meaning for the connection loss was.
         *
         * @return Possibly-null [CloseCode][net.dv8tion.jda.api.requests.CloseCode]
         */
        get() = CloseCode.from(code)
}
