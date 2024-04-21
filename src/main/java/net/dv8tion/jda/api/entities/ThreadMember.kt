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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * A [ThreadMember] represents a [Member&#39;s][Member] participation in a Thread.
 * <br></br>ThreadMembers are subscribed to [Threads][ThreadChannel] and receive updates for them, like new message and thread changes.
 * Only subscribed members are shown in a thread's sidebar.
 */
interface ThreadMember : IMentionable {
    @get:Nonnull
    val jDA: JDA?

    @get:Nonnull
    val guild: Guild?

    @get:Nonnull
    val thread: ThreadChannel

    @get:Nonnull
    val user: User?

    @JvmField
    @get:Nonnull
    val member: Member?

    @get:Nonnull
    val timeJoined: OffsetDateTime?
    val isThreadOwner: Boolean
        /**
         * True, if this [ThreadMember] owns the subscribed [ThreadChannel].
         *
         * @return True, if this [ThreadMember] owns the subscribed [ThreadChannel].
         */
        get() = thread.ownerIdLong === getIdLong()
}
