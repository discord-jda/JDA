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
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import javax.annotation.Nonnull

/**
 * Indicates that a Message was deleted in a [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
 *
 *
 * Can be used to detect when a Message is deleted. No matter if private or guild.
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
class MessageDeleteEvent(@Nonnull api: JDA, responseNumber: Long, messageId: Long, @Nonnull channel: MessageChannel) :
    GenericMessageEvent(api, responseNumber, messageId, channel)
