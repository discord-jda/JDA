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

package net.dv8tion.jda.core.events.emote.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EmoteUpdateRolesEvent extends GenericEmoteUpdateEvent
{
    protected final List<Role> oldRoles;

    public EmoteUpdateRolesEvent(JDA api, long responseNumber, Emote emote, Collection<Role> oldRoles)
    {
        super(api, responseNumber, emote);
        this.oldRoles = Collections.unmodifiableList(new LinkedList<>(oldRoles));
    }

    public List<Role> getOldRoles()
    {
        return oldRoles;
    }

    public List<Role> getNewRoles()
    {
        return emote.getRoles();
    }
}
