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

/**
 * Indicates that the [global name][User.getGlobalName] of a [User] changed. (Not Nickname)
 *
 *
 * Can be used to retrieve the User who changed their global name and their previous global name.
 *
 *
 * Identifier: `global_name`
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
 * Additionally, this event requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
class UserUpdateGlobalNameEvent(api: JDA, responseNumber: Long, user: User, oldName: String?) :
    GenericUserUpdateEvent<String?>(api, responseNumber, user, oldName, user.globalName, IDENTIFIER) {
    val oldGlobalName: String?
        /**
         * The old global name
         *
         * @return The old global name
         */
        get() = oldValue
    val newGlobalName: String?
        /**
         * The new global name
         *
         * @return The new global name
         */
        get() = newValue

    override fun toString(): String {
        return "UserUpdateGlobalName($oldValue->$newValue)"
    }

    companion object {
        const val IDENTIFIER = "global_name"
    }
}
