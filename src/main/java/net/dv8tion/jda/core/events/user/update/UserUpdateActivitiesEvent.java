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
import net.dv8tion.jda.core.entities.Activity;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

/**
 * Indicates that the {@link net.dv8tion.jda.core.entities.Activity Activity} of a {@link net.dv8tion.jda.core.entities.User User} changes.
 * <br>As with any presence updates this either happened for a {@link net.dv8tion.jda.core.entities.Member Member} in a Guild or a {@link net.dv8tion.jda.client.entities.Friend Friend}!
 *
 * <p>Can be used to retrieve the User who changed their Activities and their previous Activities.
 *
 * <p>Identifier: {@code activity}
 */
public class UserUpdateActivitiesEvent extends GenericUserPresenceEvent<List<Activity>>
{
    public static final String IDENTIFIER = "activity";

    public UserUpdateActivitiesEvent(JDA api, long responseNumber, User user, Guild guild, List<Activity> previousActivities)
    {
        super(api, responseNumber, user, guild, previousActivities,
            guild == null ? api.asClient().getFriend(user).getActivities() : guild.getMember(user).getActivities(), IDENTIFIER);
    }

    /**
     * The previous {@link net.dv8tion.jda.core.entities.Activity Activity}
     *
     * @return The previous {@link net.dv8tion.jda.core.entities.Activity Activity}
     */
    public List<Activity> getOldActivities()
    {
        return getOldValue();
    }

    /**
     * The new {@link net.dv8tion.jda.core.entities.Activity Activity}
     *
     * @return The new {@link net.dv8tion.jda.core.entities.Activity Activity}
     */
    public List<Activity> getNewActivities()
    {
        return getNewValue();
    }
}
