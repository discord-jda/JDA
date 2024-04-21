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
import javax.annotation.Nonnull

/**
 * Indicates that the [Activity][net.dv8tion.jda.api.entities.Activity] order of a [User][net.dv8tion.jda.api.entities.User] changes.
 * <br></br>As with any presence updates this happened for a [Member][net.dv8tion.jda.api.entities.Member] in a Guild!
 *
 * Can be used to retrieve the User who changed their Activities and their previous Activities.
 *
 *
 * Identifier: `activity_order`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Additionally, this event requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
class UserUpdateActivityOrderEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull previous: List<Activity?>?, @get:Nonnull
    @param:Nonnull override val member: Member
) : GenericUserUpdateEvent<List<Activity?>?>(api, responseNumber, member.user, previous, member.activities, IDENTIFIER),
    GenericUserPresenceEvent {

    @get:Nonnull
    override val guild: Guild?
        get() = member.guild

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "activity_order"
    }
}
