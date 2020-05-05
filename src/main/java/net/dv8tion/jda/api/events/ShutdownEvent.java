/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.CloseCode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Indicates that JDA has fully disconnected from Discord and will not attempt to reconnect again.
 * <br>At this stage all internal cache is invalid!
 */
public class ShutdownEvent extends Event
{
    protected final OffsetDateTime shutdownTime;
    protected final int code;

    public ShutdownEvent(@Nonnull JDA api, @Nonnull OffsetDateTime shutdownTime, int code)
    {
        super(api);
        this.shutdownTime = shutdownTime;
        this.code = code;
    }

    /**
     * Time of WebSocket disconnect
     *
     * @return {@link java.time.OffsetDateTime OffsetDateTime} representing
     *         the point in time when the connection was dropped.
     */
    @Nonnull
    public OffsetDateTime getTimeShutdown()
    {
        return shutdownTime;
    }

    /**
     * Possibly-null {@link net.dv8tion.jda.api.requests.CloseCode CloseCode}
     * representing the meaning for this ShutdownEvent.
     * <br>The raw close code can be retrieved from {@link #getCode()}
     * <br>If this is {@code null}, JDA does not know what the meaning for the connection loss was.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.requests.CloseCode CloseCode}
     */
    @Nullable
    public CloseCode getCloseCode()
    {
        return CloseCode.from(code);
    }

    /**
     * The server close code that was in the disconnect close frame
     * of this JDA instance.
     *
     * @return int close code of the Server Close-Frame
     */
    public int getCode()
    {
        return code;
    }
}
