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

package net.dv8tion.jda.core.events.emote;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.Event;

/**
 * Indicates that an {@link net.dv8tion.jda.core.entities.Emote Emote} was created/removed/updated.
 */
public abstract class GenericEmoteEvent extends Event
{
    protected final Emote emote;

    public GenericEmoteEvent(JDA api, long responseNumber, Emote emote)
    {
        super(api, responseNumber);
        this.emote = emote;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} where the emote came from
     *
     * @return The origin Guild
     */
    public Guild getGuild()
    {
        return emote.getGuild();
    }

    /**
     * The responsible {@link net.dv8tion.jda.core.entities.Emote Emote} for this event
     *
     * @return The emote
     */
    public Emote getEmote()
    {
        return emote;
    }

    /**
     * Whether this emote is managed by an integration
     *
     * @return True, if this emote is managed by an integration
     */
    public boolean isManaged()
    {
        return emote.isManaged();
    }
}
