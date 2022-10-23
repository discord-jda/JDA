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

package net.dv8tion.jda.api.events.session;

/**
 * State of a gateway session.
 *
 * @see GenericSessionEvent
 */
public enum SessionState
{
    /**
     * The session is fully loaded, including all guilds.
     *
     * @see ReadyEvent
     */
    READY,

    /**
     * The session cache has been invalidated.
     *
     * @see SessionInvalidateEvent
     */
    INVALIDATED,

    /**
     * The session has disconnected, possibly to resume.
     *
     * @see SessionDisconnectEvent
     */
    DISCONNECTED,

    /**
     * The session has resumed successfully after disconnecting.
     *
     * @see SessionResumeEvent
     */
    RESUMED,

    /**
     * The session has been recreated after being {@link #INVALIDATED invalidated}.
     *
     * @see SessionRecreateEvent
     */
    RECREATED,

    /**
     * The session has been closed and will not be reconnected.
     *
     * @see ShutdownEvent
     */
    SHUTDOWN,
}
