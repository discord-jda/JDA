/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events;

import net.dv8tion.jda.core.JDA;

/**
 * Fired when JDA does not have a specific handling for a Throwable.
 * <br>This includes {@link java.lang.Error Errors} and {@link com.neovisionaries.ws.client.WebSocketException WebSocketExceptions}
 *
 * <p>It is not recommended to simply use this and print each event as some throwables where already logged
 * by JDA. See {@link #isLogged()}.
 */
public class ExceptionEvent extends Event
{
    protected final Throwable throwable;
    protected final boolean logged;

    public ExceptionEvent(JDA api, Throwable throwable, boolean logged)
    {
        super(api, -1);
        this.throwable = throwable;
        this.logged = logged;
    }

    /**
     * Whether this Throwable was already printed using the JDA logging system
     *
     * @return True, if this throwable was already logged
     */
    public boolean isLogged()
    {
        return logged;
    }

    /**
     * The cause Throwable for this event
     *
     * @return The cause
     */
    public Throwable getCause()
    {
        return throwable;
    }
}
