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
package net.dv8tion.jda.api.events.message.react

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import javax.annotation.Nonnull

/**
 * Indicates that the reactions of a message have been cleared by a moderator
 *
 *
 * Can be used to detect when the reactions of a message are removed by a moderator
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 *
 *  * [GUILD_MESSAGE_REACTIONS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS] to work in guild text channels
 *  * [DIRECT_MESSAGE_REACTIONS][net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGE_REACTIONS] to work in private channels
 *
 */
class MessageReactionRemoveAllEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    messageId: Long,
    @Nonnull channel: MessageChannel
) : GenericMessageEvent(api, responseNumber, messageId, channel)
