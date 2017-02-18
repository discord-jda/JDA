/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import java.time.OffsetDateTime;

/**
 * <b><u>ShutdownEvent</u></b><br>
 * Fired if JDA successfully finished shutting down.<br>
 *<br>
 * Use: Confirmation of JDA#shutdown(boolean).
 */
public class ShutdownEvent extends Event
{
    protected final OffsetDateTime shutdownTime;

    public ShutdownEvent(JDA api, OffsetDateTime shutdownTime)
    {
        super(api, -1);
        this.shutdownTime = shutdownTime;
    }

    public OffsetDateTime getShutdownTime()
    {
        return shutdownTime;
    }
}
