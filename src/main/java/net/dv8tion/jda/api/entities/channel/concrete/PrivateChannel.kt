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
package net.dv8tion.jda.api.entities.channel.concrete

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.requests.RestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents the connection used for direct messaging.
 *
 * @see User.openPrivateChannel
 */
interface PrivateChannel : MessageChannel {
    /**
     * The [User][net.dv8tion.jda.api.entities.User] that this [PrivateChannel] communicates with.
     *
     *
     * This user is only null if this channel is currently uncached, and one the following occur:
     *
     *  * A reaction is removed
     *  * A reaction is added
     *  * A message is deleted
     *  * This account sends a message to a user from another shard (not shard 0)
     *
     * The consequence of this is that for any message this bot receives from a guild or from other users, the user will not be null.
     *
     * <br></br>In order to retrieve a user that is null, use [.retrieveUser]
     *
     * @return Possibly-null [User][net.dv8tion.jda.api.entities.User].
     *
     * @see .retrieveUser
     */
    @JvmField
    val user: User?

    /**
     * Retrieves the [User] that this [PrivateChannel] communicates with.
     *
     * <br></br>This method fetches the channel from the API and retrieves the User from that.
     *
     * @return A [RestAction] to retrieve the [User] that this [PrivateChannel] communicates with.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveUser(): RestAction<User?>?

    @get:Nonnull
    abstract override val name: String
}
