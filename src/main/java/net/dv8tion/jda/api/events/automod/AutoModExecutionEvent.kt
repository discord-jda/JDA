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
package net.dv8tion.jda.api.events.automod

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.automod.AutoModExecution
import net.dv8tion.jda.api.entities.automod.AutoModResponse
import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.events.*
import javax.annotation.Nonnull

/**
 * Indicates that an automated [AutoModResponse] has been triggered through an [AutoModRule].
 *
 *
 * **Requirements**<br></br>
 * This event requires the [AUTO_MODERATION_EXECUTION][GatewayIntent.AUTO_MODERATION_EXECUTION] intent to be enabled.
 * <br></br>This event will only fire for guilds where the bot has the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
 * Additionally, access to [.getContent] and [.getMatchedContent] requires the [MESSAGE_CONTENT][GatewayIntent.MESSAGE_CONTENT] intent to be enabled.
 */
class AutoModExecutionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val execution: AutoModExecution
) : Event(api, responseNumber), AutoModExecution {
    @get:Nonnull
    override val guild: Guild?
        get() = execution.guild
    override val channel: GuildMessageChannelUnion?
        get() = execution.channel

    @get:Nonnull
    override val response: AutoModResponse?
        get() = execution.response

    @get:Nonnull
    override val triggerType: AutoModTriggerType?
        get() = execution.triggerType
    override val userIdLong: Long
        get() = execution.userIdLong
    override val ruleIdLong: Long
        get() = execution.ruleIdLong
    override val messageIdLong: Long
        get() = execution.messageIdLong
    override val alertMessageIdLong: Long
        get() = execution.alertMessageIdLong

    @get:Nonnull
    override val content: String?
        get() = execution.content
    override val matchedContent: String?
        get() = execution.matchedContent
    override val matchedKeyword: String?
        get() = execution.matchedKeyword
}
