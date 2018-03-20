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

package net.dv8tion.jda.core.events.user.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

/**
 * Indicates that the username of a {@link net.dv8tion.jda.core.entities.User User} changed. (Not Nickname)
 *
 * <p>Can be used to retrieve the User who changed their username and their previous username.
 *
 * <p>Identifier: {@code name}
 */
public class UserUpdateNameEvent extends GenericUserUpdateEvent<String>
{
    public static final String IDENTIFIER = "name";

    public UserUpdateNameEvent(JDA api, long responseNumber, User user, String oldName)
    {
        super(api, responseNumber, user, oldName, user.getName(), IDENTIFIER);
    }

    /**
     * The old username
     *
     * @return The old username
     */
    public String getOldName()
    {
        return getOldValue();
    }

    /**
     * The new username
     *
     * @return The new username
     */
    public String getNewName()
    {
        return getNewValue();
    }
}
