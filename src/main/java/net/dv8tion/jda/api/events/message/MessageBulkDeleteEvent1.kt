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
package net.dv8tion.jda.api.events.message

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.events.Event
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that a bulk deletion is executed in a [GuildMessageChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel].
 * <br></br>Set [net.dv8tion.jda.api.JDABuilder.setBulkDeleteSplittingEnabled] to false in order to enable this event.
 *
 *
 * Can be used to detect that a large chunk of Messages is deleted in a GuildMessageChannel. Providing a list of Message IDs and the specific GuildMessageChannel.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 *
 *  * [GUILD_MESSAGES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES] to work in guild message channels
 *  * [DIRECT_MESSAGES][net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES] to work in private channels
 *
 */
class MessageBulkDeleteEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull protected val channel: GuildMessageChannel,
    @Nonnull messageIds: List<String>?
) : Event(api, responseNumber) {
    /**
     * List of messages that have been deleted.
     *
     * @return The list of message ids
     */
    @get:Nonnull
    val messageIds: List<String>

    init {
        this.messageIds = Collections.unmodifiableList(messageIds)
    }

    /**
     * The [GuildMessageChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel] where the messages have been deleted
     *
     * @return The TextChannel
     */
    @Nonnull
    fun getChannel(): GuildMessageChannelUnion {
        return channel as GuildMessageChannelUnion
    }

    @get:Nonnull
    val guild: Guild
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] where the messages were deleted.
         *
         * @return The Guild
         */
        get() = channel.guild
}
