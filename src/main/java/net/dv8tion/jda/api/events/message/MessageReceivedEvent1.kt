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
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import javax.annotation.Nonnull

/**
 * Indicates that a Message was received in a [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
 * <br></br>This includes [TextChannel] and [PrivateChannel]!
 *
 *
 * Can be used to detect that a Message is received in either a guild- or private channel. Providing a MessageChannel and Message.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 *
 *  * [GUILD_MESSAGES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES] to work in guild text channels
 *  * [DIRECT_MESSAGES][net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES] to work in private channels
 *
 *
 */
class MessageReceivedEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The received [Message][net.dv8tion.jda.api.entities.Message] object.
     *
     * @return The received [Message][net.dv8tion.jda.api.entities.Message] object.
     */
    @JvmField @get:Nonnull
    @param:Nonnull val message: Message
) : GenericMessageEvent(api, responseNumber, message.idLong, message.channel) {

    @get:Nonnull
    val author: User?
        /**
         * The Author of the Message received as [User][net.dv8tion.jda.api.entities.User] object.
         * <br></br>This will be never-null but might be a fake user if Message was sent via Webhook (Guild only).
         * See [Webhook.getDefaultUser].
         *
         * @return The Author of the Message.
         *
         * @see .isWebhookMessage
         */
        get() = message.author
    val member: Member?
        /**
         * The Author of the Message received as [Member][net.dv8tion.jda.api.entities.Member] object.
         * <br></br>This will be `null` in case of Message being received in
         * a [PrivateChannel]
         * or [isWebhookMessage()][.isWebhookMessage] returning `true`.
         *
         * @return The Author of the Message as null-able Member object.
         *
         * @see .isWebhookMessage
         */
        get() = message.member
    val isWebhookMessage: Boolean
        /**
         * Whether or not the Message received was sent via a Webhook.
         * <br></br>This is a shortcut for `getMessage().isWebhookMessage()`.
         *
         * @return True, if the Message was sent via Webhook
         */
        get() = message.isWebhookMessage
}
