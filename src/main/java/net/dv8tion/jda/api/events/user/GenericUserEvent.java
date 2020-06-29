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
package net.dv8tion.jda.api.events.user;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.User User} changed or started an activity.
 * <br>Every UserEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any UserEvent.
 */
public abstract class GenericUserEvent extends Event
{
    private final User user;

    public GenericUserEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user)
    {
        super(api, responseNumber);
        this.user = user;
    }

    /**
     * The related user instance
     *
     * @return The user instance related to this event
     */
    @Nonnull
    public User getUser()
    {
        return user;
    }
}
