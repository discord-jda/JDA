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
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import javax.annotation.Nonnull

/**
 * Indicates that the activities of a guild member changed.
 *
 *
 * This is fired after a sequence of [UserActivityStartEvents][net.dv8tion.jda.api.events.user.UserActivityStartEvent] and [UserActivityEndEvents][net.dv8tion.jda.api.events.user.UserActivityEndEvent]
 * are fired and can be used to handle the resulting list of activities for the member.
 *
 *
 * Identifier: `activities`
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
 *
 *
 * This also requires [CacheFlag.ACTIVITY][net.dv8tion.jda.api.utils.cache.CacheFlag.ACTIVITY] to be enabled.
 * You can enable the cache flag with [enableCache(CacheFlag.ACTIVITY)][net.dv8tion.jda.api.JDABuilder.enableCache].
 *
 * @since  4.2.1
 */
class UserUpdateActivitiesEvent(
    @Nonnull api: JDA, responseNumber: Long, @get:Nonnull
    @param:Nonnull override val member: Member, previous: List<Activity?>?
) : GenericUserUpdateEvent<List<Activity?>?>(api, responseNumber, member.user, previous, member.activities, IDENTIFIER),
    GenericUserPresenceEvent {

    @get:Nonnull
    override val guild: Guild?
        get() = member.guild

    companion object {
        const val IDENTIFIER = "activities"
    }
}
