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

package net.dv8tion.jda.core.events.emote.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;

/**
 * Indicates that the role whitelist for an {@link net.dv8tion.jda.core.entities.Emote Emote} changed.
 *
 * <p>Can be used to retrieve the old role whitelist
 *
 * <p>Identifier: {@code roles}
 */
public class EmoteUpdateRolesEvent extends GenericEmoteUpdateEvent<List<Role>>
{
    public static final String IDENTIFIER = "roles";

    public EmoteUpdateRolesEvent(JDA api, long responseNumber, Emote emote, List<Role> oldRoles)
    {
        super(api, responseNumber, emote, oldRoles, emote.getRoles(), IDENTIFIER);
    }

    /**
     * The old role whitelist
     *
     * @return The old role whitelist
     */
    public List<Role> getOldRoles()
    {
        return getOldValue();
    }

    /**
     * The new role whitelist
     *
     * @return The new role whitelist
     */
    public List<Role> getNewRoles()
    {
        return getNewValue();
    }
}
