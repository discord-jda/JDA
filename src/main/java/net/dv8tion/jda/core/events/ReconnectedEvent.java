/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
 * Indicates if JDA successfully re-established its connection to the gateway.
 * <br>All Objects have been replaced when this is fired and events were likely missed in the downtime.
 *
 * <p>Can be used to mark the continuation of event flow which was stopped by the {@link net.dv8tion.jda.core.events.DisconnectEvent DisconnectEvent}.
 * User should replace any cached Objects (like User/Guild objects).
 */
public class ReconnectedEvent extends Event
{
    public ReconnectedEvent(JDA api, long responseNumber)
    {
        super(api, responseNumber);
    }
}
