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
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import javax.annotation.Nonnull

/**
 * Indicates that a Message was edited in a [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
 *
 *
 * Can be used to detect a Message is edited in either a private or guild channel. Providing a MessageChannel and Message.
 * <br></br>This also includes whether a message is being pinned.
 *
 *
 * **JDA does not have a cache for messages and is not able to provide previous information due to limitations by the
 * Discord API!**
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
 */
class MessageUpdateEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [Message][net.dv8tion.jda.api.entities.Message] that was updated
     * <br></br>Note: Messages in JDA are not updated, they are immutable and will not change their state.
     *
     * @return The updated Message
     */
    @get:Nonnull
    @param:Nonnull val message: Message
) : GenericMessageEvent(api, responseNumber, message.idLong, message.channel) {

    @get:Nonnull
    val author: User?
        /**
         * The author of the Message.
         *
         * @return The message author
         *
         * @see net.dv8tion.jda.api.entities.User User
         */
        get() = message.author
    val member: Member?
        /**
         * Member instance for the author of this message or `null` if this
         * was not in a Guild.
         *
         * @return The Member instance for the author or null
         */
        get() = message.member
}
