/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.events;

import net.dv8tion.jda.JDA;

/**
 * <b><u>ResumedEvent</u></b><br>
 * Fired if JDA successfully re-established it's connection to the WebSocket.<br/>
 * All Objects are still in place and events are replayed.<br/>
 * <br/>
 * Use: This marks the continuation of event flow stopped by the {@link net.dv8tion.jda.events.DisconnectEvent DisconnectEvent}.
 */
public class ResumedEvent extends Event
{
    public ResumedEvent(JDA api, int responseNumber)
    {
        super(api, responseNumber);
    }
}
