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
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import javax.annotation.Nonnull

/**
 * Indicates that a guild [Member] joined a [ThreadChannel].
 *
 * @see ThreadChannel
 *
 * @see ThreadMember
 */
class ThreadMemberJoinEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    thread: ThreadChannel,
    threadMember: ThreadMember
) : GenericThreadMemberEvent(api, responseNumber, thread, threadMember.idLong, threadMember) {
    @get:Nonnull
    override val threadMember: ThreadMember?
        /**
         * The [ThreadMember] that just joined the thread.
         * This entity will be present in [ThreadChannel.getThreadMembers] list.
         *
         * @return The [ThreadMember] that just joined the thread.
         */
        get() = super.getThreadMember()

    @get:Nonnull
    override val member: Member?
        /**
         * The [ThreadMember] that just joined the thread as a guild [Member].
         *
         * @return The [ThreadMember] that just joined the thread as a guild [Member].
         */
        get() =//Explicitly override the getter from the super class to use the member return in the thread member itself because
// the ThreadMember will always have the Member while the Guild itself might not because of
            // the ChunkingFilter or a lack of GUILD_MEMBERS intent.
            this.threadMember!!.member
}
