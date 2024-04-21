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
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [OnlineStatus] of a [User] changed.
 * <br></br>As with any presence updates this happened for a [Member] in a Guild!
 *
 * Can be used to retrieve the User who changed their status and their previous status.
 *
 *
 * Identifier: `status`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_PRESENCES][GatewayIntent.GUILD_PRESENCES] intent and [CacheFlag.ONLINE_STATUS] to be enabled.
 * <br></br>[createDefault(String)][JDABuilder.createDefault] and
 * [createLight(String)][JDABuilder.createLight] disable this by default!
 *
 *
 * Additionally, this event requires the [MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
class UserUpdateOnlineStatusEvent(
    @Nonnull api: JDA, responseNumber: Long, @get:Nonnull
    @param:Nonnull override val member: Member, @Nonnull oldOnlineStatus: OnlineStatus?
) : GenericUserUpdateEvent<OnlineStatus?>(
    api,
    responseNumber,
    member.user,
    oldOnlineStatus,
    member.onlineStatus,
    IDENTIFIER
), GenericUserPresenceEvent {
    private override val guild: Guild

    init {
        guild = member.guild
    }

    @Nonnull
    override fun getGuild(): Guild? {
        return guild
    }

    @get:Nonnull
    val oldOnlineStatus: OnlineStatus?
        /**
         * The old status
         *
         * @return The old status
         */
        get() = oldValue

    @get:Nonnull
    val newOnlineStatus: OnlineStatus?
        /**
         * The new status
         *
         * @return The new status
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "status"
    }
}
