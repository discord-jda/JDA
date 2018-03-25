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

/**
 * Indicates that the username/discriminator of a {@link net.dv8tion.jda.core.entities.User User} changed. (Not Nickname)
 *
 * <p>Can be used to retrieve the User who changed their username/discriminator and their previous username/discriminator.
 *
 * @deprecated Use {@link net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent UserUpdateNameEvent} and {@link net.dv8tion.jda.core.events.user.update.UserUpdateDiscriminatorEvent UserUpdateDiscriminatorEvent}
 */
@Deprecated
public class UserNameUpdateEvent extends GenericUserEvent
{
    private final String oldName;
    private final String oldDiscriminator;

    public UserNameUpdateEvent(JDA api, long responseNumber, User user, String oldName, String oldDiscriminator)
    {
        super(api, responseNumber, user);
        this.oldName = oldName;
        this.oldDiscriminator = oldDiscriminator;
    }

    /**
     * The old username
     *
     * @return The old username
     */
    public String getOldName()
    {
        return oldName;
    }

    /**
     * The old discriminator
     *
     * @return The old discriminator
     */
    public String getOldDiscriminator()
    {
        return oldDiscriminator;
    }
}
