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
 * Indicates that the Avatar of a {@link net.dv8tion.jda.core.entities.User User} changed.
 *
 * <p>Can be used to retrieve the User who changed their avatar and their previous Avatar ID/URL.
 *
 * @deprecated Use {@link net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent UserUpdateAvatarEvent}
 */
@Deprecated
public class UserAvatarUpdateEvent extends GenericUserEvent
{
    private final String previousAvatarId;

    public UserAvatarUpdateEvent(JDA api, long responseNumber, User user, String previousAvatarId)
    {
        super(api, responseNumber, user);
        this.previousAvatarId = previousAvatarId;
    }

    /**
     * The previous avatar id
     *
     * @return The previous avatar id
     */
    public String getPreviousAvatarId()
    {
        return previousAvatarId;
    }

    /**
     * The previous avatar url
     *
     * @return The previous avatar url
     */
    public String getPreviousAvatarUrl()
    {
        return previousAvatarId == null ? null : "https://cdn.discordapp.com/avatars/" + getUser().getId() + "/" + previousAvatarId + (previousAvatarId.startsWith("a_") ? ".gif" : ".png");
    }
}
