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

package net.dv8tion.jda.core.events.role.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Role;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Role Role} updated its mentionable state.
 *
 * <p>Can be used to retrieve the old mentionable state.
 *
 * <p>Identifier: {@code mentionable}
 */
public class RoleUpdateMentionableEvent extends GenericRoleUpdateEvent<Boolean>
{
    public static final String IDENTIFIER = "mentionable";

    public RoleUpdateMentionableEvent(JDA api, long responseNumber, Role role, boolean wasMentionable)
    {
        super(api, responseNumber, role, wasMentionable, !wasMentionable, IDENTIFIER);
    }

    /**
     * Whether the role was mentionable
     *
     * @return True, if this role was mentionable before this update
     */
    public boolean wasMentionable()
    {
        return getOldValue();
    }
}
