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
package net.dv8tion.jda.api.events.thread.member

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.ThreadMember
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.thread.GenericThreadEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [ThreadMember] event has been fired.
 * If you can see a [ThreadChannel], you will receive all derived thread member events for it.
 * Current derived events:
 *
 *  * [ThreadMemberJoinEvent]
 *  * [ThreadMemberLeaveEvent]
 *
 *
 * @see Member
 *
 * @see ThreadChannel
 *
 * @see ThreadMember
 */
open class GenericThreadMemberEvent(
    @Nonnull api: JDA, responseNumber: Long, thread: ThreadChannel,
    /**
     * The id of the [ThreadMember].
     *
     * @return Long containing the Id.
     */
    val threadMemberIdLong: Long, protected val threadMember: ThreadMember
) : GenericThreadEvent(api, responseNumber, thread) {
    /**
     * The id of the [ThreadMember] that fired this and derived event.
     *
     * @return Never-null String containing the ID.
     */
    @Nonnull
    fun getThreadMemberId(): String {
        return java.lang.Long.toUnsignedString(threadMemberIdLong)
    }

    /**
     * The [ThreadMember] of the event that has been fired.
     *
     * @return The [ThreadMember] of the event that has been fired.
     */
    open fun getThreadMember(): ThreadMember? {
        return threadMember
    }

    open val member: Member?
        /**
         * The [ThreadMember] as a guild [Member].
         *
         * @return The [ThreadMember] as a guild [Member].
         */
        get() = guild.getMemberById(threadMemberIdLong)
}
