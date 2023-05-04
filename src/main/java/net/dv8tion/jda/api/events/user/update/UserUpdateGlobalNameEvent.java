/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;

/**
 * Indicates that the {@link User#getGlobalName() global name} of a {@link User} changed. (Not Nickname)
 *
 * <p>Can be used to retrieve the User who changed their global name and their previous global name.
 *
 * <p>Identifier: {@code global_name}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateGlobalNameEvent extends GenericUserUpdateEvent<String>
{
    public static final String IDENTIFIER = "global_name";

    public UserUpdateGlobalNameEvent(JDA api, long responseNumber, User user, String oldName)
    {
        super(api, responseNumber, user, oldName, user.getGlobalName(), IDENTIFIER);
    }

    /**
     * The old global name
     *
     * @return The old global name
     */
    @Nullable
    public String getOldGlobalName()
    {
        return getOldValue();
    }

    /**
     * The new global name
     *
     * @return The new global name
     */
    @Nullable
    public String getNewGlobalName()
    {
        return getNewValue();
    }

    @Override
    public String toString()
    {
        return "UserUpdateGlobalName(" + getOldValue() + "->" + getNewValue() + ')';
    }
}
