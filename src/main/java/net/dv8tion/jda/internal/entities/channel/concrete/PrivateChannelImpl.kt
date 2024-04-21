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
package net.dv8tion.jda.internal.entities.channel.concrete

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin
import net.dv8tion.jda.internal.requests.CompletedRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import javax.annotation.Nonnull

class PrivateChannelImpl(api: JDA?, id: Long, private override var user: User?) :
    AbstractChannelImpl<PrivateChannelImpl?>(id, api), PrivateChannel, MessageChannelMixin<PrivateChannelImpl?> {
    override var latestMessageIdLong: Long = 0
        private set

    @get:Nonnull
    override val type: ChannelType
        get() = ChannelType.PRIVATE

    fun getUser(): User? {
        updateUser()
        return user
    }

    @Nonnull
    override fun retrieveUser(): RestAction<User?>? {
        val user = getUser()
        return user?.let { CompletedRestAction(jda, it) }
            ?: retrievePrivateChannel()
                .map<User?>(PrivateChannel::getUser)
        //even if the user blocks the bot, this does not fail.
    }

    @get:Nonnull
    override val name: String?
        get() {
            val user = getUser()
                ?: //don't break or override the contract of @NonNull
                return ""
            return user.name
        }

    @Nonnull
    private fun retrievePrivateChannel(): RestAction<PrivateChannel> {
        val route = Route.Channels.GET_CHANNEL.compile(id)
        return RestActionImpl(
            jda,
            route
        ) { response: Response, request: Request<PrivateChannel>? ->
            (jda as JDAImpl).entityBuilder.createPrivateChannel(
                response.`object`
            )
        }
    }

    @Nonnull
    override fun delete(): RestAction<Void?> {
        val route = Route.Channels.DELETE_CHANNEL.compile(id)
        return RestActionImpl(jda, route)
    }

    override fun canTalk(): Boolean {
        //The only way user is null is when an event is dispatched that doesn't give us enough information to build the recipient user,
        // which only happens if this bot sends a message (or otherwise triggers an event) from a shard other than shard 0.
        // The event will be received on shard 0 and not have enough information to build the recipient user.
        //As such, since events will only happen in this channel if it is between the bot and the user, a null user is a valid channel state.
        // Events cannot happen between a bot and another bot, so the user would never be null in that case.
        return user == null || !user.isBot()
    }

    override fun checkCanAccessChannel() {}
    override fun checkCanSendMessage() {
        checkBot()
    }

    override fun checkCanSendMessageEmbeds() {}
    override fun checkCanSendFiles() {}
    override fun checkCanViewHistory() {}
    override fun checkCanAddReactions() {}
    override fun checkCanRemoveReactions() {}
    override fun checkCanControlMessagePins() {}
    override fun canDeleteOtherUsersMessages(): Boolean {
        return false
    }

    fun setUser(user: User?) {
        this.user = user
    }

    override fun setLatestMessageIdLong(latestMessageId: Long): PrivateChannelImpl {
        latestMessageIdLong = latestMessageId
        return this
    }

    // -- Object --
    override fun hashCode(): Int {
        return java.lang.Long.hashCode(id)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is PrivateChannelImpl) return false
        return obj.id == this.id
    }

    private fun updateUser() {
        //if the user is null then we don't even know their ID, and so we have to check that first
        if (user == null) return
        // Load user from cache if one exists, otherwise we might have an outdated user instance
        val realUser = jda.getUserById(user!!.idLong)
        if (realUser != null) user = realUser
    }

    private fun checkBot() {
        //The only way user is null is when an event is dispatched that doesn't give us enough information to build the recipient user,
        // which only happens if this bot sends a message (or otherwise triggers an event) from a shard other than shard 0.
        // The event will be received on shard 0 and not have enough information to build the recipient user.
        //As such, since events will only happen in this channel if it is between the bot and the user, a null user is a valid channel state.
        // Events cannot happen between a bot and another bot, so the user would never be null in that case.
        if (getUser() != null && getUser().isBot()) throw UnsupportedOperationException("Cannot send a private message between bots.")
    }
}
