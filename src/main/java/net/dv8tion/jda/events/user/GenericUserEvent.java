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
package net.dv8tion.jda.events.user;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>GenericUserEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.entities.User User} changes their presence. (like avatar/game)<br>
 * Every UserEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any UserEvent. <i>(No real use for the JDA user)</i>
 */
public class GenericUserEvent extends Event
{
    private final User user;

    public GenericUserEvent(JDA api, int responseNumber, User user)
    {
        super(api, responseNumber);
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }
}
