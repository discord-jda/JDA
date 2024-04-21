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
package net.dv8tion.jda.api.events.user

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [User][net.dv8tion.jda.api.entities.User] has started an [Activity]
 * in a [Guild].
 *
 *
 * This is fired for every [Guild] the user is part of. If the title of a stream
 * changes a start event is fired before an end event which will replace the activity.
 *
 *
 * The activities of the [Member] are updated before all the start/end events are fired.
 * This means you can check [Member.getActivities] when handling this event and it will already
 * contain all new activities, even ones that have not yet fired the start event.
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
 */
class UserActivityStartEvent(
    @Nonnull api: JDA, responseNumber: Long, @get:Nonnull
    @param:Nonnull override val member: Member,
    /**
     * The new activity
     *
     * @return The activity
     */
    @param:Nonnull val newActivity: Activity
) : GenericUserEvent(api, responseNumber, member.user), GenericUserPresenceEvent {

    @get:Nonnull
    override val guild: Guild?
        get() = member.guild
}
