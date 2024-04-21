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
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that a Message contains an [Embed][net.dv8tion.jda.api.entities.MessageEmbed] in a [MessageChannel][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel].
 * <br></br>Discord may need to do additional calculations and resizing tasks on messages that embed websites, thus they send the message only with content and link and use this update to add the missing embed later when the server finishes those calculations.
 *
 *
 * Can be used to retrieve MessageEmbeds from any message. No matter if private or guild.
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
class MessageEmbedEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    messageId: Long,
    @Nonnull channel: MessageChannel,
    @Nonnull embeds: List<MessageEmbed>?
) : GenericMessageEvent(api, responseNumber, messageId, channel) {
    /**
     * The list of [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed]
     *
     * @return The list of MessageEmbeds
     */
    @get:Nonnull
    val messageEmbeds: List<MessageEmbed>

    init {
        messageEmbeds = Collections.unmodifiableList(embeds)
    }
}
