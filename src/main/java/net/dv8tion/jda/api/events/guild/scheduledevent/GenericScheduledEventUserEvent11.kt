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
package net.dv8tion.jda.api.events.guild.scheduledevent

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Indicates that a [User] has subscribed or unsubscribed to a [ScheduledEvent].
 *
 *
 * Can be used to detect when someone has indicated that they have subscribed or unsubscribed to an event and also retrieve their
 * [User] object as well as the [ScheduledEvent].
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [SCHEDULED_EVENTS][net.dv8tion.jda.api.requests.GatewayIntent.SCHEDULED_EVENTS] intent and [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 */
abstract class GenericScheduledEventUserEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull scheduledEvent: ScheduledEvent,
    /**
     * The id of the user that subscribed or unsubscribed to the [ScheduledEvent].
     *
     * @return The long user id
     */
    val userIdLong: Long
) : GenericScheduledEventGatewayEvent(api, responseNumber, scheduledEvent) {

    /**
     * The id of the user that subscribed or unsubscribed to the [ScheduledEvent].
     *
     * @return The string user id
     */
    @Nonnull
    fun getUserId(): String {
        return java.lang.Long.toUnsignedString(userIdLong)
    }

    val user: User?
        /**
         * The [User] who subscribed or unsubscribed to the [ScheduledEvent].
         * <br></br>This might be missing if the user was not cached.
         * Use [.retrieveUser] to load the user.
         *
         * @return The added user or null if this information is missing
         */
        get() = api.getUserById(userIdLong)
    val member: Member?
        /**
         * The [Member] instance for the user
         * or `null` if the user is not in this guild.
         * <br></br>This will also be `null` if the member is not available in the cache.
         * Use [.retrieveMember] to load the member.
         *
         * @return Member of the added user or null if they are no longer member of this guild
         */
        get() = guild!!.getMemberById(userIdLong)

    /**
     * Retrieves the [User] that subscribed or unsubscribed to the [ScheduledEvent].
     * <br></br>If a user is known, this will return [.getUser].
     *
     * @return [RestAction] - Type: [User]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveUser(): CacheRestAction<User> {
        return jda.retrieveUserById(userIdLong)
    }

    /**
     * Retrieves the [Member] that subscribed or unsubscribed to the [ScheduledEvent].
     * <br></br>If a member is known, this will return [.getMember].
     *
     * @return [RestAction] - Type: [Member]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMember(): CacheRestAction<Member> {
        return getGuild().retrieveMemberById(userIdLong)
    }
}
