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
package net.dv8tion.jda.api.events.user.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.User.UserFlag
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that the [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of a [User][net.dv8tion.jda.api.entities.User] changed.
 *
 *
 * Can be used to retrieve the User who got their flags changed and their previous flags.
 *
 *
 * Identifier: `public_flags`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Additionally, this event also requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
class UserUpdateFlagsEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull user: User,
    @Nonnull oldFlags: EnumSet<UserFlag?>?
) : GenericUserUpdateEvent<EnumSet<UserFlag?>?>(api, responseNumber, user, oldFlags, user.getFlags(), IDENTIFIER) {
    @get:Nonnull
    val oldFlags: EnumSet<UserFlag?>?
        /**
         * Gets the old [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of the User as [EnumSet].
         *
         * @return [EnumSet] of the old [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag]
         */
        get() = oldValue
    val oldFlagsRaw: Int
        /**
         * Gets the old [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of the user and returns it as bitmask representation.
         *
         * @return The old bitmask representation of the [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag].
         */
        get() = UserFlag.getRaw(previous!!)

    @get:Nonnull
    val newFlags: EnumSet<UserFlag?>?
        /**
         * Gets the new [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of the User as [EnumSet].
         *
         * @return The new `EnumSet<{ net.dv8tion.jda.api.entities.User.UserFlag UserFlag}>` representation of the User's flags.
         */
        get() = newValue
    val newFlagsRaw: Int
        /**
         * Gets the new [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag] of the user and returns it as bitmask representation.
         *
         * @return The new bitmask representation of the [UserFlags][net.dv8tion.jda.api.entities.User.UserFlag].
         */
        get() = UserFlag.getRaw(next!!)

    companion object {
        const val IDENTIFIER = "public_flags"
    }
}
