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

import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

/**
 * Indicates that the presence of a {@link net.dv8tion.jda.core.entities.User User} has changed.
 * <br>Users don't have presences directly, this is fired when either a {@link net.dv8tion.jda.core.entities.Member Member} from a {@link net.dv8tion.jda.core.entities.Guild Guild}
 * or one of the client's {@link net.dv8tion.jda.client.entities.Friend Friends} changes their presence.
 *
 * <p>Can be used to track the presence updates of members/friends.
 */
public abstract class GenericUserPresenceEvent<T> extends GenericUserUpdateEvent<T>
{
    protected final Guild guild;

    public GenericUserPresenceEvent(
        JDA api, long responseNumber, User user, Guild guild,
        T previous, T next, String identifier)
    {
        super(api, responseNumber, user, previous, next, identifier);
        this.guild = guild;
    }

    /**
     * Possibly-null guild in which the presence has changed.
     *
     * @return The guild, or null if this is related to a {@link net.dv8tion.jda.client.entities.Friend Friend}
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Possibly-null member who changed their presence.
     *
     * @return The member, or null if this is related to a {@link net.dv8tion.jda.client.entities.Friend Friend}
     */
    public Member getMember()
    {
        return !isRelationshipUpdate() ? getGuild().getMember(getUser()) : null;
    }

    /**
     * Possibly-null friend who changed their presence.
     *
     * @return The friend, or null if this is related to a {@link net.dv8tion.jda.core.entities.Member Member}
     */
    public Friend getFriend()
    {
        return isRelationshipUpdate() ? getJDA().asClient().getFriend(getUser()) : null;
    }

    /**
     * Whether this is a change for a friend presence.
     *
     * @return True, if this was the presence update for a {@link net.dv8tion.jda.client.entities.Friend Friend}
     */
    public boolean isRelationshipUpdate()
    {
        return getGuild() == null;
    }
}
