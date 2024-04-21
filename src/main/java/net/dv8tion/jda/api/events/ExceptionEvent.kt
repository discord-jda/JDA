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
package net.dv8tion.jda.api.events

import net.dv8tion.jda.api.JDA
import javax.annotation.Nonnull

/**
 * Indicates that JDA encountered a Throwable that could not be forwarded to another end-user frontend.
 * <br></br>For instance this is fired for events in internal WebSocket handling or audio threads.
 * This includes [Errors][java.lang.Error] and [WebSocketExceptions][com.neovisionaries.ws.client.WebSocketException]
 *
 *
 * It is not recommended to simply use this and print each event as some throwables were already logged
 * by JDA. See [.isLogged].
 */
class ExceptionEvent(
    @Nonnull api: JDA,
    /**
     * The cause Throwable for this event
     *
     * @return The cause
     */
    @get:Nonnull
    @param:Nonnull val cause: Throwable,
    /**
     * Whether this Throwable was already printed using the JDA logging system
     *
     * @return True, if this throwable was already logged
     */
    val isLogged: Boolean
) : Event(api)
