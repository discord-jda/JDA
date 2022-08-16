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

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of a {@link net.dv8tion.jda.api.entities.User User} changed.
 * 
 * <p>Can be used to retrieve the User who got their flags changed and their previous flags.
 * 
 * <p>Identifier: {@code public_flags}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event also requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateFlagsEvent extends GenericUserUpdateEvent<EnumSet<User.UserFlag>>
{
    public static final String IDENTIFIER = "public_flags";
    
    public UserUpdateFlagsEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nonnull EnumSet<User.UserFlag> oldFlags)
    {
        super(api, responseNumber, user, oldFlags, user.getFlags(), IDENTIFIER);
    }

    /**
     * Gets the old {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of the User as {@link EnumSet}.
     * 
     * @return {@link EnumSet} of the old {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}
     */
    @Nonnull
    public EnumSet<User.UserFlag> getOldFlags()
    {
        return getOldValue();
    }

    /**
     * Gets the old {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of the user and returns it as bitmask representation.
     * 
     * @return The old bitmask representation of the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}.
     */
    public int getOldFlagsRaw()
    {
        return User.UserFlag.getRaw(previous);
    }

    /**
     * Gets the new {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of the User as {@link EnumSet}.
     *
     * @return The new {@code EnumSet<{@link net.dv8tion.jda.api.entities.User.UserFlag UserFlag}>} representation of the User's flags.
     */
    @Nonnull
    public EnumSet<User.UserFlag> getNewFlags()
    {
        return getNewValue();
    }

    /**
     * Gets the new {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of the user and returns it as bitmask representation.
     *
     * @return The new bitmask representation of the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}.
     */
    public int getNewFlagsRaw()
    {
        return User.UserFlag.getRaw(next);
    }
}
