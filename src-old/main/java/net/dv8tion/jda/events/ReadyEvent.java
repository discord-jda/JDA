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
 * <b><u>ReadyEvent</u></b><br>
 * Fired if our connection finished loading the ready event.<br>
 * Before this event was fired all entity related functions (like JDA#getUserById(String)) were not guaranteed to work as expected.<br>
 * <br>
 * Use: JDA finished populating internal objects and is now ready to be used. When this is fired all entities are cached and accessible.
 */
public class ReadyEvent extends Event
{
    public ReadyEvent(JDA api, int responseNumber)
    {
        super(api, responseNumber);
    }
}
