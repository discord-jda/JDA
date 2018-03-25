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
package net.dv8tion.jda.core.events.user;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.User User} changed or started an activity.
 * <br>Every UserEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any UserEvent.
 */
public abstract class GenericUserEvent extends Event
{
    private final User user;

    public GenericUserEvent(JDA api, long responseNumber, User user)
    {
        super(api, responseNumber);
        this.user = user;
    }

    /**
     * The related user instance
     *
     * @return The user instance related to this event
     */
    public User getUser()
    {
        return user;
    }
}
