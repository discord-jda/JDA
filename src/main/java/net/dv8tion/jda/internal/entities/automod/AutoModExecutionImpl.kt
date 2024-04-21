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
package net.dv8tion.jda.internal.entities.automod

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.automod.AutoModExecution
import net.dv8tion.jda.api.entities.automod.AutoModResponse
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.utils.data.DataObject
import javax.annotation.Nonnull

class AutoModExecutionImpl(@get:Nonnull override val guild: Guild, json: DataObject) : AutoModExecution {
    private override val channel: GuildMessageChannel

    @get:Nonnull
    override val response: AutoModResponse

    @get:Nonnull
    override val triggerType: AutoModTriggerType
    override val userIdLong: Long
    override val ruleIdLong: Long
    override val messageIdLong: Long
    override val alertMessageIdLong: Long

    @get:Nonnull
    override val content: String?
    override val matchedContent: String?
    override val matchedKeyword: String?

    init {
        channel = guild.getChannelById(GuildMessageChannel::class.java, json.getUnsignedLong("channel_id", 0L))!!
        response = AutoModResponseImpl(guild, json.getObject("action"))
        triggerType = AutoModTriggerType.fromKey(json.getInt("rule_trigger_type", -1))
        userIdLong = json.getUnsignedLong("user_id")
        ruleIdLong = json.getUnsignedLong("rule_id")
        messageIdLong = json.getUnsignedLong("message_id", 0L)
        alertMessageIdLong = json.getUnsignedLong("alert_system_message_id", 0L)
        content = json.getString("content", "")
        matchedContent = json.getString("matched_content", null)
        matchedKeyword = json.getString("matched_keyword", null)
    }

    fun getChannel(): GuildMessageChannelUnion? {
        return channel as GuildMessageChannelUnion
    }
}
