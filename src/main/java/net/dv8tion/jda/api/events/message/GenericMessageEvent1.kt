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
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.internal.utils.Helpers
import javax.annotation.Nonnull

/**
 * Indicates that a [Message][net.dv8tion.jda.api.entities.Message] was created/deleted/changed.
 * <br></br>Every MessageEvent is an instance of this event and can be casted.
 *
 *
 * Can be used to detect any MessageEvent.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require at least one of the following intents (Will not fire at all if neither is enabled):
 *
 *  * [GUILD_MESSAGES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES] to work in guild text channels
 *  * [DIRECT_MESSAGES][net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES] to work in private channels
 *
 */
abstract class GenericMessageEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The id for this message
     *
     * @return The id for this message
     */
    val messageIdLong: Long, @param:Nonnull protected val channel: MessageChannel
) : Event(api, responseNumber) {
    /**
     * The [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] for this Message
     *
     * @return The MessageChannel
     */
    @Nonnull
    fun getChannel(): MessageChannelUnion {
        return channel as MessageChannelUnion
    }

    @get:Nonnull
    val guildChannel: GuildMessageChannelUnion
        /**
         * The [GuildMessageChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel] for this Message
         * if it was sent in a Guild.
         * <br></br>If this Message was not received from a [Guild][net.dv8tion.jda.api.entities.Guild],
         * this will throw an [java.lang.IllegalStateException].
         *
         * @throws java.lang.IllegalStateException
         * If this was not sent in a channel in a Guild.
         *
         * @return The GuildMessageChannel
         */
        get() {
            check(isFromGuild) { "This message event did not happen in a guild" }
            return channel as GuildMessageChannelUnion
        }

    /**
     * The id for this message
     *
     * @return The id for this message
     */
    @Nonnull
    fun getMessageId(): String {
        return java.lang.Long.toUnsignedString(messageIdLong)
    }

    /**
     * Indicates whether the message is from the specified [ChannelType]
     *
     * @param  type
     * The ChannelType
     *
     * @return True, if the message is from the specified channel type
     */
    fun isFromType(@Nonnull type: ChannelType): Boolean {
        return channel.type === type
    }

    val isFromGuild: Boolean
        /**
         * Whether this message was sent in a [Guild][net.dv8tion.jda.api.entities.Guild].
         * <br></br>If this is `false` then [.getGuild] will throw an [java.lang.IllegalStateException].
         *
         * @return True, if [.getChannelType].[isGuild()][ChannelType.isGuild] is true.
         */
        get() = channelType!!.isGuild

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType] for this message
         *
         * @return The ChannelType
         */
        get() = channel.type

    @get:Nonnull
    val guild: Guild
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] the Message was received in.
         * <br></br>If this Message was not received in a [Guild][net.dv8tion.jda.api.entities.Guild],
         * this will throw an [java.lang.IllegalStateException].
         *
         * @throws java.lang.IllegalStateException
         * If this was not sent in a [net.dv8tion.jda.api.entities.channel.middleman.GuildChannel].
         *
         * @return The Guild the Message was received in
         *
         * @see .isFromGuild
         * @see .isFromType
         * @see .getChannelType
         */
        get() {
            check(isFromGuild) { "This message event did not happen in a guild" }
            return (channel as GuildChannel).guild
        }

    @get:Nonnull
    val jumpUrl: String
        /**
         * Returns the jump-to URL for the received message.
         * <br></br>Clicking this URL in the Discord client will cause the client to jump to the specified message.
         *
         * @return A String representing the jump-to URL for the message
         */
        get() = Helpers.format(Message.JUMP_URL, if (isFromGuild) guild.id else "@me", getChannel().id, getMessageId())
    val isFromThread: Boolean
        /**
         * If the message event was from a [ThreadChannel]
         *
         * @return If the message event was from a ThreadChannel
         *
         * @see ChannelType.isThread
         */
        get() = channelType!!.isThread
}
